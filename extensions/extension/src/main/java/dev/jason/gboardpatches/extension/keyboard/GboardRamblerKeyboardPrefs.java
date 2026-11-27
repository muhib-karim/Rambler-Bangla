package dev.jason.gboardpatches.extension.keyboard;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Direct-Boot-safe bridge to Rambler Bangla's shared keyboard/clipboard/launcher preferences
 * ({@code rambler_enhancements_prefs}) and Gboard's launcher activity component state.
 */
public final class GboardRamblerKeyboardPrefs {
    private static final String TAG = "GboardPatches";
    public static final String PREF_FILE = "rambler_enhancements_prefs";
    public static final String PREF_KEY_AMOLED_DARK_KEYBOARD = "pref_rambler_amoled_dark_keyboard";
    public static final String PREF_KEY_CLIPBOARD_DRAG_REORDER = "pref_rambler_clipboard_drag_reorder";
    public static final String PREF_KEY_HIDE_LAUNCHER_ICON = "pref_rambler_hide_launcher_icon";
    public static final String LAUNCHER_ACTIVITY_CLASS =
            "com.google.android.libraries.inputmethod.launcher.LauncherActivity";

    public static final boolean DEFAULT_AMOLED_DARK_KEYBOARD = false;
    public static final boolean DEFAULT_CLIPBOARD_DRAG_REORDER = true;
    public static final boolean DEFAULT_HIDE_LAUNCHER_ICON = false;

    private static final String DYNAMIC_CONFIG_CLASS =
            "dev.abotmaker.ramblerenhancements.RamblerDynamicConfig";

    private GboardRamblerKeyboardPrefs() {
    }

    public static SharedPreferences preferencesOrNull(Context context) {
        if (context == null) {
            return null;
        }
        Context app = context.getApplicationContext();
        Context base = app != null ? app : context;
        try {
            return base.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        } catch (IllegalStateException directBootLocked) {
            try {
                return base.createDeviceProtectedStorageContext()
                        .getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
            } catch (Throwable ignored) {
                return null;
            }
        } catch (Throwable throwable) {
            logWarningSafely("Failed to open Rambler keyboard preferences", throwable);
            return null;
        }
    }

    public static boolean isAmoledFeatureAvailable() {
        return readDynamicConfigFlag("FEATURE_AMOLED_DARK_KEYBOARD", true);
    }

    public static boolean isClipboardReorderAvailable() {
        return readDynamicConfigFlag("FEATURE_CLIPBOARD_REORDER", true);
    }

    public static boolean readAmoledDarkKeyboardEnabled(Context context) {
        return readBoolean(context, PREF_KEY_AMOLED_DARK_KEYBOARD, DEFAULT_AMOLED_DARK_KEYBOARD);
    }

    public static boolean writeAmoledDarkKeyboardEnabled(Context context, boolean enabled) {
        return writeBoolean(context, PREF_KEY_AMOLED_DARK_KEYBOARD, enabled);
    }

    public static boolean readClipboardDragReorderEnabled(Context context) {
        return readBoolean(context, PREF_KEY_CLIPBOARD_DRAG_REORDER, DEFAULT_CLIPBOARD_DRAG_REORDER);
    }

    public static boolean writeClipboardDragReorderEnabled(Context context, boolean enabled) {
        return writeBoolean(context, PREF_KEY_CLIPBOARD_DRAG_REORDER, enabled);
    }

    public static boolean readHideLauncherIcon(Context context) {
        if (context != null) {
            try {
                PackageManager pm = context.getPackageManager();
                if (pm != null) {
                    ComponentName cn = new ComponentName(
                            context.getPackageName(), LAUNCHER_ACTIVITY_CLASS);
                    int state = pm.getComponentEnabledSetting(cn);
                    if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                        return true;
                    }
                    if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                        return false;
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return readBoolean(context, PREF_KEY_HIDE_LAUNCHER_ICON, DEFAULT_HIDE_LAUNCHER_ICON);
    }

    public static boolean setLauncherIconHidden(Context context, boolean hide) {
        boolean saved = writeBoolean(context, PREF_KEY_HIDE_LAUNCHER_ICON, hide);
        if (context == null) {
            return saved;
        }
        try {
            PackageManager pm = context.getPackageManager();
            if (pm != null) {
                ComponentName cn = new ComponentName(
                        context.getPackageName(), LAUNCHER_ACTIVITY_CLASS);
                int newState = hide
                        ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
                pm.setComponentEnabledSetting(cn, newState, PackageManager.DONT_KILL_APP);
                return true;
            }
        } catch (Throwable err) {
            logWarningSafely("Failed to update launcher icon visibility", err);
            return false;
        }
        return saved;
    }

    private static boolean readDynamicConfigFlag(String fieldName, boolean fallbackWhenAbsent) {
        try {
            Class<?> configClass = Class.forName(DYNAMIC_CONFIG_CLASS);
            Field field = configClass.getField(fieldName);
            return field.getBoolean(null);
        } catch (ClassNotFoundException absent) {
            return fallbackWhenAbsent;
        } catch (Throwable ignored) {
            return fallbackWhenAbsent;
        }
    }

    private static boolean readBoolean(Context context, String key, boolean fallback) {
        SharedPreferences prefs = preferencesOrNull(context);
        if (prefs == null) {
            return fallback;
        }
        try {
            return prefs.contains(key) ? prefs.getBoolean(key, fallback) : fallback;
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static boolean writeBoolean(Context context, String key, boolean value) {
        try {
            SharedPreferences prefs = preferencesOrNull(context);
            return prefs != null && prefs.edit().putBoolean(key, value).commit();
        } catch (Throwable err) {
            logWarningSafely("Failed to persist setting " + key, err);
            return false;
        }
    }

    private static void logWarningSafely(String message, Throwable err) {
        try {
            Log.w(TAG, message, err);
        } catch (Throwable ignored) {
        }
    }
}
