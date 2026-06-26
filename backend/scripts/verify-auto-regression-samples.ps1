param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [switch]$RebuildSamples,
    [switch]$SkipSalarySamples,
    [switch]$SkipBusinessAcceptance,
    [switch]$SkipGeneratedTimeline,
    [switch]$SkipReportPrintArchive,
    [switch]$FailOnUnexpected,
    [string]$OutputDir = "target/auto-regression"
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$targetDir = Join-Path (Split-Path -Parent $scriptDir) "target"
$reportDir = if ([System.IO.Path]::IsPathRooted($OutputDir)) { $OutputDir } else { Join-Path (Split-Path -Parent $scriptDir) $OutputDir }
if (-not (Test-Path $reportDir)) {
    New-Item -ItemType Directory -Path $reportDir | Out-Null
}

function Invoke-Step([string]$Title, [scriptblock]$Action) {
    Write-Host ""
    Write-Host "== $Title =="
    & $Action
}

function Assert-Service() {
    try {
        $response = Invoke-WebRequest -Uri $BaseUrl -UseBasicParsing -TimeoutSec $TimeoutSec
        Write-Host ("Service responded: HTTP {0}" -f $response.StatusCode)
    } catch {
        throw "Local service probe failed for $BaseUrl. Start the backend before running auto regression samples. $($_.Exception.Message)"
    }
}

function Invoke-SampleScript([string]$Path, [hashtable]$ParamMap) {
    & $Path @ParamMap
}

function Read-Tsv([string]$Path) {
    if (-not (Test-Path $Path)) {
        return @()
    }
    return @(Import-Csv $Path -Delimiter "`t")
}

function Status-Count([object[]]$Rows, [string]$Status) {
    @($Rows | Where-Object { $_.status -eq $Status -or $_.Status -eq $Status }).Count
}

function NonMatch-Count([object[]]$Rows) {
    @($Rows | Where-Object {
        $status = if ($_.status) { $_.status } else { $_.Status }
        -not [string]::IsNullOrWhiteSpace($status) -and $status -ne "MATCH" -and $status -ne "OK"
    }).Count
}

function Add-ResultSummary([System.Collections.Generic.List[object]]$Summaries, [string]$Gate, [string]$Path, [string]$OkStatus) {
    $rows = Read-Tsv $Path
    $summaries.Add([pscustomobject]@{
        Gate = $Gate
        ResultPath = $Path
        Total = $rows.Count
        Passed = Status-Count $rows $OkStatus
        NonPassed = NonMatch-Count $rows
        Match = Status-Count $rows "MATCH"
        Ok = Status-Count $rows "OK"
        Diff = Status-Count $rows "DIFF"
        Error = Status-Count $rows "ERROR"
        RequestError = Status-Count $rows "REQUEST_ERROR"
    })
}

function Copy-IfExists([string]$Path, [string]$Name) {
    if (Test-Path $Path) {
        Copy-Item -LiteralPath $Path -Destination (Join-Path $reportDir $Name) -Force
    }
}

Assert-Service

if ($RebuildSamples) {
    Invoke-Step "Rebuild target-state samples" {
        Invoke-SampleScript (Join-Path $scriptDir "build-target-state-samples.ps1") -ParamMap @{}
    }
    Invoke-Step "Rebuild rank/judicial samples" {
        Invoke-SampleScript (Join-Path $scriptDir "build-rank-judicial-samples.ps1") -ParamMap @{}
    }
    Invoke-Step "Rebuild core-flow samples" {
        Invoke-SampleScript (Join-Path $scriptDir "build-core-flow-samples.ps1") -ParamMap @{}
    }
    Invoke-Step "Rebuild special-flow samples" {
        Invoke-SampleScript (Join-Path $scriptDir "build-special-flow-samples.ps1") -ParamMap @{}
    }
}

$sampleArgs = @{
    BaseUrl = $BaseUrl
    TimeoutSec = $TimeoutSec
    Username = $Username
    Password = $Password
}
if ($FailOnUnexpected) {
    $sampleArgs.FailOnUnexpected = $true
}

if (-not $SkipSalarySamples) {
    Invoke-Step "Salary rule sample gate" {
        Invoke-SampleScript (Join-Path $scriptDir "verify-salary-samples.ps1") -ParamMap $sampleArgs
    }
}

if (-not $SkipBusinessAcceptance) {
    Invoke-Step "Business acceptance sample gate" {
        Invoke-SampleScript (Join-Path $scriptDir "verify-business-acceptance-samples.ps1") -ParamMap $sampleArgs
    }
}

if (-not $SkipGeneratedTimeline) {
    Invoke-Step "Generated timeline sample gate" {
        Invoke-SampleScript (Join-Path $scriptDir "verify-generated-timeline-samples.ps1") -ParamMap $sampleArgs
    }
}

