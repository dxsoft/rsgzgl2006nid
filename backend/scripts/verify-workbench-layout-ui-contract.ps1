param(
    [string]$IndexPath = "backend/src/main/resources/static/index.html",
    [string]$StylesPath = "backend/src/main/resources/static/styles.css",
    [string]$AppJsPath = "backend/src/main/resources/static/app.js",
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
if (-not (Test-Path $AppJsPath)) {
    throw "Missing app.js: $AppJsPath"
}

$indexHtml = Get-Content -Path $IndexPath -Raw
$styles = Get-Content -Path $StylesPath -Raw
$appJs = Get-Content -Path $AppJsPath -Raw
$rows = New-Object System.Collections.Generic.List[object]

Add-Check -Rows $rows -Name "workbench-seven-row-shell" -Passed ($styles -match 'grid-template-rows:\s*auto auto auto auto auto minmax\(96px,\s*max-content\) minmax\(320px,\s*auto\);') -Message "Workbench shell should reserve rows for ribbon, header, summaries, tabs, metrics and main panels without compressing them into overlap."
Add-Check -Rows $rows -Name "workbench-shell-overflow-guard" -Passed ($styles -match '\.workbench-view\s*\{[\s\S]*?overflow:\s*auto;[\s\S]*?overscroll-behavior:\s*contain;') -Message "Workbench shell should scroll internally when header, filters, metrics or result panels grow."
Add-Check -Rows $rows -Name "workbench-empty-summary-hidden" -Passed ($styles -match '\.workbench-filter-summary:empty,\s*#migrationToolResult:empty') -Message "Empty summary/result bands should not consume vertical space."
Add-Check -Rows $rows -Name "workbench-metrics-scroll-boundary" -Passed ($styles -match '\.workbench-metrics\s*\{[\s\S]*?max-height:\s*min\(28vh,\s*260px\);[\s\S]*?overflow:\s*auto;') -Message "Metric cards should scroll inside their own band when content grows."
Add-Check -Rows $rows -Name "workbench-main-grid-overflow-guard" -Passed ($styles -match '\.workbench-grid\s*\{[\s\S]*?overflow:\s*hidden;') -Message "Todo/done/history panels should keep their inner scrolling bounded."
Add-Check -Rows $rows -Name "workbench-business-action-group" -Passed ($indexHtml -match 'class="workbench-action-group business-actions"' -and $indexHtml -match '&#24037;&#36164;&#19994;&#21153;') -Message "Daily salary actions should be grouped as the main business area."
Add-Check -Rows $rows -Name "workbench-normal-grade-entry-visible" -Passed ($indexHtml -match 'class="ribbon-group period-group workbench-period-group"' -and $indexHtml -match 'id="normalGradeBatchButton"[\s\S]*?&#27491;&#24120;&#26187;&#26723;&#35797;&#31639;' -and $indexHtml -match 'id="generateNormalGradeTodoButton"[\s\S]*?&#29983;&#25104;&#26187;&#26723;&#24453;&#21150;') -Message "Normal grade trial and todo generation should be visible in the main workbench salary action row."
Add-Check -Rows $rows -Name "workbench-batch-org-selector" -Passed ($indexHtml -match 'id="batchOrgSelect"[\s\S]*?&#36873;&#25321;&#21333;&#20301;' -and $appJs -match 'ensureSelectedForBatch' -and $appJs -match 'batchOrgSelect\?\.addEventListener\("change"') -Message "Workbench salary batch actions should expose an in-place organization selector."
Add-Check -Rows $rows -Name "workbench-migration-action-group" -Passed ($indexHtml -match '<details class="workbench-action-group migration-actions">' -and $indexHtml -match '<summary>&#36801;&#31227;&#26680;&#39564;</summary>') -Message "Migration verification actions should be separated behind a collapsible admin-style entry."
Add-Check -Rows $rows -Name "workbench-action-group-style" -Passed ($styles -match '\.workbench-action-group' -and $styles -match '\.workbench-action-group\.migration-actions' -and $styles -match '\.migration-action-panel') -Message "Workbench action groups should have distinct desktop styling."
Add-Check -Rows $rows -Name "workbench-result-scroll-boundary" -Passed ($styles -match '#migrationToolResult\s*\{[\s\S]*?display:\s*block;[\s\S]*?max-height:\s*min\(42vh,\s*420px\);[\s\S]*?overflow:\s*auto;[\s\S]*?contain:\s*layout paint;') -Message "Large migration/result panes should scroll and paint inside their own band instead of pushing into the work panels."
Add-Check -Rows $rows -Name "workbench-horizontal-toolbar-boundary" -Passed ($styles -match 'scrollbar-gutter:\s*stable;' -and $styles -match '\.workspace-tabs\s*\{[\s\S]*?overflow-x:\s*auto;[\s\S]*?overflow-y:\s*hidden;') -Message "Wide filter, action, and tab rows should use bounded horizontal scrolling."
Add-Check -Rows $rows -Name "workbench-action-strip-clickable-origin" -Passed ($styles -match '\.workbench-action-strip\s*\{[\s\S]*?justify-content:\s*flex-start;') -Message "Workbench action buttons should start inside their scroll container so visible buttons remain clickable."
Add-Check -Rows $rows -Name "salary-business-flow-readable-cards" -Passed ($appJs -match 'renderSalaryBusinessFlows\(flows' -and $appJs -match 'salary-flow-card' -and $appJs -notmatch 'flow\.code \|\| "-"}:\$\{\(flow\.steps') -Message "Salary business flows should render readable cards with steps, not code:length summaries."

$rows | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8
$rows | Format-Table -AutoSize

$failed = @($rows | Where-Object { $_.Status -ne "PASS" })
if ($failed.Count -gt 0) {
    throw "Workbench layout UI contract failed: $($failed.Count) check(s). See $OutputPath"
}

Write-Host "Workbench layout UI contract passed. Report: $OutputPath"
