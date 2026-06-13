param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [string]$OutputPath = "target\special-flow-samples.tsv",
    [string]$CandidatePath = "target\special-flow-candidates.tsv",
    [int]$PerType = 30,
    [int]$CandidatePerType = 180,
    [string]$JdbcUrl = "jdbc:mysql://127.0.0.1:3306/gzjsgl?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true",
    [string]$DbUser = "root",
    [string]$DbPassword = "dx262105",
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

function Hex-To-EscapedUnicode([string]$Hex) {
    if ([string]::IsNullOrWhiteSpace($Hex)) {
        return ""
    }
    $bytes = for ($i = 0; $i -lt $Hex.Length; $i += 2) {
        [Convert]::ToByte($Hex.Substring($i, 2), 16)
    }
    $text = [System.Text.Encoding]::UTF8.GetString($bytes)
    $builder = [System.Text.StringBuilder]::new()
    foreach ($ch in $text.ToCharArray()) {
        $code = [int][char]$ch
        if ($code -lt 128) {
            [void]$builder.Append($ch)
        } else {
            [void]$builder.Append(("\u{0:x4}" -f $code))
        }
    }
    return $builder.ToString()
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
      WITH typed AS (
        SELECT CONCAT(TRIM(t.dwbm), '-', TRIM(t.grbm)) person_code,
               TRIM(t.dwbm) org_code,
               CAST(t.jsnf AS UNSIGNED) year_value,
               CAST(t.jsyf AS UNSIGNED) month_value,
               LEFT(TRIM(t.zwbm2), 2) post_prefix,
               CAST(t.hj2 AS DECIMAL(12,2)) expected_total,
               TRIM(t.jslb) change_type,
               CASE
                 WHEN TRIM(t.jslb) = '\u5b66\u5386\u53d8\u5316' THEN 'education'
                 WHEN TRIM(t.jslb) = '\u6559\u62a4\u6d25\u8d34' THEN 'teacher-nurse'
                 WHEN TRIM(t.jslb) = '2006\u5957\u6539' THEN 'legacy-2006'
                 WHEN TRIM(t.jslb) IN ('\u6b63\u5e38\u7ea7\u522b', '\u7ea7\u522b\u6eda\u52a8', '\u6b63\u5e38\u6863\u6b21', '\u6b63\u5e38\u664b\u6863') THEN 'normal-grade'
               END type_key
        FROM hisbase t
        WHERE TRIM(t.jslb) IN (
          '\u5b66\u5386\u53d8\u5316', '\u6559\u62a4\u6d25\u8d34', '2006\u5957\u6539',
          '\u6b63\u5e38\u7ea7\u522b', '\u7ea7\u522b\u6eda\u52a8', '\u6b63\u5e38\u6863\u6b21', '\u6b63\u5e38\u664b\u6863'
        )
          AND t.hj2 > 0
          AND LEFT(TRIM(t.zwbm2), 2) IN ('01','02','03','04','05','06','07','08','09','10','11','21','22','23','24','25','26','27','28')
          AND (
            TRIM(t.jslb) = '2006\u5957\u6539'
            OR EXISTS (
              SELECT 1
              FROM hisbase x
              WHERE x.dwbm = t.dwbm AND x.grbm = t.grbm
                AND CAST(CONCAT(x.jsnf, LPAD(x.jsyf, 2, '0')) AS UNSIGNED)
                    < CAST(CONCAT(t.jsnf, LPAD(t.jsyf, 2, '0')) AS UNSIGNED)
            )
          )
      ),
      ranked AS (
        SELECT typed.*,
               ROW_NUMBER() OVER (
                 PARTITION BY type_key, change_type
                 ORDER BY year_value DESC, month_value DESC, person_code
               ) rn
        FROM typed
        WHERE type_key IS NOT NULL
      )
      SELECT person_code, org_code, year_value, month_value, post_prefix, expected_total, type_key,
             HEX(CONVERT(change_type USING utf8mb4)) change_hex
      FROM ranked
      WHERE rn <= ?
      ORDER BY type_key, change_type, year_value DESC, month_value DESC, person_code
      """;
  try (PreparedStatement ps = c.prepareStatement(sql)) {
    ps.setInt(1, $CandidatePerType);
    try (ResultSet rs = ps.executeQuery()) {
      System.out.println("ROW\tpersonCode\torgCode\tyear\tmonth\tprefix\texpected\ttypeKey\tchangeHex");
      while (rs.next()) {
        System.out.printf("ROW\t%s\t%s\t%d\t%d\t%s\t%s\t%s\t%s%n",
          rs.getString("person_code"),
          rs.getString("org_code"),
          rs.getInt("year_value"),
          rs.getInt("month_value"),
          rs.getString("post_prefix"),
          rs.getString("expected_total").replaceAll("\\.00$", ""),
          rs.getString("type_key"),
          rs.getString("change_hex"));
      }
    }
  }
}
/exit
"@

$rawRows = $javaSource | & $jshell -s --class-path $classpath | Where-Object { $_ -like "ROW`t*" }
if (-not $rawRows) {
    throw "No special-flow candidate rows were exported from the database."
}
$candidateLines = New-Object System.Collections.Generic.List[string]
$candidateLines.Add("personCode`torgCode`tyear`tmonth`tprefix`texpected`ttypeKey`tchangeHex")
$rawRows | ForEach-Object {
    $line = $_ -replace "^ROW`t", ""
    if ($line -ne "personCode`torgCode`tyear`tmonth`tprefix`texpected`ttypeKey`tchangeHex") {
        $candidateLines.Add($line)
    }
}
[System.IO.File]::WriteAllLines((Resolve-Path (Split-Path -Parent $CandidatePath)).Path + "\" + (Split-Path -Leaf $CandidatePath), $candidateLines, [System.Text.UTF8Encoding]::new($false))

$client = [System.Net.Http.HttpClient]::new()
$client.Timeout = [TimeSpan]::FromSeconds(30)
$selectedByType = @{
    "education" = New-Object System.Collections.Generic.List[object]
    "teacher-nurse" = New-Object System.Collections.Generic.List[object]
    "legacy-2006" = New-Object System.Collections.Generic.List[object]
    "normal-grade" = New-Object System.Collections.Generic.List[object]
}

$candidates = Import-Csv $CandidatePath -Delimiter "`t"
foreach ($candidate in $candidates) {
    $bucket = $selectedByType[$candidate.typeKey]
    if ($bucket.Count -ge $PerType) {
        continue
    }
    $changeTypeEsc = Hex-To-EscapedUnicode $candidate.changeHex
    $changeType = Decode-EscapedUnicode $changeTypeEsc
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
            $candidate | Add-Member -NotePropertyName changeTypeEsc -NotePropertyValue $changeTypeEsc -Force
            $bucket.Add($candidate)
        }
    } catch {
        continue
    }
}

$lines = New-Object System.Collections.Generic.List[string]
foreach ($typeKey in @("education", "teacher-nurse", "legacy-2006", "normal-grade")) {
    foreach ($sample in $selectedByType[$typeKey]) {
        $lines.Add("$($sample.personCode)`t$($sample.orgCode)`t$($sample.year)`t$($sample.month)`t$($sample.prefix)`t$($sample.expected)`t$($sample.changeTypeEsc)")
    }
}
[System.IO.File]::WriteAllLines((Resolve-Path (Split-Path -Parent $OutputPath)).Path + "\" + (Split-Path -Leaf $OutputPath), $lines, [System.Text.UTF8Encoding]::new($false))

foreach ($typeKey in @("education", "teacher-nurse", "legacy-2006", "normal-grade")) {
    Write-Host ("{0}: {1}" -f $typeKey, $selectedByType[$typeKey].Count)
}
Write-Host "Candidate file: $CandidatePath"
Write-Host "Sample file: $OutputPath"
