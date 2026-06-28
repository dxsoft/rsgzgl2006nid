param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [int]$Limit = 3,
    [switch]$FailOnUnexpected
)

$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"
Push-Location $backendDir
try {
    $scriptArgs = @{
        BaseUrl = $BaseUrl
        TimeoutSec = $TimeoutSec
        Username = $Username
        Password = $Password
        Limit = $Limit
        OutputPath = "target/history-write-batch-safety-contract-results.tsv"
    }
    if ($FailOnUnexpected) {
        $scriptArgs.FailOnUnexpected = $true
    }
    & (Join-Path $backendDir "scripts\verify-history-write-batch-safety-contract.ps1") @scriptArgs
} finally {
    Pop-Location
}
