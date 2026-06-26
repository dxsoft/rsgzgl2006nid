param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [string]$OutputPath = "target\target-state-samples.tsv",
    [string]$CandidatePath = "target\target-state-candidates.tsv",
    [int]$PerType = 30,
    [int]$CandidatePerType = 200,
    [string]$JdbcUrl = "jdbc:mysql://127.0.0.1:3306/gzjsgl?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true",
    [string]$DbUser = "root",
    [string]$DbPassword = $env:DB_PASSWORD,
    [string]$JavaHome = "C:\Program Files\Java\jdk-21.0.10"
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Net.Http

function Ensure-Directory([string]$Path) {
    $dir = Split-Path -Parent $Path
    if ($dir -and -not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir | Out-Null
    }
}

function Decode-EscapedUnicode([string]$Value) {
    return [regex]::Replace($Value, "\\u([0-9a-fA-F]{4})", {
        param($Match)
        [char][Convert]::ToInt32($Match.Groups[1].Value, 16)
    })
}

Ensure-Directory $OutputPath
Ensure-Directory $CandidatePath

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
      WITH ranked AS (
        SELECT CONCAT(TRIM(t.dwbm), '-', TRIM(t.grbm)) person_code,
               TRIM(t.dwbm) org_code,
               CAST(t.jsnf AS UNSIGNED) year_value,
               CAST(t.jsyf AS UNSIGNED) month_value,
               LEFT(TRIM(t.zwbm2), 2) post_prefix,
               CAST(t.hj2 AS DECIMAL(12,2)) expected_total,
               CASE TRIM(t.jslb)
                 WHEN '\u964d\u8d44\u5904\u5206' THEN 'demotion'
                 WHEN '\u5956\u52b1\u664b\u5347' THEN 'reward'
                 WHEN '\u5176\u5b83\u60c5\u51b5' THEN 'other'
               END type_key,
               ROW_NUMBER() OVER (
                 PARTITION BY TRIM(t.jslb)
                 ORDER BY CAST(t.jsnf AS UNSIGNED) DESC, CAST(t.jsyf AS UNSIGNED) DESC, TRIM(t.dwbm), TRIM(t.grbm)
               ) rn
        FROM hisbase t
        JOIN hisbase b ON b.id = (
          SELECT x.id
          FROM hisbase x
          WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
            AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
          ORDER BY x.jsnf DESC, x.jsyf DESC, x.hj2 DESC, x.bbz DESC
          LIMIT 1
        )
        WHERE TRIM(t.jslb) IN ('\u964d\u8d44\u5904\u5206', '\u5956\u52b1\u664b\u5347', '\u5176\u5b83\u60c5\u51b5')
          AND LEFT(TRIM(t.zwbm2), 2) IN ('01','02','03','04','05','06','07','08','09','10','11','21','22','23','24','25','26','27','28')
          AND t.hj2 > 0
      )
      SELECT person_code, org_code, year_value, month_value, post_prefix, expected_total, type_key
      FROM ranked
      WHERE rn <= ?
      ORDER BY type_key, year_value DESC, month_value DESC, person_code
      """;
  try (PreparedStatement ps = c.prepareStatement(sql)) {
    ps.setInt(1, $CandidatePerType);
    try (ResultSet rs = ps.executeQuery()) {
      System.out.println("ROW\tpersonCode\torgCode\tyear\tmonth\tprefix\texpected\ttypeKey");
      while (rs.next()) {
        System.out.printf("ROW\t%s\t%s\t%d\t%d\t%s\t%s\t%s%n",
          rs.getString("person_code"),
          rs.getString("org_code"),
          rs.getInt("year_value"),
          rs.getInt("month_value"),
          rs.getString("post_prefix"),
          rs.getString("expected_total").replaceAll("\\.00$", ""),
          rs.getString("type_key"));
      }
    }
  }
}
/exit
"@

$rawRows = $javaSource | & $jshell -s --class-path $classpath | Where-Object { $_ -like "ROW`t*" }
if (-not $rawRows) {
    throw "No target-state candidate rows were exported from the database."
}
$candidateLines = New-Object System.Collections.Generic.List[string]
$candidateLines.Add("personCode`torgCode`tyear`tmonth`tprefix`texpected`ttypeKey")
$rawRows | ForEach-Object {
    $line = $_ -replace "^ROW`t", ""
    if ($line -ne "personCode`torgCode`tyear`tmonth`tprefix`texpected`ttypeKey") {
        $candidateLines.Add($line)
    }
}
[System.IO.File]::WriteAllLines((Resolve-Path (Split-Path -Parent $CandidatePath)).Path + "\" + (Split-Path -Leaf $CandidatePath), $candidateLines, [System.Text.UTF8Encoding]::new($false))

$changeTypes = @{
    demotion = "\u964d\u8d44\u5904\u5206"
    reward = "\u5956\u52b1\u664b\u5347"
    other = "\u5176\u5b83\u60c5\u51b5"
}
$client = [System.Net.Http.HttpClient]::new()
$client.Timeout = [TimeSpan]::FromSeconds(30)
$selectedByType = @{
    demotion = New-Object System.Collections.Generic.List[object]
    reward = New-Object System.Collections.Generic.List[object]
    other = New-Object System.Collections.Generic.List[object]
}

$candidates = Import-Csv $CandidatePath -Delimiter "`t"
foreach ($candidate in $candidates) {
    $bucket = $selectedByType[$candidate.typeKey]
    if ($bucket.Count -ge $PerType) {
        continue
    }
    $changeType = Decode-EscapedUnicode $changeTypes[$candidate.typeKey]
    $body = @{
        personCode = $candidate.personCode
        orgCode = $candidate.orgCode
        year = [int]$candidate.year
        month = [int]$candidate.month
        changeType = $changeType
    } | ConvertTo-Json -Compress
    try {
        $content = [System.Net.Http.StringContent]::new($body, [System.Text.Encoding]::UTF8, "application/json")
        $response = $client.PostAsync("$BaseUrl/api/salary/rule-trial/normal-grade", $content).Result
        if (-not $response.IsSuccessStatusCode) {
            continue
        }
        $text = [System.Text.Encoding]::UTF8.GetString($response.Content.ReadAsByteArrayAsync().Result)
        $json = $text | ConvertFrom-Json
        if ($json.data.matchedExpected) {
            $bucket.Add($candidate)
        }
    } catch {
        continue
    }
}

$lines = New-Object System.Collections.Generic.List[string]
foreach ($typeKey in @("demotion", "reward", "other")) {
    foreach ($sample in $selectedByType[$typeKey]) {
        $lines.Add("$($sample.personCode)`t$($sample.orgCode)`t$($sample.year)`t$($sample.month)`t$($sample.prefix)`t$($sample.expected)`t$($changeTypes[$typeKey])")
    }
}
[System.IO.File]::WriteAllLines((Resolve-Path (Split-Path -Parent $OutputPath)).Path + "\" + (Split-Path -Leaf $OutputPath), $lines, [System.Text.UTF8Encoding]::new($false))

foreach ($typeKey in @("demotion", "reward", "other")) {
    Write-Host ("{0}: {1}" -f $typeKey, $selectedByType[$typeKey].Count)
}
Write-Host "Candidate file: $CandidatePath"
Write-Host "Sample file: $OutputPath"
