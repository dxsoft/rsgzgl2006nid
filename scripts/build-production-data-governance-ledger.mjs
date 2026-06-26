import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const root = path.dirname(scriptDir);
const targetDir = path.join(root, "backend", "target");

function argValue(name, fallback) {
  const index = process.argv.indexOf(name);
  return index >= 0 && index + 1 < process.argv.length ? process.argv[index + 1] : fallback;
}

const baseUrl = argValue("--base-url", "http://127.0.0.1:18080").replace(/\/$/, "");
const timeoutSec = Number(argValue("--timeout-sec", "30"));
const username = argValue("--username", "admin");
const password = argValue("--password", "admin");
const orgCodes = argValue("--org-codes", "00826,00806,00802,00818,01409").split(",").map((item) => item.trim()).filter(Boolean);
const limit = Number(argValue("--limit", "20"));
const outputDir = argValue("--output-dir", path.join(targetDir, "data-governance-ledger"));
fs.mkdirSync(outputDir, { recursive: true });

async function request(url, options = {}) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutSec * 1000);
  try {
    const response = await fetch(url, { ...options, signal: controller.signal });
    const text = await response.text();
    if (!response.ok) throw new Error(`HTTP ${response.status}: ${text.slice(0, 500)}`);
    const json = text ? JSON.parse(text) : null;
    if (json && json.success === false) throw new Error(`${json.code || "ERROR"}: ${json.message || ""}`);
    return json?.data;
  } finally {
    clearTimeout(timeout);
  }
}

let cookie = "";
if (username) {
  const login = await fetch(`${baseUrl}/api/auth/login`, {
    method: "POST",
    headers: { "content-type": "application/json; charset=utf-8" },
    body: JSON.stringify({ username, password })
  });
  cookie = login.headers.get("set-cookie") || "";
}
const headers = cookie ? { cookie } : {};

const scans = [];
for (const orgCode of orgCodes) {
  const scan = await request(`${baseUrl}/api/workbench/data-governance/scan?orgCode=${encodeURIComponent(orgCode)}&limit=${limit}`, { headers });
  scans.push(scan);
}

fs.writeFileSync(path.join(outputDir, "data-governance-ledger.json"), JSON.stringify({ generatedAt: new Date().toISOString(), scans }, null, 2), "utf8");

const issueRows = [];
for (const scan of scans) {
  for (const issue of scan.issues || []) {
    issueRows.push({
      orgCode: scan.orgCode,
      personCode: issue.personCode || "",
      personName: issue.personName || "",
      issueType: issue.issueType || issue.type || "",
      message: issue.message || issue.summary || "",
      severity: issue.severity || ""
    });
  }
}
const headersOut = ["orgCode", "personCode", "personName", "issueType", "severity", "message"];
fs.writeFileSync(
  path.join(outputDir, "data-governance-ledger.tsv"),
  [headersOut.join("\t"), ...issueRows.map((row) => headersOut.map((header) => String(row[header] ?? "").replace(/\t|\r?\n/g, " ")).join("\t"))].join("\n") + "\n",
  "utf8"
);

const report = [
  "# Production Data Governance Ledger",
  `GeneratedAt: ${new Date().toLocaleString("zh-CN", { hour12: false })}`,
  `BaseUrl: ${baseUrl}`,
  `OrgCodes: ${orgCodes.join(", ")}`,
  `IssueRows: ${issueRows.length}`,
  "",
  "Per Org:",
  ...scans.map((scan) => `- ${scan.orgCode}: issues=${scan.issueCount ?? (scan.issues || []).length}, standardReview=${scan.standardReviewCount ?? ""}, retirementDeferred=${scan.retirementDeferredNote ?? ""}`),
  "",
  "Policy:",
  "- Resolve blocking data issues before real write batches.",
  "- Known historical special cases can be kept in the migration acceptance issue ledger.",
  "- Retirement issues remain deferred to the independent retirement project.",
  "- Re-run launch readiness and real precheck after data maintenance."
].join("\n") + "\n";
fs.writeFileSync(path.join(outputDir, "data-governance-ledger.txt"), report, "utf8");

console.log("Production data governance ledger generated.");
console.log(`Report: ${path.join(outputDir, "data-governance-ledger.txt")}`);
