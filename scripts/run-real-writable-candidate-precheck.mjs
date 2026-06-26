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
const inputPath = argValue("--input", path.join(targetDir, "real-writable-candidates", "real-writable-candidates.tsv"));
const outputDir = argValue("--output-dir", path.join(targetDir, "real-writable-candidate-precheck"));
const take = Number(argValue("--take", "3"));
const workItemSuffix = argValue("--work-item-suffix", "");
const reviewBeforeConfirm = process.argv.includes("--review-before-confirm");
const reviewReason = argValue("--review-reason", "Real writable candidate precheck review.");

fs.mkdirSync(outputDir, { recursive: true });

const workItemPrefixes = {
  "normal-grade": "normal-grade",
  "entry-salary": "entry-salary",
  "post-change": "post-change",
  "allowance-change": "allowance-change",
  "transfer-salary": "transfer-salary",
  "punishment-reduction": "punishment-reduction"
};

async function request(url, options = {}) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutSec * 1000);
  try {
    const response = await fetch(url, { ...options, signal: controller.signal });
    const text = await response.text();
    if (!response.ok) throw new Error(`HTTP ${response.status}: ${text.slice(0, 1000)}`);
    const contentType = response.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
      const json = text ? JSON.parse(text) : null;
      if (json && json.success === false) throw new Error(`${json.code || "ERROR"}: ${json.message || ""}`);
      return { response, data: json };
    }
    return { response, data: text };
  } finally {
    clearTimeout(timeout);
  }
}

function parseTsv(file) {
  const text = fs.readFileSync(file, "utf8").trim();
  if (!text) return [];
  const [headerLine, ...lines] = text.split(/\r?\n/);
  const headers = headerLine.split("\t");
  return lines.filter(Boolean).map((line) => {
    const values = line.split("\t");
    return Object.fromEntries(headers.map((header, index) => [header, values[index] ?? ""]));
  });
}

function tsvCell(value) {
  return String(value ?? "").replace(/\r?\n/g, " ").replace(/\t/g, " ");
}

function writeJson(name, value) {
  fs.writeFileSync(path.join(outputDir, name), JSON.stringify(value, null, 2), "utf8");
}

function ym(row) {
  return `${row.year}-${String(row.month).padStart(2, "0")}`;
}

function workItemId(row) {
  const prefix = workItemPrefixes[row.source];
  if (!prefix) throw new Error(`Unsupported candidate source: ${row.source}`);
  const baseId = `${prefix}-${row.orgCode}-${row.year}-${String(row.month).padStart(2, "0")}-${row.personCode}`;
  return workItemSuffix ? `${baseId}-${workItemSuffix}` : baseId;
}

