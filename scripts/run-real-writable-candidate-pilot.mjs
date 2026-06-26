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
const timeoutSec = Number(argValue("--timeout-sec", "180"));
const username = argValue("--username", "admin");
const password = argValue("--password", "admin");
const inputPath = argValue("--input", path.join(targetDir, "real-writable-candidate-precheck-reviewed", "candidate-precheck-results.tsv"));
const outputDir = argValue("--output-dir", path.join(targetDir, "real-writable-candidate-pilot"));
const take = Number(argValue("--take", "1"));
const keepWritten = process.argv.includes("--keep-written");

fs.mkdirSync(outputDir, { recursive: true });

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

const selected = parseTsv(inputPath)
  .filter((row) => row.caseNo && row.previewStatus === "READY" && String(row.executable).toLowerCase() === "true")
  .slice(0, take);
if (!selected.length) {
  throw new Error(`No READY executable candidate found in ${inputPath}`);
}

const results = selected.map((row) => ({
  ...row,
  executeStatus: "PENDING",
  historyId: "",
  comparisonStatus: "",
  rollbackPreviewStatus: "",
  rollbackStatus: keepWritten ? "KEEP_WRITTEN" : "PENDING",
  note: ""
}));
const caseNos = selected.map((row) => row.caseNo);

for (const row of selected) {
  const safeCode = row.personCode.replace(/[^A-Za-z0-9_-]/g, "_");
  try {
    const history = await request(`${baseUrl}/api/salary/history/${encodeURIComponent(row.personCode)}`, { headers: headers() });
    writeJson(`before-history-${safeCode}.json`, history.data);
  } catch (error) {
    writeJson(`before-history-${safeCode}.json`, { error: error.message });
  }
}

const execute = await request(`${baseUrl}/api/workbench/history-write-plans/selected-execute`, {
  method: "POST",
  headers: { "content-type": "application/json; charset=utf-8", ...headers() },
  body: JSON.stringify({ caseNos })
});
writeJson("selected-execute.json", execute.data);
for (const item of execute.data?.data?.items || []) {
  const row = results.find((candidate) => candidate.caseNo === item.caseNo);
  if (row) {
    row.executeStatus = item.status || "";
    row.historyId = item.historyId || "";
    row.note = item.message || "";
  }
}

for (const row of results) {
  const safeCode = row.personCode.replace(/[^A-Za-z0-9_-]/g, "_");
  if (row.executeStatus === "EXECUTED") {
    try {
      const comparison = await request(`${baseUrl}/api/workbench/salary-cases/${encodeURIComponent(row.caseNo)}/history-write-comparison`, {
        headers: headers()
      });
      writeJson(`comparison-${row.caseNo}.json`, comparison.data);
      const comparisonData = comparison.data?.data || {};
      row.comparisonStatus = comparisonData.totalMatched === true ? "MATCH" : "MISMATCH";
      const audits = await request(`${baseUrl}/api/workbench/salary-cases/${encodeURIComponent(row.caseNo)}/history-write-audits.csv`, {
        headers: headers()
      });
      fs.writeFileSync(path.join(outputDir, `audits-${row.caseNo}.csv`), audits.data, "utf8");
    } catch (error) {
      row.note = `${row.note}; comparison/audit export failed: ${error.message}`;
    }
  }
  try {
    const history = await request(`${baseUrl}/api/salary/history/${encodeURIComponent(row.personCode)}`, { headers: headers() });
    writeJson(`after-write-history-${safeCode}.json`, history.data);
  } catch (error) {
    writeJson(`after-write-history-${safeCode}.json`, { error: error.message });
  }
}

if (!keepWritten) {
  for (const row of results.filter((item) => item.executeStatus === "EXECUTED")) {
    try {
      const preview = await request(`${baseUrl}/api/workbench/salary-cases/${encodeURIComponent(row.caseNo)}/history-write-rollback-preview`, {
        method: "POST",
        headers: headers()
      });
      writeJson(`rollback-preview-${row.caseNo}.json`, preview.data);
      row.rollbackPreviewStatus = preview.data?.data?.status || "";
    } catch (error) {
      row.rollbackPreviewStatus = `ERROR: ${error.message}`;
    }
  }
  const rollbackCaseNos = results.filter((item) => item.executeStatus === "EXECUTED").map((item) => item.caseNo);
  if (rollbackCaseNos.length) {
    const rollback = await request(`${baseUrl}/api/workbench/history-write-plans/selected-rollback`, {
      method: "POST",
      headers: { "content-type": "application/json; charset=utf-8", ...headers() },
      body: JSON.stringify({ caseNos: rollbackCaseNos })
    });
    writeJson("selected-rollback.json", rollback.data);
    for (const item of rollback.data?.data?.items || []) {
      const row = results.find((candidate) => candidate.caseNo === item.caseNo);
      if (row) {
        row.rollbackStatus = item.status || "";
        row.note = `${row.note}; rollback=${item.message || item.status || ""}`;
      }
    }
  }
  for (const row of results) {
    const safeCode = row.personCode.replace(/[^A-Za-z0-9_-]/g, "_");
    try {
      const history = await request(`${baseUrl}/api/salary/history/${encodeURIComponent(row.personCode)}`, { headers: headers() });
      writeJson(`after-rollback-history-${safeCode}.json`, history.data);
    } catch (error) {
      writeJson(`after-rollback-history-${safeCode}.json`, { error: error.message });
    }
  }
}

writeJson("manifest.json", {
  generatedAt: new Date().toISOString(),
  baseUrl,
  inputPath,
  outputDir,
  keepWritten,
  selected: caseNos,
  rows: results
});

const resultHeaders = [
  "caseNo", "personCode", "personName", "orgCode", "year", "month", "changeType",
  "executeStatus", "historyId", "comparisonStatus", "rollbackPreviewStatus", "rollbackStatus", "note"
];
const resultPath = path.join(outputDir, "candidate-pilot-results.tsv");
fs.writeFileSync(
  resultPath,
  [resultHeaders.join("\t"), ...results.map((row) => resultHeaders.map((header) => tsvCell(row[header])).join("\t"))].join("\n") + "\n",
  "utf8"
);

const report = [
  "# Real Writable Candidate Pilot",
  `GeneratedAt: ${new Date().toLocaleString("zh-CN", { hour12: false })}`,
  `BaseUrl: ${baseUrl}`,
  `Input: ${inputPath}`,
  `OutputDir: ${outputDir}`,
  `Mode: ${keepWritten ? "write-and-keep" : "write-then-rollback"}`,
  `Selected: ${results.length}`,
  `Executed: ${results.filter((row) => row.executeStatus === "EXECUTED").length}`,
  `RolledBack: ${results.filter((row) => row.rollbackStatus === "ROLLED_BACK").length}`,
  "",
  "Rows:",
  ...results.map((row) => `${row.personCode}\t${row.personName}\t${row.changeType}\texecute=${row.executeStatus}\thistoryId=${row.historyId}\tcomparison=${row.comparisonStatus}\trollback=${row.rollbackStatus}\t${row.note}`)
].join("\n") + "\n";
const reportPath = path.join(outputDir, "candidate-pilot-report.txt");
fs.writeFileSync(reportPath, report, "utf8");

console.log("Real writable candidate pilot completed.");
console.log(`Report: ${reportPath}`);
console.log(`Results: ${resultPath}`);
