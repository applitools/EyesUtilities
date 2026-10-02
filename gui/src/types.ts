export type RunState =
  | { kind: "idle" }
  | { kind: "running"; runId: string; utility: string }
  | { kind: "done"; runId: string; utility: string; exitCode: number; cancelled: boolean; durationMs: number; outputDir: string };

export type SseEvent =
  | { type: "run-started"; runId: string; utility: string }
  | { type: "log-line"; text: string }
  | { type: "run-finished"; utility: string; exitCode: number; cancelled: boolean; durationMs: number; outputDir: string };
