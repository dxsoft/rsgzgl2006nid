param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [int]$DoneLimit = 120,
    [int]$BatchLimit = 80,
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
        Username = $Username
        Password = $Password
        DoneLimit = $DoneLimit
        BatchLimit = $BatchLimit
        OutputPath = "target/report-print-archive-results.tsv"
    }
    if ($FailOnUnexpected) {
        $scriptArgs.FailOnUnexpected = $true
    }
    & (Join-Path $backendDir "scripts\verify-report-print-archive-samples.ps1") @scriptArgs
} finally {
    Pop-Location
}
