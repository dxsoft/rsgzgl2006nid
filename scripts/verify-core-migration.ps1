param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [string]$DbPassword = "",
    [string]$OutputPath = "",
    [int]$MaxSummaryMilliseconds = 30000,
    [int]$MavenTimeoutSec = 600,
    [switch]$SkipMaven,
    [switch]$SkipSamples,
    [switch]$FailOnUnexpected
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"
$targetDir = Join-Path $backendDir "target"
if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $OutputPath = Join-Path $targetDir "core-migration-verification-results.tsv"
}
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $OutputPath) | Out-Null

$results = [System.Collections.Generic.List[object]]::new()
if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    $DbPassword = $env:DB_PASSWORD
}
if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    throw "DB_PASSWORD is required. Set `$env:DB_PASSWORD or pass -DbPassword."
}

function Stop-ProcessTree([int]$ProcessId) {
    $children = @(Get-CimInstance Win32_Process | Where-Object { $_.ParentProcessId -eq $ProcessId })
    foreach ($child in $children) {
        Stop-ProcessTree ([int]$child.ProcessId)
    }
    Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
}

function Invoke-NativeCommand([string]$FilePath, [string[]]$Arguments, [int]$TimeoutSec = 0) {
    $command = Get-Command $FilePath -ErrorAction Stop
    $stdout = Join-Path $targetDir ("native-out-" + [guid]::NewGuid().ToString("N") + ".log")
    $stderr = Join-Path $targetDir ("native-err-" + [guid]::NewGuid().ToString("N") + ".log")
    $process = Start-Process `
        -FilePath $command.Source `
        -ArgumentList $Arguments `
        -WorkingDirectory (Get-Location).Path `
        -NoNewWindow `
        -RedirectStandardOutput $stdout `
        -RedirectStandardError $stderr `
        -PassThru
    $finished = if ($TimeoutSec -gt 0) {
        $process.WaitForExit($TimeoutSec * 1000)
    } else {
        $process.WaitForExit()
        $true
    }
    $stdoutLines = if (Test-Path $stdout) { @(Get-Content -Path $stdout) } else { @() }
    $stderrLines = if (Test-Path $stderr) { @(Get-Content -Path $stderr) } else { @() }
    if (-not $finished) {
        Stop-ProcessTree $process.Id
        $stdoutLines | Write-Host
        $stderrLines | Write-Host
        throw "Command timed out after ${TimeoutSec}s: $FilePath $($Arguments -join ' ')"
    }
    $stdoutLines | Write-Host
    $stderrLines | Write-Host
    $process.Refresh()
    if ($null -eq $process.ExitCode -and ($stdoutLines -join "`n") -like "*BUILD SUCCESS*") {
        return
    }
    if ($process.ExitCode -ne 0) {
        throw "Command failed with exit code $($process.ExitCode): $FilePath $($Arguments -join ' ')"
    }
}

function Add-StepResult([string]$Title, [string]$Status, [int]$Seconds, [string]$Message) {
    $script:results.Add([pscustomobject]@{
        Title = $Title
        Status = $Status
        Seconds = $Seconds
        Message = $Message
    })
}

function Invoke-Step([string]$Title, [scriptblock]$Action) {
    Write-Host ""
    Write-Host "== $Title =="
    $started = Get-Date
    try {
        & $Action
        $seconds = [int]((Get-Date) - $started).TotalSeconds
        Add-StepResult $Title "PASS" $seconds ""
        Write-Host ("PASS: {0} ({1}s)" -f $Title, $seconds)
    } catch {
        $seconds = [int]((Get-Date) - $started).TotalSeconds
        $message = $_.Exception.Message
        Add-StepResult $Title "FAIL" $seconds $message
        Write-Host ("FAIL: {0} ({1}s) - {2}" -f $Title, $seconds, $message)
    }
}

function Add-Skip([string]$Title, [string]$Message) {
    Add-StepResult $Title "SKIP" 0 $Message
    Write-Host ("SKIP: {0} - {1}" -f $Title, $Message)
}

