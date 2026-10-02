package com.applitools.eyesutilities.gui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** The single run slot: idle, one utility running, or the last run's outcome. */
abstract class RunState {

    private RunState() {}

    abstract Map<String, Object> toJson();

    static final class Idle extends RunState {
        static final Idle INSTANCE = new Idle();
        private Idle() {}

        @Override Map<String, Object> toJson() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("kind", "idle");
            return m;
        }
    }

    static final class Running extends RunState {
        final String runId = UUID.randomUUID().toString();
        final String utility;
        Running(String utility) { this.utility = utility; }

        @Override Map<String, Object> toJson() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("kind", "running");
            m.put("runId", runId);
            m.put("utility", utility);
            return m;
        }
    }

    static final class Done extends RunState {
        final String runId;
        final String utility;
        final int exitCode;
        final boolean cancelled;
        final long durationMs;
        final String outputDir;

        Done(String runId, String utility, int exitCode, boolean cancelled, long durationMs, String outputDir) {
            this.runId = runId;
            this.utility = utility;
            this.exitCode = exitCode;
            this.cancelled = cancelled;
            this.durationMs = durationMs;
            this.outputDir = outputDir;
        }

        @Override Map<String, Object> toJson() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("kind", "done");
            m.put("runId", runId);
            m.put("utility", utility);
            m.put("exitCode", exitCode);
            m.put("cancelled", cancelled);
            m.put("durationMs", durationMs);
            m.put("outputDir", outputDir);
            return m;
        }
    }
}
