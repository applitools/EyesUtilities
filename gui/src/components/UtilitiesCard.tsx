import { RESULT_URLS_EXAMPLE, UTILITIES, UTILITY_GROUPS, type UtilitySpec } from "../lib/utilitiesSchema";
import { API_KEY_LABEL, RESULT_URLS_LABEL, VIEW_KEY_LABEL, type KeyState, type UtilityForm, type UtilityValue } from "../lib/utilities";
import { UtilityFieldControl } from "./UtilityFieldControl";

interface Props {
  spec: UtilitySpec;
  form: UtilityForm;
  keys: KeyState;
  missingRequired: string[];
  optionsCount: number;
  optionsOpen: boolean;
  isRunning: boolean;
  isCancelling: boolean;
  onSelectUtility: (id: string) => void;
  onChangeValue: (flag: string, value: UtilityValue) => void;
  onChangeUrls: (text: string) => void;
  onChangeKeys: (keys: KeyState) => void;
  onOpenApiKey: () => void;
  onToggleOptions: () => void;
  onRun: () => void;
  onCancel: () => void;
}

const INPUT_CLASS_NAME =
  "mt-1 w-full rounded-lg border border-gray-200 px-3 py-2 text-sm transition-colors focus:border-brand-teal";

export function UtilitiesCard(p: Props) {
  const canRun = p.missingRequired.length === 0;
  return (
    <div className="card space-y-4 p-6">
      <h2 className="text-xs uppercase tracking-wider text-gray-500">Setup</h2>

      <div className="text-sm">
        <label htmlFor="utility-select" className="text-gray-700">Utility</label>
        <select id="utility-select" value={p.spec.id} onChange={(e) => p.onSelectUtility(e.target.value)}
          disabled={p.isRunning} aria-describedby="utility-description" className={INPUT_CLASS_NAME}>
          {UTILITY_GROUPS.map((g) => (
            <optgroup key={g.id} label={g.label}>
              {UTILITIES.filter((u) => u.group === g.id).map((u) => <option key={u.id} value={u.id}>{u.label}</option>)}
            </optgroup>
          ))}
        </select>
        <p id="utility-description" className="mt-1 text-xs text-gray-500">{p.spec.description}</p>
      </div>

      {p.spec.keyKind === "api" ? (
        <div className="flex items-center justify-between rounded-lg border border-gray-200 px-3 py-2 text-sm">
          <span className="text-gray-700">
            {API_KEY_LABEL} <span className="text-rose-600">*</span>
            <span className={`ml-2 text-xs ${p.keys.apiKey ? "text-brand-tealInk" : "text-gray-500"}`}>
              {p.keys.apiKey ? "✓ Set" : "Not set"}
            </span>
          </span>
          <button type="button" onClick={p.onOpenApiKey} className="text-xs font-medium text-brand-tealInk hover:underline">
            {p.keys.apiKey ? "Change" : "Enter API key"}
          </button>
        </div>
      ) : (
        <label className="block text-sm">
          <span className="text-gray-700">{VIEW_KEY_LABEL} <span className="text-rose-600">*</span></span>
          <input type="password" value={p.keys.viewKey} autoComplete="off" placeholder="paste your enterprise view key"
            onChange={(e) => p.onChangeKeys({ ...p.keys, viewKey: e.target.value })} className={INPUT_CLASS_NAME} />
          <span className="mt-1 block text-xs text-gray-500">Read-only key for downloading results. Kept in this page only — never saved to disk.</span>
        </label>
      )}

      {p.spec.acceptsResultUrls && (
        <div>
          <label htmlFor="utility-urls" className="block text-sm text-gray-700">
            {RESULT_URLS_LABEL} <span className="text-rose-600">*</span>
          </label>
          <textarea id="utility-urls" rows={3} value={p.form.urlsText} aria-describedby="utility-urls-help"
            onChange={(e) => p.onChangeUrls(e.target.value)}
            className={`${INPUT_CLASS_NAME} font-mono text-xs`} />
          <p id="utility-urls-help" className="mt-1 text-xs text-gray-500">
            One batch or test results URL per line, copied from the dashboard — or set a Date range or Batch query in Options.
          </p>
          <p className="mt-1 text-xs text-gray-500">
            e.g. <code className="break-all rounded bg-brand-teal/10 px-1.5 py-0.5 font-mono text-[11px] text-brand-tealInk">{RESULT_URLS_EXAMPLE}</code>
          </p>
        </div>
      )}

      {p.spec.required.map((field) => (
        <UtilityFieldControl key={field.flag} field={field} value={p.form.values[field.flag]} isRequired
          onChange={(v) => p.onChangeValue(field.flag, v)} />
      ))}

      <button type="button" onClick={p.onToggleOptions} disabled={p.spec.optional.length === 0}
        aria-expanded={p.optionsOpen}
        className="flex w-full items-center justify-between rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-sm transition-colors hover:bg-gray-100 disabled:text-gray-500 disabled:hover:bg-gray-50">
        <span>⚙ Options</span>
        <span className="text-xs text-gray-500">
          {p.spec.optional.length === 0 ? "none for this utility" : `${p.optionsCount > 0 ? `${p.optionsCount} set` : "none set"}${p.optionsOpen ? " ▴" : " ▾"}`}
        </span>
      </button>

      {p.spec.isDestructive && (
        <p className="rounded-lg bg-amber-50 px-3 py-2 text-xs text-amber-800">
          This changes data on the Applitools server and can't be undone from here.
        </p>
      )}

      {p.isRunning ? (
        <button type="button" disabled={p.isCancelling} onClick={p.onCancel}
          className="w-full rounded-lg bg-gray-200 py-2.5 font-semibold text-gray-700 transition-colors hover:bg-gray-300 disabled:text-gray-500 disabled:hover:bg-gray-200">
          {p.isCancelling ? "Cancelling…" : "Cancel"}
        </button>
      ) : (
        <button type="button" disabled={!canRun} onClick={p.onRun}
          aria-describedby={canRun ? undefined : "utility-run-blocked"}
          className="w-full rounded-lg bg-brand-teal py-2.5 font-semibold text-white transition-colors hover:bg-brand-tealDark disabled:bg-gray-200 disabled:text-gray-500">
          ▶ Run {p.spec.label}
        </button>
      )}
      {!p.isRunning && !canRun && (
        <p id="utility-run-blocked" className="text-xs text-gray-500">
          Still needed: {p.missingRequired.join(", ")}.
        </p>
      )}
    </div>
  );
}
