param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [string]$OutputPath = "target\tg2006-tgb-classification.tsv",
    [string]$DiffOutputPath = "target\tg2006-tgb-diffs.tsv",
    [string]$SamplePath = "target\tg2006-tgb-samples.tsv",
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

function Escape-Tsv([object]$Value) {
    if ($null -eq $Value) {
        return ""
    }
    return ([string]$Value) -replace "`t", " " -replace "`r?`n", " "
}

function U([int[]]$Codes) {
    return [string]::Concat(($Codes | ForEach-Object { [char]$_ }))
}

function Classify-Note([string]$Note) {
    if (-not $Note) {
        return "no-note"
    }
    $matchedText = U @(0x4E0E, 0x76EE, 0x6807, 0x884C, 0x4E00, 0x81F4)
    $previousPostText = U @(0x524D, 0x4EFB, 0x804C, 0x52A1, 0x6BD4, 0x8F83, 0x751F, 0x6548)
    $educationPostText = U @(0x5B66, 0x5386, 0x804C, 0x52A1)
    $educationReconvertText = U @(0x91CD, 0x65B0, 0x5957, 0x6539, 0x751F, 0x6548)
    $educationFloorText = U @(0x5B66, 0x5386, 0x4FDD, 0x5E95)
    $effectiveText = U @(0x751F, 0x6548)
    if ($Note.Contains($matchedText)) {
        return "match"
    }
    $parts = New-Object System.Collections.Generic.List[string]
    if ($Note.Contains($previousPostText)) {
        $parts.Add("previous-post")
    }
    if ($Note.Contains($educationPostText) -and $Note.Contains($educationReconvertText)) {
        $parts.Add("education-post-reconvert")
    }
    if ($Note.Contains($educationFloorText) -and $Note.Contains($effectiveText)) {
        $parts.Add("education-floor")
    }
    if ($Note.Contains("tg06")) {
        $parts.Add("secondary-adjustment-tbd")
    }
    if ($parts.Count -eq 0) {
        return "other-tbd"
    }
    return [string]::Join("+", $parts)
}

Ensure-Directory $SamplePath
Ensure-Directory $OutputPath
Ensure-Directory $DiffOutputPath

