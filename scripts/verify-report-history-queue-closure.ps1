param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [int]$MaxSummaryMilliseconds = 5000,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [switch]$FailOnUnexpected
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"
Push-Location $backendDir
try {
    $scriptArgs = @{
        BaseUrl = $BaseUrl
        TimeoutSec = $TimeoutSec
        MaxSummaryMilliseconds = $MaxSummaryMilliseconds
        Username = $Username
        Password = $Password
        OutputPath = "target/report-history-queue-closure-results.tsv"
    }
    if ($FailOnUnexpected) {
        $scriptArgs.FailOnUnexpected = $true
    }
    & (Join-Path $backendDir "scripts\verify-report-history-queue-closure.ps1") @scriptArgs
} finally {
    Pop-Location
}
