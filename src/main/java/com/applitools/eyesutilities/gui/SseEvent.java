package com.applitools.eyesutilities.gui;

/** Server-sent events; public fields are serialized by Jackson as-is. */
public abstract class SseEvent {

    public final String type;
    protected SseEvent(String type) { this.type = type; }

    public static final class RunStarted extends SseEvent {
        public final String runId;
        public final String utility;
        RunStarted(String runId, String utility) { super("run-started"); this.runId = runId; this.utility = utility; }
    }

    public static final class LogLine extends SseEvent {
        public final String text;
        LogLine(String text) { super("log-line"); this.text = text; }
    }

    public static final class RunFinished extends SseEvent {
        public final String utility;
        public final int exitCode;
        public final boolean cancelled;
        public final long durationMs;
        public final String outputDir;
        RunFinished(String utility, int exitCode, boolean cancelled, long durationMs, String outputDir) {
            super("run-finished");
            this.utility = utility; this.exitCode = exitCode; this.cancelled = cancelled;
            this.durationMs = durationMs; this.outputDir = outputDir;
        }
    }
}
