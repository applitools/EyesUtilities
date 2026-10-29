import { render, screen, fireEvent, act, within } from "@testing-library/react";
import { App } from "../src/App";

type FetchCall = { path: string; body: unknown };

const RESULT_URL = "https://eyes.applitools.com/app/batches/1/2";

class RecordingEventSource {
  static instances: RecordingEventSource[] = [];
  onopen: ((e: Event) => void) | null = null;
  onmessage: ((e: MessageEvent) => void) | null = null;
  onerror: ((e: Event) => void) | null = null;
  readyState = 1;
  constructor(public url: string) { RecordingEventSource.instances.push(this); }
  close() {}
}

describe("App", () => {
  let calls: FetchCall[];
  let origFetch: typeof globalThis.fetch;
  let origES: typeof globalThis.EventSource;

  beforeEach(() => {
    localStorage.clear();
    calls = [];
    RecordingEventSource.instances = [];
    origFetch = globalThis.fetch;
    origES = globalThis.EventSource;
    // @ts-expect-error — test stub
    globalThis.EventSource = RecordingEventSource;
    globalThis.fetch = async (url: RequestInfo | URL, init?: RequestInit) => {
      const path = url.toString();
      calls.push({ path, body: init?.body ? JSON.parse(String(init.body)) : undefined });
      if (path.endsWith("/api/status")) return new Response(JSON.stringify({ kind: "idle" }), { status: 200 });
      return new Response(JSON.stringify({ runId: "r1" }), { status: 200 });
    };
  });

  afterEach(() => {
    globalThis.fetch = origFetch;
    globalThis.EventSource = origES;
  });

  async function renderApp() {
    render(<App />);
    // Flush the on-mount status fetch so its idle snapshot can't land after a test's SSE events.
    await act(async () => {});
  }

  function enterApiKey(key: string) {
    fireEvent.click(within(screen.getByRole("banner")).getByRole("button", { name: /Enter API key/ }));
    fireEvent.change(screen.getByLabelText("API key"), { target: { value: key } });
    fireEvent.click(screen.getByRole("button", { name: "Save key" }));
  }

  function selectUtility(id: string) {
    fireEvent.change(screen.getByLabelText("Utility"), { target: { value: id } });
  }

  it("should show the selected utility's required parameters", async () => {
    await renderApp();
    selectUtility("merge");
    expect(screen.getByLabelText(/Source branch/)).toBeInTheDocument();
  });

  it("should not show another utility's required parameters", async () => {
    await renderApp();
    selectUtility("merge");
    expect(screen.queryByLabelText(/Result URL/)).not.toBeInTheDocument();
  });

  it("should keep optional flags out of the setup card until Options is opened", async () => {
    await renderApp();
    selectUtility("merge");
    expect(screen.queryByLabelText(/Target branch/)).not.toBeInTheDocument();
  });

  it("should show each optional flag with a highlighted example when Options is opened", async () => {
    await renderApp();
    selectUtility("merge");
    fireEvent.click(screen.getByRole("button", { name: /Options/ }));
    const panel = screen.getByRole("region", { name: "Merge branch options" });
    expect(within(panel).getByText("release/4.2")).toHaveClass("bg-brand-teal/10");
  });

  it("should disable Run until required parameters are filled", async () => {
    await renderApp();
    selectUtility("merge");
    expect(screen.getByRole("button", { name: "▶ Run Merge branch" })).toBeDisabled();
  });

  it("should post the utility payload when Run is clicked", async () => {
    await renderApp();
    selectUtility("merge");
    enterApiKey("api_key");
    fireEvent.change(screen.getByLabelText(/Source branch/), { target: { value: "feature/x" } });
    await act(async () => { fireEvent.click(screen.getByRole("button", { name: "▶ Run Merge branch" })); });
    expect(calls.find((c) => c.path.endsWith("/api/run"))?.body).toEqual({ command: "merge", values: { s: "feature/x" }, urls: [], key: "api_key" });
  });

  it("should never write a key to local storage", async () => {
    await renderApp();
    selectUtility("diffs");
    fireEvent.change(screen.getByPlaceholderText("paste your enterprise view key"), { target: { value: "vk_secret" } });
    fireEvent.change(screen.getByLabelText(/Result URL/), { target: { value: RESULT_URL } });
    expect(JSON.stringify({ ...localStorage })).not.toContain("vk_secret");
  });

  it("should keep each utility's form when switching between them", async () => {
    await renderApp();
    selectUtility("merge");
    fireEvent.change(screen.getByLabelText(/Source branch/), { target: { value: "feature/x" } });
    selectUtility("diffs");
    selectUtility("merge");
    expect(screen.getByLabelText(/Source branch/)).toHaveValue("feature/x");
  });

  it("should show Cancel while a utility runs", async () => {
    await renderApp();
    act(() => {
      RecordingEventSource.instances[0].onmessage?.({ data: JSON.stringify({ type: "run-started", runId: "r1", utility: "diffs" }) } as MessageEvent);
    });
    expect(screen.getByRole("button", { name: "Cancel" })).toBeInTheDocument();
  });

  it("should open the API key panel from the header button", async () => {
    await renderApp();
    fireEvent.click(within(screen.getByRole("banner")).getByRole("button", { name: /Enter API key/ }));
    expect(screen.getByRole("dialog", { name: "Applitools API key" })).toBeInTheDocument();
  });

  it("should show the key as set in the header once saved", async () => {
    await renderApp();
    enterApiKey("api_key");
    expect(within(screen.getByRole("banner")).getByRole("button", { name: /API key ✓/ })).toBeInTheDocument();
  });

  it("should open the API key panel from the setup card", async () => {
    await renderApp();
    selectUtility("merge");
    fireEvent.click(within(screen.getByRole("main")).getByRole("button", { name: "Enter API key" }));
    expect(screen.getByRole("dialog", { name: "Applitools API key" })).toBeInTheDocument();
  });

  it("should close the API key panel after saving", async () => {
    await renderApp();
    enterApiKey("api_key");
    expect(screen.queryByRole("dialog", { name: "Applitools API key" })).not.toBeInTheDocument();
  });

  it("should never write the API key to local storage", async () => {
    await renderApp();
    enterApiKey("api_secret");
    expect(JSON.stringify({ ...localStorage })).not.toContain("api_secret");
  });
});
