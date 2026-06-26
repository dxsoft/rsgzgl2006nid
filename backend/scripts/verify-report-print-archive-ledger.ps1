param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [int]$Limit = 10,
    [switch]$FailOnUnexpected,
    [string]$OutputPath = "target/report-print-archive-ledger-results.tsv"
)

$ErrorActionPreference = "Stop"

$outputDir = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

$webSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
if (-not [string]::IsNullOrWhiteSpace($Username)) {
    $loginBody = @{ username = $Username; password = $Password } | ConvertTo-Json -Compress
    Invoke-RestMethod `
        -Uri "$BaseUrl/api/auth/login" `
        -Method Post `
        -Body $loginBody `
        -ContentType "application/json; charset=utf-8" `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec | Out-Null
}

function Add-Result(
    [System.Collections.Generic.List[object]]$Rows,
    [string]$Gate,
    [string]$Status,
    [string]$Message
) {
    $Rows.Add([pscustomobject]@{
        Gate = $Gate
        Status = $Status
        Message = $Message
    })
}

function Get-ArchiveLedger([string]$Status) {
    return Invoke-RestMethod `
        -Uri "$BaseUrl/api/reports/print-archive?printStatus=$Status&limit=$Limit" `
        -Method Get `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec
}

$results = [System.Collections.Generic.List[object]]::new()

try {
    $all = (Get-ArchiveLedger "ALL").data
    $requiredFields = @("caseNo", "personCode", "orgCode", "archiveStatus", "printed", "printCount", "writeReady")
    $first = @($all.items | Select-Object -First 1)
    if ($first.Count -eq 0) {
        Add-Result $results "ledger-json-contract" "SKIP" "No salary case rows returned."
    } else {
        $missing = @($requiredFields | Where-Object { -not ($first[0].PSObject.Properties.Name -contains $_) })
        if ($missing.Count -eq 0) {
            Add-Result $results "ledger-json-contract" "OK" "Rows=$(@($all.items).Count), Printed=$($all.printed), Unprinted=$($all.unprinted)"
        } else {
            Add-Result $results "ledger-json-contract" "FAIL" "Missing fields: $($missing -join ',')"
        }
    }

    $printed = (Get-ArchiveLedger "PRINTED").data
    $printedBad = @($printed.items | Where-Object { $_.printed -ne $true }).Count
    if ($printedBad -eq 0) {
        Add-Result $results "ledger-printed-filter" "OK" "Rows=$(@($printed.items).Count)"
    } else {
        Add-Result $results "ledger-printed-filter" "FAIL" "Found $printedBad non-printed rows in PRINTED filter."
    }

    $unprinted = (Get-ArchiveLedger "UNPRINTED").data
    $unprintedBad = @($unprinted.items | Where-Object { $_.printed -eq $true }).Count
    if ($unprintedBad -eq 0) {
        Add-Result $results "ledger-unprinted-filter" "OK" "Rows=$(@($unprinted.items).Count)"
    } else {
        Add-Result $results "ledger-unprinted-filter" "FAIL" "Found $unprintedBad printed rows in UNPRINTED filter."
    }

    $csv = Invoke-WebRequest `
        -Uri "$BaseUrl/api/reports/print-archive.csv?printStatus=ALL&limit=$Limit" `
        -Method Get `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec `
        -UseBasicParsing
    $head = ($csv.Content -split "`n" | Select-Object -First 1)
    if ($csv.StatusCode -eq 200 -and $head -like '*"caseNo","personCode","personName","orgCode"*') {
        Add-Result $results "ledger-csv-contract" "OK" "Length=$($csv.RawContentLength)"
    } else {
        Add-Result $results "ledger-csv-contract" "FAIL" "Unexpected CSV header: $head"
    }
} catch {
    Add-Result $results "ledger-request" "REQUEST_ERROR" $_.Exception.Message
}

$results | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8

Write-Host ""
Write-Host "Report print archive ledger summary:"
$results | Format-Table Gate,Status,Message -AutoSize
Write-Host ""
Write-Host "Wrote $OutputPath"

$unexpected = @($results | Where-Object { $_.Status -in @("FAIL", "REQUEST_ERROR") })
if ($FailOnUnexpected -and $unexpected.Count -gt 0) {
    throw "Report print archive ledger verification found unexpected rows."
}
