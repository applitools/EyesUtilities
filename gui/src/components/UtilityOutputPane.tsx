import { useEffect, useRef } from "react";
import { findUtility } from "../lib/utilitiesSchema";
import type { RunState } from "../types";

interface Props {
  state: RunState;
  logLines: string[];
}

const STICK_TO_BOTTOM_THRESHOLD_PX = 40;
const MS_PER_SECOND = 1000;

export function UtilityOutputPane({ state, logLines }: Props) {
  const logRef = useRef<HTMLDivElement | null>(null);
  const wasNearBottomRef = useRef(true);

  const handleScroll = () => {
    const el = logRef.current;
    if (!el) return;
    wasNearBottomRef.current = el.scrollHeight - el.scrollTop - el.clientHeight <= STICK_TO_BOTTOM_THRESHOLD_PX;
  };

  useEffect(() => {
    const el = logRef.current;
    if (el && wasNearBottomRef.current) el.scrollTop = el.scrollHeight;
  }, [logLines.length]);

  const showsLog = state.kind === "running" || state.kind === "done";
  const savesFiles = state.kind === "done" && findUtility(state.utility).group === "results";
  return (
    <div className="card flex h-full flex-col p-6">
      <div className="mb-3 flex items-center justify-between">
        <h2 className="text-xs uppercase tracking-wider text-gray-500">Output</h2>
        <StateLabel state={state} />
      </div>

      {savesFiles && state.kind === "done" && !state.cancelled && (
        <div className="mb-3 rounded-lg bg-gray-50 p-3 text-sm">
          <div className="font-medium text-gray-700">Files are saved under</div>
          <div className="mt-1 break-all font-mono text-xs text-gray-600">{state.outputDir}</div>
        </div>
      )}

      {state.kind === "idle" && (
        <p className="text-sm text-gray-500">Pick a utility, fill in the required fields, and click Run.</p>
      )}

      {showsLog && (
        <div ref={logRef} onScroll={handleScroll} tabIndex={0} aria-label="EyesUtilities log" aria-live="polite"
          className="min-h-0 max-h-[60vh] flex-1 overflow-y-auto md:max-h-none">
          <pre className="h-full overflow-x-hidden whitespace-pre-wrap break-words rounded-lg bg-gray-900 p-3 font-mono text-xs text-emerald-200">
            {logLines.join("")}
          </pre>
        </div>
      )}
      {state.kind === "done" && !state.cancelled && state.exitCode === 0 && (
        // EyesUtilities catches its own errors and still exits 0, so "done" is not "succeeded".
        <p className="mt-2 text-xs text-gray-500">EyesUtilities reports server errors (bad key, unknown branch…) in the log above.</p>
      )}
    </div>
  );
}

function StateLabel({ state }: { state: RunState }) {
  if (state.kind === "idle") return <span className="text-xs text-gray-500">Idle</span>;
  if (state.kind === "running") return <span className="text-xs text-amber-700">Running {findUtility(state.utility).label}…</span>;
  const seconds = (state.durationMs / MS_PER_SECOND).toFixed(1);
  if (state.cancelled) return <span className="text-xs text-gray-600">Cancelled after {seconds}s</span>;
  if (state.exitCode !== 0) return <span className="text-xs text-rose-700">Failed (exit {state.exitCode}) — see log</span>;
  return <span className="text-xs text-gray-700">Done in {seconds}s</span>;
}
