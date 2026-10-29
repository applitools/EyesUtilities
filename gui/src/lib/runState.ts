import type { RunState, SseEvent } from "../types";

export type RunAction =
  | { type: "set"; snapshot: unknown }
  | { type: "sse"; event: SseEvent };

const IDLE: RunState = { kind: "idle" };

function isRunState(value: unknown): value is RunState {
  const kind = (value as { kind?: unknown } | null)?.kind;
  return kind === "idle" || kind === "running" || kind === "done";
}

export function runReducer(state: RunState, action: RunAction): RunState {
  if (action.type === "set") return isRunState(action.snapshot) ? action.snapshot : IDLE;
  const e = action.event;
  switch (e.type) {
    // The server emits run-started before /api/run responds, so the stream — not the POST —
    // is the authoritative idle → running transition.
    case "run-started":
      return { kind: "running", runId: e.runId, utility: e.utility };
    case "run-finished":
      if (state.kind !== "running") return state;
      return {
        kind: "done", runId: state.runId, utility: e.utility, exitCode: e.exitCode,
        cancelled: e.cancelled, durationMs: e.durationMs, outputDir: e.outputDir,
      };
    default:
      return state;
  }
}
