param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [string]$Start = "2025-01",
    [string]$End = "2026-06",
    [int]$OrgLimit = 80,
    [int]$PerPreviewLimit = 100,
    [int]$Take = 20,
    [string]$Sources = "",
    [string]$CandidateStatuses = "",
    [string]$OutputDir = ""
)

$ErrorActionPreference = "Stop"

$scriptPath = Join-Path $PSScriptRoot "scan-real-writable-candidates.mjs"
$argsList = @(
    $scriptPath,
    "--base-url", $BaseUrl,
    "--timeout-sec", "$TimeoutSec",
    "--username", $Username,
    "--password", $Password,
    "--start", $Start,
    "--end", $End,
    "--org-limit", "$OrgLimit",
    "--per-preview-limit", "$PerPreviewLimit",
    "--take", "$Take",
    "--sources", $Sources
)
if (-not [string]::IsNullOrWhiteSpace($CandidateStatuses)) {
    $argsList += @("--candidate-statuses", $CandidateStatuses)
}
if (-not [string]::IsNullOrWhiteSpace($OutputDir)) {
    $argsList += @("--output-dir", $OutputDir)
}

& node @argsList
if ($LASTEXITCODE -ne 0) {
    throw "Real writable candidate scan failed with exit code $LASTEXITCODE"
}
