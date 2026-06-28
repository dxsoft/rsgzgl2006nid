param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [int]$Limit = 3,
    [switch]$FailOnUnexpected,
    [string]$OutputPath = "target/history-write-batch-safety-contract-results.tsv"
)

$ErrorActionPreference = "Stop"

$outputDir = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

$webSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession

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

function Invoke-Api(
    [string]$Path,
    [string]$Method = "GET",
    [object]$Body = $null
) {
    $params = @{
        Uri = "$BaseUrl$Path"
        Method = $Method
        WebSession = $webSession
        TimeoutSec = $TimeoutSec
        ErrorAction = "Stop"
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Compress)
        $params.ContentType = "application/json; charset=utf-8"
    }
    return Invoke-RestMethod @params
}

function Test-TokenPreview(
    [System.Collections.Generic.List[object]]$Rows,
    [string]$Code,
    [object]$Preview
) {
    if ($Preview.success -ne $true) {
        Add-Result $Rows $Code "FAIL" "Preview returned success=false."
        return
    }
    $data = $Preview.data
    $items = @($data.items)
    if ($null -eq $data.total -or $null -eq $data.executable) {
        Add-Result $Rows $Code "FAIL" "Preview did not return total/executable counters."
        return
    }
    if ([string]::IsNullOrWhiteSpace("" + $data.safetyToken)) {
        Add-Result $Rows $Code "FAIL" "Preview did not issue safetyToken."
        return
    }
    if ([string]::IsNullOrWhiteSpace("" + $data.safetyExpiresAt)) {
        Add-Result $Rows $Code "FAIL" "Preview did not return safetyExpiresAt."
        return
    }
    if ([string]::IsNullOrWhiteSpace("" + $data.safetySummary) -or ("" + $data.safetySummary) -notlike "*tokenRequired=true*") {
        Add-Result $Rows $Code "FAIL" "Preview safetySummary does not state token requirement."
        return
    }
    if ([int]$data.total -ne $items.Count) {
        Add-Result $Rows $Code "FAIL" ("Preview total " + $data.total + " does not match item count " + $items.Count + ".")
        return
    }
    Add-Result $Rows $Code "OK" ("total=" + $data.total + "; executable=" + $data.executable + "; token=" + ("" + $data.safetyToken).Substring(0, 8))
}

function Invoke-RejectedExecute(
    [System.Collections.Generic.List[object]]$Rows,
    [string]$Code,
    [string]$Path,
    [object]$Body
) {
    try {
        $response = Invoke-Api $Path "POST" $Body
        if ($response.success -eq $true) {
            Add-Result $Rows $Code "FAIL" "Execute unexpectedly succeeded without a safety token."
        } else {
            Add-Result $Rows $Code "OK" ("Rejected without token: " + $response.message)
        }
    } catch {
        Add-Result $Rows $Code "OK" ("Rejected without token: " + $_.Exception.Message)
    }
}

$results = [System.Collections.Generic.List[object]]::new()

try {
    Invoke-Api "/api/auth/login" "POST" @{ username = $Username; password = $Password } | Out-Null
    Add-Result $results "login" "OK" "Authenticated."
} catch {
    Add-Result $results "login" "REQUEST_ERROR" $_.Exception.Message
}

try {
    $batchPreviewPath = "/api/workbench/history-write-plans/batch-preview?status=PREPARED&limit=$Limit"
    $batchPreview = Invoke-Api $batchPreviewPath "POST"
    Test-TokenPreview $results "batch-preview-safety-token" $batchPreview
} catch {
    Add-Result $results "batch-preview-safety-token" "REQUEST_ERROR" $_.Exception.Message
}

Invoke-RejectedExecute $results "batch-execute-rejects-missing-token" "/api/workbench/history-write-plans/batch-execute?status=PREPARED&limit=$Limit" @{ safetyToken = "" }

