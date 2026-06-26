param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [string]$InputPath = "",
    [string]$OutputDir = "",
    [switch]$KeepWritten
)

$ErrorActionPreference = "Stop"

$scriptPath = Join-Path $PSScriptRoot "run-real-history-write-pilot.mjs"
$argsList = @(
    $scriptPath,
    "--base-url", $BaseUrl,
    "--timeout-sec", "$TimeoutSec",
    "--username", $Username,
    "--password", $Password
)
if (-not [string]::IsNullOrWhiteSpace($InputPath)) {
    $argsList += @("--input", $InputPath)
}
if (-not [string]::IsNullOrWhiteSpace($OutputDir)) {
    $argsList += @("--output-dir", $OutputDir)
}
if ($KeepWritten) {
    $argsList += "--keep-written"
}

& node @argsList
if ($LASTEXITCODE -ne 0) {
    throw "Real history write pilot failed with exit code $LASTEXITCODE"
}
