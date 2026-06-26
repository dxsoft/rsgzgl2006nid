param(
    [string[]]$SamplePath = @(
        "target/cross-type-samples.tsv",
        "target/normal-grade-expanded-samples.tsv",
        "target/core-flow-samples.tsv",
        "target/special-flow-samples.tsv",
        "target/rank-judicial-samples.tsv",
        "target/target-state-samples.tsv"
    ),
    [string[]]$PersonCode = @(),
    [string]$OutputPath = "target/generated-timeline-coverage.tsv",
    [string]$SummaryPath = "target/generated-timeline-coverage-summary.tsv",
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [string]$Username = "admin",
    [string]$Password = "admin",
    [switch]$SkipLogin,
    [int]$Limit = 200,
    [int]$TimeoutSec = 30
)

$ErrorActionPreference = "Stop"

function Escape-Unicode([string]$Value) {
    if ($null -eq $Value) {
        return ""
    }
    $builder = [System.Text.StringBuilder]::new()
    foreach ($ch in $Value.ToCharArray()) {
        $code = [int][char]$ch
        if ($code -lt 128) {
            [void]$builder.Append($ch)
        } else {
            [void]$builder.Append(("\u{0:x4}" -f $code))
        }
    }
    return $builder.ToString()
}

function Error-Message([object]$ErrorRecord) {
    $message = $ErrorRecord.Exception.Message
    $response = $ErrorRecord.Exception.Response
    if ($null -eq $response) {
        return $message
    }
    try {
        $stream = $response.GetResponseStream()
        if ($null -eq $stream) {
            return $message
        }
        $reader = [System.IO.StreamReader]::new($stream)
        $body = $reader.ReadToEnd()
        if ([string]::IsNullOrWhiteSpace($body)) {
            return $message
        }
        return "$message $body"
    } catch {
        return $message
    }
}

function Ensure-OutputDirectory([string]$Path) {
    $outputDir = Split-Path -Parent $Path
    if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path $outputDir)) {
        New-Item -ItemType Directory -Path $outputDir | Out-Null
    }
}

function Coverage-Status([int]$ExpectedCount, [int]$MatchedHistoryCount, [int]$MissingHistoryCount, [int]$UnsupportedHistoryCount) {
    if ($MissingHistoryCount -gt 0 -or $MatchedHistoryCount -lt $ExpectedCount) {
        return "MISSING_HISTORY"
    }
    if ($UnsupportedHistoryCount -gt 0 -and $ExpectedCount -eq 0) {
        return "UNSUPPORTED_HISTORY"
    }
    if ($UnsupportedHistoryCount -gt 0) {
        return "COVERED_WITH_UNSUPPORTED"
    }
    return "OK"
}

function Result-Status([object]$Data) {
    if ($Data.errorCount -gt 0) {
        return "ERROR"
    }
    if ($Data.differentCount -gt 0) {
        return "DIFF"
    }
    if ($Data.missingHistoryCount -gt 0) {
        return "MISSING_HISTORY"
    }
    return "OK"
}

function Sum-Property([object[]]$Rows, [string]$PropertyName) {
    $measure = $Rows | Measure-Object -Property $PropertyName -Sum
    if ($null -eq $measure.Sum) {
        return 0
    }
    return [int]$measure.Sum
}

$personCodes = New-Object System.Collections.Generic.HashSet[string]
foreach ($rawCode in $PersonCode) {
    foreach ($code in ($rawCode -split ",")) {
        if (-not [string]::IsNullOrWhiteSpace($code)) {
            [void]$personCodes.Add($code.Trim())
        }
    }
}

if ($personCodes.Count -eq 0) {
    foreach ($path in $SamplePath) {
        if (-not (Test-Path $path)) {
            continue
        }
        Get-Content $path |
            Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
            ForEach-Object {
                $parts = $_ -split "`t"
                if ($parts.Length -eq 0) {
                    return
                }
                $code = $parts[0].Trim().Trim('"')
                if ([string]::IsNullOrWhiteSpace($code) -or $code -eq "PersonCode") {
                    return
                }
                [void]$personCodes.Add($code)
            }
    }
}

if ($personCodes.Count -eq 0) {
    throw "No person codes found. Pass -PersonCode or provide sample TSV files."
}

