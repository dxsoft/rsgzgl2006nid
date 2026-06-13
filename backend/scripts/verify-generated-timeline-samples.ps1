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
    [switch]$FailOnUnexpected
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

$personCodes = New-Object System.Collections.Generic.HashSet[string]
foreach ($path in $SamplePath) {
    if (-not (Test-Path $path)) {
        continue
    }
    Get-Content $path |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
        ForEach-Object {
            $parts = $_ -split "`t"
            if ($parts.Length -gt 0 -and -not [string]::IsNullOrWhiteSpace($parts[0])) {
                [void]$personCodes.Add($parts[0])
            }
        }
}

if ($personCodes.Count -eq 0) {
    throw "No person codes found from sample files."
}

$results = New-Object System.Collections.Generic.List[object]
foreach ($personCode in ($personCodes | Sort-Object)) {
    try {
        $encodedPersonCode = [uri]::EscapeDataString($personCode)
        $response = Invoke-RestMethod `
            -Uri "$BaseUrl/api/salary/timeline-generated/$encodedPersonCode`?limit=$Limit" `
            -Method Get `
            -TimeoutSec $TimeoutSec
        $data = $response.data
        $status = if ($data.errorCount -gt 0) {
            "ERROR"
        } elseif ($data.differentCount -gt 0) {
            "DIFF"
        } elseif ($data.missingHistoryCount -gt 0) {
            "MISSING_HISTORY"
        } else {
            "OK"
        }
        $badItems = @($data.items | Where-Object { $_.status -ne "MATCH" })
        $firstBad = $badItems | Select-Object -First 1
        $results.Add([pscustomobject]@{
            PersonCode = $personCode
            Status = $status
            ExpectedCount = $data.expectedCount
            MatchedCount = $data.matchedCount
            DifferentCount = $data.differentCount
            MissingHistoryCount = $data.missingHistoryCount
            ErrorCount = $data.errorCount
            UnsupportedHistoryCount = $data.unsupportedHistoryCount
            FirstBadYear = if ($null -eq $firstBad) { "" } else { $firstBad.year }
            FirstBadMonth = if ($null -eq $firstBad) { "" } else { $firstBad.month }
            FirstBadChangeTypeEsc = if ($null -eq $firstBad) { "" } else { Escape-Unicode $firstBad.changeType }
            FirstBadStatus = if ($null -eq $firstBad) { "" } else { $firstBad.status }
            FirstBadMessageEsc = if ($null -eq $firstBad) { "" } else { Escape-Unicode $firstBad.message }
        })
    } catch {
        $results.Add([pscustomobject]@{
            PersonCode = $personCode
            Status = "REQUEST_ERROR"
            ExpectedCount = 0
            MatchedCount = 0
            DifferentCount = 0
            MissingHistoryCount = 0
            ErrorCount = 1
            UnsupportedHistoryCount = 0
            FirstBadYear = ""
            FirstBadMonth = ""
            FirstBadChangeTypeEsc = ""
            FirstBadStatus = "REQUEST_ERROR"
            FirstBadMessageEsc = Escape-Unicode (Error-Message $_)
        })
    }
}

$outputDir = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

$results | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8

$results | Group-Object Status | Sort-Object Name | Select-Object Name,Count | Format-Table -AutoSize
Write-Host ""
Write-Host "Wrote $OutputPath"

$unexpected = @($results | Where-Object { $_.Status -ne "OK" })
Write-Host ("Unexpected generated timeline non-OK: {0}" -f $unexpected.Count)
if ($FailOnUnexpected -and $unexpected.Count -gt 0) {
    throw "Generated timeline verification found unexpected non-OK rows."
}
