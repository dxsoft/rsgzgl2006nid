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
const start = argValue("--start", "2025-01");
const end = argValue("--end", "2026-06");
const orgLimit = Number(argValue("--org-limit", "80"));
const perPreviewLimit = Number(argValue("--per-preview-limit", "100"));
const take = Number(argValue("--take", "20"));
const sources = argValue("--sources", "")
  .split(",")
  .map((source) => source.trim())
  .filter(Boolean);
const candidateStatuses = argValue("--candidate-statuses", "")
  .split(",")
  .map((status) => status.trim().toUpperCase())
  .filter(Boolean);
const outputDir = argValue("--output-dir", path.join(targetDir, "real-writable-candidates"));
fs.mkdirSync(outputDir, { recursive: true });

const endpoints = [
  ["normal-grade", "/api/workbench/normal-grade-applications/preview"],
  ["entry-salary", "/api/workbench/entry-salary-applications/preview"],
  ["post-change", "/api/workbench/post-change-applications/preview"],
  ["allowance-change", "/api/workbench/allowance-change-applications/preview"],
  ["transfer-salary", "/api/workbench/transfer-salary-applications/preview"],
  ["punishment-reduction", "/api/workbench/punishment-reduction-applications/preview"]
].filter(([source]) => !sources.length || sources.includes(source));

if (!endpoints.length) {
  throw new Error(`No endpoints selected. Requested sources: ${sources.join(",")}`);
}

async function request(url, options = {}) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutSec * 1000);
  try {
    const response = await fetch(url, { ...options, signal: controller.signal });
    const text = await response.text();
    if (!response.ok) throw new Error(`HTTP ${response.status}: ${text.slice(0, 500)}`);
    const json = text ? JSON.parse(text) : null;
    if (json && json.success === false) throw new Error(`${json.code || "ERROR"}: ${json.message || ""}`);
    return { response, data: json?.data };
  } finally {
    clearTimeout(timeout);
  }
}

function monthsBetween(startYm, endYm) {
  const [startYear, startMonth] = startYm.split("-").map(Number);
  const [endYear, endMonth] = endYm.split("-").map(Number);
  const months = [];
  let year = startYear;
  let month = startMonth;
  while (year * 100 + month <= endYear * 100 + endMonth) {
    months.push({ year, month });
    month += 1;
    if (month > 12) {
      year += 1;
      month = 1;
    }
  }
  return months.reverse();
}

function flattenOrgs(nodes, output = []) {
  for (const node of nodes || []) {
    if (node.orgCode) output.push({ orgCode: node.orgCode, orgName: node.orgName || "" });
    if (Array.isArray(node.children)) flattenOrgs(node.children, output);
  }
  return output;
}

function tsvCell(value) {
  return String(value ?? "").replace(/\r?\n/g, " ").replace(/\t/g, " ");
}

function candidateStatusAllowed(item) {
  if (!candidateStatuses.length) return true;
  return candidateStatuses.includes(String(item.status || "").toUpperCase());
}

let cookie = "";
if (username) {
  const login = await request(`${baseUrl}/api/auth/login`, {
    method: "POST",
    headers: { "content-type": "application/json; charset=utf-8" },
    body: JSON.stringify({ username, password })
  });
  cookie = login.response.headers.get("set-cookie") || "";
}
const headers = cookie ? { cookie } : {};

const orgTree = await request(`${baseUrl}/api/org/tree`, { headers });
const orgs = flattenOrgs(orgTree.data).slice(0, orgLimit);
const months = monthsBetween(start, end);
const candidates = [];
const candidateKeys = new Set();
const summary = [];
const errors = [];

