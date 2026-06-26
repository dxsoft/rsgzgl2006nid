param(
    [string[]]$SamplePath = @(
        "target/cross-type-samples.tsv",
        "target/normal-grade-expanded-samples.tsv",
        "target/core-flow-samples.tsv",
        "target/special-flow-samples.tsv",
        "target/rank-judicial-samples.tsv",
        "target/target-state-samples.tsv"
    ),
    [string]$OutputPath = "target/generated-timeline-diagnostics.tsv",
    [string]$BaseUrl = "http://127.0.0.1:18080",
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

function Diagnosis-Category([string]$Source, [string]$Status, [string]$Message, [string]$ChangeType) {
    if ($Status -eq "REQUEST_ERROR") {
        return "REQUEST_ERROR"
    }
    $messageEsc = Escape-Unicode $Message
    if ($Message -match "jslb=|hj2=|History" -or $messageEsc -match "\\u540c\\u5e74\\u6708\\u5386\\u53f2\\u5de5\\u8d44\\u884c|\\u524d\\u540e\\u5df2\\u6709\\u5386\\u53f2\\u5de5\\u8d44\\u884c") {
        return "HISTORY_NEARBY"
    }
    if ($Source -eq "dryzwbh") {
        return "POST_BASE"
    }
    if ($Source -eq "dxl") {
        return "EDUCATION_BASE"
    }
    if ($Source -eq "dndkh") {
        return "ASSESSMENT_STATE"
    }
    if ($Source -eq "jx") {
        return "RANK_BASE"
    }
    if ($Source -eq "hjxx") {
        return "REWARD_PUNISHMENT_BASE"
    }
    if ($Source -eq "dryjbxx") {
        return "BASIC_INFO"
    }
    if ($messageEsc -match "dryzwbh|zwbm|\\u4efb\\u804c|\\u804c\\u52a1|\\u804c\\u7ea7") {
        return "POST_BASE"
    }
    if ($messageEsc -match "dxl|\\u5b66\\u5386|\\u6bd5\\u4e1a") {
        return "EDUCATION_BASE"
    }
    if ($messageEsc -match "xckh|\\u8003\\u6838|\\u8d77\\u7b97|\\u672a\\u5b9a|\\u4e0d\\u5408\\u683c") {
        return "ASSESSMENT_STATE"
    }
    if ($messageEsc -match "\\bjx\\b|jxjtbz|\\u8b66\\u8854|\\u6d25\\u8d34") {
        return "RANK_BASE"
    }
    if ($messageEsc -match "hjxx|\\u5956\\u60e9|\\u5904\\u5206|\\u964d\\u8d44|\\u5956\\u52b1") {
        return "REWARD_PUNISHMENT_BASE"
    }
    if ($Status -eq "MISSING_HISTORY") {
        return "MISSING_HISTORY"
    }
    if ($Status -eq "ERROR") {
        return "TRIAL_ERROR"
    }
    if ($Status -eq "DIFF") {
        return "AMOUNT_DIFF"
    }
    return "OTHER"
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

$diagnostics = New-Object System.Collections.Generic.List[object]
foreach ($personCode in ($personCodes | Sort-Object)) {
    try {
        $encodedPersonCode = [uri]::EscapeDataString($personCode)
        $response = Invoke-RestMethod `
            -Uri "$BaseUrl/api/salary/timeline-generated/$encodedPersonCode`?limit=$Limit" `
            -Method Get `
            -TimeoutSec $TimeoutSec
        $data = $response.data
        $badItems = @($data.items | Where-Object { $_.status -ne "MATCH" })
        foreach ($item in $badItems) {
            $message = [string]$item.message
            $category = Diagnosis-Category `
                -Source ([string]$item.source) `
                -Status ([string]$item.status) `
                -Message $message `
                -ChangeType ([string]$item.changeType)
            $diagnostics.Add([pscustomobject]@{
                PersonCode = $personCode
                Year = $item.year
                Month = $item.month
                ChangeType = $item.changeType
                ChangeTypeEsc = Escape-Unicode $item.changeType
                Status = $item.status
                DiagnosisCategory = $category
                Source = $item.source
                SourceId = $item.sourceId
                HistoryId = $item.historyId
                BaselineHistoryId = $item.baselineHistoryId
                HistoryTotalAmount = $item.historyTotalAmount
                CalculatedTotalAmount = $item.calculatedTotalAmount
                DifferenceWithExpected = $item.differenceWithExpected
                Message = $message
                MessageEsc = Escape-Unicode $message
            })
        }
    } catch {
        $message = Error-Message $_
        $diagnostics.Add([pscustomobject]@{
            PersonCode = $personCode
            Year = ""
            Month = ""
            ChangeType = ""
            ChangeTypeEsc = ""
            Status = "REQUEST_ERROR"
            DiagnosisCategory = "请求异常"
            Source = ""
            SourceId = ""
            HistoryId = ""
            BaselineHistoryId = ""
            HistoryTotalAmount = 0
            CalculatedTotalAmount = 0
            DifferenceWithExpected = 0
            Message = $message
            MessageEsc = Escape-Unicode $message
        })
    }
}

$outputDir = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

$diagnostics | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8

$diagnostics | Group-Object DiagnosisCategory | Sort-Object Count -Descending | Select-Object Count,Name | Format-Table -AutoSize
Write-Host ""
$diagnostics | Group-Object Status | Sort-Object Name | Select-Object Count,Name | Format-Table -AutoSize
Write-Host ""
Write-Host "Wrote $OutputPath"
