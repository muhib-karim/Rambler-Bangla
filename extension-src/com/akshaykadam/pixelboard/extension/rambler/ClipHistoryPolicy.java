package com.akshaykadam.pixelboard.extension.rambler;

/** Android InputType/EditorInfo constants, kept SDK-free for host tests. */
public final class ClipHistoryPolicy {
    /** A clipboard change is handled within milliseconds; anything older is a re-read, not a live copy. */
    static final long FRESH_AFTER_MS = 10_000;
    static final long FRESH_BEFORE_MS = 1_000;

    private ClipHistoryPolicy() {}

    /** Password fields and editors that opted out of personalized learning (incognito-style). */
    public static boolean privateEditor(int inputType, int imeOptions) {
        int kind = inputType & 0xf;
        int variation = inputType & 0xff0;
        boolean password = (kind == 1 && (variation == 0x80 || variation == 0x90 || variation == 0xe0))
                || (kind == 2 && variation == 0x10);
        return password || (imeOptions & 0x1000000) != 0;
    }

    /** The History tab is offered only in a real, non-private editor with stock clipboard enabled. */
    public static boolean showHistory(int inputType, int imeOptions, boolean enabled, boolean incognito) {
        return (inputType & 0xf) != 0 && !privateEditor(inputType, imeOptions) && enabled && !incognito;
    }

    /**
     * Mirrors stock clipboard recording (opt-in, not incognito), drops sensitive or masked clips, and also refuses
     * while a password or no-learning editor is active. With no editor session the stock rule alone applies.
     */
    public static boolean recordClip(boolean inputStarted, int inputType, int imeOptions, boolean enabled,
                                     boolean incognito, boolean sensitive, boolean masked) {
        return enabled && !incognito && !sensitive && !masked && !(inputStarted && privateEditor(inputType, imeOptions));
    }

    public static boolean fresh(long clipTimestamp, long now) {
        return clipTimestamp > 0 && now - clipTimestamp <= FRESH_AFTER_MS && clipTimestamp - now <= FRESH_BEFORE_MS;
    }

    /**
     * When private editor sessions ran, so a copy made inside one is refused even if focus has moved on before the
     * clipboard read is handled. Sessions less than MERGE_MS apart are merged; the gap between them counts as private.
     * Main thread only.
     */
    public static final class PrivateWindow {
        static final long SLACK_MS = 1_000;
        static final long MERGE_MS = 20_000;
        private long start = -1;
        private long end = -1;
        private boolean open;

        public void enter(long now) {
            if (!open && (start < 0 || now - end > MERGE_MS)) start = now;
            open = true;
        }

        public void leave(long now) {
            if (!open) return;
            open = false;
            end = now;
        }

        public boolean covers(long clipTimestamp) {
            return start >= 0 && clipTimestamp >= start - SLACK_MS && (open || clipTimestamp <= end + SLACK_MS);
        }
    }

    /** The ClipData and ClipDescription come from two Binder reads; both must describe the same event. */
    public static boolean sameClip(long embeddedTimestamp, long fetchedTimestamp) {
        return embeddedTimestamp > 0 && embeddedTimestamp == fetchedTimestamp;
    }
}
