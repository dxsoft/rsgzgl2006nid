param(
    [string]$OutputPath = "target\probationary-data-issues.tsv",
    [string]$JdbcUrl = "jdbc:mysql://127.0.0.1:3306/gzjsgl?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true",
    [string]$DbUser = "root",
    [string]$DbPassword = $env:DB_PASSWORD,
    [string]$JavaHome = "C:\Program Files\Java\jdk-21.0.10"
)

$ErrorActionPreference = "Stop"

function Ensure-Directory([string]$Path) {
    $dir = Split-Path -Parent $Path
    if ($dir -and -not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir | Out-Null
    }
}

Ensure-Directory $OutputPath

$classpathFile = Join-Path (Get-Location) "target\classpath.txt"
if (-not (Test-Path $classpathFile)) {
    $classpathFile = Join-Path (Get-Location) "backend\target\classpath.txt"
}
if (-not (Test-Path $classpathFile)) {
    throw "Cannot find target\classpath.txt. Run from the backend directory or build the dependency classpath first."
}
$classpath = Get-Content $classpathFile -Raw
$jshell = Join-Path $JavaHome "bin\jshell.exe"
if (-not (Test-Path $jshell)) {
    $jshell = "jshell.exe"
}

$javaSource = @"
import java.sql.*;
String url = "$JdbcUrl";
try (Connection c = DriverManager.getConnection(url, "$DbUser", "$DbPassword")) {
  String sql = """
      SELECT CONCAT(TRIM(t.dwbm), '-', TRIM(t.grbm)) person_code,
             TRIM(t.dwbm) org_code,
             CAST(t.jsnf AS UNSIGNED) year_value,
             CAST(t.jsyf AS UNSIGNED) month_value,
             TRIM(t.zwbm2) post_code,
             CAST(t.hj2 AS DECIMAL(12,2)) total_amount,
             CASE
               WHEN TRIM(t.zwbm2) = '' THEN 'blank-post'
               WHEN LOCATE('F', TRIM(t.zwbm2)) = 0 THEN 'formal-post'
               ELSE 'f-post'
             END issue_category,
             CASE
               WHEN EXISTS (
                 SELECT 1 FROM hisbase x
                 WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                   AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                       < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
               ) THEN 1 ELSE 0
             END has_baseline
      FROM hisbase t
      WHERE HEX(CONVERT(TRIM(t.jslb) USING utf8mb4)) = 'E8A781E4B9A0E5B7A5E8B584'
        AND (t.hj2 IS NULL OR t.hj2 = 0)
      ORDER BY year_value, month_value, org_code, person_code
      """;
  try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
    System.out.println("ROW\tpersonCode\torgCode\tyear\tmonth\tpostCode\ttotalAmount\tissueCategory\thasBaseline\tnote");
    while (rs.next()) {
      String category = rs.getString("issue_category");
      int hasBaseline = rs.getInt("has_baseline");
      String note = hasBaseline == 0 && !"f-post".equals(category)
          ? "zero probationary placeholder without F post and baseline"
          : "zero probationary placeholder";
      System.out.printf("ROW\t%s\t%s\t%d\t%d\t%s\t%s\t%s\t%d\t%s%n",
        rs.getString("person_code"),
        rs.getString("org_code"),
        rs.getInt("year_value"),
        rs.getInt("month_value"),
        rs.getString("post_code"),
        rs.getString("total_amount").replaceAll("\\.00$", ""),
        category,
        hasBaseline,
        note);
    }
  }
}
/exit
"@

$rawRows = $javaSource | & $jshell -s --class-path $classpath | Where-Object { $_ -like "ROW`t*" }
if (-not $rawRows) {
    throw "No probationary data issue rows were exported from the database."
}
$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("personCode`torgCode`tyear`tmonth`tpostCode`ttotalAmount`tissueCategory`thasBaseline`tnote")
$rawRows | ForEach-Object {
    $line = $_ -replace "^ROW`t", ""
    if ($line -ne "personCode`torgCode`tyear`tmonth`tpostCode`ttotalAmount`tissueCategory`thasBaseline`tnote") {
        $lines.Add($line)
    }
}
[System.IO.File]::WriteAllLines((Resolve-Path (Split-Path -Parent $OutputPath)).Path + "\" + (Split-Path -Leaf $OutputPath), $lines, [System.Text.UTF8Encoding]::new($false))

Import-Csv $OutputPath -Delimiter "`t" |
    Group-Object issueCategory, hasBaseline |
    Sort-Object Count -Descending |
    Select-Object Count, Name |
    Format-Table -AutoSize

Write-Host "Issue file: $OutputPath"
