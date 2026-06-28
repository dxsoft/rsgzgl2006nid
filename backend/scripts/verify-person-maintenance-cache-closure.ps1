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
$summary = "person maintenance todo cache closure $runId"
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
$refreshCount = if ($null -ne $refresh.Data.count) { [long]$refresh.Data.count } else { -1 }
if ($refreshCount -lt 0) {
    throw "Refresh response did not return a non-negative count."
}

$after = Invoke-Api -Name "base-status-after-refresh" -Path "/api/persons/$([uri]::EscapeDataString($PersonCode))/base-status"
Add-Result -Rows $rows -Step $after -Detail "status=$($after.Data.todoCacheStatus) refreshed=$($after.Data.todoCacheRefreshedAt)"
Assert-Passed $after
if ("DIRTY" -eq [string]$after.Data.todoCacheStatus) {
    throw "Expected todo cache status to be refreshed, still DIRTY."
}

$metric = Invoke-Api -Name "salary-todo-metric-after-refresh" -Path "/api/workbench/metrics/salary-todo"
Add-Result -Rows $rows -Step $metric -Detail "count=$($metric.Data.count)"
Assert-Passed $metric
if ($null -eq $metric.Data.count) {
    throw "Salary todo metric did not return a count."
}
$metricCount = [long]$metric.Data.count
if ($metricCount -ne $refreshCount) {
    throw "Salary todo metric count $metricCount does not match refresh count $refreshCount."
}

$todoPagePath = "/api/workbench/items?status=TODO&offset=0&limit=12&keyword=&changeType=&source=&caseStatus=&trialStatus=&reviewStatus=&workflowStatus=&closureStatus=&nextAction="
$todoPage = Invoke-Api -Name "workbench-todo-after-refresh" -Path $todoPagePath
Assert-Passed $todoPage
if ($null -eq $todoPage.Data) {
    throw "Workbench todo page returned no data."
}
$todoItems = @($todoPage.Data.items)
if ($todoItems.Count -lt 1) {
    $todoItems = @($todoPage.Data.records)
}
$todoTotal = if ($null -ne $todoPage.Data.total) { [long]$todoPage.Data.total } else { [long]$todoItems.Count }
Add-Result -Rows $rows -Step $todoPage -Detail "total=$todoTotal items=$($todoItems.Count)"
if ($todoTotal -ne $refreshCount) {
    throw "Workbench TODO total $todoTotal does not match refresh count $refreshCount."
}
if ($todoTotal -gt 0 -and $todoItems.Count -lt 1) {
    throw "Workbench TODO page reported total $todoTotal but returned no visible items."
}
if ($todoItems.Count -gt 0) {
    $missingAction = @($todoItems | Where-Object {
        [string]::IsNullOrWhiteSpace([string]$_.id) `
            -or [string]::IsNullOrWhiteSpace([string]$_.personCode) `
            -or [string]::IsNullOrWhiteSpace([string]$_.businessType) `
            -or [string]::IsNullOrWhiteSpace([string]$_.nextActionCode) `
            -or [string]::IsNullOrWhiteSpace([string]$_.nextActionLabel)
    })
    if ($missingAction.Count -gt 0) {
        throw "Workbench TODO page returned item(s) without handling action metadata."
    }
    $firstTodo = $todoItems[0]
    $Rows.Add([pscustomobject]@{
        Name = "workbench-todo-action-metadata"
        Status = "PASS"
        Milliseconds = 0
        Detail = "first=$($firstTodo.id) action=$($firstTodo.nextActionCode)"
        Message = ""
    })
    $originalSource = [string]$firstTodo.source
    $previewRequest = @{
        workItemId = $firstTodo.id
        source = if ($originalSource -and $originalSource -ne "SALARY_EVENT") { "SALARY_EVENT" } else { $originalSource }
        businessType = $firstTodo.businessType
        personCode = $firstTodo.personCode
        personName = $firstTodo.personName
        orgCode = $firstTodo.orgCode
        year = [int]$firstTodo.year
        month = [int]$firstTodo.month
        title = $firstTodo.title
        summary = if ($originalSource -and $originalSource -ne "SALARY_EVENT") {
            "$($firstTodo.summary); source=$originalSource"
        } else {
            $firstTodo.summary
        }
    }
    $preview = Invoke-Api -Name "workbench-todo-preview" -Path "/api/workbench/salary-cases/preview" -Method "POST" -Body $previewRequest
    Add-Result -Rows $rows -Step $preview -Detail "workItem=$($preview.Data.workItemId) trial=$($preview.Data.trialStatus)"
    Assert-Passed $preview
    if ([string]$preview.Data.workItemId -ne [string]$firstTodo.id) {
        throw "Preview work item id does not match TODO item id."
    }
    if ([string]$preview.Data.personCode -ne [string]$firstTodo.personCode) {
        throw "Preview person code does not match TODO item person code."
    }
    if ([string]$preview.Data.businessType -ne [string]$firstTodo.businessType) {
        throw "Preview business type does not match TODO item business type."
    }
    if ([string]::IsNullOrWhiteSpace([string]$preview.Data.trialStatus)) {
        throw "Preview did not return trial status."
    }
    if ([string]::IsNullOrWhiteSpace([string]$preview.Data.trialSummary)) {
        throw "Preview did not return trial summary."
    }
    $previewChanges = @($preview.Data.trialChanges)
    $hasPreviewAmounts = $null -ne $preview.Data.trialBaselineTotal `
        -or $null -ne $preview.Data.trialCalculatedTotal `
        -or $null -ne $preview.Data.trialExpectedTotal `
        -or $null -ne $preview.Data.trialDifference
    $Rows.Add([pscustomobject]@{
        Name = "workbench-todo-preview-detail"
        Status = "PASS"
        Milliseconds = 0
        Detail = "summary=$($preview.Data.trialSummary) amounts=$hasPreviewAmounts changes=$($previewChanges.Count)"
        Message = ""
    })
}

$rows | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8
$rows | Format-Table -AutoSize

$failed = @($rows | Where-Object { $_.Status -ne "PASS" })
if ($failed.Count -gt 0) {
    throw "Person maintenance cache closure failed: $($failed.Count) step(s). See $OutputPath"
}

Write-Host "Person maintenance cache closure passed for $PersonCode. Report: $OutputPath"
