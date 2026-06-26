$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"
Push-Location $backendDir
try {
    & (Join-Path $backendDir "scripts\build-special-flow-samples.ps1") @args
} finally {
    Pop-Location
}