function Invoke-MavenTestGroup([string]$Title, [string]$TestSelector) {
    Invoke-Step $Title {
        $env:DB_PASSWORD = $DbPassword
        Invoke-NativeCommand "mvn" @("test", "-Dtest=$TestSelector") $MavenTimeoutSec
    }
}

if (-not $SkipMaven) {
    Push-Location $backendDir
    try {
        Invoke-MavenTestGroup "System permissions and menus regression" "SystemPermissionRegressionTests#workbenchWithoutSalaryTodoDoesNotReturnSalaryItems+workbenchCsvRequiresExportPermission+governanceAcceptanceAndGeneratedIssueActionsRequireDedicatedMenus+roleTemplatesRequireRolePermissionAndCanApplyTemplate+createUserCanAssignInitialRolesAndOrganizations+authorizationChangesAreAudited"
        Invoke-MavenTestGroup "Workbench todo cache regression" "SystemPermissionRegressionTests#salaryTodoCacheCanBeMarkedDirtyByBaseDataChange+personBaseChangeRegistrationMarksSalaryTodoCacheDirty+workbenchUserStatePersistsPerCurrentUser+salaryTodoItemIncludesLatestBaseChangeSummary"
        Invoke-MavenTestGroup "Person maintenance dirty-cache regression" "SystemPermissionRegressionTests#personPostMaintenanceMarksSalaryTodoCacheDirty+personEducationMaintenanceMarksSalaryTodoCacheDirty+personAssessmentMaintenanceMarksSalaryTodoCacheDirty+personBaseInfoMaintenanceMarksSalaryTodoCacheDirty"
        Invoke-MavenTestGroup "Salary todo candidate filter regression" "SystemPermissionRegressionTests#salaryTodoRefreshExcludesBlockedBasePostCandidates"
        Invoke-MavenTestGroup "Salary case workflow regression" "SystemPermissionRegressionTests#workbenchItemsRespectOrganizationScope+salaryTodoCanBeCompletedIntoWorkbenchDoneCase+salaryDifferentTrialRequiresDifferenceReason+salaryCaseCompletionRespectsOrganizationScope+salaryCaseDetailRespectsOrganizationScope"
        Invoke-MavenTestGroup "History write safety regression" "SystemPermissionRegressionTests#historyWriteExecuteCreatesHisbaseRowAndUpdatesSidChain+historyWriteExecuteBlocksWhenSidChainChangesAfterPreview"
        Invoke-MavenTestGroup "Organization scope and generated timeline regression" "SystemPermissionRegressionTests#organizationScopeRestrictsPeopleApis+organizationScopeRestrictsOrganizationTree+organizationScopeRestrictsSalaryApis+organizationScopeRestrictsSalaryHistoryDetails+organizationScopeRestrictsSalaryActionCommands+generatedTimelineBatchRespectsOrganizationScopeAndExportsCsv+generatedTimelineIssueTodoCanBeReviewedAndRetested"
        Invoke-MavenTestGroup "Migration support endpoint regression" "SystemPermissionRegressionTests#migrationSupportEndpointsCoverFormsApplicationGovernanceReportsAndAcceptance"
        Invoke-MavenTestGroup "Salary rule and timeline regression" "NormalGradeTrialRegressionTests,SalaryTimelineRegressionTests"
    } finally {
        Pop-Location
    }
} else {
    Add-Skip "Maven regression gates" "SkipMaven was specified."
}

