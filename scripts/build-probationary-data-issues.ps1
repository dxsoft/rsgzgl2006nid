$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"
Push-Location $backendDir
try {
    & (Join-Path $backendDir "scripts\build-probationary-data-issues.ps1") @args
} finally {
    Pop-Location
}
