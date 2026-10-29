import { useEffect, useReducer, useRef, useState } from "react";
import applitoolsLogo from "./assets/applitools-logo.png";
import { ApiKeyPanel } from "./components/ApiKeyPanel";
import { UtilitiesCard } from "./components/UtilitiesCard";
import { UtilityOptionsPanel } from "./components/UtilityOptionsPanel";
import { UtilityOutputPane } from "./components/UtilityOutputPane";
import { api, ApiError } from "./lib/api";
import { runReducer } from "./lib/runState";
import { connectSse, type SseHandle } from "./lib/sse";
import { DEFAULT_UTILITY_ID, README_URL, findUtility } from "./lib/utilitiesSchema";
import {
  EMPTY_FORM, countSetOptions, listMissingRequired, loadForms, saveForms, toUtilityPayload,
  type KeyState, type UtilityForm, type UtilityForms, type UtilityValue,
} from "./lib/utilities";
import { getVersion } from "./lib/version";

const SELECTED_UTILITY_KEY = "eyesutilities.selected";
const CONFLICT_STATUS = 409;
const EMPTY_KEYS: KeyState = { apiKey: "", viewKey: "" };

function readSelectedUtility(): string {
  try { return window.localStorage.getItem(SELECTED_UTILITY_KEY) ?? DEFAULT_UTILITY_ID; } catch { return DEFAULT_UTILITY_ID; }
}

function writeSelectedUtility(id: string) {
  try { window.localStorage.setItem(SELECTED_UTILITY_KEY, id); } catch { /* private mode / disabled */ }
}

export function App() {
  const [runState, dispatch] = useReducer(runReducer, { kind: "idle" });
  const [logLines, setLogLines] = useState<string[]>([]);
  const [selectedId, setSelectedId] = useState(readSelectedUtility);
  const [forms, setForms] = useState<UtilityForms>(loadForms);
  // Deliberately React state only: keys are credentials and must never reach localStorage.
  const [keys, setKeys] = useState<KeyState>(EMPTY_KEYS);
  const [optionsOpen, setOptionsOpen] = useState(false);
  const [apiKeyOpen, setApiKeyOpen] = useState(false);
  const [runError, setRunError] = useState<string | null>(null);
  const [cancelling, setCancelling] = useState(false);
  const esRef = useRef<SseHandle | null>(null);

  const spec = findUtility(selectedId);
  const form = forms[spec.id] ?? EMPTY_FORM;
  const isRunning = runState.kind === "running";

  const resyncStatus = () => api.status().then((s) => dispatch({ type: "set", snapshot: s })).catch(() => {});

  useEffect(() => {
    resyncStatus();
    esRef.current = connectSse(
      (e) => {
        if (e.type === "log-line") setLogLines((l) => [...l, e.text]);
        else dispatch({ type: "sse", event: e });
      },
      // Events emitted while the stream was down are gone, so re-sync on every (re)connect.
      resyncStatus,
    );
    return () => esRef.current?.close();
  }, []);

  // Cancel stops the child process asynchronously; keep "Cancelling…" until run-finished arrives.
  useEffect(() => {
    if (runState.kind !== "running") setCancelling(false);
  }, [runState.kind]);

  const updateForm = (update: (prev: UtilityForm) => UtilityForm) => {
    setForms((prev) => {
      const next = { ...prev, [spec.id]: update(prev[spec.id] ?? EMPTY_FORM) };
      saveForms(next);
      return next;
    });
  };

  const handleChangeValue = (flag: string, value: UtilityValue) =>
    updateForm((prev) => ({ ...prev, values: { ...prev.values, [flag]: value } }));

  const handleSelectUtility = (id: string) => {
    setSelectedId(id);
    writeSelectedUtility(id);
    setRunError(null);
  };

  const handleRun = async () => {
    setRunError(null);
    setLogLines([]);
    try {
      await api.run(toUtilityPayload(spec, form, keys));
    } catch (err) {
      setRunError(err instanceof Error ? err.message : String(err));
      // 409: another tab holds the single run slot — re-sync so this tab shows that run.
      if (err instanceof ApiError && err.status === CONFLICT_STATUS) resyncStatus();
    }
  };

  const handleCancel = () => {
    api.cancel().catch(() => {});
    setCancelling(true);
  };

  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <header className="mb-8">
        <div className="flex flex-wrap items-center gap-x-2.5 gap-y-2">
          <img src={applitoolsLogo} alt="Applitools" className="h-7 w-7" />
          <span className="text-[15px] font-semibold tracking-tight text-brand-navy">EyesUtilities</span>
          {getVersion() && (
            <span className="rounded-full bg-gray-100 px-2 py-0.5 font-mono text-[11px] tabular-nums tracking-tight text-gray-500">
              v{getVersion()}
            </span>
          )}
          <button type="button" onClick={() => setApiKeyOpen((v) => !v)} aria-expanded={apiKeyOpen}
            className="ml-auto flex items-center gap-1.5 rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-xs font-medium text-gray-700 shadow-sm transition-colors hover:bg-gray-50">
            <span aria-hidden="true">🔑</span>
            {keys.apiKey ? "API key ✓" : "Enter API key"}
          </button>
          <a href={README_URL} target="_blank" rel="noreferrer"
            className="text-xs font-medium text-brand-tealInk transition-colors hover:text-brand-navy">Docs ↗</a>
        </div>
        <p className="mt-2 text-sm leading-relaxed text-gray-500">
          Download diffs, images, GIFs and reports, manage branches and baselines, and administer teams in Applitools Eyes.
        </p>
      </header>
      {apiKeyOpen && (
        <ApiKeyPanel hasKey={keys.apiKey.length > 0}
          onSave={(apiKey) => setKeys((k) => ({ ...k, apiKey }))}
          onClear={() => setKeys((k) => ({ ...k, apiKey: "" }))}
          onClose={() => setApiKeyOpen(false)} />
      )}
      <main>
        <div className="grid grid-cols-1 gap-8 md:grid-cols-2">
          <div className="space-y-4">
            <UtilitiesCard
              spec={spec}
              form={form}
              keys={keys}
              missingRequired={listMissingRequired(spec, form, keys)}
              optionsCount={countSetOptions(spec, form.values)}
              optionsOpen={optionsOpen}
              isRunning={isRunning}
              isCancelling={cancelling}
              onSelectUtility={handleSelectUtility}
              onChangeValue={handleChangeValue}
              onChangeUrls={(text) => updateForm((prev) => ({ ...prev, urlsText: text }))}
              onChangeKeys={setKeys}
              onOpenApiKey={() => setApiKeyOpen(true)}
              onToggleOptions={() => setOptionsOpen((v) => !v)}
              onRun={handleRun}
              onCancel={handleCancel}
            />
            {runError && <p role="alert" className="text-sm text-rose-700">{runError}</p>}
          </div>
          {/* md:relative + md:absolute/inset-0: at md+ this cell adds no height to the row, so
              Output always matches the Setup column. Below md it flows normally. */}
          <div className="md:relative">
            <div className="md:absolute md:inset-0">
              <UtilityOutputPane state={runState} logLines={logLines} />
            </div>
          </div>
        </div>
        {optionsOpen && spec.optional.length > 0 && (
          <div className="mt-6">
            <UtilityOptionsPanel spec={spec} values={form.values} onChange={handleChangeValue}
              onClose={() => setOptionsOpen(false)} />
          </div>
        )}
      </main>
    </div>
  );
}