$selectedCaseNos = @()
try {
    $plans = Invoke-Api "/api/workbench/history-write-plans?status=PREPARED&limit=$Limit"
    $selectedCaseNos = @($plans.data | ForEach-Object { "" + $_.caseNo } | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Select-Object -First $Limit)
} catch {
    Add-Result $results "selected-preview-source-plans" "REQUEST_ERROR" $_.Exception.Message
}

if ($selectedCaseNos.Count -lt 1) {
    Add-Result $results "selected-preview-safety-token" "SKIP" "No prepared history write plan is available."
    Add-Result $results "selected-execute-rejects-missing-token" "SKIP" "No prepared history write plan is available."
} else {
    Add-Result $results "selected-preview-source-plans" "OK" ("caseNos=" + ($selectedCaseNos -join ","))
    try {
        $selectedPreview = Invoke-Api "/api/workbench/history-write-plans/selected-preview" "POST" @{ caseNos = $selectedCaseNos }
        Test-TokenPreview $results "selected-preview-safety-token" $selectedPreview
    } catch {
        Add-Result $results "selected-preview-safety-token" "REQUEST_ERROR" $_.Exception.Message
    }
    Invoke-RejectedExecute $results "selected-execute-rejects-missing-token" "/api/workbench/history-write-plans/selected-execute" @{ caseNos = $selectedCaseNos; safetyToken = "" }
}

$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$appJs = Join-Path $root "backend\src\main\resources\static\app.js"
$controller = Join-Path $root "backend\src\main\java\com\dx\rsgzgl\system\controller\WorkbenchController.java"
$service = Join-Path $root "backend\src\main\java\com\dx\rsgzgl\system\service\WorkbenchService.java"
$anchors = @(
    @{ Code = "frontend-final-confirm-gate"; File = $appJs; Pattern = "batchFinalConfirmGateHtml" },
    @{ Code = "frontend-safety-token-panel"; File = $appJs; Pattern = "batchSafetyTokenHtml" },
    @{ Code = "frontend-expiry-block"; File = $appJs; Pattern = "batchSafetyTokenExpired" },
    @{ Code = "frontend-execute-sends-token"; File = $appJs; Pattern = "safetyToken: confirmedPreview.safetyToken" },
    @{ Code = "backend-batch-preview-endpoint"; File = $controller; Pattern = '"/history-write-plans/batch-preview"' },
    @{ Code = "backend-batch-execute-endpoint"; File = $controller; Pattern = '"/history-write-plans/batch-execute"' },
    @{ Code = "backend-selected-preview-endpoint"; File = $controller; Pattern = '"/history-write-plans/selected-preview"' },
    @{ Code = "backend-selected-execute-endpoint"; File = $controller; Pattern = '"/history-write-plans/selected-execute"' },
    @{ Code = "backend-token-create"; File = $service; Pattern = "createBatchWriteSafetyToken" },
    @{ Code = "backend-token-validate"; File = $service; Pattern = "validateBatchWriteSafetyToken" },
    @{ Code = "backend-token-consume"; File = $service; Pattern = "batchWriteSafetyTokens.remove" },
    @{ Code = "backend-token-audit"; File = $service; Pattern = "history-write-batch-safety-consume" }
)

foreach ($anchor in $anchors) {
    try {
        $content = Get-Content -Raw -Path $anchor.File
        if ($content.Contains($anchor.Pattern)) {
            Add-Result $results $anchor.Code "OK" $anchor.Pattern
        } else {
            Add-Result $results $anchor.Code "FAIL" ("Missing " + $anchor.Pattern)
        }
    } catch {
        Add-Result $results $anchor.Code "REQUEST_ERROR" $_.Exception.Message
    }
}

$results | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8

Write-Host ""
Write-Host "History write batch safety contract summary:"
$results | Format-Table Code,Status,Message -AutoSize
Write-Host ""
Write-Host "Wrote $OutputPath"

$unexpected = @($results | Where-Object { $_.Status -in @("FAIL", "REQUEST_ERROR") })
if ($FailOnUnexpected -and $unexpected.Count -gt 0) {
    throw "History write batch safety contract verification found unexpected rows."
}
