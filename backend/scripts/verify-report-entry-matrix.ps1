param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [switch]$FailOnUnexpected,
    [string]$OutputPath = "target/report-entry-matrix-results.tsv"
)

$ErrorActionPreference = "Stop"

$outputDir = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

$webSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession

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
        -TimeoutSec $TimeoutSec `
        -ErrorAction Stop
}

try {
    Invoke-RestMethod `
        -Uri "$BaseUrl/api/auth/login" `
        -Method Post `
        -Body (@{ username = $Username; password = $Password } | ConvertTo-Json -Compress) `
        -ContentType "application/json; charset=utf-8" `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec `
        -ErrorAction Stop | Out-Null
} catch {
    throw "Login failed for $BaseUrl. Ensure the backend is running and credentials are valid. $($_.Exception.Message)"
}

$results = [System.Collections.Generic.List[object]]::new()

$requiredCatalogRows = @(
    @{ Code = "STANDARD_TABLE_PRINT"; Url = "/api/reports/standard-tables/print" },
    @{ Code = "SALARY_CHANGE_LEDGER_PRINT"; Url = "/api/reports/salary-change-ledger/print" },
    @{ Code = "ASSESSMENT_SUMMARY_PRINT"; Url = "/api/reports/assessment-summary/print" },
    @{ Code = "PERSON_ROSTER_PRINT"; Url = "/api/reports/person-roster/print" },
    @{ Code = "SALARY_CASE_APPROVAL_PRINT"; Url = "/api/reports/salary-case-approval/print" },
    @{ Code = "SALARY_ROSTER_PRINT"; Url = "/api/reports/salary-roster/print" },
    @{ Code = "SALARY_HISTORY_PRINT"; Url = "/api/reports/salary-history/print" }
)

try {
    $catalog = Invoke-Api "/api/reports/catalog"
    $items = @($catalog.data)
    $migrated = @($items | Where-Object { -not [string]::IsNullOrWhiteSpace("" + $_.printUrl) })
    $pending = @($items | Where-Object { [string]::IsNullOrWhiteSpace("" + $_.printUrl) })
    if ($migrated.Count -lt $requiredCatalogRows.Count) {
        Add-Result $results "report-catalog-migrated-count" "FAIL" "Migrated report rows too few: $($migrated.Count)"
    } elseif ($pending.Count -lt 1) {
        Add-Result $results "report-catalog-pending-visible" "FAIL" "Pending legacy report rows are not visible."
    } else {
        Add-Result $results "report-catalog-summary" "OK" "total=$($items.Count); migrated=$($migrated.Count); pending=$($pending.Count)"
    }

    foreach ($row in $requiredCatalogRows) {
        $match = @($items | Where-Object { ("" + $_.code) -eq $row.Code } | Select-Object -First 1)
        if ($match.Count -lt 1) {
            Add-Result $results ("catalog-" + $row.Code) "FAIL" "Missing catalog row."
        } elseif (("" + $match[0].printUrl).Contains($row.Url)) {
            Add-Result $results ("catalog-" + $row.Code) "OK" $match[0].printUrl
        } else {
            Add-Result $results ("catalog-" + $row.Code) "FAIL" ("Unexpected printUrl=" + $match[0].printUrl)
        }
    }
} catch {
    Add-Result $results "report-catalog-summary" "REQUEST_ERROR" $_.Exception.Message
}

try {
    $archive = Invoke-Api "/api/reports/print-archive?printStatus=ALL&limit=5"
    $rows = @($archive.data.items)
    Add-Result $results "report-print-archive-query" "OK" ("rows=" + $rows.Count)
} catch {
    Add-Result $results "report-print-archive-query" "REQUEST_ERROR" $_.Exception.Message
}

try {
    $batches = Invoke-Api "/api/reports/print-batches?limit=5"
    $rows = @($batches.data.items)
    Add-Result $results "report-print-batches-query" "OK" ("rows=" + $rows.Count)
} catch {
    Add-Result $results "report-print-batches-query" "REQUEST_ERROR" $_.Exception.Message
}

$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$appJs = Join-Path $root "backend\src\main\resources\static\app.js"
$controller = Join-Path $root "backend\src\main\java\com\dx\rsgzgl\system\controller\SalaryReportController.java"
$anchors = @(
    @{ Code = "frontend-single-approval-print"; File = $appJs; Pattern = "data-salary-case-approval-print" },
    @{ Code = "frontend-approval-roster-print"; File = $appJs; Pattern = "data-case-approval-roster-print" },
    @{ Code = "frontend-selected-approval-validate"; File = $appJs; Pattern = "/api/reports/salary-case-approvals/selected/validate" },
    @{ Code = "frontend-selected-approval-print"; File = $appJs; Pattern = "/api/reports/salary-case-approvals/selected/print" },
    @{ Code = "frontend-salary-history-print"; File = $appJs; Pattern = "data-case-salary-history-print" },
    @{ Code = "frontend-salary-history-export"; File = $appJs; Pattern = "data-case-salary-history-export" },
    @{ Code = "frontend-change-ledger-print"; File = $appJs; Pattern = "data-case-change-ledger-print" },
    @{ Code = "frontend-change-ledger-export"; File = $appJs; Pattern = "data-case-change-ledger-export" },
    @{ Code = "frontend-report-archive"; File = $appJs; Pattern = "data-case-report-archive" },
    @{ Code = "frontend-report-archive-export"; File = $appJs; Pattern = "data-case-report-archive-export" },
    @{ Code = "frontend-report-batch-detail"; File = $appJs; Pattern = "data-report-print-batch" },
    @{ Code = "frontend-report-batch-export"; File = $appJs; Pattern = "data-report-print-batch-export" },
    @{ Code = "frontend-report-batch-package"; File = $appJs; Pattern = "data-report-print-batch-package" },
    @{ Code = "frontend-report-batch-reprint"; File = $appJs; Pattern = "data-report-print-batch-reprint" },
    @{ Code = "backend-single-approval-print"; File = $controller; Pattern = '"/salary-case-approval/print"' },
    @{ Code = "backend-single-approval-validate"; File = $controller; Pattern = '"/salary-case-approval/validate"' },
    @{ Code = "backend-approval-roster-print"; File = $controller; Pattern = '"/salary-case-approval-roster/print"' },
    @{ Code = "backend-batch-approval-print"; File = $controller; Pattern = '"/salary-case-approvals/print"' },
    @{ Code = "backend-selected-approval-print"; File = $controller; Pattern = '"/salary-case-approvals/selected/print"' },
    @{ Code = "backend-selected-approval-validate"; File = $controller; Pattern = '"/salary-case-approvals/selected/validate"' },
    @{ Code = "backend-salary-history-print"; File = $controller; Pattern = '"/salary-history/print"' },
    @{ Code = "backend-salary-history-csv"; File = $controller; Pattern = '"/salary-history.csv"' },
    @{ Code = "backend-change-ledger-print"; File = $controller; Pattern = '"/salary-change-ledger/print"' },
    @{ Code = "backend-change-ledger-csv"; File = $controller; Pattern = '"/salary-change-ledger.csv"' },
    @{ Code = "backend-print-archive-query"; File = $controller; Pattern = '"/print-archive"' },
    @{ Code = "backend-print-archive-csv"; File = $controller; Pattern = '"/print-archive.csv"' },
    @{ Code = "backend-print-batches-query"; File = $controller; Pattern = '"/print-batches"' },
    @{ Code = "backend-print-batch-detail"; File = $controller; Pattern = '"/print-batches/{batchNo}"' },
    @{ Code = "backend-print-batch-export"; File = $controller; Pattern = '"/print-batches/{batchNo}.csv"' },
    @{ Code = "backend-print-batch-package"; File = $controller; Pattern = '"/print-batches/{batchNo}/acceptance-package.zip"' },
    @{ Code = "backend-print-batch-reprint"; File = $controller; Pattern = '"/print-batches/{batchNo}/reprint"' }
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
Write-Host "Report entry matrix summary:"
$results | Format-Table Code,Status,Message -AutoSize
Write-Host ""
Write-Host "Wrote $OutputPath"

$unexpected = @($results | Where-Object { $_.Status -in @("FAIL", "REQUEST_ERROR") })
if ($FailOnUnexpected -and $unexpected.Count -gt 0) {
    throw "Report entry matrix verification found unexpected rows."
}
