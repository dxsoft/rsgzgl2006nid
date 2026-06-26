param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [int]$DoneLimit = 120,
    [int]$BatchLimit = 80,
    [switch]$FailOnUnexpected,
    [string]$OutputPath = "target/report-print-archive-results.tsv"
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

function Add-Result(
    [System.Collections.Generic.List[object]]$Rows,
    [string]$Gate,
    [string]$CaseNo,
    [string]$Status,
    [string]$Message
) {
    $Rows.Add([pscustomobject]@{
        Gate = $Gate
        CaseNo = $CaseNo
        Status = $Status
        MessageEsc = Escape-Unicode $Message
    })
}

function Get-CaseDetail([string]$CaseNo) {
    $encoded = [uri]::EscapeDataString($CaseNo)
    $response = Invoke-RestMethod `
        -Uri "$BaseUrl/api/workbench/salary-cases/$encoded" `
        -Method Get `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec
    return $response.data
}

function Get-DoneItems() {
    $response = Invoke-RestMethod `
        -Uri "$BaseUrl/api/workbench/items?status=DONE&limit=$DoneLimit" `
        -Method Get `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec
    return @($response.data.items)
}

function Invoke-HistoryWriteConfirm([string]$CaseNo) {
    $encoded = [uri]::EscapeDataString($CaseNo)
    $response = Invoke-RestMethod `
        -Uri "$BaseUrl/api/workbench/salary-cases/$encoded/history-write-confirm" `
        -Method Post `
        -Body "{}" `
        -ContentType "application/json; charset=utf-8" `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec
    return $response.data
}

function Invoke-BatchPreview() {
    $response = Invoke-RestMethod `
        -Uri "$BaseUrl/api/workbench/history-write-plans/batch-preview?status=PREPARED&limit=$BatchLimit" `
        -Method Post `
        -Body "{}" `
        -ContentType "application/json; charset=utf-8" `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec
    return $response.data
}

$results = [System.Collections.Generic.List[object]]::new()

try {
    $items = Get-DoneItems
    $printedDetail = $null
    $unprintedDetail = $null
    foreach ($item in $items) {
        if (-not ("" + $item.id).StartsWith("GZ-")) {
            continue
        }
        try {
            $detail = Get-CaseDetail $item.id
            if ($null -eq $printedDetail -and $detail.reportPrintArchive.printed -eq $true) {
                $printedDetail = $detail
            }
            if ($null -eq $unprintedDetail -and $detail.snapshotExists -eq $true -and $detail.reportPrintArchive.printed -ne $true) {
                $unprintedDetail = $detail
            }
            if ($null -ne $printedDetail -and $null -ne $unprintedDetail) {
                break
            }
        } catch {
            Add-Result $results "case-detail-scan" $item.id "REQUEST_ERROR" (Error-Message $_)
        }
    }

    if ($null -eq $printedDetail) {
        Add-Result $results "printed-archive-detail" "" "SKIP" "No printed salary case found in DONE sample page."
    } elseif ($printedDetail.reportPrintArchive.status -eq "PRINTED" -and $printedDetail.reportPrintArchive.printCount -gt 0) {
        $batch = if ($printedDetail.reportPrintArchive.latestBatchNo) { $printedDetail.reportPrintArchive.latestBatchNo } else { $printedDetail.reportPrintArchive.latestTargetCode }
        Add-Result $results "printed-archive-detail" $printedDetail.caseNo "OK" "printed=true, batch=$batch"
    } else {
        Add-Result $results "printed-archive-detail" $printedDetail.caseNo "FAIL" "Printed case did not expose PRINTED archive status."
    }

    if ($null -eq $unprintedDetail) {
        Add-Result $results "unprinted-history-confirm-block" "" "SKIP" "No unprinted salary case with snapshot found in DONE sample page."
    } else {
        $confirm = Invoke-HistoryWriteConfirm $unprintedDetail.caseNo
        $issues = @($confirm.issues)
        $hasPrintBlock = @($issues | Where-Object { $_ -like "BLOCKED: approval report must be printed before confirming history write*" }).Count -gt 0
        if ($confirm.executable -eq $false -and $hasPrintBlock) {
            Add-Result $results "unprinted-history-confirm-block" $unprintedDetail.caseNo "OK" "executable=false, print block found"
        } else {
            Add-Result $results "unprinted-history-confirm-block" $unprintedDetail.caseNo "FAIL" "Expected executable=false with approval report block."
        }
    }

    $batchPreview = Invoke-BatchPreview
    $batchItems = @($batchPreview.items)
    $computedExecutable = @($batchItems | Where-Object {
        $_.writable -eq $true `
            -and @("READY", "WARNING") -contains $_.status `
            -and (($_.issues -join "|") -notlike "*approval report has not been printed*")
    }).Count
    $unprintedWarnings = @($batchItems | Where-Object { ($_.issues -join "|") -like "*approval report has not been printed*" }).Count
    if ([int]$batchPreview.executable -eq [int]$computedExecutable) {
        Add-Result $results "batch-preview-print-gate" "" "OK" "executable=$($batchPreview.executable), computed=$computedExecutable, unprintedWarnings=$unprintedWarnings"
    } else {
        Add-Result $results "batch-preview-print-gate" "" "FAIL" "executable=$($batchPreview.executable), computed=$computedExecutable, unprintedWarnings=$unprintedWarnings"
    }
} catch {
    Add-Result $results "report-print-archive-gate" "" "REQUEST_ERROR" (Error-Message $_)
}

$results | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8

Write-Host ""
Write-Host "Report print archive sample summary:"
$results | Format-Table Gate,CaseNo,Status,MessageEsc -AutoSize
Write-Host ""
Write-Host "Wrote $OutputPath"

$unexpected = @($results | Where-Object { $_.Status -in @("FAIL", "REQUEST_ERROR") })
if ($FailOnUnexpected -and $unexpected.Count -gt 0) {
    throw "Report print archive sample verification found unexpected rows."
}
