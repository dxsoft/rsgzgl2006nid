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
const scanLimit = Number(argValue("--scan-limit", "200"));
const take = Number(argValue("--take", "5"));
const outputPath = argValue("--output", path.join(targetDir, "real-history-write-precheck.tsv"));
const reportPath = outputPath.replace(/\.[^.]+$/, ".txt");

fs.mkdirSync(path.dirname(outputPath), { recursive: true });

async function request(url, options = {}) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutSec * 1000);
  try {
    const response = await fetch(url, { ...options, signal: controller.signal });
    const text = await response.text();
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${text.slice(0, 500)}`);
    }
    return { response, data: text ? JSON.parse(text) : null };
  } finally {
    clearTimeout(timeout);
  }
}

function tsvCell(value) {
  return String(value ?? "").replace(/\r?\n/g, " ").replace(/\t/g, " ");
}

function toNumber(value) {
  return value === null || value === undefined || value === "" ? "" : value;
}

function isRecognizedBusinessType(value) {
  const text = String(value ?? "").trim();
  return text && !text.includes("?") && !text.includes("\uFFFD");
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

const done = await request(`${baseUrl}/api/workbench/items?status=DONE&limit=${scanLimit}`, {
  headers: cookie ? { cookie } : {}
});
const doneItems = done.data?.data?.items || [];
const realItems = doneItems
  .filter((item) => item.source === "SALARY_EVENT" && isRecognizedBusinessType(item.businessType) && item.personCode)
  .sort((a, b) => {
    const type = String(a.businessType).localeCompare(String(b.businessType), "zh-Hans-CN");
    if (type !== 0) return type;
    return Number(b.year || 0) * 100 + Number(b.month || 0) - (Number(a.year || 0) * 100 + Number(a.month || 0));
  });

const selected = [];
const seenTypes = new Set();
for (const item of realItems) {
  if (seenTypes.has(item.businessType)) continue;
  selected.push(item);
  seenTypes.add(item.businessType);
  if (selected.length >= take) break;
}
for (const item of realItems) {
  if (selected.length >= take) break;
  if (selected.some((existing) => existing.id === item.id)) continue;
  selected.push(item);
}

const rows = [];
for (const item of selected) {
  try {
    const preview = await request(`${baseUrl}/api/workbench/salary-cases/preview`, {
      method: "POST",
      headers: {
        "content-type": "application/json; charset=utf-8",
        ...(cookie ? { cookie } : {})
      },
      body: JSON.stringify({
        workItemId: item.id,
        source: "SALARY_EVENT",
        businessType: item.businessType,
        personCode: item.personCode,
        personName: item.personName,
        orgCode: item.orgCode,
        year: item.year,
        month: item.month,
        title: item.title,
        summary: item.summary
      })
    });
    const data = preview.data?.data || {};
    rows.push({
      workItemId: item.id,
      personCode: item.personCode,
      personName: item.personName,
      orgCode: item.orgCode,
      year: item.year,
      month: item.month,
      businessType: item.businessType,
      trialStatus: data.trialStatus,
      trialMatched: data.trialMatched,
      baselineTotal: toNumber(data.trialBaselineTotal),
      calculatedTotal: toNumber(data.trialCalculatedTotal),
      expectedTotal: toNumber(data.trialExpectedTotal),
      difference: toNumber(data.trialDifference),
      canCreateWritePlan: "NO",
      reason: data.trialStatus === "MATCH" || data.trialStatus === "DIFFERENT"
        ? "Preview only. Business confirmation required before completing salary case and creating write plan."
        : "Trial preview not ready: " + (data.trialSummary || "no summary"),
      trialSummary: data.trialSummary || ""
    });
  } catch (error) {
    rows.push({
      workItemId: item.id,
      personCode: item.personCode,
      personName: item.personName,
      orgCode: item.orgCode,
      year: item.year,
      month: item.month,
      businessType: item.businessType,
      trialStatus: "REQUEST_ERROR",
      trialMatched: "",
      baselineTotal: "",
      calculatedTotal: "",
      expectedTotal: "",
      difference: "",
      canCreateWritePlan: "NO",
      reason: error.message,
      trialSummary: ""
    });
  }
}

const headers = [
  "workItemId",
  "personCode",
  "personName",
  "orgCode",
  "year",
  "month",
  "businessType",
  "trialStatus",
  "trialMatched",
  "baselineTotal",
  "calculatedTotal",
  "expectedTotal",
  "difference",
  "canCreateWritePlan",
  "reason",
  "trialSummary"
];
const tsv = [
  headers.join("\t"),
  ...rows.map((row) => headers.map((header) => tsvCell(row[header])).join("\t"))
].join("\n") + "\n";
fs.writeFileSync(outputPath, tsv, "utf8");

const summary = new Map();
for (const row of rows) {
  summary.set(row.businessType, (summary.get(row.businessType) || 0) + 1);
}
const report = [
  "# Real History Write Precheck",
  `GeneratedAt: ${new Date().toLocaleString("zh-CN", { hour12: false })}`,
  `BaseUrl: ${baseUrl}`,
  `ScannedDoneItems: ${doneItems.length}`,
  `SelectedItems: ${rows.length}`,
  "Mode: preview only; no salary case completion, no history write plan creation, no hisbase write.",
  `Output: ${outputPath}`,
  "",
  "Summary:",
  ...Array.from(summary.entries()).map(([key, count]) => `- ${key}: ${count}`),
  "",
  "Rows:",
  ...rows.map((row) => `${row.personCode}\t${row.personName}\t${row.orgCode}\t${row.year}-${String(row.month).padStart(2, "0")}\t${row.businessType}\t${row.trialStatus}\tcalc=${row.calculatedTotal}\texpected=${row.expectedTotal}\tdiff=${row.difference}\t${row.reason}`)
].join("\n") + "\n";
fs.writeFileSync(reportPath, report, "utf8");

console.log("Real history write precheck generated.");
console.log(`TSV: ${outputPath}`);
console.log(`Report: ${reportPath}`);
