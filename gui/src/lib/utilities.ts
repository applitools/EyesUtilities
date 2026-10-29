import type { UtilityField, UtilitySpec } from "./utilitiesSchema";

export type UtilityValue = string | boolean;
export type UtilityValues = Record<string, UtilityValue>;

export interface UtilityForm {
  values: UtilityValues;
  urlsText: string;
}

/** One saved form per utility, so switching the dropdown never wipes what was typed. */
export type UtilityForms = Record<string, UtilityForm>;

export interface UtilityPayload {
  command: string;
  values: UtilityValues;
  urls: string[];
  /** The view key for download utilities, the API key for everything else. */
  key: string;
}

/** Both keys live only in page memory for the session — never in storage. */
export interface KeyState {
  apiKey: string;
  viewKey: string;
}

const FORMS_STORAGE_KEY = "eyesutilities.forms";
const URL_SEPARATOR = /\s+/;
export const RESULT_URLS_LABEL = "Result URL(s)";
export const VIEW_KEY_LABEL = "View key";
export const API_KEY_LABEL = "Applitools API key";

export const EMPTY_FORM: UtilityForm = { values: {}, urlsText: "" };

export function parseResultUrls(text: string): string[] {
  return text.split(URL_SEPARATOR).map((u) => u.trim()).filter((u) => u.length > 0);
}

function isSet(value: UtilityValue | undefined): boolean {
  return typeof value === "boolean" ? value : (value ?? "").trim().length > 0;
}

function hasBatchLookup(values: UtilityValues): boolean {
  return isSet(values.dr) || isSet(values.q);
}

/** Labels of the required inputs that are still empty, in on-screen order. */
export function listMissingRequired(spec: UtilitySpec, form: UtilityForm, keys: KeyState): string[] {
  const missing: string[] = [];
  if (keyFor(spec, keys).length === 0) missing.push(spec.keyKind === "api" ? API_KEY_LABEL : VIEW_KEY_LABEL);
  if (spec.acceptsResultUrls && parseResultUrls(form.urlsText).length === 0 && !hasBatchLookup(form.values)) {
    missing.push(RESULT_URLS_LABEL);
  }
  for (const field of spec.required) {
    if (!isSet(form.values[field.flag])) missing.push(field.label);
  }
  return missing;
}

export function countSetOptions(spec: UtilitySpec, values: UtilityValues): number {
  return spec.optional.filter((f) => isSet(values[f.flag])).length;
}

function pickSetValues(fields: UtilityField[], values: UtilityValues): UtilityValues {
  const picked: UtilityValues = {};
  for (const field of fields) {
    const value = values[field.flag];
    if (isSet(value)) picked[field.flag] = typeof value === "string" ? value.trim() : value;
  }
  return picked;
}

function keyFor(spec: UtilitySpec, keys: KeyState): string {
  return (spec.keyKind === "api" ? keys.apiKey : keys.viewKey).trim();
}

export function toUtilityPayload(spec: UtilitySpec, form: UtilityForm, keys: KeyState): UtilityPayload {
  return {
    command: spec.id,
    values: pickSetValues([...spec.required, ...spec.optional], form.values),
    urls: spec.acceptsResultUrls ? parseResultUrls(form.urlsText) : [],
    key: keyFor(spec, keys),
  };
}

export function loadForms(): UtilityForms {
  try {
    const raw = localStorage.getItem(FORMS_STORAGE_KEY);
    return raw ? (JSON.parse(raw) as UtilityForms) : {};
  } catch {
    return {};
  }
}

/** Keys are never part of a form, so they never reach storage. */
export function saveForms(forms: UtilityForms): void {
  try { localStorage.setItem(FORMS_STORAGE_KEY, JSON.stringify(forms)); } catch { /* private mode / disabled */ }
}
