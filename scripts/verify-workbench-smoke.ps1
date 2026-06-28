param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [int]$MaxSummaryMilliseconds = 5000,
    [int]$MaxTodoMilliseconds = 5000,
    [int]$MaxDoneMilliseconds = 8000,
    [int]$MaxHistoryMilliseconds = 5000,
    [string]$Username = "admin",
    [string]$Password = "admin"
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"
Push-Location $backendDir
try {
    & (Join-Path $backendDir "scripts\verify-workbench-smoke.ps1") `
        -BaseUrl $BaseUrl `
        -TimeoutSec $TimeoutSec `
        -MaxSummaryMilliseconds $MaxSummaryMilliseconds `
        -MaxTodoMilliseconds $MaxTodoMilliseconds `
        -MaxDoneMilliseconds $MaxDoneMilliseconds `
        -MaxHistoryMilliseconds $MaxHistoryMilliseconds `
        -Username $Username `
        -Password $Password `
        -OutputPath "target/workbench-smoke-results.tsv"
} finally {
    Pop-Location
}
