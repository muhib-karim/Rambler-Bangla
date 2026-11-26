/*
 * Rambler Bangla - SwiftKey-style @mention & email suggestion strip runtime.
 *
 * Hooks Gboard's InputMethodService lifecycle (onStartInputView, onUpdateSelection,
 * onWindowHidden) to learn completed @handles / @domains and display a sleek,
 * scrollable Material You pill strip above the keyboard whenever typing an @ prefix.
 */
package dev.abotmaker.ramblerenhancements;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Displays SwiftKey-style personalized {@code @} handle &amp; email domain chips on Gboard's
 * suggestion strip while keeping password and incognito fields strictly fail-closed.
 */
public final class RamblerAtMentionStripRuntime {
    static final String STRIP_TAG = "rambler-bangla-at-mention-strip";
    private static final String CALCULATOR_TAG = "gboard-patches-calculator-suggestion";
    private static final int TEXT_LOOKBACK = 256;
    private static final int MAX_CHIPS = 10;
    private static final int STRIP_HEIGHT_DP = 42;

    private static final Object LOCK = new Object();
    private static WeakReference<InputMethodService> activeService = new WeakReference<>(null);
    private static WeakReference<View> activeInputView = new WeakReference<>(null);
    private static WeakReference<View> activeStripView = new WeakReference<>(null);
    private static boolean sensitiveSession;
    private static String lastRenderedKey;
    private static final Set<String> sessionLearnedKeys = new HashSet<String>();
    private static String pendingToken;
    private static boolean pendingEmailContext;
    private static int pendingBeforeLen = -1;

    private RamblerAtMentionStripRuntime() {
    }

    public static void onInputViewStarted(Object receiver, EditorInfo info) {
        try {
            if (!RamblerDynamicConfig.FEATURE_AT_MENTION_SUGGESTIONS) {
                return;
            }
            if (!(receiver instanceof InputMethodService service)) {
                return;
            }
            flushPendingToken(service);
            boolean sensitive = RamblerTextPipeline.isSensitiveOrIncognitoField(info);
            synchronized (LOCK) {
                activeService = new WeakReference<>(service);
                activeInputView.clear();
                sensitiveSession = sensitive;
                lastRenderedKey = null;
                sessionLearnedKeys.clear();
                clearPendingLocked();
            }
            hideStrip();
        } catch (Throwable ignored) {
            // Never disrupt Gboard.
        }
    }

    public static void onSelectionUpdated(
            Object receiver, View inputView, int selectionStart, int selectionEnd) {
        try {
            if (!RamblerDynamicConfig.FEATURE_AT_MENTION_SUGGESTIONS) {
                return;
            }
            if (!(receiver instanceof InputMethodService service)) {
                hideStrip();
                return;
            }
            boolean collapsed = selectionStart >= 0 && selectionStart == selectionEnd;
            synchronized (LOCK) {
                activeService = new WeakReference<>(service);
                activeInputView = new WeakReference<>(inputView);
                if (sensitiveSession || !collapsed) {
                    hideStrip();
                    return;
                }
            }
            if (!RamblerAtMentionStore.isEnabled(service)) {
                hideStrip();
                return;
            }
            postToMain(() -> refreshStrip(service, inputView));
        } catch (Throwable ignored) {
            // Never disrupt Gboard.
        }
    }

    public static void onInputWindowHidden() {
        try {
            InputMethodService prevService;
            synchronized (LOCK) {
                prevService = activeService.get();
            }
            flushPendingToken(prevService);
            hideStrip();
            synchronized (LOCK) {
                activeService.clear();
                activeInputView.clear();
                sensitiveSession = false;
                lastRenderedKey = null;
                sessionLearnedKeys.clear();
                clearPendingLocked();
            }
        } catch (Throwable ignored) {
            // Never disrupt Gboard.
        }
    }

    private static void clearPendingLocked() {
        pendingToken = null;
        pendingEmailContext = false;
        pendingBeforeLen = -1;
    }

    private static void flushPendingToken(Context context) {
        String toLearn;
        boolean emailCtx;
        synchronized (LOCK) {
            if (sensitiveSession) {
                clearPendingLocked();
                return;
            }
            toLearn = pendingToken;
            emailCtx = pendingEmailContext;
            clearPendingLocked();
        }
        if (context != null && toLearn != null
                && RamblerAtMentionStore.isEnabled(context)
                && RamblerAtMentionStore.isValidPendingTokenForCommit(toLearn, emailCtx)) {
            RamblerAtMentionStore.recordToken(context, toLearn);
        }
    }

