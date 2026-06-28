param(
    [switch]$FailOnUnexpected,
    [string]$OutputPath = "target/case-detail-ui-contract-results.tsv"
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
    @{ Code = "case-detail-open-endpoint"; File = $appJs; Pattern = '/api/workbench/salary-cases/${encodeURIComponent(caseNo)}'; Message = "Detail page opens salary case endpoint" },
    @{ Code = "case-detail-next-action-section"; File = $appJs; Pattern = "caseClosureNextActionHtml"; Message = "Next action section" },
    @{ Code = "case-detail-closure-actions"; File = $appJs; Pattern = "caseClosureActionButton"; Message = "Closure action mapping" },
    @{ Code = "case-detail-refresh-action"; File = $appJs; Pattern = "data-refresh-case-detail"; Message = "Refresh detail action" },
    @{ Code = "case-detail-trial-status"; File = $appJs; Pattern = "case-trial"; Message = "Trial status panel" },
    @{ Code = "case-detail-snapshot-action"; File = $appJs; Pattern = "data-snapshot-case-no"; Message = "Snapshot action" },
    @{ Code = "case-detail-review-action"; File = $appJs; Pattern = "data-review-case-no"; Message = "Trial review action" },
    @{ Code = "case-detail-cancel-action"; File = $appJs; Pattern = "data-cancel-case-no"; Message = "Cancel handling action" },
    @{ Code = "case-detail-history-section"; File = $appJs; Pattern = "historyWriteSectionHtml"; Message = "History write section" },
    @{ Code = "case-detail-history-preview"; File = $appJs; Pattern = "data-history-write-preview-case-no"; Message = "History preview/create plan action" },
    @{ Code = "case-detail-history-plan"; File = $appJs; Pattern = "data-history-write-plan-case-no"; Message = "History plan action" },
    @{ Code = "case-detail-history-execute"; File = $appJs; Pattern = "data-history-write-execute-case-no"; Message = "History execute action" },
    @{ Code = "case-detail-history-comparison"; File = $appJs; Pattern = "data-history-write-comparison-case-no"; Message = "History comparison action" },
    @{ Code = "case-detail-history-retest"; File = $appJs; Pattern = "data-history-write-comparison-retest-case-no"; Message = "History comparison retest action" },
    @{ Code = "case-detail-history-review"; File = $appJs; Pattern = "data-history-write-inline-review-case-no"; Message = "Inline history review action" },
    @{ Code = "case-detail-history-audit"; File = $appJs; Pattern = "historyWriteAuditTimelineHtml"; Message = "History audit timeline" },
    @{ Code = "case-detail-history-audit-export"; File = $appJs; Pattern = "data-history-write-audit-export-case-no"; Message = "History audit export action" },
    @{ Code = "case-detail-history-result"; File = $appJs; Pattern = "historyWriteResultInlineHtml"; Message = "History write result panel" },
    @{ Code = "case-detail-history-rollback"; File = $appJs; Pattern = "historyWriteRollbackInlineHtml"; Message = "Rollback preview panel" },
    @{ Code = "case-detail-history-rollback-export"; File = $appJs; Pattern = "data-history-write-rollback-preview-export-case-no"; Message = "Rollback preview export action" },
    @{ Code = "case-detail-report-section"; File = $appJs; Pattern = "reportPrintSectionHtml"; Message = "Report print section" },
    @{ Code = "case-detail-report-approval"; File = $appJs; Pattern = "data-salary-case-approval-print"; Message = "Approval report print action" },
    @{ Code = "case-detail-report-archive"; File = $appJs; Pattern = "data-case-report-archive"; Message = "Report archive action" },
    @{ Code = "case-detail-report-audit"; File = $appJs; Pattern = "data-case-report-audit-target"; Message = "Report audit lookup action" },
    @{ Code = "case-detail-flow-style"; File = $stylesCss; Pattern = ".case-flow-steps"; Message = "Closure flow layout style" },
    @{ Code = "case-detail-history-status-style"; File = $stylesCss; Pattern = ".case-history-status"; Message = "History status style" },
    @{ Code = "case-detail-change-row-style"; File = $stylesCss; Pattern = ".case-change-row"; Message = "Detail row style" },
    @{ Code = "case-detail-audit-style"; File = $stylesCss; Pattern = ".case-audit-row"; Message = "Audit timeline style" },
    @{ Code = "case-detail-action-style"; File = $stylesCss; Pattern = ".case-detail-actions"; Message = "Detail action layout style" },
    @{ Code = "case-detail-sticky-footer-style"; File = $stylesCss; Pattern = ".case-detail-dialog > .case-detail-actions"; Message = "Detail dialog command footer style" },
    @{ Code = "case-detail-inline-actions-style"; File = $stylesCss; Pattern = ".case-detail-actions.inline-actions"; Message = "Inline action groups should not inherit sticky footer behavior" }
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
Write-Host "Case detail UI contract summary:"
$results | Format-Table Code,Status,Message -AutoSize
Write-Host ""
Write-Host "Wrote $OutputPath"

$unexpected = @($results | Where-Object { $_.Status -in @("FAIL", "REQUEST_ERROR") })
if ($FailOnUnexpected -and $unexpected.Count -gt 0) {
    throw "Case detail UI contract verification found unexpected rows."
}
