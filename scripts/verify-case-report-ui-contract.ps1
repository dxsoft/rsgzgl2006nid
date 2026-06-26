param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [switch]$FailOnUnexpected
)

$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"
Push-Location $backendDir
try {
    $scriptArgs = @{
        OutputPath = "target/case-report-ui-contract-results.tsv"
    }
    if ($FailOnUnexpected) {
        $scriptArgs.FailOnUnexpected = $true
    }
    & (Join-Path $backendDir "scripts\verify-case-report-ui-contract.ps1") @scriptArgs
} finally {
    Pop-Location
}