function businessType(row) {
  if (row.ruleType === "INSTITUTION_POST_CHANGE") return "\u804c\u52a1\u53d8\u5316";
  if (row.ruleType === "PROBATIONARY_NEW_SALARY") return "\u65b0\u8fdb\u5de5\u8d44";
  if (row.ruleType === "REGULARIZATION_GRADE_PLACEMENT") return "\u8f6c\u6b63\u5b9a\u7ea7";
  if (row.source === "transfer-salary") return "\u8c03\u5165\u5b9a\u8d44";
  if (row.ruleType === "SALARY_GRADE_INCREMENT") return "\u6b63\u5e38\u85aa\u7ea7";
  if (row.ruleType === "GRADE_INCREMENT") return "\u6b63\u5e38\u6863\u6b21";
  if (row.ruleType === "LEVEL_PROMOTION") return "\u6b63\u5e38\u7ea7\u522b";
  return row.changeType;
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
const headers = () => cookie ? { cookie } : {};

const inputRows = parseTsv(inputPath)
  .filter((row) => row.personCode && row.orgCode && row.year && row.month && row.changeType)
  .slice(0, take);
if (!inputRows.length) {
  writeJson("manifest.json", {
    generatedAt: new Date().toISOString(),
    baseUrl,
    inputPath,
    outputDir,
    mode: "complete-case-and-confirm-history-write-only",
    reviewBeforeConfirm,
    noHisbaseWrite: true,
    rows: []
  });
  const emptyHeaders = [
    "source", "orgCode", "orgName", "year", "month", "personCode", "personName",
    "changeType", "ruleType", "status", "workItemId", "caseNo", "trialStatus",
    "reviewApplied", "previewStatus", "executable", "writable", "writePlanId", "issues", "note"
  ];
  const resultPath = path.join(outputDir, "candidate-precheck-results.tsv");
  fs.writeFileSync(resultPath, emptyHeaders.join("\t") + "\n", "utf8");
  const reportPath = path.join(outputDir, "candidate-precheck-report.txt");
  fs.writeFileSync(reportPath, [
    "# Real Writable Candidate Precheck",
    `GeneratedAt: ${new Date().toLocaleString("zh-CN", { hour12: false })}`,
    `BaseUrl: ${baseUrl}`,
    `Input: ${inputPath}`,
    `OutputDir: ${outputDir}`,
    "Mode: complete salary case and confirm history write plan only.",
    `ReviewBeforeConfirm: ${reviewBeforeConfirm}`,
    "Safety: no execute endpoint was called; hisbase was not written.",
    "Selected: 0",
    "Ready: 0",
    "Blocked: 0",
    "Errors: 0",
    "",
    "Rows:"
  ].join("\n") + "\n", "utf8");
  console.log("Real writable candidate precheck completed.");
  console.log(`Report: ${reportPath}`);
  console.log(`Results: ${resultPath}`);
  process.exit(0);
}

const results = [];
for (const row of inputRows) {
  const id = workItemId(row);
  const rowBusinessType = businessType(row);
  const body = {
    workItemId: id,
    source: "SALARY_EVENT",
    businessType: rowBusinessType,
    personCode: row.personCode,
    personName: row.personName,
    orgCode: row.orgCode,
    year: Number(row.year),
    month: Number(row.month),
    title: rowBusinessType,
    summary: `real writable candidate precheck ${ym(row)} ${row.beforeValue || ""}->${row.afterValue || ""}`,
    differenceReason: "real writable candidate precheck",
    force: true,
    forceReason: "real writable candidate precheck"
  };
  try {
    const completed = await request(`${baseUrl}/api/workbench/salary-cases`, {
      method: "POST",
      headers: { "content-type": "application/json; charset=utf-8", ...headers() },
      body: JSON.stringify(body)
    });
    const caseNo = completed.data?.data?.id;
    const trialStatus = completed.data?.data?.trialStatus || "";
    let reviewApplied = false;
    if (reviewBeforeConfirm && ["DIFFERENT", "ERROR"].includes(trialStatus)) {
      await request(`${baseUrl}/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/review`, {
        method: "POST",
        headers: { "content-type": "application/json; charset=utf-8", ...headers() },
        body: JSON.stringify({ reviewReason })
      });
      reviewApplied = true;
    }
    const preview = await request(`${baseUrl}/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/history-write-confirm`, {
      method: "POST",
      headers: headers()
    });
    writeJson(`confirm-${caseNo}.json`, preview.data);
    const data = preview.data?.data || {};
    results.push({
      ...row,
      changeType: rowBusinessType,
      workItemId: id,
      caseNo,
      trialStatus,
      reviewApplied,
      previewStatus: data.status || "",
      executable: data.executable,
      writable: data.writable,
      writePlanId: data.writePlanId || "",
      issues: (data.issues || []).join(" | "),
      note: `${reviewApplied ? "case reviewed, " : ""}case completed and history write plan confirmed; no execute call was made`
    });
  } catch (error) {
    results.push({
      ...row,
      changeType: rowBusinessType,
      workItemId: id,
      caseNo: "",
      trialStatus: "ERROR",
      reviewApplied: false,
      previewStatus: "ERROR",
      executable: false,
      writable: false,
      writePlanId: "",
      issues: error.message,
      note: "candidate precheck failed"
    });
  }
}

writeJson("manifest.json", {
  generatedAt: new Date().toISOString(),
  baseUrl,
  inputPath,
  outputDir,
  mode: "complete-case-and-confirm-history-write-only",
  workItemSuffix,
  reviewBeforeConfirm,
  noHisbaseWrite: true,
  rows: results
});

const resultHeaders = [
  "source", "orgCode", "orgName", "year", "month", "personCode", "personName",
  "changeType", "ruleType", "status", "workItemId", "caseNo", "trialStatus",
  "reviewApplied", "previewStatus", "executable", "writable", "writePlanId", "issues", "note"
];
const resultPath = path.join(outputDir, "candidate-precheck-results.tsv");
fs.writeFileSync(
  resultPath,
  [resultHeaders.join("\t"), ...results.map((row) => resultHeaders.map((header) => tsvCell(row[header])).join("\t"))].join("\n") + "\n",
  "utf8"
);

const report = [
  "# Real Writable Candidate Precheck",
  `GeneratedAt: ${new Date().toLocaleString("zh-CN", { hour12: false })}`,
  `BaseUrl: ${baseUrl}`,
  `Input: ${inputPath}`,
  `OutputDir: ${outputDir}`,
  "Mode: complete salary case and confirm history write plan only.",
  `ReviewBeforeConfirm: ${reviewBeforeConfirm}`,
  "Safety: no execute endpoint was called; hisbase was not written.",
  `Selected: ${results.length}`,
  `Ready: ${results.filter((row) => row.previewStatus === "READY").length}`,
  `Blocked: ${results.filter((row) => row.previewStatus === "BLOCKED").length}`,
  `Errors: ${results.filter((row) => row.previewStatus === "ERROR").length}`,
  "",
  "Rows:",
  ...results.map((row) => `${row.personCode}\t${row.personName}\t${row.changeType}\t${row.previewStatus}\texecutable=${row.executable}\twritable=${row.writable}\tcase=${row.caseNo}\t${row.issues || row.note}`)
].join("\n") + "\n";
const reportPath = path.join(outputDir, "candidate-precheck-report.txt");
fs.writeFileSync(reportPath, report, "utf8");

console.log("Real writable candidate precheck completed.");
console.log(`Report: ${reportPath}`);
console.log(`Results: ${resultPath}`);
