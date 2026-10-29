export type UtilityFieldType = "text" | "number" | "checkbox";

export type UtilityKeyKind = "view" | "api";

export interface UtilityField {
  /** EyesUtilities short flag, without the dash. */
  flag: string;
  label: string;
  type: UtilityFieldType;
  help: string;
  /** A sample value, shown lightly highlighted under the field so users see the expected shape. */
  example: string;
}

export interface UtilitySpec {
  /** Matches the backend allowlist (UtilityCommand.id). */
  id: string;
  label: string;
  group: UtilityGroupId;
  description: string;
  keyKind: UtilityKeyKind;
  acceptsResultUrls: boolean;
  /** Irreversible server-side changes get a visible warning next to the Run button. */
  isDestructive?: boolean;
  required: UtilityField[];
  optional: UtilityField[];
}

export type UtilityGroupId = "results" | "baselines" | "admin";

export const UTILITY_GROUPS: { id: UtilityGroupId; label: string }[] = [
  { id: "results", label: "Download results" },
  { id: "baselines", label: "Branches & baselines" },
  { id: "admin", label: "Team admin" },
];

export const RESULT_URLS_EXAMPLE = "https://eyes.applitools.com/app/batches/00000251/00000252";

const SERVER: UtilityField = {
  flag: "as", label: "Server", type: "text",
  help: "Applitools server host, for dedicated or on-prem clouds. Default: eyes.applitools.com.",
  example: "mycompany.applitools.com",
};

const DISABLE_SSL: UtilityField = {
  flag: "dv", label: "Disable SSL verify", type: "checkbox",
  help: "Skip SSL certificate validation. Insecure — only if your network requires it.",
  example: "-dv",
};

const BATCH_LOOKUP: UtilityField[] = [
  { flag: "dr", label: "Date range", type: "text",
    help: "Find batches that ran in this range instead of listing result URLs. Format [month-day-year,month-day-year].",
    example: "[1-5-2026,1-31-2026]" },
  { flag: "q", label: "Batch query", type: "text",
    help: "Find batches by id instead of listing result URLs. Separate several with ;. Usually needs User name and User id.",
    example: "sid:00000251;sid:00000252" },
  { flag: "un", label: "User name", type: "text", help: "Applitools user name, for date-range and query lookups.", example: "jane.doe" },
  { flag: "ui", label: "User id", type: "text", help: "Applitools internal user id, for date-range and query lookups.", example: "00000251abcdef" },
  { flag: "l", label: "Batch limit", type: "number", help: "Maximum number of batches a date range or query can return. Default: 1000.", example: "200" },
];

const DESTINATION: UtilityField = {
  flag: "d", label: "Destination", type: "text",
  help: "Path template for the downloaded files; {workdir_root} is Downloads/EyesUtilities. Other placeholders: {batch_id}, {test_id}, {step_index}, {step_tag}, {artifact_type}, {file_ext}.",
  example: "{workdir_root}/Diffs/{batch_id}/file:{step_index}_{step_tag}.{file_ext}",
};

const RESULTS_OPTIONS: UtilityField[] = [...BATCH_LOOKUP, DESTINATION, SERVER, DISABLE_SSL];

const INTERVAL: UtilityField = {
  flag: "i", label: "Frame interval (ms)", type: "number",
  help: "How long each frame shows in the GIF. Default: 1000.", example: "500",
};

const ORG_ID: UtilityField = {
  flag: "or", label: "Organization id", type: "text",
  help: "Your organization id, as it appears in dashboard URLs.", example: "5f1e2d3c4b5a69788796a5b4",
};

const TEAM_ID: UtilityField = {
  flag: "ti", label: "Team id", type: "text",
  help: "Team id — run Get teams to look it up.", example: "61a2b3c4d5e6f708192a3b4c",
};