    private static void refreshStrip(InputMethodService service, View inputView) {
        try {
            synchronized (LOCK) {
                if (sensitiveSession
                        || activeService.get() != service
                        || activeInputView.get() != inputView) {
                    hideStripNow();
                    return;
                }
            }
            if (!RamblerAtMentionStore.isEnabled(service)) {
                hideStripNow();
                return;
            }
            InputConnection ic = service.getCurrentInputConnection();
            if (ic == null) {
                hideStripNow();
                return;
            }
            CharSequence beforeCursor = ic.getTextBeforeCursor(TEXT_LOOKBACK, 0);
            String beforeStr = beforeCursor == null ? "" : beforeCursor.toString();

            // If field suddenly cleared (e.g. user pressed Send/Enter right after typing "@grok"),
            // flush the pending token unless the user merely backspaced a 1-char token down to empty.
            if (beforeStr.isEmpty()) {
                boolean shouldFlush;
                synchronized (LOCK) {
                    shouldFlush = pendingToken != null && pendingBeforeLen > 1;
                }
                if (shouldFlush) {
                    flushPendingToken(service);
                } else {
                    synchronized (LOCK) {
                        clearPendingLocked();
                    }
                }
                synchronized (LOCK) {
                    sessionLearnedKeys.clear();
                }
                hideStripNow();
                return;
            }

            // 1. Learn all newly completed @handles / @domains in the lookback window.
            List<String> completed = RamblerAtMentionStore.extractCompletedTokens(beforeStr);
            if (!completed.isEmpty()) {
                int searchFrom = 0;
                for (String token : completed) {
                    int pos = beforeStr.indexOf(token, searchFrom);
                    if (pos >= 0) {
                        searchFrom = pos + token.length();
                    }
                    String learnedKey = token.toLowerCase(java.util.Locale.US) + "#" + pos;
                    boolean isNew;
                    synchronized (LOCK) {
                        isNew = sessionLearnedKeys.add(learnedKey);
                    }
                    if (isNew) {
                        RamblerAtMentionStore.recordToken(service, token);
                    }
                }
            }

            // 2. Check for an active @ prefix right before the cursor.
            RamblerAtMentionStore.ActivePrefix active =
                    RamblerAtMentionStore.extractActivePrefix(beforeStr);
            if (active == null) {
                // User finished typing (space/newline/punctuation) or moved cursor away.
                // Any space-terminated token was already recorded above; clear pending.
                synchronized (LOCK) {
                    clearPendingLocked();
                }
                hideStripNow();
                return;
            }

            // Track the active @token so pressing Send/Enter/Hide learns it even without a space.
            synchronized (LOCK) {
                pendingToken = active.prefix;
                pendingEmailContext = active.emailDomainContext;
                pendingBeforeLen = beforeStr.length();
            }

            List<String> suggestions =
                    RamblerAtMentionStore.suggestForContext(service, active, MAX_CHIPS);
            if (suggestions.isEmpty()) {
                hideStripNow();
                return;
            }

            if (!(inputView instanceof FrameLayout host)) {
                hideStripNow();
                return;
            }
            // Never overlap the calculator suggestion overlay.
            if (host.findViewWithTag(CALCULATOR_TAG) != null) {
                hideStripNow();
                return;
            }

            int topMargin = resolveTopMargin(host);
            if (topMargin < 0) {
                hideStripNow();
                return;
            }

            String renderKey = active.prefix + "|" + active.emailDomainContext + "|" + suggestions;
            View currentStrip = activeStripView.get();
            if (currentStrip != null && currentStrip.getParent() == host
                    && renderKey.equals(lastRenderedKey)) {
                return;
            }

            showStrip(service, host, topMargin, active, suggestions, renderKey);
        } catch (Throwable ignored) {
            hideStripNow();
        }
    }

    private static void showStrip(
            InputMethodService service,
            FrameLayout host,
            int topMargin,
            RamblerAtMentionStore.ActivePrefix active,
            List<String> suggestions,
            String renderKey) {
        hideStripNow();
        Context context = host.getContext();
        boolean dark = isDarkMode(context);
        int barBg = dark ? 0xEE18191C : 0xF5F1F3F4;
        int chipBg = dark ? 0xFF2D2F33 : 0xFFFFFFFF;
        int chipStroke = dark ? 0xFF3E4148 : 0xFFDADCE0;
        int chipText = dark ? Color.WHITE : 0xFF202124;

        HorizontalScrollView scroll = new HorizontalScrollView(context);
        scroll.setTag(STRIP_TAG);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.setElevation(dp(context, 20));
        scroll.setBackground(roundedRect(barBg, 0, dp(context, 18)));
        scroll.setPadding(dp(context, 6), dp(context, 4), dp(context, 6), dp(context, 4));

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        for (String token : suggestions) {
            TextView chip = new TextView(context);
            chip.setText(token);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            chip.setTypeface(Typeface.DEFAULT_BOLD);
            chip.setTextColor(chipText);
            chip.setSingleLine(true);
            chip.setGravity(Gravity.CENTER);
            chip.setBackground(roundedRect(chipBg, chipStroke, dp(context, 16)));
            chip.setPadding(dp(context, 12), dp(context, 5), dp(context, 12), dp(context, 5));
            chip.setClickable(true);
            chip.setFocusable(true);
            chip.setOnClickListener(v -> acceptChip(service, active, token));
            chip.setOnLongClickListener(v -> {
                if (RamblerAtMentionStore.forgetToken(service, token)) {
                    try {
                        Toast.makeText(service, "Removed " + token, Toast.LENGTH_SHORT).show();
                    } catch (Throwable ignored) {
                        // Toast best-effort.
                    }
                    lastRenderedKey = null;
                    refreshStrip(service, host);
                }
                return true;
            });
            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            chipParams.setMarginEnd(dp(context, 6));
            row.addView(chip, chipParams);
        }

        scroll.addView(row, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        int availableWidth = Math.max(dp(context, 220), host.getWidth() - dp(context, 16));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                availableWidth, dp(context, STRIP_HEIGHT_DP));
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.topMargin = topMargin;
        host.addView(scroll, params);
        scroll.bringToFront();

        synchronized (LOCK) {
            activeStripView = new WeakReference<>(scroll);
            lastRenderedKey = renderKey;
        }
    }

