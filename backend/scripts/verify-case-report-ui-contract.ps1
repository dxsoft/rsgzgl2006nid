param(
    [switch]$FailOnUnexpected,
    [string]$OutputPath = "target/case-report-ui-contract-results.tsv"
)

$ErrorActionPreference = "Stop"

$outputDir = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

function Add-Result(
    [System.Collections.Generic.List[object]]$Rows,
    [string]$Code,
    [string]$Status,
    [string]$Message
) {
    $Rows.Add([pscustomobject]@{
        Code = $Code
        Status = $Status
        Message = $Message
    })
}

$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$appJs = Join-Path $root "backend\src\main\resources\static\app.js"
$stylesCss = Join-Path $root "backend\src\main\resources\static\styles.css"

$checks = @(
    @{ Code = "case-report-status-bar"; File = $appJs; Pattern = "data-case-report-status"; Message = "Detail page report status bar" },
    @{ Code = "case-report-status-ready"; File = $appJs; Pattern = 'reportStatusClass = printReady ? "ready"'; Message = "Printed state is visible" },
    @{ Code = "case-report-status-blocked"; File = $appJs; Pattern = 'printGateBlocked ? "\u5386\u53f2\u5199\u5165\u524d\u9700\u5148\u6253\u5370\u5ba1\u6279\u8868"'; Message = "Unprinted history-write gate is visible" },
    @{ Code = "case-report-primary-print"; File = $appJs; Pattern = 'class="case-snapshot-button primary" data-salary-case-approval-print'; Message = "Primary approval print action" },
    @{ Code = "case-report-forms-group"; File = $appJs; Pattern = 'data-case-report-group="forms"'; Message = "Forms group" },
    @{ Code = "case-report-history-group"; File = $appJs; Pattern = 'data-case-report-group="history"'; Message = "History trace group" },
    @{ Code = "case-report-archive-group"; File = $appJs; Pattern = 'data-case-report-group="archive"'; Message = "Archive audit group" },
    @{ Code = "case-report-approval-print"; File = $appJs; Pattern = "data-salary-case-approval-print"; Message = "Approval report print action" },
    @{ Code = "case-report-approval-roster"; File = $appJs; Pattern = "data-case-approval-roster-print"; Message = "Approval roster action" },
    @{ Code = "case-report-salary-history-print"; File = $appJs; Pattern = "data-case-salary-history-print"; Message = "Salary history print action" },
    @{ Code = "case-report-change-ledger-print"; File = $appJs; Pattern = "data-case-change-ledger-print"; Message = "Change ledger print action" },
    @{ Code = "case-report-salary-history-export"; File = $appJs; Pattern = "data-case-salary-history-export"; Message = "Salary history CSV action" },
    @{ Code = "case-report-change-ledger-export"; File = $appJs; Pattern = "data-case-change-ledger-export"; Message = "Change ledger CSV action" },
    @{ Code = "case-report-archive-ledger"; File = $appJs; Pattern = "data-case-report-archive"; Message = "Archive ledger action" },
    @{ Code = "case-report-archive-export"; File = $appJs; Pattern = "data-case-report-archive-export"; Message = "Archive CSV action" },
    @{ Code = "case-report-actions-grid"; File = $stylesCss; Pattern = "grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));"; Message = "Grouped action layout" },
    @{ Code = "case-report-status-style-ready"; File = $stylesCss; Pattern = ".case-report-status.ready"; Message = "Ready status style" },
    @{ Code = "case-report-status-style-blocked"; File = $stylesCss; Pattern = ".case-report-status.blocked"; Message = "Blocked status style" },
    @{ Code = "case-report-primary-style"; File = $stylesCss; Pattern = ".case-report-status button.primary"; Message = "Primary action style" },
    @{ Code = "case-report-text-overflow"; File = $stylesCss; Pattern = "text-overflow: ellipsis;"; Message = "Long status text is constrained" }
)

$results = [System.Collections.Generic.List[object]]::new()
foreach ($check in $checks) {
    try {
        $content = Get-Content -Raw -Path $check.File
        if ($content.Contains($check.Pattern)) {
            Add-Result $results $check.Code "OK" $check.Message
        } else {
            Add-Result $results $check.Code "FAIL" ("Missing " + $check.Pattern)
        }
    } catch {
        Add-Result $results $check.Code "REQUEST_ERROR" $_.Exception.Message
    }
}

$results | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8

Write-Host ""
Write-Host "Case report UI contract summary:"
$results | Format-Table Code,Status,Message -AutoSize
Write-Host ""
Write-Host "Wrote $OutputPath"

$unexpected = @($results | Where-Object { $_.Status -in @("FAIL", "REQUEST_ERROR") })
if ($FailOnUnexpected -and $unexpected.Count -gt 0) {
    throw "Case report UI contract verification found unexpected rows."
}
