param(
    [switch]$FailOnUnexpected,
    [string]$OutputPath = ""
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$backend = Join-Path $root "backend"
$script = Join-Path $backend "scripts/verify-generated-timeline-level-contract.ps1"

Push-Location $backend
try {
    $args = @{}
    if ($FailOnUnexpected) {
        $args.FailOnUnexpected = $true
    }
    if (-not [string]::IsNullOrWhiteSpace($OutputPath)) {
        $args.OutputPath = $OutputPath
    }
    & $script @args
} finally {
    Pop-Location
}
