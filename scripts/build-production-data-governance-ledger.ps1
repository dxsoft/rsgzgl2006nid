param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [string]$OrgCodes = "00826,00806,00802,00818,01409",
    [int]$Limit = 20,
    [string]$OutputDir = ""
)

$ErrorActionPreference = "Stop"

$scriptPath = Join-Path $PSScriptRoot "build-production-data-governance-ledger.mjs"
$argsList = @(
    $scriptPath,
    "--base-url", $BaseUrl,
    "--timeout-sec", "$TimeoutSec",
    "--username", $Username,
    "--password", $Password,
    "--org-codes", $OrgCodes,
    "--limit", "$Limit"
)
if (-not [string]::IsNullOrWhiteSpace($OutputDir)) {
    $argsList += @("--output-dir", $OutputDir)
}

& node @argsList
if ($LASTEXITCODE -ne 0) {
    throw "Data governance ledger failed with exit code $LASTEXITCODE"
}