    private static void acceptChip(
            InputMethodService service,
            RamblerAtMentionStore.ActivePrefix originalActive,
            String token) {
        try {
            InputConnection ic = service != null ? service.getCurrentInputConnection() : null;
            if (ic == null) {
                hideStripNow();
                return;
            }
            CharSequence beforeCursor = ic.getTextBeforeCursor(TEXT_LOOKBACK, 0);
            RamblerAtMentionStore.ActivePrefix current =
                    RamblerAtMentionStore.extractActivePrefix(beforeCursor);
            if (current == null) {
                hideStripNow();
                return;
            }
            boolean isEmailDomain = current.emailDomainContext || originalActive.emailDomainContext;
            // Append a trailing space for standalone @handles (like SwiftKey), none for email domains.
            String commit = isEmailDomain ? token : (token + " ");
            ic.beginBatchEdit();
            try {
                if (ic.deleteSurroundingText(current.deleteCount, 0)) {
                    ic.commitText(commit, 1);
                }
            } finally {
                ic.endBatchEdit();
            }
            RamblerAtMentionStore.recordToken(service, token);
            synchronized (LOCK) {
                clearPendingLocked();
            }
            hideStripNow();
        } catch (Throwable ignored) {
            hideStripNow();
        }
    }

    private static int resolveTopMargin(FrameLayout host) {
        if (host == null || host.getWidth() <= 0 || host.getHeight() <= 0) {
            return -1;
        }
        int[] hostLocation = new int[2];
        host.getLocationOnScreen(hostLocation);
        int keyboardTop = findKeyboardPanelTop(
                host, host, hostLocation[1], Integer.MAX_VALUE, 0);
        if (keyboardTop == Integer.MAX_VALUE) {
            return -1;
        }
        return Math.max(0, keyboardTop - hostLocation[1] + dp(host.getContext(), 2));
    }

    private static int findKeyboardPanelTop(
            FrameLayout host, View current, int hostScreenTop, int bestScreenTop, int depth) {
        if (current == null || depth > 16 || current.getVisibility() != View.VISIBLE
                || !current.isAttachedToWindow()) {
            return bestScreenTop;
        }
        if (current != host
                && current.getWidth() >= Math.round(host.getWidth() * 0.85f)
                && current.getHeight() >= dp(host.getContext(), 160)) {
            int[] location = new int[2];
            current.getLocationOnScreen(location);
            int hostBottom = hostScreenTop + host.getHeight();
            int minimumTop = hostScreenTop + host.getHeight() / 3;
            if (location[1] >= minimumTop
                    && location[1] + current.getHeight()
                    >= hostBottom - dp(host.getContext(), 48)) {
                bestScreenTop = Math.min(bestScreenTop, location[1]);
            }
        }
        if (current instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                bestScreenTop = findKeyboardPanelTop(
                        host, group.getChildAt(i), hostScreenTop, bestScreenTop, depth + 1);
            }
        }
        return bestScreenTop;
    }

    private static void hideStrip() {
        postToMain(RamblerAtMentionStripRuntime::hideStripNow);
    }

    private static void hideStripNow() {
        View current;
        View inputView;
        synchronized (LOCK) {
            current = activeStripView.get();
            activeStripView.clear();
            inputView = activeInputView.get();
            lastRenderedKey = null;
        }
        try {
            if (current != null && current.getParent() instanceof ViewGroup parent) {
                parent.removeView(current);
            }
            if (inputView instanceof FrameLayout host) {
                for (int i = host.getChildCount() - 1; i >= 0; i--) {
                    View child = host.getChildAt(i);
                    if (child != null && STRIP_TAG.equals(child.getTag())) {
                        host.removeViewAt(i);
                    }
                }
            }
        } catch (Throwable ignored) {
            // Never disrupt Gboard.
        }
    }

    private static void postToMain(Runnable action) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                action.run();
            } else {
                new Handler(Looper.getMainLooper()).post(action);
            }
        } catch (Throwable ignored) {
            // Never disrupt Gboard.
        }
    }

    private static boolean isDarkMode(Context context) {
        return context != null
                && (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    private static GradientDrawable roundedRect(int fillColor, int strokeColor, int radiusPx) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(fillColor);
        bg.setCornerRadius(radiusPx);
        if (strokeColor != 0) {
            bg.setStroke(1, strokeColor);
        }
        return bg;
    }

    private static int dp(Context context, int value) {
        float density = context != null
                ? context.getResources().getDisplayMetrics().density : 1f;
        return Math.max(1, Math.round(value * density));
    }
}
