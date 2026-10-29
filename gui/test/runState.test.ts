import { runReducer } from "../src/lib/runState";
import type { RunState } from "../src/types";

const RUNNING: RunState = { kind: "running", runId: "r1", utility: "diffs" };

describe("runReducer", () => {
  it("should enter running when a run starts", () => {
    expect(runReducer({ kind: "idle" }, { type: "sse", event: { type: "run-started", runId: "r1", utility: "diffs" } })).toEqual(RUNNING);
  });

  it("should record the outcome when the run finishes", () => {
    const next = runReducer(RUNNING, {
      type: "sse",
      event: { type: "run-finished", utility: "diffs", exitCode: 0, cancelled: true, durationMs: 1200, outputDir: "/d" },
    });
    expect(next).toEqual({ kind: "done", runId: "r1", utility: "diffs", exitCode: 0, cancelled: true, durationMs: 1200, outputDir: "/d" });
  });

  it("should ignore a finish event when no run is live", () => {
    const finished = { type: "run-finished" as const, utility: "diffs", exitCode: 0, cancelled: false, durationMs: 1, outputDir: "/d" };
    expect(runReducer({ kind: "idle" }, { type: "sse", event: finished })).toEqual({ kind: "idle" });
  });

  it("should adopt a valid status snapshot", () => {
    expect(runReducer({ kind: "idle" }, { type: "set", snapshot: RUNNING })).toEqual(RUNNING);
  });

  it("should fall back to idle for an unrecognised snapshot", () => {
    expect(runReducer(RUNNING, { type: "set", snapshot: { kind: "bogus" } })).toEqual({ kind: "idle" });
  });
});
