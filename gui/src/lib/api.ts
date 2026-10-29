import { getToken } from "./token";
import type { UtilityPayload } from "./utilities";

export class ApiError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

/** Callers branch on the status (e.g. 409 = a utility is already running), never on the text. */
async function toApiError(res: Response): Promise<ApiError> {
  const raw = await res.text().catch(() => "");
  let message = raw;
  try {
    const parsed = JSON.parse(raw) as { error?: string };
    if (parsed.error) message = parsed.error;
  } catch { /* not the server's {"error": …} shape — show the body as-is */ }
  return new ApiError(res.status, message || `Request failed (${res.status})`);
}

async function http<T>(method: string, path: string, body?: unknown): Promise<T> {
  const res = await fetch(path, {
    method,
    headers: {
      "Authorization": `Bearer ${getToken()}`,
      ...(body ? { "Content-Type": "application/json" } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) throw await toApiError(res);
  if (res.status === 204) return undefined as unknown as T;
  return res.json();
}

export const api = {
  status: () => http<unknown>("GET", "/api/status"),
  run: (payload: UtilityPayload) => http<{ runId: string }>("POST", "/api/run", payload),
  cancel: () => http<void>("POST", "/api/cancel"),
};
