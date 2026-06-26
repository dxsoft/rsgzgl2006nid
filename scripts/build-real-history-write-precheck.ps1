param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [int]$ScanLimit = 200,
    [int]$Take = 5,
    [string]$OutputPath = ""
)

$ErrorActionPreference = "Stop"

$scriptPath = Join-Path $PSScriptRoot "build-real-history-write-precheck.mjs"
$argsList = @(
    $scriptPath,
    "--base-url", $BaseUrl,
    "--timeout-sec", "$TimeoutSec",
    "--username", $Username,
    "--password", $Password,
    "--scan-limit", "$ScanLimit",
    "--take", "$Take"
)
if (-not [string]::IsNullOrWhiteSpace($OutputPath)) {
    $argsList += @("--output", $OutputPath)
}

& node @argsList
if ($LASTEXITCODE -ne 0) {
    throw "Real history write precheck failed with exit code $LASTEXITCODE"
}
