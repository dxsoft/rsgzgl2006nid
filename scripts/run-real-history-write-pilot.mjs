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
const inputPath = argValue("--input", path.join(targetDir, "real-history-write-precheck.tsv"));
const outputDir = argValue("--output-dir", path.join(targetDir, "real-history-write-pilot"));
const keepWritten = process.argv.includes("--keep-written");

fs.mkdirSync(outputDir, { recursive: true });

async function request(url, options = {}) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutSec * 1000);
  try {
    const response = await fetch(url, { ...options, signal: controller.signal });
    const text = await response.text();
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${text.slice(0, 1000)}`);
    }
    const contentType = response.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
      const json = text ? JSON.parse(text) : null;
      if (json && json.success === false) {
        throw new Error(`${json.code || "ERROR"}: ${json.message || ""}`);
      }
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
  const file = path.join(outputDir, name);
  fs.writeFileSync(file, JSON.stringify(value, null, 2), "utf8");
  return file;
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

const inputRows = parseTsv(inputPath).slice(0, 5);
if (!inputRows.length) {
  throw new Error(`No pilot rows found in ${inputPath}`);
}

const manifest = {
  generatedAt: new Date().toISOString(),
  baseUrl,
  inputPath,
  outputDir,
  keepWritten,
  mode: keepWritten ? "write-and-keep" : "write-then-rollback",
  rows: []
};

const results = [];
const caseNos = [];

for (const row of inputRows) {
  const safeCode = row.personCode.replace(/[^A-Za-z0-9_-]/g, "_");
  const backup = {};
  try {
    backup.history = (await request(`${baseUrl}/api/salary/history/${encodeURIComponent(row.personCode)}`, { headers: headers() })).data;
  } catch (error) {
    backup.historyError = error.message;
  }
  writeJson(`backup-${safeCode}.json`, backup);

  const createBody = {
    workItemId: row.workItemId,
    source: "SALARY_EVENT",
    businessType: row.businessType,
    personCode: row.personCode,
    personName: row.personName,
    orgCode: row.orgCode,
    year: Number(row.year),
    month: Number(row.month),
    title: row.businessType,
    summary: `real pilot preview/write rehearsal ${row.year}-${String(row.month).padStart(2, "0")}`,
    differenceReason: "real pilot matched precheck",
    forceReason: "real pilot controlled rehearsal"
  };
  try {
    const completed = await request(`${baseUrl}/api/workbench/salary-cases`, {
      method: "POST",
      headers: { "content-type": "application/json; charset=utf-8", ...headers() },
      body: JSON.stringify(createBody)
    });
    const caseNo = completed.data?.data?.id;
    caseNos.push(caseNo);
    const confirm = await request(`${baseUrl}/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/history-write-confirm`, {
      method: "POST",
      headers: headers()
    });
    writeJson(`confirm-${caseNo}.json`, confirm.data);
    results.push({
      ...row,
      caseNo,
      previewStatus: confirm.data?.data?.status,
      executable: confirm.data?.data?.executable,
      writable: confirm.data?.data?.writable,
      issues: (confirm.data?.data?.issues || []).join(" | "),
      writeStatus: "PENDING",
      historyId: "",
      rollbackStatus: "",
      note: "case completed and plan confirmed"
    });
  } catch (error) {
    results.push({
      ...row,
      caseNo: "",
      previewStatus: "ERROR",
      executable: "false",
      writable: "false",
      issues: error.message,
      writeStatus: "SKIPPED",
      historyId: "",
      rollbackStatus: "",
      note: "completion or preview failed"
    });
  }
}

const executableCaseNos = results
  .filter((row) => String(row.executable) === "true" || row.executable === true)
  .map((row) => row.caseNo)
  .filter(Boolean);

if (executableCaseNos.length) {
  const executeResult = await request(`${baseUrl}/api/workbench/history-write-plans/selected-execute`, {
    method: "POST",
    headers: { "content-type": "application/json; charset=utf-8", ...headers() },
    body: JSON.stringify({ caseNos: executableCaseNos })
  });
  writeJson("selected-execute.json", executeResult.data);
  for (const item of executeResult.data?.data?.items || []) {
    const row = results.find((candidate) => candidate.caseNo === item.caseNo);
    if (row) {
      row.writeStatus = item.status;
      row.historyId = item.historyId || "";
      row.note = item.message || row.note;
    }
  }
  for (const caseNo of executableCaseNos) {
    try {
      const comparison = await request(`${baseUrl}/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/history-write-comparison`, {
        headers: headers()
      });
      writeJson(`comparison-${caseNo}.json`, comparison.data);
      const audits = await request(`${baseUrl}/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/history-write-audits.csv`, {
        headers: headers()
      });
      fs.writeFileSync(path.join(outputDir, `audits-${caseNo}.csv`), audits.data, "utf8");
    } catch (error) {
      const row = results.find((candidate) => candidate.caseNo === caseNo);
      if (row) row.note = `${row.note}; post-write export failed: ${error.message}`;
    }
  }
  if (!keepWritten) {
    for (const caseNo of executableCaseNos) {
      try {
        const preview = await request(`${baseUrl}/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/history-write-rollback-preview`, {
          method: "POST",
          headers: headers()
        });
        writeJson(`rollback-preview-${caseNo}.json`, preview.data);
      } catch (error) {
        const row = results.find((candidate) => candidate.caseNo === caseNo);
        if (row) row.rollbackStatus = `PREVIEW_ERROR: ${error.message}`;
      }
    }
    const rollbackResult = await request(`${baseUrl}/api/workbench/history-write-plans/selected-rollback`, {
      method: "POST",
      headers: { "content-type": "application/json; charset=utf-8", ...headers() },
      body: JSON.stringify({ caseNos: executableCaseNos })
    });
    writeJson("selected-rollback.json", rollbackResult.data);
    for (const item of rollbackResult.data?.data?.items || []) {
      const row = results.find((candidate) => candidate.caseNo === item.caseNo);
      if (row) {
        row.rollbackStatus = item.status;
        row.note = `${row.note}; rollback=${item.message || item.status}`;
      }
    }
  }
} else {
  writeJson("selected-execute.json", { data: { total: 0, items: [] }, skipped: "No executable plans." });
}

manifest.rows = results;
writeJson("manifest.json", manifest);

const resultHeaders = [
  "workItemId", "caseNo", "personCode", "personName", "orgCode", "year", "month",
  "businessType", "trialStatus", "previewStatus", "executable", "writable",
  "writeStatus", "historyId", "rollbackStatus", "issues", "note"
];
const tsv = [
  resultHeaders.join("\t"),
  ...results.map((row) => resultHeaders.map((header) => tsvCell(row[header])).join("\t"))
].join("\n") + "\n";
const resultPath = path.join(outputDir, "pilot-results.tsv");
fs.writeFileSync(resultPath, tsv, "utf8");

const report = [
  "# Real History Write Pilot",
  `GeneratedAt: ${new Date().toLocaleString("zh-CN", { hour12: false })}`,
  `BaseUrl: ${baseUrl}`,
  `Input: ${inputPath}`,
  `OutputDir: ${outputDir}`,
  `Mode: ${keepWritten ? "write-and-keep" : "write-then-rollback"}`,
  `Selected: ${results.length}`,
  `Executable: ${executableCaseNos.length}`,
  `Executed: ${results.filter((row) => row.writeStatus === "EXECUTED").length}`,
  `RolledBack: ${results.filter((row) => row.rollbackStatus === "ROLLED_BACK").length}`,
  "",
  "Rows:",
  ...results.map((row) => `${row.personCode}\t${row.personName}\t${row.businessType}\t${row.previewStatus}\texecutable=${row.executable}\twrite=${row.writeStatus}\trollback=${row.rollbackStatus}\t${row.note}`)
].join("\n") + "\n";
const reportPath = path.join(outputDir, "pilot-report.txt");
fs.writeFileSync(reportPath, report, "utf8");

console.log("Real history write pilot completed.");
console.log(`Report: ${reportPath}`);
console.log(`Results: ${resultPath}`);
