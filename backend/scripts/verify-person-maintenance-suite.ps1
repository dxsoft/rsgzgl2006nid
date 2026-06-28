param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [switch]$IncludeCacheClosure,
    [switch]$IncludeCaseCreate,
    [string]$OutputPath = "target/person-maintenance-suite-results.tsv"
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$outputDir = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

function Invoke-Step {
    param(
        [string]$Name,
        [scriptblock]$Action
    )

    $sw = [Diagnostics.Stopwatch]::StartNew()
    try {
        & $Action
        $sw.Stop()
        return [pscustomobject]@{
            Name = $Name
            Status = "PASS"
            Milliseconds = $sw.ElapsedMilliseconds
            Message = ""
        }
    } catch {
        $sw.Stop()
        return [pscustomobject]@{
            Name = $Name
            Status = "FAIL"
            Milliseconds = $sw.ElapsedMilliseconds
            Message = $_.Exception.Message
        }
    }
}

$rows = New-Object System.Collections.Generic.List[object]

if ($IncludeCaseCreate) {
    $IncludeCacheClosure = $true
}

$rows.Add((Invoke-Step -Name "person-maintenance-ui-contract" -Action {
    & (Join-Path $scriptDir "verify-person-maintenance-ui-contract.ps1") `
        -OutputPath "target/person-maintenance-ui-contract.tsv" | Out-Host
}))

$rows.Add((Invoke-Step -Name "person-code-options" -Action {
    & (Join-Path $scriptDir "verify-person-code-options.ps1") `
        -BaseUrl $BaseUrl `
        -TimeoutSec $TimeoutSec `
        -Username $Username `
        -Password $Password `
        -OutputPath "target/person-code-options-results.tsv" | Out-Host
}))

if ($IncludeCacheClosure) {
    $rows.Add((Invoke-Step -Name "person-maintenance-cache-closure" -Action {
        $cacheClosureArgs = @{
            BaseUrl = $BaseUrl
            TimeoutSec = $TimeoutSec
            Username = $Username
            Password = $Password
            OutputPath = "target/person-maintenance-cache-closure-results.tsv"
        }
        if ($IncludeCaseCreate) {
            $cacheClosureArgs.IncludeCaseCreate = $true
        }
        & (Join-Path $scriptDir "verify-person-maintenance-cache-closure.ps1") @cacheClosureArgs | Out-Host
    }))
}

$rows | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8
$rows | Format-Table -AutoSize

$failed = @($rows | Where-Object { $_.Status -ne "PASS" })
if ($failed.Count -gt 0) {
    throw "Person maintenance suite failed: $($failed.Count) step(s). See $OutputPath"
}

Write-Host "Person maintenance suite passed. Report: $OutputPath"
