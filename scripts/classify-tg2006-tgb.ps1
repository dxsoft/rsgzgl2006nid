$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"
Push-Location $backendDir
try {
    & (Join-Path $backendDir "scripts\classify-tg2006-tgb.ps1") @args
} finally {
    Pop-Location
}
