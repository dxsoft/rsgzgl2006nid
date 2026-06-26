param(
    [Parameter(Mandatory = $true)]
    [string]$ScriptName,
    [Parameter(ValueFromRemainingArguments = $true)]
    [object[]]$RemainingArgs
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"
$scriptPath = Join-Path (Join-Path $backendDir "scripts") $ScriptName

if (-not (Test-Path $scriptPath)) {
    throw "Backend script not found: $scriptPath"
}

Push-Location $backendDir
try {
    & $scriptPath @RemainingArgs
} finally {
    Pop-Location
}
