param(
    [int]$TimeoutSec = 20
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"
$targetDir = Join-Path $backendDir "target"
$pidPath = Join-Path $targetDir "backend-dev.pid"

function Stop-ProcessTree([int]$ProcessId) {
    $children = @(Get-CimInstance Win32_Process | Where-Object { $_.ParentProcessId -eq $ProcessId })
    foreach ($child in $children) {
        Stop-ProcessTree ([int]$child.ProcessId)
    }
    Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
}

if (-not (Test-Path -LiteralPath $pidPath)) {
    Write-Host "No managed backend dev pid file found: $pidPath"
    exit 0
}

$pidText = (Get-Content -LiteralPath $pidPath -Raw).Trim()
$processId = 0
if (-not [int]::TryParse($pidText, [ref]$processId)) {
    Remove-Item -LiteralPath $pidPath -Force -ErrorAction SilentlyContinue
    throw "Invalid backend dev pid file content: $pidText"
}

$process = Get-Process -Id $processId -ErrorAction SilentlyContinue
if ($null -eq $process) {
    Remove-Item -LiteralPath $pidPath -Force -ErrorAction SilentlyContinue
    Write-Host "Managed backend dev process is not running. Removed stale pid file."
    exit 0
}

Stop-ProcessTree $processId
$deadline = (Get-Date).AddSeconds($TimeoutSec)
while ((Get-Date) -lt $deadline) {
    if ($null -eq (Get-Process -Id $processId -ErrorAction SilentlyContinue)) {
        Remove-Item -LiteralPath $pidPath -Force -ErrorAction SilentlyContinue
        Write-Host "Stopped backend dev process. pid=$processId"
        exit 0
    }
    Start-Sleep -Milliseconds 500
}

Remove-Item -LiteralPath $pidPath -Force -ErrorAction SilentlyContinue
throw "Timed out waiting for backend dev process to stop. pid=$processId"
