import { useEffect, useRef, useState } from "react";

interface Props {
  hasKey: boolean;
  onSave: (key: string) => void;
  onClear: () => void;
  onClose: () => void;
}

/** Inline panel (not a modal) so it works with keyboard and screen readers without a focus trap. */
export function ApiKeyPanel({ hasKey, onSave, onClear, onClose }: Props) {
  // Starts empty even when a key is set: the saved key is never echoed back into the DOM.
  const [draft, setDraft] = useState("");
  const inputRef = useRef<HTMLInputElement | null>(null);

  useEffect(() => { inputRef.current?.focus(); }, []);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (draft.trim().length === 0) return;
    onSave(draft.trim());
    onClose();
  };

  return (
    <section role="dialog" aria-label="Applitools API key" className="card mb-6 p-5"
      onKeyDown={(e) => { if (e.key === "Escape") onClose(); }}>
      <form onSubmit={handleSubmit} className="space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-xs uppercase tracking-wider text-gray-500">Applitools API key</h2>
          <button type="button" onClick={onClose} className="text-gray-500 hover:text-gray-700" aria-label="Close API key panel">✕</button>
        </div>
        <label htmlFor="api-key-input" className="sr-only">API key</label>
        <input id="api-key-input" ref={inputRef} type="password" autoComplete="off" value={draft}
          placeholder={hasKey ? "Enter a new key to replace the current one" : "Paste your API key"}
          onChange={(e) => setDraft(e.target.value)} aria-describedby="api-key-help"
          className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm transition-colors focus:border-brand-teal" />
        <p id="api-key-help" className="text-xs text-gray-500">
          Used by branch, baseline and team-admin utilities. Kept in this page only — never saved to disk, so re-enter it after a reload.
        </p>
        <div className="flex gap-2">
          <button type="submit" disabled={draft.trim().length === 0}
            className="rounded-lg bg-brand-teal px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-brand-tealDark disabled:bg-gray-200 disabled:text-gray-500">
            Save key
          </button>
          {hasKey && (
            <button type="button" onClick={() => { onClear(); onClose(); }}
              className="rounded-lg border border-gray-200 bg-gray-50 px-4 py-2 text-sm transition-colors hover:bg-gray-100">
              Remove key
            </button>
          )}
        </div>
      </form>
    </section>
  );
}
