param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [int]$MaxSummaryMilliseconds = 30000,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [switch]$FailOnUnexpected,
    [string]$OutputPath = "target/report-history-queue-closure-results.tsv"
)

$ErrorActionPreference = "Stop"

$outputDir = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

$webSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
if (-not [string]::IsNullOrWhiteSpace($Username)) {
    $loginBody = @{ username = $Username; password = $Password } | ConvertTo-Json -Compress
    try {
        Invoke-RestMethod `
            -Uri "$BaseUrl/api/auth/login" `
            -Method Post `
            -Body $loginBody `
            -ContentType "application/json; charset=utf-8" `
            -WebSession $webSession `
            -TimeoutSec $TimeoutSec `
            -ErrorAction Stop | Out-Null
    } catch {
        throw "Login failed for $BaseUrl. Ensure the backend is running and credentials are valid. $($_.Exception.Message)"
    }
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

function Invoke-Api([string]$Path) {
    return Invoke-RestMethod `
        -Uri "$BaseUrl$Path" `
        -Method Get `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec
}

function Find-PrintableCaseNo() {
    try {
        $itemsResponse = Invoke-Api "/api/workbench/items?status=DONE&limit=160"
        foreach ($item in @($itemsResponse.data.items)) {
            $caseNo = "" + $item.id
            if (-not $caseNo.StartsWith("GZ-")) {
                continue
            }
            try {
                $encoded = [uri]::EscapeDataString($caseNo)
                $validation = Invoke-Api "/api/reports/salary-case-approval/validate?caseNo=$encoded"
                if ($validation.data.printable -eq $true) {
                    return $caseNo
                }
            } catch {
            }
        }
    } catch {
    }
    return ""
}

function Invoke-SelectedValidate([string[]]$CaseNos) {
    $body = [System.Text.StringBuilder]::new()
    foreach ($caseNo in $CaseNos) {
        if ($body.Length -gt 0) {
            [void]$body.Append("&")
        }
        [void]$body.Append("caseNo=")
        [void]$body.Append([uri]::EscapeDataString($caseNo))
    }
    return Invoke-RestMethod `
        -Uri "$BaseUrl/api/reports/salary-case-approvals/selected/validate" `
        -Method Post `
        -Body $body.ToString() `
        -ContentType "application/x-www-form-urlencoded; charset=utf-8" `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec
}

$results = [System.Collections.Generic.List[object]]::new()

try {
    $plans = Invoke-Api "/api/workbench/history-write-plans?limit=10"
    Add-Result $results "history-write-plans" "OK" ("Rows=" + @($plans.data).Count)
} catch {
    Add-Result $results "history-write-plans" "REQUEST_ERROR" $_.Exception.Message
}

try {
    $ledger = Invoke-Api "/api/workbench/history-write-review-ledger?limit=10"
    Add-Result $results "history-write-review-ledger" "OK" ("Total=" + $ledger.data.total)
} catch {
    Add-Result $results "history-write-review-ledger" "REQUEST_ERROR" $_.Exception.Message
}

try {
    $batches = Invoke-Api "/api/reports/print-batches?limit=5"
    Add-Result $results "report-print-batches" "OK" ("Rows=" + @($batches.data.items).Count)
} catch {
    Add-Result $results "report-print-batches" "REQUEST_ERROR" $_.Exception.Message
}

try {
    $summaryWatch = [System.Diagnostics.Stopwatch]::StartNew()
    $summary = Invoke-Api "/api/workbench/summary"
    $summaryWatch.Stop()
    $queueMetrics = @($summary.data.metrics | Where-Object { ("" + $_.code).StartsWith("HISTORY_QUEUE_") })
    $missingQueues = @("HISTORY_QUEUE_BLOCKED", "HISTORY_QUEUE_PREPARED", "HISTORY_QUEUE_REVIEW", "HISTORY_QUEUE_RETEST") |
        Where-Object { $code = $_; -not @($queueMetrics | Where-Object { $_.code -eq $code }) }
    if ($missingQueues.Count -gt 0) {
        Add-Result $results "workbench-summary-history-queue-metrics" "FAIL" ("Missing " + ($missingQueues -join ","))
    } elseif ($summaryWatch.ElapsedMilliseconds -gt $MaxSummaryMilliseconds) {
        Add-Result $results "workbench-summary-history-queue-metrics" "FAIL" ("Slow summary " + $summaryWatch.ElapsedMilliseconds + "ms > " + $MaxSummaryMilliseconds + "ms")
    } else {
        $counts = ($queueMetrics | ForEach-Object { ("" + $_.code + "=" + $_.count) }) -join "; "
        Add-Result $results "workbench-summary-history-queue-metrics" "OK" ($summaryWatch.ElapsedMilliseconds.ToString() + "ms; " + $counts)
    }
} catch {
    Add-Result $results "workbench-summary-history-queue-metrics" "REQUEST_ERROR" $_.Exception.Message
}

try {
    Invoke-SelectedValidate @() | Out-Null
    Add-Result $results "selected-approval-empty-validate" "FAIL" "Empty request unexpectedly succeeded."
} catch {
    $statusCode = $null
    try {
        $statusCode = $_.Exception.Response.StatusCode.value__
    } catch {
    }
    if ($statusCode -eq 400 -or $statusCode -eq 500) {
        Add-Result $results "selected-approval-empty-validate" "OK" "Rejected empty selected validation request."
    } else {
        Add-Result $results "selected-approval-empty-validate" "REQUEST_ERROR" $_.Exception.Message
    }
}

$printableCaseNo = Find-PrintableCaseNo
if ([string]::IsNullOrWhiteSpace($printableCaseNo)) {
    Add-Result $results "selected-approval-sample-validate" "SKIP" "No printable salary case found."
} else {
    try {
        $selectedValidation = Invoke-SelectedValidate @($printableCaseNo)
        if ($selectedValidation.data.printable -eq $true -and [int]$selectedValidation.data.caseCount -eq 1) {
            Add-Result $results "selected-approval-sample-validate" "OK" $printableCaseNo
        } else {
            Add-Result $results "selected-approval-sample-validate" "FAIL" ("Printable=" + $selectedValidation.data.printable + "; caseCount=" + $selectedValidation.data.caseCount)
        }
    } catch {
        Add-Result $results "selected-approval-sample-validate" "REQUEST_ERROR" $_.Exception.Message
    }
}

$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$appJs = Join-Path $root "backend\src\main\resources\static\app.js"
$stylesCss = Join-Path $root "backend\src\main\resources\static\styles.css"
$controller = Join-Path $root "backend\src\main\java\com\dx\rsgzgl\system\controller\SalaryReportController.java"
$anchors = @(
    @{ Code = "frontend-printed-ready-write"; File = $appJs; Pattern = "executePrintedReadyHistoryQueue" },
    @{ Code = "frontend-unprinted-selected-print"; File = $appJs; Pattern = "printUnprintedBlockedHistoryQueue" },
    @{ Code = "frontend-batch-refresh"; File = $appJs; Pattern = "refreshQueuesAfterBatchReportPrint" },
    @{ Code = "frontend-batch-query"; File = $appJs; Pattern = "loadReportPrintBatches" },
    @{ Code = "frontend-batch-to-history-queue"; File = $appJs; Pattern = "sendReportBatchToHistoryQueue" },
    @{ Code = "frontend-history-queue-source-batch"; File = $appJs; Pattern = "data-history-plan-queue-batch" },
    @{ Code = "frontend-history-final-confirm-gate"; File = $appJs; Pattern = "batchFinalConfirmGateHtml" },
    @{ Code = "frontend-history-batch-safety-token"; File = $appJs; Pattern = "batchSafetyTokenHtml" },
    @{ Code = "frontend-history-token-expiry-block"; File = $appJs; Pattern = "batchSafetyTokenExpired" },
    @{ Code = "frontend-history-token-refresh"; File = $appJs; Pattern = "data-batch-preview-refresh" },
    @{ Code = "frontend-history-postwrite-followup"; File = $appJs; Pattern = "data-batch-followup-comparison" },
    @{ Code = "frontend-history-ledger-queue"; File = $appJs; Pattern = "openHistoryBatchLedgerQueue" },
    @{ Code = "frontend-history-ledger-failed-queue"; File = $appJs; Pattern = 'data-history-batch-ledger-queue-type="FAILED"' },
    @{ Code = "frontend-history-batch-audit-row"; File = $appJs; Pattern = "historyBatchAuditRowHtml" },
    @{ Code = "frontend-history-batch-audit-filter"; File = $appJs; Pattern = "data-history-batch-audit-filter" },
    @{ Code = "frontend-history-batch-audit-csv"; File = $appJs; Pattern = "historyBatchAuditCsvRows" },
    @{ Code = "frontend-history-batch-visible-queue"; File = $appJs; Pattern = "openHistoryBatchAuditVisibleQueue" },
    @{ Code = "frontend-history-queue-metric"; File = $appJs; Pattern = "HISTORY_QUEUE_" },
    @{ Code = "backend-history-batch-item-audit"; File = (Join-Path $root "backend\src\main\java\com\dx\rsgzgl\system\service\WorkbenchService.java"); Pattern = "recordHistoryWriteBatchItem" },
    @{ Code = "backend-history-queue-metric"; File = (Join-Path $root "backend\src\main\java\com\dx\rsgzgl\system\service\WorkbenchService.java"); Pattern = "HISTORY_QUEUE_" },
    @{ Code = "frontend-history-single-confirm-gate"; File = $appJs; Pattern = "singleFinalConfirmGateHtml" },
    @{ Code = "frontend-history-single-execute-confirm"; File = $appJs; Pattern = "confirmSingleHistoryWriteExecute" },
    @{ Code = "frontend-archive-write-gate"; File = $appJs; Pattern = "writeGateText" },
    @{ Code = "frontend-report-catalog-summary"; File = $appJs; Pattern = "data-report-catalog-summary" },
    @{ Code = "frontend-report-catalog-migrated"; File = $appJs; Pattern = "data-report-catalog-migrated" },
    @{ Code = "frontend-report-catalog-pending"; File = $appJs; Pattern = "data-report-catalog-pending" },
    @{ Code = "frontend-report-catalog-print-action"; File = $appJs; Pattern = "data-report-print-url" },
    @{ Code = "frontend-report-catalog-case-gate"; File = $appJs; Pattern = "{caseNo}" },
    @{ Code = "frontend-report-catalog-status-style"; File = $stylesCss; Pattern = ".report-catalog-chip.migrated" },
    @{ Code = "frontend-case-report-salary-history"; File = $appJs; Pattern = "data-case-salary-history-print" },
    @{ Code = "frontend-case-report-change-ledger"; File = $appJs; Pattern = "data-case-change-ledger-print" },
    @{ Code = "frontend-case-report-history-url"; File = $appJs; Pattern = "/api/reports/salary-history/print" },
    @{ Code = "frontend-case-report-ledger-url"; File = $appJs; Pattern = "/api/reports/salary-change-ledger/print" },
    @{ Code = "frontend-case-report-salary-history-csv"; File = $appJs; Pattern = "data-case-salary-history-export" },
    @{ Code = "frontend-case-report-change-ledger-csv"; File = $appJs; Pattern = "data-case-change-ledger-export" },
    @{ Code = "frontend-case-report-history-csv-url"; File = $appJs; Pattern = "/api/reports/salary-history.csv" },
    @{ Code = "frontend-case-report-ledger-csv-url"; File = $appJs; Pattern = "/api/reports/salary-change-ledger.csv" },
    @{ Code = "frontend-case-report-status"; File = $appJs; Pattern = "data-case-report-status" },
    @{ Code = "frontend-case-report-group-forms"; File = $appJs; Pattern = 'data-case-report-group="forms"' },
    @{ Code = "frontend-case-report-group-history"; File = $appJs; Pattern = 'data-case-report-group="history"' },
    @{ Code = "frontend-case-report-group-archive"; File = $appJs; Pattern = 'data-case-report-group="archive"' },
    @{ Code = "frontend-case-report-primary-style"; File = $stylesCss; Pattern = ".case-report-status.blocked" },
    @{ Code = "backend-selected-validate"; File = $controller; Pattern = "salary-case-approvals/selected/validate" },
    @{ Code = "backend-selected-print"; File = $controller; Pattern = "salary-case-approvals/selected/print" },
    @{ Code = "backend-print-batches-query"; File = $controller; Pattern = "reportPrintBatches" }
)

foreach ($anchor in $anchors) {
    try {
        $content = Get-Content -Raw -Path $anchor.File
        if ($content.Contains($anchor.Pattern)) {
            Add-Result $results $anchor.Code "OK" $anchor.Pattern
        } else {
            Add-Result $results $anchor.Code "FAIL" ("Missing " + $anchor.Pattern)
        }
    } catch {
        Add-Result $results $anchor.Code "REQUEST_ERROR" $_.Exception.Message
    }
}

$results | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8

Write-Host ""
Write-Host "Report/history queue closure summary:"
$results | Format-Table Code,Status,Message -AutoSize
Write-Host ""
Write-Host "Wrote $OutputPath"

$unexpected = @($results | Where-Object { $_.Status -in @("FAIL", "REQUEST_ERROR") })
if ($FailOnUnexpected -and $unexpected.Count -gt 0) {
    throw "Report/history queue closure verification found unexpected rows."
}