$classpathFile = Join-Path (Get-Location) "target\classpath.txt"
if (-not (Test-Path $classpathFile)) {
    $classpathFile = Join-Path (Get-Location) "backend\target\classpath.txt"
}
if (-not (Test-Path $classpathFile)) {
    throw "Cannot find target\classpath.txt. Run mvn dependency:build-classpath first or run from the backend directory."
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
      SELECT CONCAT(TRIM(dwbm), '-', TRIM(grbm)) person_code,
             TRIM(dwbm) org_code,
             CAST(jsnf AS UNSIGNED) year_value,
             CAST(jsyf AS UNSIGNED) month_value,
             TRIM(zwbm2) post_code,
             TRIM(jbgzjb2) target_level,
             TRIM(zwgzdc2) target_step,
             hj2 expected_total
      FROM hisbase
      WHERE TRIM(jslb)=?
        AND LEFT(TRIM(zwbm2),2)='01'
        AND hj2 > 0
      ORDER BY TRIM(dwbm), TRIM(grbm), CAST(jsnf AS UNSIGNED), CAST(jsyf AS UNSIGNED)
      """;
  try (PreparedStatement ps = c.prepareStatement(sql)) {
    ps.setString(1, "2006\u5957\u6539");
    try (ResultSet rs = ps.executeQuery()) {
      System.out.println("ROW\tpersonCode\torgCode\tyear\tmonth\tpostCode\ttargetLevel\ttargetStep\texpectedTotal");
      while (rs.next()) {
        System.out.printf(
          "ROW\t%s\t%s\t%d\t%d\t%s\t%s\t%s\t%s%n",
          rs.getString("person_code"),
          rs.getString("org_code"),
          rs.getInt("year_value"),
          rs.getInt("month_value"),
          rs.getString("post_code"),
          rs.getString("target_level"),
          rs.getString("target_step"),
          rs.getString("expected_total")
        );
      }
    }
  }
}
/exit
"@

$rawRows = $javaSource | & $jshell -s --class-path $classpath | Where-Object { $_ -like "ROW`t*" }
if (-not $rawRows) {
    throw "No 2006 TGB candidate rows were exported from the database."
}
$sampleLines = New-Object System.Collections.Generic.List[string]
$sampleLines.Add("personCode`torgCode`tyear`tmonth`tpostCode`ttargetLevel`ttargetStep`texpectedTotal")
$rawRows | ForEach-Object {
    $line = $_ -replace "^ROW`t", ""
    if ($line -ne "personCode`torgCode`tyear`tmonth`tpostCode`ttargetLevel`ttargetStep`texpectedTotal") {
        $sampleLines.Add($line)
    }
}
[System.IO.File]::WriteAllLines((Resolve-Path (Split-Path -Parent $SamplePath)).Path + "\" + (Split-Path -Leaf $SamplePath), $sampleLines, [System.Text.UTF8Encoding]::new($false))

$samples = Import-Csv $SamplePath -Delimiter "`t"
$client = [System.Net.Http.HttpClient]::new()
$client.Timeout = [TimeSpan]::FromSeconds(30)
$changeType = [string]::Concat("2006", [char]0x5957, [char]0x6539)
$matchedText = U @(0x4E0E, 0x76EE, 0x6807, 0x884C, 0x4E00, 0x81F4)

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("personCode`torgCode`tyear`tmonth`tpostCode`ttargetLevel`ttargetStep`texpectedTotal`tmatchedExpected`tstatus`tcategory`tafterValue`truleNote")
$index = 0
foreach ($sample in $samples) {
    $index++
    if ($index % 100 -eq 0) {
        Write-Host "Processed $index / $($samples.Count)"
    }
    $bodyObject = [ordered]@{
        personCode = $sample.personCode
        orgCode = $sample.orgCode
        year = [int]$sample.year
        month = [int]$sample.month
        changeType = $changeType
    }
    $body = $bodyObject | ConvertTo-Json -Compress
    try {
        $content = [System.Net.Http.StringContent]::new($body, [System.Text.Encoding]::UTF8, "application/json")
        $response = $client.PostAsync("$BaseUrl/api/salary/rule-trial/normal-grade", $content).Result
        $bytes = $response.Content.ReadAsByteArrayAsync().Result
        $text = [System.Text.Encoding]::UTF8.GetString($bytes)
        if (-not $response.IsSuccessStatusCode) {
            $message = $text -replace "`t", " " -replace "`r?`n", " "
            $lines.Add("$($sample.personCode)`t$($sample.orgCode)`t$($sample.year)`t$($sample.month)`t$($sample.postCode)`t$($sample.targetLevel)`t$($sample.targetStep)`t$($sample.expectedTotal)`t`tERROR`thttp-error`t`t$(Escape-Tsv $message)")
            continue
        }
        $json = $text | ConvertFrom-Json
        $data = $json.data
        $change = @($data.changes | Where-Object { $_.itemCode -eq "TG2006_TGB" } | Select-Object -First 1)
        if (-not $change) {
            $lines.Add("$($sample.personCode)`t$($sample.orgCode)`t$($sample.year)`t$($sample.month)`t$($sample.postCode)`t$($sample.targetLevel)`t$($sample.targetStep)`t$($sample.expectedTotal)`t$($data.matchedExpected)`tNO_TGB`tno-tgb-check`t`t")
            continue
        }
        $note = [string]$change.ruleNote
        $status = if ($note.Contains($matchedText)) { "MATCH" } else { "DIFF" }
        $category = Classify-Note $note
        $lines.Add("$($sample.personCode)`t$($sample.orgCode)`t$($sample.year)`t$($sample.month)`t$($sample.postCode)`t$($sample.targetLevel)`t$($sample.targetStep)`t$($sample.expectedTotal)`t$($data.matchedExpected)`t$status`t$(Escape-Tsv $category)`t$(Escape-Tsv $change.afterValue)`t$(Escape-Tsv $note)")
    } catch {
        $lines.Add("$($sample.personCode)`t$($sample.orgCode)`t$($sample.year)`t$($sample.month)`t$($sample.postCode)`t$($sample.targetLevel)`t$($sample.targetStep)`t$($sample.expectedTotal)`t`tERROR`tscript-error`t`t$(Escape-Tsv $_.Exception.Message)")
    }
}

[System.IO.File]::WriteAllLines((Resolve-Path (Split-Path -Parent $OutputPath)).Path + "\" + (Split-Path -Leaf $OutputPath), $lines, [System.Text.UTF8Encoding]::new($false))

$diffLines = $lines | Where-Object { $_ -like "personCode`t*" -or $_ -match "`tDIFF`t" }
[System.IO.File]::WriteAllLines((Resolve-Path (Split-Path -Parent $DiffOutputPath)).Path + "\" + (Split-Path -Leaf $DiffOutputPath), $diffLines, [System.Text.UTF8Encoding]::new($false))

Import-Csv $OutputPath -Delimiter "`t" |
    Group-Object status, category |
    Sort-Object Count -Descending |
    Select-Object Count, Name |
    Format-Table -AutoSize

Write-Host "Sample file: $SamplePath"
Write-Host "Result file: $OutputPath"
Write-Host "Diff file: $DiffOutputPath"
