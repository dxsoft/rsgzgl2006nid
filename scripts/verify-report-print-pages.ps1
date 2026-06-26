param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [string]$OrgCode = "001",
    [int]$Year = 2026,
    [int]$Month = 6,
    [int]$AssessmentYear = 2025,
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
        OrgCode = $OrgCode
        Year = $Year
        Month = $Month
        AssessmentYear = $AssessmentYear
        OutputPath = "target/report-print-page-results.tsv"
    }
    if ($FailOnUnexpected) {
        $scriptArgs.FailOnUnexpected = $true
    }
    & (Join-Path $backendDir "scripts\verify-report-print-pages.ps1") @scriptArgs
} finally {
    Pop-Location
}
