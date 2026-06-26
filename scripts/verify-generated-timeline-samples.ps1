param(
    [string[]]$SamplePath = @(
        "target/cross-type-samples.tsv",
        "target/normal-grade-expanded-samples.tsv",
        "target/core-flow-samples.tsv",
        "target/special-flow-samples.tsv",
        "target/rank-judicial-samples.tsv",
        "target/target-state-samples.tsv"
    ),
    [string]$OutputPath = "target/generated-timeline-results.tsv",
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$Limit = 200,
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [switch]$FailOnUnexpected
)

$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"
Push-Location $backendDir
try {
    $scriptArgs = @{
        SamplePath = $SamplePath
        OutputPath = $OutputPath
        BaseUrl = $BaseUrl
        Limit = $Limit
        TimeoutSec = $TimeoutSec
        Username = $Username
        Password = $Password
    }
    if ($FailOnUnexpected) {
        $scriptArgs.FailOnUnexpected = $true
    }
    & (Join-Path $backendDir "scripts\verify-generated-timeline-samples.ps1") @scriptArgs
} finally {
    Pop-Location
}
