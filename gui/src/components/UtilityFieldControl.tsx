import type { UtilityField } from "../lib/utilitiesSchema";
import type { UtilityValue } from "../lib/utilities";

interface Props {
  field: UtilityField;
  value: UtilityValue | undefined;
  isRequired?: boolean;
  onChange: (value: UtilityValue) => void;
}

const INPUT_CLASS_NAME =
  "mt-1 w-full rounded-lg border border-gray-200 px-3 py-2 text-sm transition-colors focus:border-brand-teal";

export function UtilityFieldControl({ field, value, isRequired, onChange }: Props) {
  const id = `utility-${field.flag}`;
  const helpId = `${id}-help`;
  return (
    <div>
      {field.type === "checkbox" ? (
        <label htmlFor={id} className="flex items-center gap-2 text-sm text-gray-700">
          <input id={id} type="checkbox" checked={value === true} aria-describedby={helpId}
            onChange={(e) => onChange(e.target.checked)} />
          {field.label}
        </label>
      ) : (
        <>
          <label htmlFor={id} className="flex items-baseline gap-1.5 text-sm text-gray-700">
            <span>{field.label}</span>
            {isRequired && <span className="text-rose-600">*</span>}
            <span className="font-mono text-[10px] text-gray-500">-{field.flag}</span>
          </label>
          <input id={id} type={field.type === "number" ? "number" : "text"} aria-describedby={helpId}
            value={typeof value === "string" ? value : ""}
            onChange={(e) => onChange(e.target.value)}
            className={INPUT_CLASS_NAME} />
        </>
      )}
      <p id={helpId} className="mt-1 text-xs text-gray-500">{field.help}</p>
      <ExampleChip field={field} />
    </div>
  );
}

/** Lightly highlighted so it reads as a sample to copy from, not as a value already entered. */
function ExampleChip({ field }: { field: UtilityField }) {
  return (
    <p className="mt-1 text-xs text-gray-500">
      {field.type === "checkbox" ? "CLI flag " : "e.g. "}
      <code className="break-all rounded bg-brand-teal/10 px-1.5 py-0.5 font-mono text-[11px] text-brand-tealInk">
        {field.example}
      </code>
    </p>
  );
}