export const UTILITIES: UtilitySpec[] = [
  {
    id: "diffs", label: "Download diffs", group: "results", keyKind: "view", acceptsResultUrls: true,
    description: "Download the diff images of failed steps.",
    required: [], optional: RESULTS_OPTIONS,
  },
  {
    id: "images", label: "Download images", group: "results", keyKind: "view", acceptsResultUrls: true,
    description: "Download the baseline and checkpoint images of each step.",
    required: [],
    optional: [
      { flag: "st", label: "Test statuses", type: "text",
        help: "Download only tests with these statuses, separated by ;. Any of Passed, Failed, Unresolved, Running, Aborted. Also groups the images by status.",
        example: "Failed;Unresolved" },
      { flag: "gs", label: "Group by status", type: "checkbox", help: "Put the images in one folder per test status.", example: "-gs" },
      { flag: "a", label: "Only checkpoints (actual)", type: "checkbox", help: "Save only the checkpoint images.", example: "-a" },
      { flag: "b", label: "Only baselines", type: "checkbox", help: "Save only the baseline images.", example: "-b" },
      ...RESULTS_OPTIONS,
    ],
  },
  {
    id: "anidiffs", label: "Animated diffs (GIF)", group: "results", keyKind: "view", acceptsResultUrls: true,
    description: "Build an animated GIF that flips between baseline, checkpoint and diff for each failed step.",
    required: [], optional: [INTERVAL, ...RESULTS_OPTIONS],
  },
  {
    id: "playback", label: "Playback (GIF)", group: "results", keyKind: "view", acceptsResultUrls: true,
    description: "Build an animated GIF that plays a test's steps in order.",
    required: [],
    optional: [
      { flag: "m", label: "Mark diffs", type: "checkbox", help: "Highlight the differences on each frame.", example: "-m" },
      INTERVAL,
      ...RESULTS_OPTIONS,
    ],
  },
  {
    id: "report", label: "HTML report", group: "results", keyKind: "view", acceptsResultUrls: true,
    description: "Generate an HTML report for a batch from a Velocity template.",
    required: [],
    optional: [
      { flag: "rt", label: "Report title", type: "text", help: "Title shown at the top of the report.", example: "Release 4.2 — visual review" },
      { flag: "t", label: "Template file", type: "text",
        help: "Path to a Velocity template. Default: report.templ, downloaded from the EyesUtilities repo when it isn't in the output folder.",
        example: "/Users/me/templates/report.templ" },
      { flag: "d", label: "Output file", type: "text", help: "Report file, relative to Downloads/EyesUtilities. Default: report.html.", example: "reports/release-4.2.html" },
      ...BATCH_LOOKUP,
      SERVER,
      DISABLE_SSL,
    ],
  },
  {
    id: "merge", label: "Merge branch", group: "baselines", keyKind: "api", acceptsResultUrls: false, isDestructive: true,
    description: "Merge a source branch's baselines into a target branch.",
    required: [
      { flag: "s", label: "Source branch", type: "text", help: "Branch whose baselines are merged.", example: "feature/login-redesign" },
    ],
    optional: [
      { flag: "t", label: "Target branch", type: "text", help: "Branch to merge into. Default: the main (default) branch.", example: "release/4.2" },
      { flag: "d", label: "Delete source branch", type: "checkbox", help: "Delete the source branch after a successful merge.", example: "-d" },
      { flag: "db", label: "Delete source baselines", type: "checkbox", help: "Delete the baselines associated with the source branch.", example: "-db" },
      { flag: "gt", label: "Git merge timestamp", type: "text", help: "Index baselines by this git merge timestamp.", example: "2026-09-30T14:05:00Z" },
      SERVER,
      DISABLE_SSL,
    ],
  },
  {
    id: "copyBaselines", label: "Copy baselines", group: "baselines", keyKind: "api", acceptsResultUrls: false, isDestructive: true,
    description: "Copy baselines from one branch to another.",
    required: [
      { flag: "t", label: "Target branch", type: "text", help: "Branch that receives the baselines.", example: "release/4.2" },
    ],
    optional: [
      { flag: "s", label: "Source branch", type: "text", help: "Branch to copy from. Default: the main (default) branch.", example: "feature/login-redesign" },
      { flag: "an", label: "App name", type: "text", help: "Copy only baselines of this application.", example: "Checkout Web" },
      { flag: "li", label: "Limit", type: "number", help: "Maximum number of baselines to copy.", example: "50" },
      SERVER,
      DISABLE_SSL,
    ],
  },
  {
    id: "deleteBaselines", label: "Delete baselines", group: "baselines", keyKind: "api", acceptsResultUrls: false, isDestructive: true,
    description: "Permanently delete baselines by id.",
    required: [
      { flag: "bld", label: "Baseline ids", type: "text", help: "Comma-separated baseline ids.", example: "5f1e2d3c4b5a6978,5f1e2d3c4b5a6979" },
    ],
    optional: [SERVER, DISABLE_SSL],
  },
  {
    id: "admin.getTeams", label: "Get teams", group: "admin", keyKind: "api", acceptsResultUrls: false,
    description: "List all teams in the organization.",
    required: [ORG_ID], optional: [],
  },
  {
    id: "admin.addTeam", label: "Add team", group: "admin", keyKind: "api", acceptsResultUrls: false,
    description: "Create a new team in the organization.",
    required: [ORG_ID, { flag: "tn", label: "Team name", type: "text", help: "Name of the new team.", example: "Mobile QA" }],
    optional: [],
  },
  {
    id: "admin.getUsers", label: "Get users", group: "admin", keyKind: "api", acceptsResultUrls: false,
    description: "List the users of a team.",
    required: [ORG_ID, TEAM_ID], optional: [],
  },
  {
    id: "admin.addUser", label: "Add user", group: "admin", keyKind: "api", acceptsResultUrls: false,
    description: "Add a user to a team.",
    required: [ORG_ID, TEAM_ID],
    optional: [
      { flag: "ne", label: "Email", type: "text", help: "Email of the new user.", example: "jane.doe@example.com" },
      { flag: "ni", label: "User id", type: "text", help: "User id to add — usually the email address.", example: "jane.doe@example.com" },
      { flag: "nn", label: "Full name", type: "text", help: "The user's full name.", example: "Jane Doe" },
      { flag: "ad", label: "Team admin", type: "checkbox", help: "Give the user team-admin permissions.", example: "-ad" },
      { flag: "ve", label: "Viewer", type: "checkbox", help: "Give the user viewer-only permissions.", example: "-ve" },
    ],
  },
  {
    id: "admin.remUser", label: "Remove user", group: "admin", keyKind: "api", acceptsResultUrls: false, isDestructive: true,
    description: "Remove a user from a team. Removing a user from the whole organization asks for confirmation, so it is CLI-only.",
    required: [
      ORG_ID,
      { flag: "ri", label: "User id", type: "text", help: "User id to remove — usually the email address.", example: "jane.doe@example.com" },
      TEAM_ID,
    ],
    optional: [],
  },
];

export const DEFAULT_UTILITY_ID = UTILITIES[0].id;

export function findUtility(id: string): UtilitySpec {
  return UTILITIES.find((u) => u.id === id) ?? UTILITIES[0];
}

export const README_URL = "https://github.com/applitools/EyesUtilities#readme";
