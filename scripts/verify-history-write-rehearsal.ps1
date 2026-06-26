param(
    [string]$DbPassword = "",
    [string]$ReportPath = ""
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"
$targetDir = Join-Path $backendDir "target"
if ([string]::IsNullOrWhiteSpace($ReportPath)) {
    $ReportPath = Join-Path $targetDir "history-write-rehearsal-report.txt"
}
New-Item -ItemType Directory -Force -Path $targetDir | Out-Null

if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    $DbPassword = $env:DB_PASSWORD
}
if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    throw "DB_PASSWORD is required. Set `$env:DB_PASSWORD or pass -DbPassword."
}

$startedAt = Get-Date
$report = New-Object System.Collections.Generic.List[string]

function Add-Report([string]$Line = "") {
    $script:report.Add($Line)
    Write-Host $Line
}

Add-Report "# History Write Rehearsal Report"
Add-Report ("GeneratedAt: {0}" -f $startedAt.ToString("yyyy-MM-dd HH:mm:ss"))
Add-Report ("GitCommit: {0}" -f ((& git -C $root rev-parse --short HEAD 2>$null) -join ""))
Add-Report "CaseNo: GZ-TMP-HISTORY-WRITE"
Add-Report "WorkItemId: tmp-test-history-write-success"
Add-Report "PersonCode: 001-00055"
Add-Report "Period: 2099-01"
Add-Report "Scope: controlled temporary rehearsal data; writes are rolled back by the test."
Add-Report ""
Add-Report "Checks:"
Add-Report "- prepare temporary source hisbase row"
Add-Report "- create done salary case and snapshot"
Add-Report "- preview and confirm history write"
Add-Report "- batch execute one prepared write plan"
Add-Report "- verify inserted hisbase amount and sid chain"
Add-Report "- compare written fields and export comparison CSV"
Add-Report "- export write audit CSV"
Add-Report "- preview rollback and batch rollback"
Add-Report "- verify inserted hisbase row is deleted and sid chain is restored"
Add-Report ""

Push-Location $backendDir
try {
    $env:DB_PASSWORD = $DbPassword
    $output = & mvn "-Dtest=SystemPermissionRegressionTests#historyWriteExecuteCreatesHisbaseRowAndUpdatesSidChain" test
    $exitCode = $LASTEXITCODE
    foreach ($line in $output) {
        if ($null -ne $line) {
            Add-Report $line.ToString()
        }
    }
    if ($exitCode -ne 0) {
        Add-Report ""
        Add-Report ("Overall: FAIL exitCode={0}" -f $exitCode)
        throw "History write rehearsal failed. See report: $ReportPath"
    }
} finally {
    Pop-Location
}

Add-Report ""
Add-Report "Overall: PASS"
Add-Report ("FinishedAt: {0}" -f (Get-Date).ToString("yyyy-MM-dd HH:mm:ss"))
$report | Set-Content -Encoding UTF8 -Path $ReportPath

Write-Host ""
Write-Host "History write rehearsal passed. Report: $ReportPath"
