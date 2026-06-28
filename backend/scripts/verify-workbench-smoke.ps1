param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [int]$MaxSummaryMilliseconds = 5000,
    [int]$MaxTodoMilliseconds = 5000,
    [int]$MaxDoneMilliseconds = 8000,
    [int]$MaxHistoryMilliseconds = 5000,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [string]$OutputPath = "target/workbench-smoke-results.tsv"
)

$ErrorActionPreference = "Stop"

$outputDir = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

$webSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession

function Invoke-Api {
    param(
        [string]$Name,
        [string]$Path,
        [int]$MaxMilliseconds
    )

    $sw = [Diagnostics.Stopwatch]::StartNew()
    try {
        $response = Invoke-RestMethod `
            -Uri "$BaseUrl$Path" `
            -Method Get `
            -WebSession $webSession `
            -TimeoutSec $TimeoutSec `
            -ErrorAction Stop
        $sw.Stop()

        if ($response.success -ne $true) {
            throw "API returned success=false"
        }
        if ($sw.ElapsedMilliseconds -gt $MaxMilliseconds) {
            throw "API exceeded ${MaxMilliseconds}ms"
        }

        $count = ""
        if ($null -ne $response.data) {
            if ($null -ne $response.data.items) {
                $count = @($response.data.items).Count
            } elseif ($null -ne $response.data.metrics) {
                $count = @($response.data.metrics).Count
            } elseif ($response.data -is [array]) {
                $count = @($response.data).Count
            }
        }

        return [pscustomobject]@{
            Name = $Name
            Status = "PASS"
            Milliseconds = $sw.ElapsedMilliseconds
            Count = $count
            Message = ""
        }
    } catch {
        $sw.Stop()
        return [pscustomobject]@{
            Name = $Name
            Status = "FAIL"
            Milliseconds = $sw.ElapsedMilliseconds
            Count = ""
            Message = $_.Exception.Message
        }
    }
}

if (-not [string]::IsNullOrWhiteSpace($Username)) {
    $loginBody = @{ username = $Username; password = $Password } | ConvertTo-Json -Compress
    Invoke-RestMethod `
        -Uri "$BaseUrl/api/auth/login" `
        -Method Post `
        -Body $loginBody `
        -ContentType "application/json; charset=utf-8" `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec `
        -ErrorAction Stop | Out-Null
}

$checks = @(
    @{ Name = "auth-me"; Path = "/api/auth/me"; Max = 2000 },
    @{ Name = "system-menus"; Path = "/api/system/menus"; Max = 3000 },
    @{ Name = "workbench-summary"; Path = "/api/workbench/summary"; Max = $MaxSummaryMilliseconds },
    @{ Name = "workbench-todo-default"; Path = "/api/workbench/items?status=TODO&offset=0&limit=12&keyword=&changeType=&source=&caseStatus=&trialStatus=&reviewStatus=&workflowStatus=&closureStatus=&nextAction="; Max = $MaxTodoMilliseconds },
    @{ Name = "workbench-done-default"; Path = "/api/workbench/items?status=DONE&offset=0&limit=12&keyword=&changeType=&source=&caseStatus=DONE&trialStatus=&reviewStatus=&workflowStatus=&closureStatus=&nextAction="; Max = $MaxDoneMilliseconds },
    @{ Name = "workbench-done-more"; Path = "/api/workbench/items?status=DONE&offset=12&limit=12&keyword=&changeType=&source=&caseStatus=DONE&trialStatus=&reviewStatus=&workflowStatus=&closureStatus=&nextAction="; Max = $MaxDoneMilliseconds },
    @{ Name = "workbench-done-closure-filter"; Path = "/api/workbench/items?status=DONE&offset=0&limit=12&keyword=&changeType=&source=&caseStatus=DONE&trialStatus=&reviewStatus=&workflowStatus=&closureStatus=PENDING&nextAction="; Max = $MaxDoneMilliseconds },
    @{ Name = "history-plans-default"; Path = "/api/workbench/history-write-plans?status=&comparisonStatus=&reviewStatus=&retestStatus=&maintenanceStatus=&priority=&action=&readyOnly=false&blockedOnly=false&missingPrintOnly=false&keyword=&offset=0&limit=12"; Max = $MaxHistoryMilliseconds },
    @{ Name = "history-plans-prepared"; Path = "/api/workbench/history-write-plans?status=PREPARED&comparisonStatus=&reviewStatus=&retestStatus=&maintenanceStatus=&priority=&action=&readyOnly=false&blockedOnly=false&missingPrintOnly=false&keyword=&offset=0&limit=12"; Max = $MaxHistoryMilliseconds },
    @{ Name = "history-plans-review"; Path = "/api/workbench/history-write-plans?status=EXECUTED&comparisonStatus=MISMATCHED&reviewStatus=PENDING&retestStatus=&maintenanceStatus=&priority=&action=&readyOnly=false&blockedOnly=false&missingPrintOnly=false&keyword=&offset=0&limit=12"; Max = $MaxHistoryMilliseconds }
)

$rows = New-Object System.Collections.Generic.List[object]
foreach ($check in $checks) {
    $rows.Add((Invoke-Api -Name $check.Name -Path $check.Path -MaxMilliseconds $check.Max))
}

$rows | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8
$rows | Format-Table -AutoSize

$failed = @($rows | Where-Object { $_.Status -ne "PASS" })
if ($failed.Count -gt 0) {
    throw "Workbench smoke verification failed: $($failed.Count) check(s). See $OutputPath"
}

Write-Host "Workbench smoke verification passed. Report: $OutputPath"
