param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 20,
    [switch]$FailOnUnexpected
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$verifyScript = Join-Path $scriptDir "verify-cross-type-samples.ps1"

Write-Host "Verifying cross-type samples..."
& $verifyScript `
    -SamplePath "target/cross-type-samples.tsv" `
    -OutputPath "target/cross-type-results.tsv" `
    -BaseUrl $BaseUrl `
    -TimeoutSec $TimeoutSec

Write-Host ""
Write-Host "Verifying normal grade expanded samples..."
& $verifyScript `
    -SamplePath "target/normal-grade-expanded-samples.tsv" `
    -OutputPath "target/normal-grade-expanded-results.tsv" `
    -BaseUrl $BaseUrl `
    -TimeoutSec $TimeoutSec `
    -IgnoreSampleChangeType

if (Test-Path "target/target-state-samples.tsv") {
    Write-Host ""
    Write-Host "Verifying target-state adjustment samples..."
    & $verifyScript `
        -SamplePath "target/target-state-samples.tsv" `
        -OutputPath "target/target-state-results.tsv" `
        -BaseUrl $BaseUrl `
        -TimeoutSec $TimeoutSec
}

if (Test-Path "target/rank-judicial-samples.tsv") {
    Write-Host ""
    Write-Host "Verifying rank and judicial samples..."
    & $verifyScript `
        -SamplePath "target/rank-judicial-samples.tsv" `
        -OutputPath "target/rank-judicial-results.tsv" `
        -BaseUrl $BaseUrl `
        -TimeoutSec $TimeoutSec
}

if (Test-Path "target/core-flow-samples.tsv") {
    Write-Host ""
    Write-Host "Verifying core flow samples..."
    & $verifyScript `
        -SamplePath "target/core-flow-samples.tsv" `
        -OutputPath "target/core-flow-results.tsv" `
        -BaseUrl $BaseUrl `
        -TimeoutSec $TimeoutSec
}

if (Test-Path "target/special-flow-samples.tsv") {
    Write-Host ""
    Write-Host "Verifying special flow samples..."
    & $verifyScript `
        -SamplePath "target/special-flow-samples.tsv" `
        -OutputPath "target/special-flow-results.tsv" `
        -BaseUrl $BaseUrl `
        -TimeoutSec $TimeoutSec
}

if (Test-Path (Join-Path $scriptDir "business-acceptance-samples.tsv")) {
    Write-Host ""
    Write-Host "Verifying business acceptance samples..."
    & $verifyScript `
        -SamplePath (Join-Path $scriptDir "business-acceptance-samples.tsv") `
        -OutputPath "target/business-acceptance-results.tsv" `
        -BaseUrl $BaseUrl `
        -TimeoutSec $TimeoutSec
}

$knownIssuePath = Join-Path $scriptDir "known-sample-issues.tsv"
$knownIssues = @()
if (Test-Path $knownIssuePath) {
    $knownIssues = Import-Csv $knownIssuePath -Delimiter "`t"
}
$allowedCrossTypeIssues = @($knownIssues |
    Where-Object { $_.sampleSet -eq "cross-type" } |
    ForEach-Object { $_.personCode + "|" + $_.status })
$allowedNormalGradeIssues = @($knownIssues |
    Where-Object { $_.sampleSet -eq "normal-grade" } |
    ForEach-Object { $_.personCode + "|" + $_.status })

$crossTypeUnexpected = Import-Csv "target/cross-type-results.tsv" -Delimiter "`t" |
    Where-Object { $_.status -ne "MATCH" -and $allowedCrossTypeIssues -notcontains ($_.personCode + "|" + $_.status) }
$normalGradeUnexpected = Import-Csv "target/normal-grade-expanded-results.tsv" -Delimiter "`t" |
    Where-Object { $_.status -ne "MATCH" -and $allowedNormalGradeIssues -notcontains ($_.personCode + "|" + $_.status) }
