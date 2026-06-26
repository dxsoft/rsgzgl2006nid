param(
    [string]$OutputPath = "target\jslb-coverage.tsv",
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

function Decode-HexUtf8([string]$Hex) {
    if ([string]::IsNullOrWhiteSpace($Hex)) {
        return ""
    }
    $bytes = for ($i = 0; $i -lt $Hex.Length; $i += 2) {
        [Convert]::ToByte($Hex.Substring($i, 2), 16)
    }
    return [System.Text.Encoding]::UTF8.GetString($bytes)
}

function Escape-Tsv([object]$Value) {
    if ($null -eq $Value) {
        return ""
    }
    return ([string]$Value) -replace "`t", " " -replace "`r?`n", " "
}

function Coverage([string]$Hex) {
    switch ($Hex) {
        "E8B083E6A087E6998BE58D87" { return @("RULED", "standard-adjustment", "standard year switch") }
        "E8B083E695B4E6A087E58786" { return @("RULED", "standard-adjustment", "standard year switch") }
        "E6B4A5E8B4B4E58F98E58C96" { return @("RULED", "standard-adjustment", "target allowances/standards") }
        "E8818CE58AA1E58F98E58C96" { return @("RULED", "post-change", "civil/institution/worker/judicial paths") }
        "E6ADA3E5B8B8E7BAA7E588AB" { return @("RULED", "normal-grade", "normal step/level/salary-grade promotion") }
        "E6ADA3E5B8B8E6A1A3E6ACA1" { return @("RULED", "normal-grade", "normal step/level/salary-grade promotion") }
        "E6ADA3E5B8B8E6998BE6A1A3" { return @("RULED", "normal-grade", "normal step/level/salary-grade promotion") }
        "E7BAA7E588ABE6BB9AE58AA8" { return @("RULED", "normal-grade", "normal step/level/salary-grade promotion") }
        "E8ADA6E8A194E58F98E58C96" { return @("RULED", "police-rank", "police rank allowance recalculation") }
        "E8ADA6E8A194E6B4A5E8B4B4" { return @("RULED", "police-rank", "police rank allowance recalculation") }
        "E8BDACE6ADA3E5AE9AE7BAA7" { return @("RULED", "regularization", "education/appointment placement") }
        "E8B083E585A5E5AE9AE8B584" { return @("RULED", "regularization", "entrance placement or target-state recalculation") }
        "E8BDACE4B89AE5AE9AE8B584" { return @("RULED", "regularization", "entrance placement or target-state recalculation") }
        "E98080E4BC8DE5AE9AE8B584" { return @("RULED", "regularization", "entrance placement or target-state recalculation") }
        "E8A781E4B9A0E5B7A5E8B584" { return @("RULED", "probationary", "probationary salary by education") }
        "E696B0E8BF9BE5B7A5E8B584" { return @("RULED", "entrance", "probationary/formal entrance salary") }
        "32303036E5A597E694B9" { return @("RULED", "legacy-2006", "legacy 2006 conversion") }
        "E8ADA6E59198E5A597E694B9" { return @("RULED", "civil-rank", "civil rank target state") }
        "E8818CE7BAA7E5A597E694B9" { return @("RULED", "civil-rank", "civil rank target state") }
        "E8818CE7BAA7E6998BE58D87" { return @("RULED", "civil-rank", "civil rank target state") }
        "E5ADA6E58E86E58F98E58C96" { return @("RULED", "education-change", "education change recalculation") }
        "E69599E68AA4E6B4A5E8B4B4" { return @("RULED", "teacher-nurse", "teacher/nurse allowance recalculation") }
        "E6B395E6A380E5A597E694B9" { return @("RULED", "judicial-conversion", "judicial conversion") }
        "E6B395E5AE98E7AD89E7BAA7" { return @("RULED", "judicial-allowance", "judicial rank/allowance recalculation") }
        "E6A380E5AF9FE7AD89E7BAA7" { return @("RULED", "judicial-allowance", "judicial rank/allowance recalculation") }
        "E5AEA1E588A4E6B4A5E8B4B4" { return @("RULED", "judicial-allowance", "judicial rank/allowance recalculation") }
        "E6A380E5AF9FE6B4A5E8B4B4" { return @("RULED", "judicial-allowance", "judicial rank/allowance recalculation") }
        "E9998DE8B584E5A484E58886" { return @("RULED", "target-state", "target-state recalculation") }
        "E5A596E58AB1E6998BE58D87" { return @("RULED", "target-state", "target-state recalculation") }
        "E585B6E5AE83E68385E586B5" { return @("RULED", "target-state", "target-state recalculation") }
        "" { return @("DATA", "blank-change-type", "blank change type") }
        default { return @("UNMAPPED", "pending", "needs sampling") }
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
  String sql = "SELECT HEX(CONVERT(j USING utf8mb4)) h, c FROM (SELECT TRIM(jslb) j, COUNT(*) c FROM hisbase WHERE hj2>0 GROUP BY TRIM(jslb)) x ORDER BY c DESC";
  try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
    while (rs.next()) {
      System.out.println("ROW\t" + rs.getString("h") + "\t" + rs.getInt("c"));
    }
  }
}
/exit
"@

$rawRows = $javaSource | & $jshell -s --class-path $classpath | Where-Object { $_ -like "ROW`t*" }
if (-not $rawRows) {
    throw "No jslb rows exported from database."
}

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("changeType`tcount`tstatus`tcategory`tnote")
foreach ($row in $rawRows) {
    $parts = $row -split "`t"
    if ($parts.Length -lt 3) {
        continue
    }
    $hex = $parts[1]
    $changeType = Decode-HexUtf8 $hex
    $coverage = Coverage $hex
    $lines.Add("$(Escape-Tsv $changeType)`t$($parts[2])`t$($coverage[0])`t$($coverage[1])`t$(Escape-Tsv $coverage[2])")
}

[System.IO.File]::WriteAllLines((Resolve-Path (Split-Path -Parent $OutputPath)).Path + "\" + (Split-Path -Leaf $OutputPath), $lines, [System.Text.UTF8Encoding]::new($false))

Import-Csv $OutputPath -Delimiter "`t" |
    Group-Object status |
    Sort-Object Count -Descending |
    Select-Object Count, Name |
    Format-Table -AutoSize

Write-Host "Coverage file: $OutputPath"
