param(
    [string]$AppJsPath = "backend/src/main/resources/static/app.js",
    [string]$IndexPath = "backend/src/main/resources/static/index.html",
    [string]$StylesPath = "backend/src/main/resources/static/styles.css",
    [string]$OutputPath = "target/person-maintenance-ui-contract.tsv"
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

if (-not (Test-Path $AppJsPath)) {
    throw "Missing app.js: $AppJsPath"
}
if (-not (Test-Path $StylesPath)) {
    throw "Missing styles.css: $StylesPath"
}
if (-not (Test-Path $IndexPath)) {
    throw "Missing index.html: $IndexPath"
}

$appJs = Get-Content -Path $AppJsPath -Raw
$indexHtml = Get-Content -Path $IndexPath -Raw
$styles = Get-Content -Path $StylesPath -Raw
$rows = New-Object System.Collections.Generic.List[object]

$nodeCheck = & node --check $AppJsPath 2>&1
Add-Check -Rows $rows -Name "app-js-syntax" -Passed ($LASTEXITCODE -eq 0) -Message (($nodeCheck | Out-String).Trim())

Add-Check -Rows $rows -Name "code-option-code-input-class" -Passed ($appJs -match 'code-option-code-input') -Message "Code fields should be visually distinct and read-only."
Add-Check -Rows $rows -Name "code-option-name-input-class" -Passed ($appJs -match 'code-option-name-input') -Message "Name fields should show picker affordance."
Add-Check -Rows $rows -Name "code-option-empty-state" -Passed ($appJs -match 'code-option-empty' -and $styles -match '\.code-option-empty') -Message "Picker search with no result should render explicit empty state."
Add-Check -Rows $rows -Name "required-field-aria-invalid" -Passed ($appJs -match 'aria-invalid' -and $styles -match '\[aria-invalid="true"\]') -Message "Required field validation should expose ARIA invalid state."
Add-Check -Rows $rows -Name "required-field-scroll-focus" -Passed ($appJs -match 'scrollIntoView\(\{ block: "center", inline: "nearest" \}\)' -and $appJs -match 'input\.focus\(\)') -Message "Invalid field should scroll into view and receive focus."
Add-Check -Rows $rows -Name "base-status-cache-card" -Passed ($appJs -match 'base-status-cache' -and $styles -match '\.base-status-cache') -Message "Base status should show salary todo cache state."
Add-Check -Rows $rows -Name "base-change-tags" -Passed ($appJs -match 'base-change-tags' -and $styles -match '\.base-change-tags') -Message "Base change ledger should show type/source tags."
Add-Check -Rows $rows -Name "base-info-readonly-style" -Passed ($styles -match '\.person-base-info-form input\[readonly\]' -and $styles -match '\.person-base-info-form \.code-option-code-input\[readonly\]') -Message "Read-only fields and code fields should remain visually distinct."
Add-Check -Rows $rows -Name "base-info-save-action-style" -Passed ($styles -match '\.person-base-info-form #saveBaseInfoButton') -Message "Base info save action should be visually anchored in the edit form."
Add-Check -Rows $rows -Name "base-info-editor-default-hidden" -Passed ($indexHtml -match 'id="personBaseInfoForm"\s+class="person-base-info-form hidden"') -Message "Base info editor should be hidden by default for clearer read-only viewing."
Add-Check -Rows $rows -Name "base-info-toggle-action" -Passed ($indexHtml -match 'id="toggleBaseInfoEditButton"' -and $appJs -match 'toggleBaseInfoEditor' -and $appJs -match 'setBaseInfoEditorVisible') -Message "Base info editor should open only from the explicit edit action."
Add-Check -Rows $rows -Name "base-info-toggle-action-style" -Passed ($styles -match '\.section-heading-action' -and $styles -match '\.section-heading-action\[aria-expanded="true"\]') -Message "Base info edit toggle should have a clear desktop command style."

$rows | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8
$rows | Format-Table -AutoSize

$failed = @($rows | Where-Object { $_.Status -ne "PASS" })
if ($failed.Count -gt 0) {
    throw "Person maintenance UI contract failed: $($failed.Count) check(s). See $OutputPath"
}

Write-Host "Person maintenance UI contract passed. Report: $OutputPath"
