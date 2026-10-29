import { README_URL, type UtilitySpec } from "../lib/utilitiesSchema";
import type { UtilityValue, UtilityValues } from "../lib/utilities";
import { UtilityFieldControl } from "./UtilityFieldControl";

interface Props {
  spec: UtilitySpec;
  values: UtilityValues;
  onChange: (flag: string, value: UtilityValue) => void;
  onClose: () => void;
}

export function UtilityOptionsPanel({ spec, values, onChange, onClose }: Props) {
  return (
    <section aria-label={`${spec.label} options`} className="rounded-xl border border-gray-200 bg-white p-4">
      <div className="mb-3 flex items-center justify-between">
        <span className="text-xs uppercase tracking-wider text-gray-500">Options — {spec.label}</span>
        <button type="button" onClick={onClose} className="text-gray-500 hover:text-gray-700" aria-label="Close options">✕</button>
      </div>
      <p className="mb-4 text-xs text-gray-500">
        All optional — leave a field empty to use the EyesUtilities default.{" "}
        <a href={README_URL} target="_blank" rel="noreferrer" className="text-brand-tealInk hover:underline">EyesUtilities README ↗</a>
      </p>
      <div className="grid grid-cols-1 gap-x-6 gap-y-5 md:grid-cols-2 xl:grid-cols-3">
        {spec.optional.map((field) => (
          <UtilityFieldControl key={field.flag} field={field} value={values[field.flag]}
            onChange={(v) => onChange(field.flag, v)} />
        ))}
      </div>
    </section>
  );
}
