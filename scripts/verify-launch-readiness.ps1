param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [string]$DbPassword = "",
    [string]$ReportPath = "",
    [int]$MavenTimeoutSec = 300,
    [switch]$FullCoreMigration,
    [switch]$SkipMavenRegression,
    [switch]$SkipCoreMigration,
    [switch]$StartBackend,
    [switch]$StopBackendAfter,
    [switch]$SkipOnlineBusinessClosure,
    [switch]$SkipPackage
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"
$targetDir = Join-Path $backendDir "target"
if ([string]::IsNullOrWhiteSpace($ReportPath)) {
    $ReportPath = Join-Path $targetDir "launch-readiness-report.txt"
}
New-Item -ItemType Directory -Force -Path $targetDir | Out-Null

$report = New-Object System.Collections.Generic.List[string]
$results = New-Object System.Collections.Generic.List[object]
$startedAt = Get-Date
if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    $DbPassword = $env:DB_PASSWORD
}
$requiresDbPassword = $StartBackend -or -not $SkipMavenRegression -or -not $SkipCoreMigration -or -not $SkipPackage
if ($requiresDbPassword -and [string]::IsNullOrWhiteSpace($DbPassword)) {
    throw "DB_PASSWORD is required for backend start, Maven regression, core migration, or packaging. Set `$env:DB_PASSWORD or pass -DbPassword."
}

function Add-Report([string]$Line = "") {
    $script:report.Add($Line)
    Write-Host $Line
}

function Stop-ProcessTree([int]$ProcessId) {
    try {
        $children = @(Get-CimInstance Win32_Process | Where-Object { $_.ParentProcessId -eq $ProcessId })
        foreach ($child in $children) {
            Stop-ProcessTree ([int]$child.ProcessId)
        }
    } catch {
        # Process tree inspection can be blocked in restricted shells; kill the root below.
    }
    Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
}

function Invoke-NativeCommand([string]$FilePath, [string[]]$Arguments, [int]$TimeoutSec = 0) {
    $command = Get-Command $FilePath -ErrorAction Stop
    $stdout = Join-Path $targetDir ("launch-native-out-" + [guid]::NewGuid().ToString("N") + ".log")
    $stderr = Join-Path $targetDir ("launch-native-err-" + [guid]::NewGuid().ToString("N") + ".log")
    $quoteArgument = {
        param([string]$Value)
        if ($null -eq $Value) {
            return '""'
        }
        $escaped = $Value.Replace('"', '\"')
        if ($escaped -match '\s') {
            return '"' + $escaped + '"'
        }
        return $escaped
    }
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $command.Source
    $psi.Arguments = (($Arguments | ForEach-Object { & $quoteArgument $_ }) -join " ")
    $psi.WorkingDirectory = (Get-Location).Path
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $psi
    if (-not $process.Start()) {
        throw "Command failed to start: $FilePath $($Arguments -join ' ')"
    }
    $stdoutTask = $process.StandardOutput.ReadToEndAsync()
    $stderrTask = $process.StandardError.ReadToEndAsync()
    $finished = if ($TimeoutSec -gt 0) {
        $process.WaitForExit($TimeoutSec * 1000)
    } else {
        $process.WaitForExit()
        $true
    }
    if (-not $finished) {
        Stop-ProcessTree $process.Id
        $process.WaitForExit()
    }
    $stdoutText = $stdoutTask.Result
    $stderrText = $stderrTask.Result
    $stdoutText | Set-Content -Encoding UTF8 -Path $stdout
    $stderrText | Set-Content -Encoding UTF8 -Path $stderr
    $stdoutLines = @($stdoutText -split "`r?`n" | Where-Object { $_ -ne "" })
    $stderrLines = @($stderrText -split "`r?`n" | Where-Object { $_ -ne "" })
    foreach ($line in $stdoutLines) {
        Write-Output $line
    }
    foreach ($line in $stderrLines) {
        Write-Output $line
    }
    if (-not $finished) {
        throw "Command timed out after ${TimeoutSec}s: $FilePath $($Arguments -join ' '). stdout=$stdout stderr=$stderr"
    }
    $process.Refresh()
    if ($process.ExitCode -ne 0) {
        throw "Command failed with exit code $($process.ExitCode): $FilePath $($Arguments -join ' '). stdout=$stdout stderr=$stderr"
    }
}