$base = $BaseUrl.TrimEnd("/")
$webSession = [Microsoft.PowerShell.Commands.WebRequestSession]::new()
if (-not $SkipLogin) {
    $loginBody = @{
        username = $Username
        password = $Password
    } | ConvertTo-Json
    [void](Invoke-RestMethod `
        -Uri "$base/api/auth/login" `
        -Method Post `
        -Body $loginBody `
        -ContentType "application/json" `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec)
}
$coverageRows = New-Object System.Collections.Generic.List[object]

foreach ($person in ($personCodes | Sort-Object)) {
    try {
        $encodedPersonCode = [uri]::EscapeDataString($person)
        $response = Invoke-RestMethod `
            -Uri "$base/api/salary/timeline-generated/$encodedPersonCode`?limit=$Limit" `
            -Method Get `
            -WebSession $webSession `
            -TimeoutSec $TimeoutSec
        $data = $response.data
        $personStatus = Result-Status $data
        foreach ($coverage in @($data.coverage)) {
            $status = Coverage-Status `
                -ExpectedCount ([int]$coverage.expectedCount) `
                -MatchedHistoryCount ([int]$coverage.matchedHistoryCount) `
                -MissingHistoryCount ([int]$coverage.missingHistoryCount) `
                -UnsupportedHistoryCount ([int]$coverage.unsupportedHistoryCount)
            $coverageRows.Add([pscustomobject]@{
                PersonCode = $person
                PersonStatus = $personStatus
                ChangeType = $coverage.changeType
                ChangeTypeEsc = Escape-Unicode $coverage.changeType
                CoverageStatus = $status
                ExpectedCount = $coverage.expectedCount
                MatchedHistoryCount = $coverage.matchedHistoryCount
                MissingHistoryCount = $coverage.missingHistoryCount
                UnsupportedHistoryCount = $coverage.unsupportedHistoryCount
                PersonExpectedCount = $data.expectedCount
                PersonMatchedCount = $data.matchedCount
                PersonDifferentCount = $data.differentCount
                PersonMissingHistoryCount = $data.missingHistoryCount
                PersonErrorCount = $data.errorCount
                PersonUnsupportedHistoryCount = $data.unsupportedHistoryCount
                MessageEsc = ""
            })
        }
    } catch {
        $coverageRows.Add([pscustomobject]@{
            PersonCode = $person
            PersonStatus = "REQUEST_ERROR"
            ChangeType = ""
            ChangeTypeEsc = ""
            CoverageStatus = "REQUEST_ERROR"
            ExpectedCount = 0
            MatchedHistoryCount = 0
            MissingHistoryCount = 0
            UnsupportedHistoryCount = 0
            PersonExpectedCount = 0
            PersonMatchedCount = 0
            PersonDifferentCount = 0
            PersonMissingHistoryCount = 0
            PersonErrorCount = 1
            PersonUnsupportedHistoryCount = 0
            MessageEsc = Escape-Unicode (Error-Message $_)
        })
    }
}

$summaryRows = New-Object System.Collections.Generic.List[object]
$coverageRows |
    Where-Object { -not [string]::IsNullOrWhiteSpace($_.ChangeType) } |
    Group-Object ChangeType |
    Sort-Object Name |
    ForEach-Object {
        $rows = @($_.Group)
        $expected = Sum-Property -Rows $rows -PropertyName "ExpectedCount"
        $matched = Sum-Property -Rows $rows -PropertyName "MatchedHistoryCount"
        $missing = Sum-Property -Rows $rows -PropertyName "MissingHistoryCount"
        $unsupported = Sum-Property -Rows $rows -PropertyName "UnsupportedHistoryCount"
        $summaryRows.Add([pscustomobject]@{
            ChangeType = $_.Name
            ChangeTypeEsc = Escape-Unicode $_.Name
            PersonCount = $rows.Count
            CoverageStatus = Coverage-Status `
                -ExpectedCount $expected `
                -MatchedHistoryCount $matched `
                -MissingHistoryCount $missing `
                -UnsupportedHistoryCount $unsupported
            ExpectedCount = $expected
            MatchedHistoryCount = $matched
            MissingHistoryCount = $missing
            UnsupportedHistoryCount = $unsupported
        })
    }

Ensure-OutputDirectory $OutputPath
Ensure-OutputDirectory $SummaryPath

$coverageRows | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8
$summaryRows | Export-Csv -Path $SummaryPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8

$coverageRows |
    Group-Object CoverageStatus |
    Sort-Object Name |
    Select-Object Name,Count |
    Format-Table -AutoSize

Write-Host ""
$summaryRows |
    Sort-Object MissingHistoryCount,UnsupportedHistoryCount -Descending |
    Select-Object ChangeTypeEsc,ExpectedCount,MatchedHistoryCount,MissingHistoryCount,UnsupportedHistoryCount,CoverageStatus |
    Format-Table -AutoSize

Write-Host ""
Write-Host "Wrote $OutputPath"
Write-Host "Wrote $SummaryPath"
