param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 20,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [switch]$FailOnUnexpected
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$verifyScript = Join-Path $scriptDir "verify-cross-type-samples.ps1"
$samplePath = Join-Path $scriptDir "business-acceptance-samples.tsv"
$outputPath = "target/business-acceptance-results.tsv"

& $verifyScript `
    -SamplePath $samplePath `
    -OutputPath $outputPath `
    -BaseUrl $BaseUrl `
    -TimeoutSec $TimeoutSec `
    -Username $Username `
    -Password $Password

$sampleRows = Get-Content $samplePath | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | ForEach-Object {
    $parts = $_ -split "`t"
    [pscustomobject]@{
        PersonCode = $parts[0]
        OrgCode = $parts[1]
        Year = $parts[2]
        Month = $parts[3]
        TargetTotal = [decimal]$parts[5]
    }
}

$targetByKey = @{}
foreach ($sample in $sampleRows) {
    $targetByKey[$sample.PersonCode + "|" + $sample.Year + "|" + $sample.Month] = $sample.TargetTotal
}

$results = Import-Csv $outputPath -Delimiter "`t"
$unexpected = @($results | Where-Object {
    $key = $_.personCode + "|" + $_.year + "|" + $_.month
    $target = $targetByKey[$key]
    $_.status -ne "MATCH" -or [decimal]$_.actual -ne $target
})

Write-Host ""
Write-Host ("Business acceptance unexpected rows: {0}" -f $unexpected.Count)
if ($unexpected.Count -gt 0) {
    $unexpected | Select-Object personCode, orgCode, year, month, jslbEsc, status, expected, actual, diff, message | Format-Table -AutoSize
}

if ($FailOnUnexpected -and $unexpected.Count -gt 0) {
    throw "Business acceptance sample verification found unexpected rows."
}