if (-not $SkipSamples) {
    Invoke-Step "Local service probe" {
        try {
            $response = Invoke-WebRequest -Uri $BaseUrl -UseBasicParsing -TimeoutSec $TimeoutSec
            Write-Host ("Service responded: HTTP {0}" -f $response.StatusCode)
        } catch {
            throw "Local service probe failed for $BaseUrl. Start the backend before running sample gates. $($_.Exception.Message)"
        }
    }

    $sampleArgs = @{
        BaseUrl = $BaseUrl
        TimeoutSec = $TimeoutSec
        Username = $Username
        Password = $Password
    }
    if ($FailOnUnexpected) {
        $sampleArgs.FailOnUnexpected = $true
    }
    $requiredSalarySampleFiles = @(
        (Join-Path $backendDir "target\cross-type-samples.tsv"),
        (Join-Path $backendDir "target\normal-grade-expanded-samples.tsv")
    )
    $missingSalarySampleFiles = @($requiredSalarySampleFiles | Where-Object { -not (Test-Path -LiteralPath $_) })
    if ($missingSalarySampleFiles.Count -gt 0) {
        Add-Skip "Salary sample gate" ("Generated salary sample TSV files are missing. Run sample build scripts first: " + ($missingSalarySampleFiles -join ", "))
    } else {
        Invoke-Step "Salary sample gate" {
            & (Join-Path $PSScriptRoot "verify-salary-samples.ps1") @sampleArgs
        }
    }
    Invoke-Step "Business acceptance sample gate" {
        & (Join-Path $PSScriptRoot "verify-business-acceptance-samples.ps1") @sampleArgs
    }
    if ($missingSalarySampleFiles.Count -gt 0) {
        Add-Skip "Generated timeline sample gate" "Generated timeline sample gate needs salary sample person codes. Run sample build scripts first."
    } else {
        Invoke-Step "Generated timeline sample gate" {
            & (Join-Path $PSScriptRoot "verify-generated-timeline-samples.ps1") @sampleArgs
        }
    }
    Invoke-Step "Generated timeline normal level contract gate" {
        & (Join-Path $PSScriptRoot "verify-generated-timeline-level-contract.ps1") @sampleArgs
    }
    Invoke-Step "Report print archive sample gate" {
        & (Join-Path $PSScriptRoot "verify-report-print-archive-samples.ps1") @sampleArgs
    }
    Invoke-Step "Report print archive ledger gate" {
        & (Join-Path $PSScriptRoot "verify-report-print-archive-ledger.ps1") @sampleArgs
    }
    Invoke-Step "Report entry matrix gate" {
        & (Join-Path $PSScriptRoot "verify-report-entry-matrix.ps1") @sampleArgs
    }
    Invoke-Step "Report print page gate" {
        & (Join-Path $PSScriptRoot "verify-report-print-pages.ps1") @sampleArgs
    }
    Invoke-Step "Report CSV export gate" {
        & (Join-Path $PSScriptRoot "verify-report-csv-exports.ps1") @sampleArgs
    }
    Invoke-Step "Case report UI contract gate" {
        & (Join-Path $PSScriptRoot "verify-case-report-ui-contract.ps1") @sampleArgs
    }
    Invoke-Step "Case detail UI contract gate" {
        & (Join-Path $PSScriptRoot "verify-case-detail-ui-contract.ps1") @sampleArgs
    }
    Invoke-Step "History write batch safety contract gate" {
        & (Join-Path $PSScriptRoot "verify-history-write-batch-safety-contract.ps1") @sampleArgs
    }
    $queueArgs = $sampleArgs.Clone()
    $queueArgs.MaxSummaryMilliseconds = $MaxSummaryMilliseconds
    Invoke-Step "Report/history queue closure gate" {
        & (Join-Path $PSScriptRoot "verify-report-history-queue-closure.ps1") @queueArgs
    }
} else {
    Add-Skip "Sample gates" "SkipSamples was specified."
}

$results | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8

Write-Host ""
Write-Host "Core migration verification summary:"
$results | Format-Table Status,Title,Seconds,Message -AutoSize
Write-Host ""
Write-Host "Wrote $OutputPath"
Write-Host "Core migration verification completed."

$failed = @($results | Where-Object { $_.Status -eq "FAIL" })
if ($failed.Count -gt 0) {
    throw "Core migration verification failed. See report: $OutputPath"
}
