param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [string]$OrgCode = "001",
    [int]$Year = 2026,
    [int]$Month = 6,
    [int]$AssessmentYear = 2025,
    [switch]$FailOnUnexpected,
    [string]$OutputPath = "target/report-print-page-results.tsv"
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

function Invoke-PrintPage([string]$Path) {
    return Invoke-WebRequest `
        -Uri "$BaseUrl$Path" `
        -Method Get `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec `
        -UseBasicParsing
}

function Test-Page(
    [System.Collections.Generic.List[object]]$Rows,
    [string]$Code,
    [string]$Path,
    [string]$TitleMarker,
    [string]$ContainerMarker = "print-sheet",
    [switch]$RequirePrintSheet
) {
    try {
        $response = Invoke-PrintPage $Path
        $content = "" + $response.Content
        $issues = [System.Collections.Generic.List[string]]::new()
        if ($response.StatusCode -ne 200) {
            $issues.Add("HTTP $($response.StatusCode)")
        }
        if (-not $content.Contains($TitleMarker)) {
            $issues.Add("missing title marker")
        }
        if ($RequirePrintSheet -and -not $content.Contains($ContainerMarker)) {
            $issues.Add("missing $ContainerMarker")
        }
        if (-not $content.Contains("</body></html>")) {
            $issues.Add("missing html close")
        }
        if ($issues.Count -eq 0) {
            Add-Result $Rows $Code "OK" "Length=$($content.Length)"
        } else {
            Add-Result $Rows $Code "FAIL" ($issues -join "; ")
        }
    } catch {
        Add-Result $Rows $Code "REQUEST_ERROR" $_.Exception.Message
    }
}

function Get-PrintableCaseNo() {
    try {
        $itemsResponse = Invoke-RestMethod `
            -Uri "$BaseUrl/api/workbench/items?status=DONE&limit=120" `
            -Method Get `
            -WebSession $webSession `
            -TimeoutSec $TimeoutSec
        foreach ($item in @($itemsResponse.data.items)) {
            $caseNo = "" + $item.id
            if (-not $caseNo.StartsWith("GZ-")) {
                continue
            }
            try {
                $encoded = [uri]::EscapeDataString($caseNo)
                $validation = Invoke-RestMethod `
                    -Uri "$BaseUrl/api/reports/salary-case-approval/validate?caseNo=$encoded" `
                    -Method Get `
                    -WebSession $webSession `
                    -TimeoutSec $TimeoutSec
                if ($validation.data.printable -eq $true) {
                    return $caseNo
                }
            } catch {
            }
        }
    } catch {
    }
    return ""
}

function Resolve-CatalogPrintUrl([string]$PrintUrl, [string]$CaseNo) {
    $path = "" + $PrintUrl
    if ([string]::IsNullOrWhiteSpace($path)) {
        return ""
    }
    if ($path.Contains("{caseNo}")) {
        if ([string]::IsNullOrWhiteSpace($CaseNo)) {
            return ""
        }
        $path = $path.Replace("{caseNo}", [uri]::EscapeDataString($CaseNo))
    }
    $path = $path.Replace("{orgCode}", [uri]::EscapeDataString($OrgCode))
    $path = $path.Replace("{year}", [string]$Year)
    $path = $path.Replace("{month}", [string]$Month)
    if ($path.Contains("/assessment-summary/print") -and -not $path.Contains("year=")) {
        $join = if ($path.Contains("?")) { "&" } else { "?" }
        $path = $path + $join + "year=$AssessmentYear"
    }
    if (-not $path.Contains("limit=")) {
        $join = if ($path.Contains("?")) { "&" } else { "?" }
        $path = $path + $join + "limit=5"
    }
    return $path
}

