param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [string]$DbPassword = "",
    [switch]$StartBackend,
    [switch]$StopBackendAfter,
    [switch]$SkipSalarySamples,
    [switch]$SkipReportPrintArchiveSamples,
    [switch]$SkipHistoryQueueClosure,
    [string]$ReportPath = ""
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"
$targetDir = Join-Path $backendDir "target"
if ([string]::IsNullOrWhiteSpace($ReportPath)) {
    $ReportPath = Join-Path $targetDir "online-business-closure-report.txt"
}
New-Item -ItemType Directory -Force -Path $targetDir | Out-Null

if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    $DbPassword = $env:DB_PASSWORD
}

$results = [System.Collections.Generic.List[object]]::new()
$report = [System.Collections.Generic.List[string]]::new()
$startedAt = Get-Date

function Add-Report([string]$Line = "") {
    $script:report.Add($Line)
    Write-Host $Line
}

function Add-StepResult([string]$Title, [string]$Status, [int]$Seconds, [string]$Message) {
    $script:results.Add([pscustomobject]@{
        Title = $Title
        Status = $Status
        Seconds = $Seconds
        Message = $Message
    })
}

function Invoke-Step([string]$Title, [scriptblock]$Action) {
    Add-Report ""
    Add-Report "== $Title =="
    $stepStarted = Get-Date
    try {
        $output = & $Action
        foreach ($line in @($output)) {
            if ($null -ne $line) {
                Add-Report ("  " + $line.ToString())
            }
        }
        $seconds = [int]((Get-Date) - $stepStarted).TotalSeconds
        Add-StepResult $Title "PASS" $seconds ""
        Add-Report ("PASS: {0} ({1}s)" -f $Title, $seconds)
    } catch {
        $seconds = [int]((Get-Date) - $stepStarted).TotalSeconds
        $message = $_.Exception.Message
        Add-StepResult $Title "FAIL" $seconds $message
        Add-Report ("FAIL: {0} ({1}s) - {2}" -f $Title, $seconds, $message)
    }
}

function Invoke-VerifyScript([string]$Path, [hashtable]$Arguments) {
    & $Path @Arguments
}

Add-Report "# Online Business Closure Verification"
Add-Report ("GeneratedAt: {0}" -f $startedAt.ToString("yyyy-MM-dd HH:mm:ss"))
Add-Report ("BaseUrl: {0}" -f $BaseUrl)
Add-Report ("GitCommit: {0}" -f ((& git -C $root rev-parse --short HEAD 2>$null) -join ""))

try {
    if ($StartBackend) {
        Invoke-Step "Start managed backend" {
            if ([string]::IsNullOrWhiteSpace($DbPassword)) {
                throw "DB_PASSWORD is required for -StartBackend. Set `$env:DB_PASSWORD or pass -DbPassword."
            }
            & (Join-Path $PSScriptRoot "start-backend-dev.ps1") -DbPassword $DbPassword -TimeoutSec 120
        }
    }

    Invoke-Step "Local service health" {
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/health" -TimeoutSec $TimeoutSec -ErrorAction Stop
        $response | ConvertTo-Json -Depth 10 -Compress
    }

    $commonArgs = @{
        BaseUrl = $BaseUrl
        TimeoutSec = $TimeoutSec
        Username = $Username
        Password = $Password
        FailOnUnexpected = $true
    }

    Invoke-Step "Report CSV exports" {
        Invoke-VerifyScript (Join-Path $PSScriptRoot "verify-report-csv-exports.ps1") $commonArgs
    }

    Invoke-Step "Report print pages" {
        Invoke-VerifyScript (Join-Path $PSScriptRoot "verify-report-print-pages.ps1") $commonArgs
    }

    if (-not $SkipHistoryQueueClosure) {
        Invoke-Step "Report/history queue closure" {
            Invoke-VerifyScript (Join-Path $PSScriptRoot "verify-report-history-queue-closure.ps1") $commonArgs
        }
    } else {
        Add-StepResult "Report/history queue closure" "SKIP" 0 "Skipped by switch."
    }

    if (-not $SkipReportPrintArchiveSamples) {
        Invoke-Step "Report print archive samples" {
            Invoke-VerifyScript (Join-Path $PSScriptRoot "verify-report-print-archive-samples.ps1") $commonArgs
        }
        Invoke-Step "Report print archive ledger" {
            Invoke-VerifyScript (Join-Path $PSScriptRoot "verify-report-print-archive-ledger.ps1") $commonArgs
        }
    } else {
        Add-StepResult "Report print archive samples" "SKIP" 0 "Skipped by switch."
        Add-StepResult "Report print archive ledger" "SKIP" 0 "Skipped by switch."
    }

    Invoke-Step "Business acceptance samples" {
        Invoke-VerifyScript (Join-Path $PSScriptRoot "verify-business-acceptance-samples.ps1") $commonArgs
    }

    if (-not $SkipSalarySamples) {
        Invoke-Step "Salary sample gate" {
            Invoke-VerifyScript (Join-Path $PSScriptRoot "verify-salary-samples.ps1") $commonArgs
        }
    } else {
        Add-StepResult "Salary sample gate" "SKIP" 0 "Skipped by switch."
    }
} finally {
    if ($StopBackendAfter) {
        Invoke-Step "Stop managed backend" {
            & (Join-Path $PSScriptRoot "stop-backend-dev.ps1")
        }
    }
}

$failed = @($results | Where-Object { $_.Status -eq "FAIL" })
Add-Report ""
Add-Report "== Summary =="
foreach ($result in $results) {
    Add-Report ("{0}`t{1}`t{2}s`t{3}" -f $result.Status, $result.Title, $result.Seconds, $result.Message)
}
Add-Report ("Overall: {0}" -f ($(if ($failed.Count -eq 0) { "PASS" } else { "FAIL" })))
Add-Report ("FinishedAt: {0}" -f (Get-Date).ToString("yyyy-MM-dd HH:mm:ss"))

$report | Set-Content -Encoding UTF8 -Path $ReportPath

if ($failed.Count -gt 0) {
    throw "Online business closure verification failed. See report: $ReportPath"
}

Write-Host ""
Write-Host "Online business closure verification passed. Report: $ReportPath"
