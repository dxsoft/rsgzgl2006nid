param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [string]$OrgCode = "001",
    [int]$Year = 2026,
    [int]$Month = 6,
    [switch]$FailOnUnexpected,
    [string]$OutputPath = "target/report-csv-export-results.tsv"
)

$ErrorActionPreference = "Stop"

$outputDir = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

$webSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
if (-not [string]::IsNullOrWhiteSpace($Username)) {
    $loginBody = @{ username = $Username; password = $Password } | ConvertTo-Json -Compress
    try {
        Invoke-RestMethod `
            -Uri "$BaseUrl/api/auth/login" `
            -Method Post `
            -Body $loginBody `
            -ContentType "application/json; charset=utf-8" `
            -WebSession $webSession `
            -TimeoutSec $TimeoutSec `
            -ErrorAction Stop | Out-Null
    } catch {
        throw "Login failed for $BaseUrl. Ensure the backend is running and credentials are valid. $($_.Exception.Message)"
    }
}

function Add-Result(
    [System.Collections.Generic.List[object]]$Rows,
    [string]$Code,
    [string]$Status,
    [string]$Message
) {
    $Rows.Add([pscustomobject]@{
        Code = $Code
        Status = $Status
        Message = $Message
    })
}

function First-Csv-Line([string]$Content) {
    return (("" + $Content).TrimStart([char]0xfeff) -split "`r?`n" | Select-Object -First 1)
}

function U([int[]]$Codes) {
    return -join ($Codes | ForEach-Object { [char]$_ })
}

function Test-Csv(
    [System.Collections.Generic.List[object]]$Rows,
    [string]$Code,
    [string]$Path,
    [string]$ExpectedHeadPart
) {
    try {
        $response = Invoke-WebRequest `
            -Uri "$BaseUrl$Path" `
            -Method Get `
            -WebSession $webSession `
            -TimeoutSec $TimeoutSec `
            -UseBasicParsing
        $head = First-Csv-Line $response.Content
        if ($response.StatusCode -eq 200 -and $head.Contains($ExpectedHeadPart)) {
            Add-Result $Rows $Code "OK" "Length=$($response.RawContentLength)"
        } else {
            Add-Result $Rows $Code "FAIL" "Header=$head"
        }
    } catch {
        Add-Result $Rows $Code "REQUEST_ERROR" $_.Exception.Message
    }
}

function Invoke-JsonUtf8([string]$Path) {
    $response = Invoke-WebRequest `
        -Uri "$BaseUrl$Path" `
        -Method Get `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec `
        -UseBasicParsing
    $stream = $response.RawContentStream
    $stream.Position = 0
    $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
    $json = $reader.ReadToEnd()
    return $json | ConvertFrom-Json
}

function Test-ReportCatalogContract([System.Collections.Generic.List[object]]$Rows) {
    try {
        $catalog = Invoke-JsonUtf8 "/api/reports/catalog"
        $items = @($catalog.data)
        $migrated = @($items | Where-Object { ("" + $_.migrationStatus) -eq (U @(0x5df2, 0x8fc1, 0x79fb)) })
        $pending = @($items | Where-Object { ("" + $_.migrationStatus) -eq (U @(0x5f85, 0x8fc1, 0x79fb)) })
        $missingUrl = @($migrated | Where-Object { [string]::IsNullOrWhiteSpace("" + $_.printUrl) })
        if ($items.Count -le 0) {
            Add-Result $Rows "report-catalog-contract" "FAIL" "Catalog is empty"
        } elseif ($migrated.Count -lt 7) {
            Add-Result $Rows "report-catalog-contract" "FAIL" "Migrated rows too few: $($migrated.Count)"
        } elseif ($pending.Count -le 0) {
            Add-Result $Rows "report-catalog-contract" "FAIL" "Pending legacy rows not visible"
        } elseif ($missingUrl.Count -gt 0) {
            Add-Result $Rows "report-catalog-contract" "FAIL" "Migrated rows missing printUrl: $($missingUrl.code -join ',')"
        } else {
            Add-Result $Rows "report-catalog-contract" "OK" "Total=$($items.Count), Migrated=$($migrated.Count), Pending=$($pending.Count)"
        }
    } catch {
        Add-Result $Rows "report-catalog-contract" "REQUEST_ERROR" $_.Exception.Message
    }
}

$results = [System.Collections.Generic.List[object]]::new()

Test-Csv $results "report-catalog" "/api/reports/catalog.csv" (U @(0x62a5, 0x8868, 0x7f16, 0x7801))
Test-ReportCatalogContract $results
Test-Csv $results "salary-history" "/api/reports/salary-history.csv?orgCode=$OrgCode&yearFrom=2006&yearTo=$Year&limit=5" (U @(0x4eba, 0x5458, 0x7f16, 0x7801))
Test-Csv $results "salary-roster" "/api/reports/salary-roster.csv?orgCode=$OrgCode&year=$Year&month=$Month&limit=5" (U @(0x5e8f, 0x53f7))
Test-Csv $results "salary-roster-dynamic" "/api/reports/salary-roster.csv?orgCode=$OrgCode&year=$Year&month=$Month&columns=ZWGZSE2,JBGZSE2,JCGZ2,JXGZ&limit=5" (U @(0x5e8f, 0x53f7))
Test-Csv $results "person-roster" "/api/reports/person-roster.csv?orgCode=$OrgCode&year=$Year&month=$Month&limit=5" (U @(0x8eab, 0x4efd, 0x8bc1, 0x53f7))
Test-Csv $results "salary-change-ledger" "/api/reports/salary-change-ledger.csv?orgCode=$OrgCode&year=$Year&month=$Month&limit=5" (U @(0x529e, 0x7406, 0x7f16, 0x53f7))
Test-Csv $results "salary-case-approval-roster" "/api/reports/salary-case-approval-roster.csv?orgCode=$OrgCode&year=$Year&month=$Month&limit=5" (U @(0x5199, 0x5165, 0x8ba1, 0x5212))
Test-Csv $results "report-print-archive" "/api/reports/print-archive.csv?printStatus=ALL&limit=5" "caseNo"

$results | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8

Write-Host ""
Write-Host "Report CSV export summary:"
$results | Format-Table Code,Status,Message -AutoSize
Write-Host ""
Write-Host "Wrote $OutputPath"

$unexpected = @($results | Where-Object { $_.Status -in @("FAIL", "REQUEST_ERROR") })
if ($FailOnUnexpected -and $unexpected.Count -gt 0) {
    throw "Report CSV export verification found unexpected rows."
}
