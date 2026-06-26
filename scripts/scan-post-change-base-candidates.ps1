param(
    [string]$Start = "2025-01",
    [string]$End = "2026-06",
    [int]$Take = 20,
    [int]$MaxPostRows = 3000,
    [string]$OutputDir = "backend\target\post-change-base-candidates",
    [string]$JdbcUrl = "jdbc:mysql://127.0.0.1:3306/gzjsgl?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true",
    [string]$DbUser = "root",
    [string]$DbPassword = $env:DB_PASSWORD,
    [string]$JavaHome = "C:\Program Files\Java\jdk-21.0.10"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

$classpathFile = Join-Path (Get-Location) "backend\target\classpath.txt"
if (-not (Test-Path $classpathFile)) {
    $classpathFile = Join-Path (Get-Location) "target\classpath.txt"
}
if (-not (Test-Path $classpathFile)) {
    throw "Cannot find backend\target\classpath.txt or target\classpath.txt."
}
$classpath = Get-Content $classpathFile -Raw
$jshell = Join-Path $JavaHome "bin\jshell.exe"
if (-not (Test-Path $jshell)) {
    $jshell = "jshell.exe"
}

$startYm = [int]($Start.Replace("-", ""))
$endYm = [int]($End.Replace("-", ""))
$postChangeHex = "E8818CE58AA1E58F98E58C96"

$javaSource = @"
import java.sql.*;
import java.util.*;
String url = "$JdbcUrl";
String user = "$DbUser";
String password = "$DbPassword";
int startYm = $startYm;
int endYm = $endYm;
int take = $Take;
int maxPostRows = $MaxPostRows;
String postChangeHex = "$postChangeHex";
Set<String> prefixes = Set.of("07", "08", "09", "10", "11");
try (Connection c = DriverManager.getConnection(url, user, password)) {
  String sql = """
      WITH target_posts AS (
        SELECT TRIM(z.dwbm) org_code,
               TRIM(z.grbm) person_no,
               TRIM(COALESCE(p.xm, '')) person_name,
               TRIM(COALESCE(o.dwmc, '')) org_name,
               TRIM(z.zwbm) target_post,
               TRIM(z.srny) start_month,
               CAST(z.id AS CHAR) source_id
        FROM dryzwbh z
        LEFT JOIN dryjbxx p ON p.dwbm = z.dwbm AND p.grbm = z.grbm
        LEFT JOIN dwbm o ON o.dwbm = z.dwbm
        WHERE TRIM(COALESCE(z.srny, '')) <> ''
          AND TRIM(COALESCE(z.zwbm, '')) <> ''
          AND LEFT(TRIM(z.zwbm), 2) IN ('07','08','09','10','11')
          AND CAST(REPLACE(TRIM(z.srny), '.', '') AS UNSIGNED) BETWEEN 200607 AND 209912
        ORDER BY CAST(REPLACE(TRIM(z.srny), '.', '') AS UNSIGNED) DESC, TRIM(z.dwbm), TRIM(z.grbm), z.id DESC
        LIMIT ?
      ),
      targeted AS (
        SELECT target_posts.*,
               YEAR(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(start_month, '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) target_year,
               MONTH(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(start_month, '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) target_month
        FROM target_posts
      ),
      ranked AS (
        SELECT targeted.*,
               TRIM(h.id) baseline_id,
               TRIM(COALESCE(h.zwbm2, '')) baseline_post,
               TRIM(COALESCE(h.tbnd, '')) baseline_standard_year,
               TRIM(COALESCE(h.jslb, '')) baseline_type,
               h.hj2 baseline_total,
               ROW_NUMBER() OVER (
                 PARTITION BY targeted.source_id
                 ORDER BY CAST(TRIM(h.jsnf) AS UNSIGNED) DESC,
                          CAST(TRIM(h.jsyf) AS UNSIGNED) DESC,
                          h.id DESC
               ) rn
        FROM targeted
        JOIN hisbase h ON TRIM(h.dwbm) = targeted.org_code
                      AND TRIM(h.grbm) = targeted.person_no
                      AND (
                        CAST(TRIM(h.jsnf) AS UNSIGNED) < targeted.target_year
                        OR (
                          CAST(TRIM(h.jsnf) AS UNSIGNED) = targeted.target_year
                          AND CAST(TRIM(h.jsyf) AS UNSIGNED) < targeted.target_month
                        )
                      )
        WHERE targeted.target_year * 100 + targeted.target_month BETWEEN ? AND ?
          AND NOT EXISTS (
            SELECT 1
            FROM hisbase same_h
            WHERE TRIM(same_h.dwbm) = targeted.org_code
              AND TRIM(same_h.grbm) = targeted.person_no
              AND CAST(TRIM(same_h.jsnf) AS UNSIGNED) = targeted.target_year
              AND CAST(TRIM(same_h.jsyf) AS UNSIGNED) = targeted.target_month
              AND HEX(CONVERT(TRIM(same_h.jslb) USING utf8mb4)) = ?
          )
          AND NOT EXISTS (
            SELECT 1
            FROM hisbase later_h
            WHERE TRIM(later_h.dwbm) = targeted.org_code
              AND TRIM(later_h.grbm) = targeted.person_no
              AND (
                CAST(TRIM(later_h.jsnf) AS UNSIGNED) > targeted.target_year
                OR (
                  CAST(TRIM(later_h.jsnf) AS UNSIGNED) = targeted.target_year
                  AND CAST(TRIM(later_h.jsyf) AS UNSIGNED) > targeted.target_month
                )
              )
          )
          AND NOT EXISTS (
            SELECT 1
            FROM salary_business_case sc
            WHERE sc.work_item_id = CONCAT(
                'post-change-', targeted.org_code, '-',
                targeted.target_year, '-',
                LPAD(targeted.target_month, 2, '0'), '-',
                targeted.org_code, '-', targeted.person_no
              )
              AND COALESCE(sc.status, '') <> 'CANCELLED'
          )
      )
      SELECT *
      FROM ranked
      WHERE rn = 1
        AND LEFT(baseline_post, 2) IN ('07','08','09','10','11')
        AND LEFT(target_post, 2) IN ('07','08','09','10','11')
        AND baseline_post <> target_post
        AND (
          (
            target_post LIKE '%F%'
            AND EXISTS (
              SELECT 1
              FROM bz06_zzdz std
              WHERE LEFT(TRIM(std.zzzwbm), 2) = CASE
                  WHEN target_post > '10' THEN '10'
                  ELSE LEFT(target_post, 2)
                END
                AND TRIM(std.tbnd) = COALESCE(
                  NULLIF(baseline_standard_year, ''),
                  (
                    SELECT MAX(TRIM(std_year.tbnd))
                    FROM bz06_zzdz std_year
                    WHERE CAST(TRIM(std_year.tbnd) AS UNSIGNED) <= target_year
                  )
                )
            )
          )
          OR (
            target_post NOT LIKE '%F%'
            AND EXISTS (
              SELECT 1
              FROM bz06_zwgz std
              WHERE TRIM(std.zwbm) = target_post
                AND TRIM(std.tbnd) = COALESCE(
                  NULLIF(baseline_standard_year, ''),
                  (
                    SELECT MAX(TRIM(std_year.tbnd))
                    FROM bz06_zwgz std_year
                    WHERE CAST(TRIM(std_year.tbnd) AS UNSIGNED) <= target_year
                  )
                )
            )
          )
        )
      ORDER BY target_year DESC, target_month DESC, org_code, person_no
      LIMIT ?
      """;
  PreparedStatement candidates = c.prepareStatement(sql);
  candidates.setInt(1, maxPostRows);
  candidates.setInt(2, startYm);
  candidates.setInt(3, endYm);
  candidates.setString(4, postChangeHex);
  candidates.setInt(5, take);

  System.out.println("ROW\tsource\torgCode\torgName\tyear\tmonth\tpersonCode\tpersonName\tchangeType\truleType\tstatus\tbeforeValue\tafterValue\tchangeAmount\tmessage");
  int found = 0;
  try (ResultSet rs = candidates.executeQuery()) {
    while (rs.next()) {
      String org = rs.getString("org_code");
      String personNo = rs.getString("person_no");
      String targetPost = rs.getString("target_post");
      String startMonth = rs.getString("start_month");
      int year = rs.getInt("target_year");
      int month = rs.getInt("target_month");
      String baselineId = rs.getString("baseline_id");
      String baselinePost = rs.getString("baseline_post");
      String baselineTotal = String.valueOf(rs.getObject("baseline_total") == null ? "" : rs.getObject("baseline_total"));
      String personCode = org + "-" + personNo;
      String message = "dryzwbh srny=" + startMonth
          + ", baseline=" + baselineId
          + ", baselineTotal=" + baselineTotal;
      System.out.printf(
          "ROW\tpost-change\t%s\t\t%d\t%d\t%s\t\tPOST_CHANGE_CN\tINSTITUTION_POST_CHANGE\tBASE_CANDIDATE\t%s\t%s\t\t%s%n",
          org,
          year,
          month,
          personCode,
          baselinePost,
          targetPost,
          message
      );
      found++;
    }
  }
  System.out.println("DIAG\tfound=" + found);
  String summarySql = """
      WITH target_posts AS (
        SELECT TRIM(z.dwbm) org_code,
               TRIM(z.grbm) person_no,
               TRIM(z.zwbm) target_post,
               TRIM(z.srny) start_month
        FROM dryzwbh z
        WHERE TRIM(COALESCE(z.srny, '')) <> ''
          AND TRIM(COALESCE(z.zwbm, '')) <> ''
          AND LEFT(TRIM(z.zwbm), 2) IN ('07','08','09','10','11')
          AND CAST(REPLACE(TRIM(z.srny), '.', '') AS UNSIGNED) BETWEEN 200607 AND 209912
        ORDER BY CAST(REPLACE(TRIM(z.srny), '.', '') AS UNSIGNED) DESC, TRIM(z.dwbm), TRIM(z.grbm), z.id DESC
        LIMIT ?
      ),
      targeted AS (
        SELECT target_posts.*,
               YEAR(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(start_month, '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) target_year,
               MONTH(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(start_month, '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) target_month
        FROM target_posts
      )
      SELECT COUNT(*) total_rows,
             SUM(CASE WHEN target_post LIKE '%F%' THEN 1 ELSE 0 END) probationary_rows,
             SUM(CASE WHEN EXISTS (
               SELECT 1 FROM salary_business_case sc
               WHERE sc.work_item_id = CONCAT(
                   'post-change-', targeted.org_code, '-',
                   targeted.target_year, '-',
                   LPAD(targeted.target_month, 2, '0'), '-',
                   targeted.org_code, '-', targeted.person_no
                 )
                 AND COALESCE(sc.status, '') <> 'CANCELLED'
             ) THEN 1 ELSE 0 END) open_case_rows
      FROM targeted
      WHERE target_year * 100 + target_month BETWEEN ? AND ?
      """;
  PreparedStatement summary = c.prepareStatement(summarySql);
  summary.setInt(1, maxPostRows);
  summary.setInt(2, startYm);
  summary.setInt(3, endYm);
  try (ResultSet rs = summary.executeQuery()) {
    if (rs.next()) {
      System.out.printf(
          "SUMMARY\ttotalRows=%d\tprobationaryRows=%d\topenCaseRows=%d%n",
          rs.getLong("total_rows"),
          rs.getLong("probationary_rows"),
          rs.getLong("open_case_rows")
      );
    }
  }
}
/exit
"@

$tempScript = Join-Path $OutputDir "scan-post-change-base-candidates.jsh"
[System.IO.File]::WriteAllText((Join-Path (Resolve-Path $OutputDir).Path (Split-Path -Leaf $tempScript)), $javaSource, [System.Text.UTF8Encoding]::new($false))
$jshellExitCode = 0
$previousErrorActionPreference = $ErrorActionPreference
$ErrorActionPreference = "Continue"
try {
    $raw = & $jshell -s --class-path $classpath $tempScript 2>&1
    $jshellExitCode = $LASTEXITCODE
}
finally {
    $ErrorActionPreference = $previousErrorActionPreference
    Remove-Item -Path $tempScript -ErrorAction SilentlyContinue
}
if ($jshellExitCode -ne 0) {
    throw "jshell failed with exit code $jshellExitCode.`n$($raw -join "`n")"
}
$rows = $raw | Where-Object { $_ -like "ROW`t*" }
$summaryRows = $raw | Where-Object { $_ -like "SUMMARY`t*" }
$diagnostics = $raw | Where-Object { $_ -notlike "ROW`t*" -and $_ -notlike "SUMMARY`t*" -and -not [string]::IsNullOrWhiteSpace($_) }
$postChangeName = -join ([char[]](0x804c, 0x52a1, 0x53d8, 0x5316))

$headers = "source`torgCode`torgName`tyear`tmonth`tpersonCode`tpersonName`tchangeType`truleType`tstatus`tbeforeValue`tafterValue`tchangeAmount`tmessage"
$candidateLines = New-Object System.Collections.Generic.List[string]
$candidateLines.Add($headers)
foreach ($row in $rows) {
    $line = $row -replace "^ROW`t", ""
    $line = $line -replace "POST_CHANGE_CN", $postChangeName
    if ($line -ne $headers) {
        $candidateLines.Add($line)
    }
}

$candidatePath = Join-Path $OutputDir "post-change-base-candidates.tsv"
[System.IO.File]::WriteAllLines((Resolve-Path $OutputDir).Path + "\" + (Split-Path -Leaf $candidatePath), $candidateLines, [System.Text.UTF8Encoding]::new($false))

$summary = @{}
foreach ($summaryRow in $summaryRows) {
    foreach ($cell in (($summaryRow -replace "^SUMMARY`t", "") -split "`t")) {
        $parts = $cell -split "=", 2
        if ($parts.Count -eq 2) {
            $summary[$parts[0]] = $parts[1]
        }
    }
}

$reportPath = Join-Path $OutputDir "post-change-base-candidates.md"
$candidateCount = [Math]::Max(0, $candidateLines.Count - 1)
$report = @(
    "# Post Change Base Candidates",
    "GeneratedAt: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')",
    "Period: $Start..$End",
    "Take: $Take",
    "MaxPostRows: $MaxPostRows",
    "CandidateCount: $candidateCount",
    "TotalRows: $($summary.totalRows)",
    "ProbationaryRows: $($summary.probationaryRows)",
    "OpenCaseRows: $($summary.openCaseRows)",
    "",
    "Rules:",
    "- Source table: dryzwbh.",
    "- Target business type: $postChangeName.",
    "- Institution post prefixes only: 07/08/09/10/11.",
    "- Exclude when target-month hisbase already has 职务变化.",
    "- Exclude when latest baseline hisbase zwbm2 already equals target dryzwbh.zwbm.",
    "- Exclude when later hisbase rows exist after target month.",
    "- Exclude when a non-cancelled salary business case already exists for the generated work item id.",
    "- Validate target standard: post codes containing F use bz06_zzdz; other post codes use bz06_zwgz.",
    "",
    "Diagnostics:",
    ($diagnostics -join "`n")
) -join "`n"
Set-Content -Path $reportPath -Value ($report + "`n") -Encoding UTF8

Write-Host "Post-change base candidate scan completed."
Write-Host "Candidates: $candidatePath"
Write-Host "Report: $reportPath"
Get-Content $reportPath
