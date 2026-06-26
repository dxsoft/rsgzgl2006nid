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
const outputDir = argValue("--output-dir", path.join(targetDir, "production-permissions"));
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
    return { response, data: json?.data };
  } finally {
    clearTimeout(timeout);
  }
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

const [templates, roles, users, menus] = await Promise.all([
  request(`${baseUrl}/api/system/role-templates`, { headers }),
  request(`${baseUrl}/api/system/roles`, { headers }),
  request(`${baseUrl}/api/system/users`, { headers }),
  request(`${baseUrl}/api/system/menu-admin`, { headers })
]);

const snapshot = {
  generatedAt: new Date().toISOString(),
  baseUrl,
  requiredHistoryPermissions: ["SALARY_DONE", "SALARY_HISTORY_WRITE", "SALARY_HISTORY_ROLLBACK", "SALARY_EXPORT"],
  roleTemplates: templates.data,
  roles: roles.data,
  users: users.data,
  menus: menus.data
};
fs.writeFileSync(path.join(outputDir, "permission-snapshot.json"), JSON.stringify(snapshot, null, 2), "utf8");

const writerTemplate = (templates.data || []).find((item) => item.code === "SALARY_WRITER");
const usersWithWrite = (users.data || []).filter((user) =>
  (user.roleCodes || []).some((roleCode) => {
    const role = (roles.data || []).find((candidate) => candidate.code === roleCode);
    return (role?.menuCodes || []).includes("SALARY_HISTORY_WRITE");
  })
);
const usersWithRollback = (users.data || []).filter((user) =>
  (user.roleCodes || []).some((roleCode) => {
    const role = (roles.data || []).find((candidate) => candidate.code === roleCode);
    return (role?.menuCodes || []).includes("SALARY_HISTORY_ROLLBACK");
  })
);

const report = [
  "# Production Permission Snapshot",
  `GeneratedAt: ${new Date().toLocaleString("zh-CN", { hour12: false })}`,
  `BaseUrl: ${baseUrl}`,
  "",
  "Required Permissions:",
  "- SALARY_DONE: view done salary cases and write plans",
  "- SALARY_HISTORY_WRITE: execute history writes",
  "- SALARY_HISTORY_ROLLBACK: rollback history writes",
  "- SALARY_EXPORT: export plans/audits/reports",
  "",
  `SALARY_WRITER template contains write permission: ${Boolean(writerTemplate?.menuCodes?.includes("SALARY_HISTORY_WRITE"))}`,
  `SALARY_WRITER template contains rollback permission: ${Boolean(writerTemplate?.menuCodes?.includes("SALARY_HISTORY_ROLLBACK"))}`,
  `Users with SALARY_HISTORY_WRITE: ${usersWithWrite.map((user) => user.username).join(", ") || "(none)"}`,
  `Users with SALARY_HISTORY_ROLLBACK: ${usersWithRollback.map((user) => user.username).join(", ") || "(none)"}`,
  "",
  "Action Required Before Production:",
  "- Keep SALARY_HISTORY_WRITE limited to designated salary write operators.",
  "- Keep SALARY_HISTORY_ROLLBACK limited to designated operators or administrators.",
  "- Verify each user orgCodes before real write batches.",
  "- Export this snapshot with each launch readiness report."
].join("\n") + "\n";
fs.writeFileSync(path.join(outputDir, "permission-snapshot.txt"), report, "utf8");

console.log("Production permission snapshot exported.");
console.log(`Report: ${path.join(outputDir, "permission-snapshot.txt")}`);
