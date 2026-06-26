param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 180,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [Alias("Input")]
    [string]$InputPath = "",
    [int]$Take = 1,
    [switch]$KeepWritten,
    [string]$OutputDir = ""
)

$ErrorActionPreference = "Stop"

$scriptPath = Join-Path $PSScriptRoot "run-real-writable-candidate-pilot.mjs"
$argsList = @(
    $scriptPath,
    "--base-url", $BaseUrl,
    "--timeout-sec", "$TimeoutSec",
    "--username", $Username,
    "--password", $Password,
    "--take", "$Take"
)
if ($KeepWritten) {
    $argsList += "--keep-written"
}
if (-not [string]::IsNullOrWhiteSpace($InputPath)) {
    $argsList += @("--input", $InputPath)
}
if (-not [string]::IsNullOrWhiteSpace($OutputDir)) {
    $argsList += @("--output-dir", $OutputDir)
}

& node @argsList
if ($LASTEXITCODE -ne 0) {
    throw "Real writable candidate pilot failed with exit code $LASTEXITCODE"
}