for (const { year, month } of months) {
  for (const org of orgs) {
    for (const [source, endpoint] of endpoints) {
      const url = `${baseUrl}${endpoint}?orgCode=${encodeURIComponent(org.orgCode)}&year=${year}&month=${month}&limit=${perPreviewLimit}`;
      try {
        const preview = await request(url, {
          method: "POST",
          headers
        });
        const data = preview.data || {};
        if (Number(data.eligibleCount || 0) > 0) {
          summary.push({
            source,
            orgCode: org.orgCode,
            orgName: org.orgName,
            year,
            month,
            checkedCount: data.checkedCount,
            eligibleCount: data.eligibleCount,
            matchedCount: data.matchedCount,
            differentCount: data.differentCount,
            noExpectedCount: data.noExpectedCount,
            skippedCount: data.skippedCount
          });
        }
        for (const item of data.items || []) {
          if (!candidateStatusAllowed(item)) continue;
          const candidateKey = `${source}|${year}|${month}|${item.personCode}`;
          if (candidateKeys.has(candidateKey)) continue;
          candidateKeys.add(candidateKey);
          candidates.push({
            source,
            orgCode: item.orgCode || org.orgCode,
            orgName: item.orgName || org.orgName,
            year,
            month,
            personCode: item.personCode,
            personName: item.personName,
            changeType: item.changeType,
            ruleType: item.ruleType,
            status: item.status,
            beforeValue: item.beforeValue,
            afterValue: item.afterValue,
            changeAmount: item.changeAmount,
            message: item.message || item.ruleNote || ""
          });
          if (candidates.length >= take) break;
        }
      } catch (error) {
        errors.push({ source, orgCode: org.orgCode, year, month, error: error.message });
      }
      if (candidates.length >= take) break;
    }
    if (candidates.length >= take) break;
  }
  if (candidates.length >= take) break;
}

const resultHeaders = [
  "source", "orgCode", "orgName", "year", "month", "personCode", "personName",
  "changeType", "ruleType", "status", "beforeValue", "afterValue", "changeAmount", "message"
];
fs.writeFileSync(
  path.join(outputDir, "real-writable-candidates.tsv"),
  [resultHeaders.join("\t"), ...candidates.map((row) => resultHeaders.map((header) => tsvCell(row[header])).join("\t"))].join("\n") + "\n",
  "utf8"
);
fs.writeFileSync(path.join(outputDir, "real-writable-candidates.json"), JSON.stringify({ generatedAt: new Date().toISOString(), start, end, orgLimit, perPreviewLimit, summary, candidates, errors }, null, 2), "utf8");

const report = [
  "# Real Writable Candidate Scan",
  `GeneratedAt: ${new Date().toLocaleString("zh-CN", { hour12: false })}`,
  `BaseUrl: ${baseUrl}`,
  `Period: ${start}..${end}`,
  `Sources: ${sources.length ? sources.join(",") : "all"}`,
  `CandidateStatuses: ${candidateStatuses.length ? candidateStatuses.join(",") : "all"}`,
  `OrgScanned: ${orgs.length}`,
  `CandidateCount: ${candidates.length}`,
  `SummaryRows: ${summary.length}`,
  `Errors: ${errors.length}`,
  "Mode: preview only; no todo generation, no salary case completion, no write plan creation, no hisbase write.",
  "",
  "Candidates:",
  ...(candidates.length
    ? candidates.map((row) => `- ${row.personCode} ${row.personName} ${row.orgCode} ${row.year}-${String(row.month).padStart(2, "0")} ${row.changeType} ${row.ruleType} ${row.status} ${row.beforeValue || ""}->${row.afterValue || ""}`)
    : ["- none"]),
  "",
  "Next:",
  candidates.length
    ? "- Business owner should confirm selected candidates before generating todo/case/write plans."
    : "- No real writable candidate was found in the scanned range; expand org/month range or move to deployment/user acceptance."
].join("\n") + "\n";
fs.writeFileSync(path.join(outputDir, "real-writable-candidates.txt"), report, "utf8");

console.log("Real writable candidate scan completed.");
console.log(`Report: ${path.join(outputDir, "real-writable-candidates.txt")}`);
