param(
    [string]$DbPassword = "",
    [string]$DbUrl = "",
    [string]$DbUsername = "",
    [int]$Port = 18080,
    [int]$TimeoutSec = 90,
    [switch]$Force,
    [switch]$SkipHealthCheck
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"
$targetDir = Join-Path $backendDir "target"
$pidPath = Join-Path $targetDir "backend-dev.pid"
New-Item -ItemType Directory -Force -Path $targetDir | Out-Null

if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    $DbPassword = $env:DB_PASSWORD
}
if ([string]::IsNullOrWhiteSpace($DbUrl)) {
    $DbUrl = $env:DB_URL
}
if ([string]::IsNullOrWhiteSpace($DbUsername)) {
    $DbUsername = $env:DB_USERNAME
}
if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    throw "DB_PASSWORD is required. Set `$env:DB_PASSWORD or pass -DbPassword."
}

function Test-ProcessId([int]$ProcessId) {
    return $null -ne (Get-Process -Id $ProcessId -ErrorAction SilentlyContinue)
}

function Get-PortOwner([int]$LocalPort) {
    return Get-NetTCPConnection -LocalPort $LocalPort -ErrorAction SilentlyContinue |
        Where-Object { $_.State -eq "Listen" } |
        Select-Object -First 1
}

if (Test-Path -LiteralPath $pidPath) {
    $existingPidText = (Get-Content -LiteralPath $pidPath -Raw).Trim()
    $existingPid = 0
    if ([int]::TryParse($existingPidText, [ref]$existingPid) -and (Test-ProcessId $existingPid)) {
        if (-not $Force) {
            Write-Host "Backend dev process is already running. pid=$existingPid"
            Write-Host "Health: http://127.0.0.1:$Port/api/health"
            exit 0
        }
        & (Join-Path $PSScriptRoot "stop-backend-dev.ps1")
    }
}

$portOwner = Get-PortOwner $Port
if ($null -ne $portOwner) {
    if (-not $Force) {
        throw "Port $Port is already in use by pid $($portOwner.OwningProcess). Stop it or rerun with -Force."
    }
    Write-Host "Port $Port is already in use by pid $($portOwner.OwningProcess); -Force will not stop unmanaged processes."
    throw "Cannot safely start because port $Port is occupied by an unmanaged process."
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$stdout = Join-Path $targetDir "backend-dev-$timestamp.out.log"
$stderr = Join-Path $targetDir "backend-dev-$timestamp.err.log"

$env:DB_PASSWORD = $DbPassword
if (-not [string]::IsNullOrWhiteSpace($DbUrl)) {
    $env:DB_URL = $DbUrl
}
if (-not [string]::IsNullOrWhiteSpace($DbUsername)) {
    $env:DB_USERNAME = $DbUsername
}
$env:SERVER_PORT = "" + $Port

$mvn = Get-Command "mvn" -ErrorAction Stop
$process = Start-Process `
    -FilePath $mvn.Source `
    -ArgumentList @("spring-boot:run") `
    -WorkingDirectory $backendDir `
    -WindowStyle Hidden `
    -RedirectStandardOutput $stdout `
    -RedirectStandardError $stderr `
    -PassThru

Set-Content -LiteralPath $pidPath -Value ("" + $process.Id) -Encoding ASCII

Write-Host "Started backend dev process. pid=$($process.Id)"
Write-Host "stdout=$stdout"
Write-Host "stderr=$stderr"

if ($SkipHealthCheck) {
    exit 0
}

$healthUrl = "http://127.0.0.1:$Port/api/health"
$deadline = (Get-Date).AddSeconds($TimeoutSec)
while ((Get-Date) -lt $deadline) {
    if ($process.HasExited) {
        $tail = if (Test-Path -LiteralPath $stderr) {
            (Get-Content -LiteralPath $stderr -Tail 40) -join "`n"
        } else {
            ""
        }
        Remove-Item -LiteralPath $pidPath -Force -ErrorAction SilentlyContinue
        throw "Backend exited before health check passed. ExitCode=$($process.ExitCode). stderr tail:`n$tail"
    }
    try {
        $response = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 3 -ErrorAction Stop
        Write-Host "Backend health check passed: $healthUrl"
        if ($null -ne $response) {
            $response | ConvertTo-Json -Depth 10 -Compress | Write-Host
        }
        exit 0
    } catch {
        Start-Sleep -Seconds 2
    }
    $process.Refresh()
}

throw "Backend did not become healthy within ${TimeoutSec}s. See logs: $stdout $stderr"