function Test-CatalogMigratedLinks(
    [System.Collections.Generic.List[object]]$Rows,
    [string]$CaseNo
) {
    try {
        $catalog = Invoke-RestMethod `
            -Uri "$BaseUrl/api/reports/catalog" `
            -Method Get `
            -WebSession $webSession `
            -TimeoutSec $TimeoutSec
        $items = @($catalog.data | Where-Object { -not [string]::IsNullOrWhiteSpace("" + $_.printUrl) })
        $checked = 0
        $skipped = 0
        $issues = [System.Collections.Generic.List[string]]::new()
        foreach ($item in $items) {
            $path = Resolve-CatalogPrintUrl ("" + $item.printUrl) $CaseNo
            if ([string]::IsNullOrWhiteSpace($path)) {
                $skipped += 1
                continue
            }
            try {
                $response = Invoke-PrintPage $path
                if ($response.StatusCode -ne 200) {
                    $issues.Add(("" + $item.code) + ": HTTP " + $response.StatusCode)
                }
                $checked += 1
            } catch {
                $issues.Add(("" + $item.code) + ": " + $_.Exception.Message)
            }
        }
        if ($checked -lt 6) {
            $issues.Add("checked migrated links too few: $checked")
        }
        if ($issues.Count -eq 0) {
            Add-Result $Rows "report-catalog-migrated-links" "OK" "Checked=$checked, Skipped=$skipped"
        } else {
            Add-Result $Rows "report-catalog-migrated-links" "FAIL" ($issues -join "; ")
        }
    } catch {
        Add-Result $Rows "report-catalog-migrated-links" "REQUEST_ERROR" $_.Exception.Message
    }
}

$results = [System.Collections.Generic.List[object]]::new()

$caseNo = Get-PrintableCaseNo
if ([string]::IsNullOrWhiteSpace($caseNo)) {
    Add-Result $results "salary-case-approval" "SKIP" "No printable salary case found."
} else {
    Test-Page `
        -Rows $results `
        -Code "salary-case-approval" `
        -Path ("/api/reports/salary-case-approval/print?caseNo=" + [uri]::EscapeDataString($caseNo)) `
        -TitleMarker "approval-real-title" `
        -ContainerMarker "approval-doc" `
        -RequirePrintSheet
}

Test-Page $results "salary-case-approval-roster" "/api/reports/salary-case-approval-roster/print?orgCode=$OrgCode&year=$Year&month=$Month&limit=5" "&#24037;&#36164;&#23457;&#25209;&#28165;&#20876;" -RequirePrintSheet
Test-Page $results "salary-roster" "/api/reports/salary-roster/print?orgCode=$OrgCode&year=$Year&month=$Month&columns=ZWGZSE2,JBGZSE2,JCGZ2,JXGZ&limit=5" "&#24037;&#36164;&#34920;" -RequirePrintSheet
Test-Page $results "person-roster" "/api/reports/person-roster/print?orgCode=$OrgCode&year=$Year&month=$Month&limit=5" "&#20154;&#21592;&#24037;&#36164;&#33457;&#21517;&#20876;" -RequirePrintSheet
Test-Page $results "salary-history" "/api/reports/salary-history/print?orgCode=$OrgCode&yearFrom=2006&yearTo=$Year&limit=5" "&#24037;&#36164;&#21382;&#21490;&#21464;&#21160;&#26126;&#32454;" -RequirePrintSheet
Test-Page $results "salary-change-ledger" "/api/reports/salary-change-ledger/print?orgCode=$OrgCode&year=$Year&month=$Month&limit=5" "&#24037;&#36164;&#21464;&#21160;&#31649;&#29702;&#21488;&#36134;" -RequirePrintSheet
Test-Page $results "assessment-summary" "/api/reports/assessment-summary/print?orgCode=$OrgCode&year=$AssessmentYear&limit=5" "&#24180;&#24230;&#32771;&#26680;&#32479;&#35745;&#34920;" -RequirePrintSheet
Test-Page $results "standard-table" "/api/reports/standard-tables/print?tableName=bz06_jbt&limit=5" "&#24037;&#36164;&#26631;&#20934;&#34920;" -RequirePrintSheet
Test-CatalogMigratedLinks $results $caseNo

$results | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8

Write-Host ""
Write-Host "Report print page summary:"
$results | Format-Table Code,Status,Message -AutoSize
Write-Host ""
Write-Host "Wrote $OutputPath"

$unexpected = @($results | Where-Object { $_.Status -in @("FAIL", "REQUEST_ERROR") })
if ($FailOnUnexpected -and $unexpected.Count -gt 0) {
    throw "Report print page verification found unexpected rows."
}
