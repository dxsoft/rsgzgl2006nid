param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [Alias("Input")]
    [string]$InputPath = "",
    [int]$Take = 3,
    [string]$WorkItemSuffix = "",
    [switch]$ReviewBeforeConfirm,
    [string]$ReviewReason = "Real writable candidate precheck: target-month legacy history is absent and the case is reviewed before write precheck.",
    [string]$OutputDir = ""
)

$ErrorActionPreference = "Stop"

$scriptPath = Join-Path $PSScriptRoot "run-real-writable-candidate-precheck.mjs"
$argsList = @(
    $scriptPath,
    "--base-url", $BaseUrl,
    "--timeout-sec", "$TimeoutSec",
    "--username", $Username,
    "--password", $Password,
    "--take", "$Take",
    "--work-item-suffix", $WorkItemSuffix
)
if ($ReviewBeforeConfirm) {
    $argsList += @("--review-before-confirm", "--review-reason", $ReviewReason)
}
if (-not [string]::IsNullOrWhiteSpace($InputPath)) {
    $argsList += @("--input", $InputPath)
}
if (-not [string]::IsNullOrWhiteSpace($OutputDir)) {
    $argsList += @("--output-dir", $OutputDir)
}

& node @argsList
if ($LASTEXITCODE -ne 0) {
    throw "Real writable candidate precheck failed with exit code $LASTEXITCODE"
}
