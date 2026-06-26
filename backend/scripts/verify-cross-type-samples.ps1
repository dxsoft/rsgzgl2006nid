param(
    [string]$SamplePath = "target/cross-type-samples.tsv",
    [string]$OutputPath = "target/cross-type-results-current.tsv",
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 20,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [switch]$IgnoreSampleChangeType
)

$ErrorActionPreference = "Stop"

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

function Decode-EscapedUnicode([string]$Value) {
    if ([string]::IsNullOrWhiteSpace($Value)) {
        return $Value
    }
    return [regex]::Replace($Value, "\\u([0-9a-fA-F]{4})", {
        param($Match)
        [char][Convert]::ToInt32($Match.Groups[1].Value, 16)
    })
}

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

function Csv-Value([object]$Value) {
    if ($null -eq $Value) {
        return '""'
    }
    $text = [string]$Value
    return '"' + $text.Replace('"', '""') + '"'
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

$resolvedSamplePath = Resolve-Path $SamplePath
$rows = Get-Content $resolvedSamplePath | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | ForEach-Object {
    $parts = $_ -split "`t"
    if ($parts.Length -lt 7) {
        throw "Invalid sample row: $_"
    }
    [pscustomobject]@{
        PersonCode = $parts[0]
        OrgCode = $parts[1]
        Year = [int]$parts[2]
        Month = [int]$parts[3]
        Prefix = $parts[4]
        ExpectedSampleAmount = $parts[5]
        ChangeType = if ($IgnoreSampleChangeType) { "" } else { Decode-EscapedUnicode $parts[6] }
    }
}

$results = New-Object System.Collections.Generic.List[object]
foreach ($row in $rows) {
    try {
        $payload = @{
            personCode = $row.PersonCode
            orgCode = $row.OrgCode
            year = $row.Year
            month = $row.Month
        }
        if (-not $IgnoreSampleChangeType) {
            $payload.changeType = $row.ChangeType
        }
        $body = $payload | ConvertTo-Json -Compress

        $response = Invoke-RestMethod `
            -Uri "$BaseUrl/api/salary/rule-trial/normal-grade" `
            -Method Post `
            -Body $body `
            -ContentType "application/json; charset=utf-8" `
            -WebSession $webSession `
            -TimeoutSec $TimeoutSec

        $data = $response.data
        $status = if ($data.matchedExpected) {
            "MATCH"
        } elseif ($null -eq $data.expectedHistoryId) {
            "NO_EXPECTED"
        } else {
            "DIFF"
        }
        $results.Add([pscustomobject]@{
            PersonCode = $row.PersonCode
            OrgCode = $row.OrgCode
            Year = $row.Year
            Month = $row.Month
            Prefix = $row.Prefix
            ChangeTypeEsc = Escape-Unicode $row.ChangeType
            Status = $status
            Expected = $data.expectedTotalAmount
            Actual = $data.calculatedTotalAmount
            Diff = $data.differenceWithExpected
            ChangeCodes = (($data.changes | ForEach-Object { $_.itemCode }) -join ",")
            Message = ""
        })
    } catch {
        $results.Add([pscustomobject]@{
            PersonCode = $row.PersonCode
            OrgCode = $row.OrgCode
            Year = $row.Year
            Month = $row.Month
            Prefix = $row.Prefix
            ChangeTypeEsc = Escape-Unicode $row.ChangeType
            Status = "ERROR"
            Expected = $row.ExpectedSampleAmount
            Actual = ""
            Diff = ""
            ChangeCodes = ""
            Message = Error-Message $_
        })
    }
}

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add('"personCode"' + "`t" + '"orgCode"' + "`t" + '"year"' + "`t" + '"month"' + "`t" + '"prefix"' + "`t" + '"jslbEsc"' + "`t" + '"status"' + "`t" + '"expected"' + "`t" + '"actual"' + "`t" + '"diff"' + "`t" + '"changeCodes"' + "`t" + '"message"')
foreach ($result in $results) {
    $lines.Add((
        (Csv-Value $result.PersonCode),
        (Csv-Value $result.OrgCode),
        (Csv-Value $result.Year),
        (Csv-Value $result.Month),
        (Csv-Value $result.Prefix),
        (Csv-Value $result.ChangeTypeEsc),
        (Csv-Value $result.Status),
        (Csv-Value $result.Expected),
        (Csv-Value $result.Actual),
        (Csv-Value $result.Diff),
        (Csv-Value $result.ChangeCodes),
        (Csv-Value $result.Message)
    ) -join "`t")
}

$outputDirectory = Split-Path $OutputPath -Parent
if (-not [string]::IsNullOrWhiteSpace($outputDirectory) -and -not (Test-Path $outputDirectory)) {
    New-Item -ItemType Directory -Path $outputDirectory | Out-Null
}
$lines | Set-Content -Path $OutputPath -Encoding UTF8

$results |
    Group-Object Status, ChangeTypeEsc |
    ForEach-Object {
        $first = $_.Group[0]
        [pscustomobject]@{
            Status = $first.Status
            ChangeTypeEsc = $first.ChangeTypeEsc
            Count = $_.Count
        }
    } |
    Sort-Object Status, ChangeTypeEsc |
    Format-Table -AutoSize

Write-Host "Wrote $OutputPath"
