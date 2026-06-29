param(
    [string]$IndexPath = "backend/src/main/resources/static/index.html",
    [string]$StylesPath = "backend/src/main/resources/static/styles.css",
    [string]$OutputPath = "target/workbench-layout-ui-contract.tsv"
)

$ErrorActionPreference = "Stop"

$outputDir = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

function Add-Check {
    param(
        [System.Collections.Generic.List[object]]$Rows,
        [string]$Name,
        [bool]$Passed,
        [string]$Message = ""
    )

    $Rows.Add([pscustomobject]@{
        Name = $Name
        Status = if ($Passed) { "PASS" } else { "FAIL" }
        Message = $Message
    })
}

if (-not (Test-Path $StylesPath)) {
    throw "Missing styles.css: $StylesPath"
}
if (-not (Test-Path $IndexPath)) {
    throw "Missing index.html: $IndexPath"
}

$indexHtml = Get-Content -Path $IndexPath -Raw
$styles = Get-Content -Path $StylesPath -Raw
$rows = New-Object System.Collections.Generic.List[object]

Add-Check -Rows $rows -Name "workbench-seven-row-shell" -Passed ($styles -match 'grid-template-rows:\s*auto auto auto auto auto minmax\(0,\s*auto\) minmax\(320px,\s*1fr\);') -Message "Workbench shell should reserve rows for ribbon, header, summaries, tabs, metrics and main panels."
Add-Check -Rows $rows -Name "workbench-shell-overflow-guard" -Passed ($styles -match '\.workbench-view\s*\{[\s\S]*?overflow:\s*hidden;') -Message "Workbench shell should keep children from visually overlapping outside the grid."
Add-Check -Rows $rows -Name "workbench-empty-summary-hidden" -Passed ($styles -match '\.workbench-filter-summary:empty,\s*#migrationToolResult:empty') -Message "Empty summary/result bands should not consume vertical space."
Add-Check -Rows $rows -Name "workbench-metrics-scroll-boundary" -Passed ($styles -match '\.workbench-metrics\s*\{[\s\S]*?max-height:\s*min\(28vh,\s*260px\);[\s\S]*?overflow:\s*auto;') -Message "Metric cards should scroll inside their own band when content grows."
Add-Check -Rows $rows -Name "workbench-main-grid-overflow-guard" -Passed ($styles -match '\.workbench-grid\s*\{[\s\S]*?overflow:\s*hidden;') -Message "Todo/done/history panels should keep their inner scrolling bounded."
Add-Check -Rows $rows -Name "workbench-business-action-group" -Passed ($indexHtml -match 'class="workbench-action-group business-actions"' -and $indexHtml -match '&#24037;&#36164;&#19994;&#21153;') -Message "Daily salary actions should be grouped as the main business area."
Add-Check -Rows $rows -Name "workbench-migration-action-group" -Passed ($indexHtml -match 'class="workbench-action-group migration-actions"' -and $indexHtml -match '&#36801;&#31227;&#26680;&#39564;') -Message "Migration verification actions should be separated from the daily salary business area."
Add-Check -Rows $rows -Name "workbench-action-group-style" -Passed ($styles -match '\.workbench-action-group' -and $styles -match '\.workbench-action-group\.migration-actions') -Message "Workbench action groups should have distinct desktop styling."

$rows | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8
$rows | Format-Table -AutoSize

$failed = @($rows | Where-Object { $_.Status -ne "PASS" })
if ($failed.Count -gt 0) {
    throw "Workbench layout UI contract failed: $($failed.Count) check(s). See $OutputPath"
}

Write-Host "Workbench layout UI contract passed. Report: $OutputPath"
