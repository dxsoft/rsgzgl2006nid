param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 20,
    [string]$Username = "admin",
    [string]$Password = "admin",
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
    }
    if ($FailOnUnexpected) {
        $scriptArgs.FailOnUnexpected = $true
    }
    & (Join-Path $backendDir "scripts\verify-business-acceptance-samples.ps1") @scriptArgs
} finally {
    Pop-Location
}
