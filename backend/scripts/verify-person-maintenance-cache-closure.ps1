param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [string]$PersonCode = "",
    [string]$OutputPath = "target/person-maintenance-cache-closure-results.tsv"
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
        [string]$Method = "GET",
        [object]$Body = $null
    )

    $sw = [Diagnostics.Stopwatch]::StartNew()
    try {
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
        $response = Invoke-RestMethod @params
        $sw.Stop()
        if ($response.success -ne $true) {
            throw "API returned success=false"
        }
        return [pscustomobject]@{
            Name = $Name
            Status = "PASS"
            Milliseconds = $sw.ElapsedMilliseconds
            Data = $response.data
            Message = ""
        }
    } catch {
        $sw.Stop()
        return [pscustomobject]@{
            Name = $Name
            Status = "FAIL"
            Milliseconds = $sw.ElapsedMilliseconds
            Data = $null
            Message = $_.Exception.Message
        }
    }
}

function Add-Result {
    param(
        [System.Collections.Generic.List[object]]$Rows,
        [object]$Step,
        [string]$Detail = ""
    )

    $Rows.Add([pscustomobject]@{
        Name = $Step.Name
        Status = $Step.Status
        Milliseconds = $Step.Milliseconds
        Detail = $Detail
        Message = $Step.Message
    })
}

function Assert-Passed {
    param([object]$Step)
    if ($Step.Status -ne "PASS") {
        throw "$($Step.Name) failed: $($Step.Message)"
    }
}

$rows = New-Object System.Collections.Generic.List[object]

$login = Invoke-Api -Name "login" -Path "/api/auth/login" -Method "POST" -Body @{ username = $Username; password = $Password }
Add-Result -Rows $rows -Step $login
Assert-Passed $login

if ([string]::IsNullOrWhiteSpace($PersonCode)) {
    $people = Invoke-Api -Name "select-person" -Path "/api/persons?page=1&size=1"
    Add-Result -Rows $rows -Step $people
    Assert-Passed $people
    $items = @($people.Data.records)
    if ($items.Count -lt 1) {
        $items = @($people.Data.items)
    }
    if ($items.Count -lt 1) {
        throw "No person available for verification."
    }
    $PersonCode = $items[0].personCode
}

$before = Invoke-Api -Name "base-status-before" -Path "/api/persons/$([uri]::EscapeDataString($PersonCode))/base-status"
Add-Result -Rows $rows -Step $before -Detail "status=$($before.Data.todoCacheStatus)"
Assert-Passed $before

$runId = "VERIFY-" + (Get-Date -Format "yyyyMMddHHmmss")
$summary = "人员维护待办刷新闭环验收 $runId"
$change = Invoke-Api -Name "create-base-change" -Path "/api/persons/$([uri]::EscapeDataString($PersonCode))/base-changes" -Method "POST" -Body @{
    dataType = "dryjbxx"
    sourceTable = "person_base_change_log"
    sourceId = $runId
    summary = $summary
    changeYear = [int](Get-Date -Format "yyyy")
    changeMonth = [int](Get-Date -Format "MM")
}
Add-Result -Rows $rows -Step $change -Detail "person=$PersonCode sourceId=$runId"
Assert-Passed $change

$dirty = Invoke-Api -Name "base-status-dirty" -Path "/api/persons/$([uri]::EscapeDataString($PersonCode))/base-status"
Add-Result -Rows $rows -Step $dirty -Detail "status=$($dirty.Data.todoCacheStatus) latest=$($dirty.Data.latestChangeSummary)"
Assert-Passed $dirty
if ("DIRTY" -ne [string]$dirty.Data.todoCacheStatus) {
    throw "Expected todo cache status DIRTY, got '$($dirty.Data.todoCacheStatus)'."
}
if ([string]$dirty.Data.latestChangeSummary -ne $summary) {
    throw "Latest base change summary mismatch."
}

$refresh = Invoke-Api -Name "refresh-todo-cache" -Path "/api/workbench/salary-todo-cache/refresh" -Method "POST"
Add-Result -Rows $rows -Step $refresh -Detail "metric=$($refresh.Data.count)"
Assert-Passed $refresh

$after = Invoke-Api -Name "base-status-after-refresh" -Path "/api/persons/$([uri]::EscapeDataString($PersonCode))/base-status"
Add-Result -Rows $rows -Step $after -Detail "status=$($after.Data.todoCacheStatus) refreshed=$($after.Data.todoCacheRefreshedAt)"
Assert-Passed $after
if ("DIRTY" -eq [string]$after.Data.todoCacheStatus) {
    throw "Expected todo cache status to be refreshed, still DIRTY."
}

$rows | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8
$rows | Format-Table -AutoSize

$failed = @($rows | Where-Object { $_.Status -ne "PASS" })
if ($failed.Count -gt 0) {
    throw "Person maintenance cache closure failed: $($failed.Count) step(s). See $OutputPath"
}

Write-Host "Person maintenance cache closure passed for $PersonCode. Report: $OutputPath"