function Invoke-ReadinessStep([string]$Title, [scriptblock]$Action) {
    Add-Report ""
    Add-Report "== $Title =="
    $stepStarted = Get-Date
    try {
        $output = & $Action
        foreach ($line in $output) {
            if ($null -ne $line) {
                Add-Report ("  " + $line.ToString())
            }
        }
        $duration = [int]((Get-Date) - $stepStarted).TotalSeconds
        Add-Report ("PASS: {0} ({1}s)" -f $Title, $duration)
        $script:results.Add([pscustomobject]@{ Title = $Title; Status = "PASS"; Seconds = $duration; Message = "" })
    } catch {
        $duration = [int]((Get-Date) - $stepStarted).TotalSeconds
        $message = $_.Exception.Message
        Add-Report ("FAIL: {0} ({1}s) - {2}" -f $Title, $duration, $message)
        $script:results.Add([pscustomobject]@{ Title = $Title; Status = "FAIL"; Seconds = $duration; Message = $message })
    }
}

function Assert-Contains([string]$Value, [string]$Expected, [string]$Message) {
    if ($Value -notlike "*$Expected*") {
        throw $Message
    }
}

function Resolve-BaseUrlPort([string]$Url) {
    try {
        $uri = [System.Uri]$Url
        if ($uri.Port -gt 0) {
            return $uri.Port
        }
    } catch {
    }
    return 18080
}

$gitCommit = (& git -C $root rev-parse --short HEAD 2>$null)
$gitStatus = (& git -C $root status --short 2>$null)

Add-Report "# Launch Readiness Report"
Add-Report ("GeneratedAt: {0}" -f $startedAt.ToString("yyyy-MM-dd HH:mm:ss"))
Add-Report ("BaseUrl: {0}" -f $BaseUrl)
Add-Report ("GitCommit: {0}" -f ($gitCommit -join ""))
Add-Report ("ReportPath: {0}" -f $ReportPath)
Add-Report ("WorkspaceChanges: {0}" -f (@($gitStatus).Count))

Invoke-ReadinessStep "Required launch documents" {
    $docsDir = Join-Path $root "docs"
    $checklist = Get-ChildItem -Path $docsDir -Filter "*.md" |
        Where-Object { Select-String -Path $_.FullName -Pattern "SALARY_HISTORY_WRITE" -Quiet } |
        Select-Object -First 1
    $coreDoc = Join-Path $root "docs/core-migration-verification.md"
    $readme = Join-Path $backendDir "README.md"
    if ($null -eq $checklist) {
        throw "Launch checklist document containing SALARY_HISTORY_WRITE is missing under $docsDir"
    }
    foreach ($path in @($checklist.FullName, $coreDoc, $readme)) {
        if (-not (Test-Path $path)) {
            throw "Required document is missing: $path"
        }
        "Found: $path"
    }
    $coreText = Get-Content -Encoding UTF8 $coreDoc -Raw
    $readmeText = Get-Content -Encoding UTF8 $readme -Raw
    Assert-Contains $coreText "history-write batch" "Core migration document does not link the launch checklist."
    Assert-Contains $readmeText "production history-write preparation" "Backend README does not link the launch checklist."
}

