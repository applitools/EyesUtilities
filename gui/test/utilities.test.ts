import { findUtility } from "../src/lib/utilitiesSchema";
import { EMPTY_FORM, countSetOptions, listMissingRequired, parseResultUrls, toUtilityPayload } from "../src/lib/utilities";

const DIFFS = findUtility("diffs");
const MERGE = findUtility("merge");
const URL_A = "https://eyes.applitools.com/app/batches/1/2";
const URL_B = "https://eyes.applitools.com/app/batches/3/4";
const KEYS = { apiKey: "api", viewKey: "view" };

describe("parseResultUrls", () => {
  it("should split URLs on newlines and spaces", () => {
    expect(parseResultUrls(`${URL_A}\n  ${URL_B} `)).toEqual([URL_A, URL_B]);
  });

  it("should return empty array when text is blank", () => {
    expect(parseResultUrls(" \n ")).toEqual([]);
  });
});

describe("listMissingRequired", () => {
  it("should require a view key for download utilities", () => {
    expect(listMissingRequired(DIFFS, { values: {}, urlsText: URL_A }, { ...KEYS, viewKey: " " })).toEqual(["View key"]);
  });

  it("should require result URLs when no date range or query is set", () => {
    expect(listMissingRequired(DIFFS, EMPTY_FORM, KEYS)).toEqual(["Result URL(s)"]);
  });

  it("should accept a date range in place of result URLs", () => {
    expect(listMissingRequired(DIFFS, { values: { dr: "[1-5-2026,1-31-2026]" }, urlsText: "" }, KEYS)).toEqual([]);
  });

  it("should require an API key for branch utilities", () => {
    expect(listMissingRequired(MERGE, { values: { s: "feature/x" }, urlsText: "" }, { ...KEYS, apiKey: "" })).toEqual(["Applitools API key"]);
  });

  it("should list empty required flags by label", () => {
    expect(listMissingRequired(MERGE, { values: { s: "  " }, urlsText: "" }, KEYS)).toEqual(["Source branch"]);
  });
});

describe("toUtilityPayload", () => {
  it("should send only flags that are set", () => {
    const payload = toUtilityPayload(MERGE, { values: { s: " feature/x ", t: "", d: false, db: true }, urlsText: "" }, KEYS);
    expect(payload.values).toEqual({ s: "feature/x", db: true });
  });

  it("should drop values of flags the utility does not define", () => {
    const payload = toUtilityPayload(MERGE, { values: { s: "feature/x", bld: "abc" }, urlsText: "" }, KEYS);
    expect(payload.values).toEqual({ s: "feature/x" });
  });

  it("should send the view key for download utilities", () => {
    expect(toUtilityPayload(DIFFS, { values: {}, urlsText: URL_A }, { ...KEYS, viewKey: " vk " }).key).toBe("vk");
  });

  it("should send the API key for branch utilities", () => {
    expect(toUtilityPayload(MERGE, { values: { s: "x" }, urlsText: "" }, KEYS).key).toBe("api");
  });

  it("should send no result URLs for utilities that take none", () => {
    expect(toUtilityPayload(MERGE, { values: { s: "x" }, urlsText: URL_A }, KEYS).urls).toEqual([]);
  });
});

describe("countSetOptions", () => {
  it("should count only optional flags that are set", () => {
    expect(countSetOptions(MERGE, { s: "x", t: "main", dv: true, d: false })).toBe(2);
  });
});