if (-not $SkipReportPrintArchive) {
    Invoke-Step "Report print archive sample gate" {
        $reportPrintArgs = $sampleArgs.Clone()
        $reportPrintArgs.OutputPath = Join-Path $targetDir "report-print-archive-results.tsv"
        Invoke-SampleScript (Join-Path $scriptDir "verify-report-print-archive-samples.ps1") -ParamMap $reportPrintArgs
    }
}

$summaries = [System.Collections.Generic.List[object]]::new()
Add-ResultSummary $summaries "cross-type" (Join-Path $targetDir "cross-type-results.tsv") "MATCH"
Add-ResultSummary $summaries "normal-grade-expanded" (Join-Path $targetDir "normal-grade-expanded-results.tsv") "MATCH"
Add-ResultSummary $summaries "target-state" (Join-Path $targetDir "target-state-results.tsv") "MATCH"
Add-ResultSummary $summaries "rank-judicial" (Join-Path $targetDir "rank-judicial-results.tsv") "MATCH"
Add-ResultSummary $summaries "core-flow" (Join-Path $targetDir "core-flow-results.tsv") "MATCH"
Add-ResultSummary $summaries "special-flow" (Join-Path $targetDir "special-flow-results.tsv") "MATCH"
Add-ResultSummary $summaries "business-acceptance" (Join-Path $targetDir "business-acceptance-results.tsv") "MATCH"
Add-ResultSummary $summaries "generated-timeline" (Join-Path $targetDir "generated-timeline-results.tsv") "OK"
Add-ResultSummary $summaries "report-print-archive" (Join-Path $targetDir "report-print-archive-results.tsv") "OK"

$summaryPath = Join-Path $reportDir "auto-regression-summary.tsv"
$summaries | Export-Csv -Path $summaryPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8

Copy-IfExists (Join-Path $scriptDir "known-sample-issues.tsv") "known-sample-issues.tsv"
Copy-IfExists (Join-Path $targetDir "cross-type-results.tsv") "cross-type-results.tsv"
Copy-IfExists (Join-Path $targetDir "normal-grade-expanded-results.tsv") "normal-grade-expanded-results.tsv"
Copy-IfExists (Join-Path $targetDir "target-state-results.tsv") "target-state-results.tsv"
Copy-IfExists (Join-Path $targetDir "rank-judicial-results.tsv") "rank-judicial-results.tsv"
Copy-IfExists (Join-Path $targetDir "core-flow-results.tsv") "core-flow-results.tsv"
Copy-IfExists (Join-Path $targetDir "special-flow-results.tsv") "special-flow-results.tsv"
Copy-IfExists (Join-Path $targetDir "business-acceptance-results.tsv") "business-acceptance-results.tsv"
Copy-IfExists (Join-Path $targetDir "generated-timeline-results.tsv") "generated-timeline-results.tsv"
Copy-IfExists (Join-Path $targetDir "report-print-archive-results.tsv") "report-print-archive-results.tsv"

$generatedAt = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$totalRows = ($summaries | Measure-Object -Property Total -Sum).Sum
$totalNonPassed = ($summaries | Measure-Object -Property NonPassed -Sum).Sum
$requestErrors = ($summaries | Measure-Object -Property RequestError -Sum).Sum
$status = if ($requestErrors -gt 0) { "ERROR" } elseif ($totalNonPassed -gt 0) { "WARN" } else { "PASS" }

$reportLines = [System.Collections.Generic.List[string]]::new()
$reportLines.Add("Auto Regression Samples")
$reportLines.Add("=======================")
$reportLines.Add("")
$reportLines.Add("GeneratedAt: $generatedAt")
$reportLines.Add("BaseUrl: $BaseUrl")
$reportLines.Add("Status: $status")
$reportLines.Add("TotalRows: $totalRows")
$reportLines.Add("NonPassedRows: $totalNonPassed")
$reportLines.Add("RequestErrors: $requestErrors")
$reportLines.Add("RebuildSamples: $($RebuildSamples.IsPresent)")
$reportLines.Add("")
$reportLines.Add("Gate Summary")
$reportLines.Add("------------")
foreach ($summary in $summaries) {
    $reportLines.Add(("{0}: total={1}, passed={2}, nonPassed={3}, requestError={4}" -f $summary.Gate, $summary.Total, $summary.Passed, $summary.NonPassed, $summary.RequestError))
}
$reportLines.Add("")
$reportLines.Add("Artifacts")
$reportLines.Add("---------")
$reportLines.Add($summaryPath)
$reportLines.Add($reportDir)

$reportPath = Join-Path $reportDir "auto-regression-report.txt"
$reportLines | Set-Content -Path $reportPath -Encoding UTF8

Write-Host ""
Write-Host "Auto regression sample summary:"
$summaries | Format-Table Gate,Total,Passed,NonPassed,RequestError -AutoSize
Write-Host ""
Write-Host "Wrote $summaryPath"
Write-Host "Wrote $reportPath"

if ($FailOnUnexpected -and $requestErrors -gt 0) {
    throw "Auto regression samples found request errors."
}