$targetStateUnexpected = @()
if (Test-Path "target/target-state-results.tsv") {
    $targetStateUnexpected = Import-Csv "target/target-state-results.tsv" -Delimiter "`t" |
        Where-Object { $_.status -ne "MATCH" }
}
$rankJudicialUnexpected = @()
if (Test-Path "target/rank-judicial-results.tsv") {
    $rankJudicialUnexpected = Import-Csv "target/rank-judicial-results.tsv" -Delimiter "`t" |
        Where-Object { $_.status -ne "MATCH" }
}
$coreFlowUnexpected = @()
if (Test-Path "target/core-flow-results.tsv") {
    $coreFlowUnexpected = Import-Csv "target/core-flow-results.tsv" -Delimiter "`t" |
        Where-Object { $_.status -ne "MATCH" }
}
$specialFlowUnexpected = @()
if (Test-Path "target/special-flow-results.tsv") {
    $specialFlowUnexpected = Import-Csv "target/special-flow-results.tsv" -Delimiter "`t" |
        Where-Object { $_.status -ne "MATCH" }
}
$businessAcceptanceUnexpected = @()
if (Test-Path "target/business-acceptance-results.tsv") {
    $businessSampleRows = Get-Content (Join-Path $scriptDir "business-acceptance-samples.tsv") |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
        ForEach-Object {
            $parts = $_ -split "`t"
            [pscustomobject]@{
                PersonCode = $parts[0]
                Year = $parts[2]
                Month = $parts[3]
                TargetTotal = [decimal]$parts[5]
            }
        }
    $businessTargetByKey = @{}
    foreach ($sample in $businessSampleRows) {
        $businessTargetByKey[$sample.PersonCode + "|" + $sample.Year + "|" + $sample.Month] = $sample.TargetTotal
    }
    $businessAcceptanceUnexpected = Import-Csv "target/business-acceptance-results.tsv" -Delimiter "`t" |
        Where-Object {
            $key = $_.personCode + "|" + $_.year + "|" + $_.month
            $target = $businessTargetByKey[$key]
            $_.status -ne "MATCH" -or [decimal]$_.actual -ne $target
        }
}

Write-Host ""
Write-Host "Verification audit:"
Write-Host ("Cross-type unexpected non-match: {0}" -f @($crossTypeUnexpected).Count)
Write-Host ("Normal-grade unexpected non-match: {0}" -f @($normalGradeUnexpected).Count)
Write-Host ("Target-state unexpected non-match: {0}" -f @($targetStateUnexpected).Count)
Write-Host ("Rank/judicial unexpected non-match: {0}" -f @($rankJudicialUnexpected).Count)
Write-Host ("Core-flow unexpected non-match: {0}" -f @($coreFlowUnexpected).Count)
Write-Host ("Special-flow unexpected non-match: {0}" -f @($specialFlowUnexpected).Count)
Write-Host ("Business acceptance unexpected non-match/amount: {0}" -f @($businessAcceptanceUnexpected).Count)

if ($FailOnUnexpected -and (@($crossTypeUnexpected).Count -gt 0 -or @($normalGradeUnexpected).Count -gt 0 -or @($targetStateUnexpected).Count -gt 0 -or @($rankJudicialUnexpected).Count -gt 0 -or @($coreFlowUnexpected).Count -gt 0 -or @($specialFlowUnexpected).Count -gt 0 -or @($businessAcceptanceUnexpected).Count -gt 0)) {
    Write-Host ""
    Write-Host "Unexpected cross-type rows:"
    $crossTypeUnexpected | Select-Object personCode, orgCode, year, month, jslbEsc, status, expected, actual, diff, message | Format-Table -AutoSize
    Write-Host "Unexpected normal-grade rows:"
    $normalGradeUnexpected | Select-Object personCode, orgCode, year, month, jslbEsc, status, expected, actual, diff, message | Format-Table -AutoSize
    Write-Host "Unexpected target-state rows:"
    $targetStateUnexpected | Select-Object personCode, orgCode, year, month, jslbEsc, status, expected, actual, diff, message | Format-Table -AutoSize
    Write-Host "Unexpected rank/judicial rows:"
    $rankJudicialUnexpected | Select-Object personCode, orgCode, year, month, jslbEsc, status, expected, actual, diff, message | Format-Table -AutoSize
    Write-Host "Unexpected core-flow rows:"
    $coreFlowUnexpected | Select-Object personCode, orgCode, year, month, jslbEsc, status, expected, actual, diff, message | Format-Table -AutoSize
    Write-Host "Unexpected special-flow rows:"
    $specialFlowUnexpected | Select-Object personCode, orgCode, year, month, jslbEsc, status, expected, actual, diff, message | Format-Table -AutoSize
    Write-Host "Unexpected business acceptance rows:"
    $businessAcceptanceUnexpected | Select-Object personCode, orgCode, year, month, jslbEsc, status, expected, actual, diff, message | Format-Table -AutoSize
    throw "Salary sample verification found unexpected non-matching rows."
}