if ($StartBackend) {
    Invoke-ReadinessStep "Start managed backend" {
        & (Join-Path $PSScriptRoot "start-backend-dev.ps1") `
            -DbPassword $DbPassword `
            -Port (Resolve-BaseUrlPort $BaseUrl) `
            -TimeoutSec 120
    }
}

Invoke-ReadinessStep "Local service probe" {
    $response = Invoke-WebRequest -Uri $BaseUrl -UseBasicParsing -TimeoutSec $TimeoutSec
    "HTTP status: $($response.StatusCode)"
}

Invoke-ReadinessStep "History write permissions exposed" {
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
        "Logged in as $Username"
    }
    $templates = Invoke-RestMethod `
        -Uri "$BaseUrl/api/system/role-templates" `
        -Method Get `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec
    $json = $templates | ConvertTo-Json -Depth 20 -Compress
    Assert-Contains $json "SALARY_HISTORY_WRITE" "SALARY_HISTORY_WRITE is not exposed by role templates."
    Assert-Contains $json "SALARY_HISTORY_ROLLBACK" "SALARY_HISTORY_ROLLBACK is not exposed by role templates."
    "SALARY_HISTORY_WRITE and SALARY_HISTORY_ROLLBACK are available."
}

Invoke-ReadinessStep "Maven regression gates" {
    if ($SkipMavenRegression) {
        "Skipped by -SkipMavenRegression. Run verify-core-migration.ps1 -SkipSamples before production history-write."
        return
    }
    if ($FullCoreMigration) {
        "Covered by the full core migration gate."
        return
    }
    $args = @(
        "-ExecutionPolicy", "Bypass",
        "-File", (Join-Path $PSScriptRoot "verify-core-migration.ps1"),
        "-BaseUrl", $BaseUrl,
        "-TimeoutSec", "$TimeoutSec",
        "-Username", $Username,
        "-Password", $Password,
        "-DbPassword", $DbPassword,
        "-MavenTimeoutSec", "$MavenTimeoutSec",
        "-SkipSamples"
    )
    $env:DB_PASSWORD = $DbPassword
    Invoke-NativeCommand "powershell" $args ($MavenTimeoutSec * 10)
}

if (-not $SkipCoreMigration) {
    Invoke-ReadinessStep "Core migration gate" {
        $args = @(
            "-ExecutionPolicy", "Bypass",
            "-File", (Join-Path $PSScriptRoot "verify-core-migration.ps1"),
            "-BaseUrl", $BaseUrl,
            "-TimeoutSec", "$TimeoutSec",
            "-Username", $Username,
            "-Password", $Password,
            "-DbPassword", $DbPassword,
            "-MavenTimeoutSec", "$MavenTimeoutSec",
            "-FailOnUnexpected"
        )
        if (-not $FullCoreMigration) {
            $args += "-SkipMaven"
        }
        $env:DB_PASSWORD = $DbPassword
        Invoke-NativeCommand "powershell" $args
    }
}

if (-not $SkipOnlineBusinessClosure) {
    Invoke-ReadinessStep "Online business closure gate" {
        $args = @(
            "-ExecutionPolicy", "Bypass",
            "-File", (Join-Path $PSScriptRoot "verify-online-business-closure.ps1"),
            "-BaseUrl", $BaseUrl,
            "-TimeoutSec", "$TimeoutSec",
            "-Username", $Username,
            "-Password", $Password,
            "-DbPassword", $DbPassword
        )
        Invoke-NativeCommand "powershell" $args ($MavenTimeoutSec * 10)
    }
}

if (-not $SkipPackage) {
    Invoke-ReadinessStep "Package jar" {
        Push-Location $backendDir
        try {
            $env:DB_PASSWORD = $DbPassword
            Invoke-NativeCommand "mvn" @("-DskipTests", "package") $MavenTimeoutSec
        } finally {
            Pop-Location
        }
    }
}

if ($StopBackendAfter) {
    Invoke-ReadinessStep "Stop managed backend" {
        & (Join-Path $PSScriptRoot "stop-backend-dev.ps1")
    }
}

$failed = @($results | Where-Object { $_.Status -ne "PASS" })
Add-Report ""
Add-Report "== Summary =="
foreach ($result in $results) {
    Add-Report ("{0}`t{1}`t{2}s`t{3}" -f $result.Status, $result.Title, $result.Seconds, $result.Message)
}
Add-Report ("Overall: {0}" -f ($(if ($failed.Count -eq 0) { "PASS" } else { "FAIL" })))
Add-Report ("FinishedAt: {0}" -f (Get-Date).ToString("yyyy-MM-dd HH:mm:ss"))

$report | Set-Content -Encoding UTF8 -Path $ReportPath

if ($failed.Count -gt 0) {
    throw "Launch readiness failed. See report: $ReportPath"
}

Write-Host ""
Write-Host "Launch readiness passed. Report: $ReportPath"
