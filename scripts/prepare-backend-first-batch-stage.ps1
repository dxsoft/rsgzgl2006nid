param(
    [switch]$Apply,
    [string]$ManifestPath = ""
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Push-Location $repoRoot
try {
    if ([string]::IsNullOrWhiteSpace($ManifestPath)) {
        $ManifestPath = Join-Path "backend" "target\backend-first-batch-stage-files.txt"
    }

    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-backend-version-control.ps1 | Out-Host
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\build-backend-submit-review.ps1 | Out-Host

    $firstBatchPaths = @(
        "backend/pom.xml",
        "backend/README.md",
        "backend/src/main/java/com/dx/rsgzgl/RsgzglBackendApplication.java",
        "backend/src/main/java/com/dx/rsgzgl/common",
        "backend/src/main/java/com/dx/rsgzgl/config",
        "backend/src/main/java/com/dx/rsgzgl/org",
        "backend/src/main/java/com/dx/rsgzgl/person",
        "backend/src/main/java/com/dx/rsgzgl/salary",
        "backend/src/main/java/com/dx/rsgzgl/system",
        "backend/scripts",
        "backend/src/main/resources/application.yml",
        "backend/src/main/resources/db",
        "backend/src/main/resources/static/index.html",
        "backend/src/main/resources/static/app.js",
        "backend/src/main/resources/static/styles.css",
        "backend/src/test/java/com/dx/rsgzgl",
        "docs/backend-migration-version-control-checklist.md",
        "docs/backend-migration-submit-manifest.md",
        "scripts/build-backend-submit-review.ps1",
        "scripts/check-backend-version-control.ps1",
        "scripts/prepare-backend-first-batch-stage.ps1",
        "scripts/start-backend-dev.ps1",
        "scripts/stop-backend-dev.ps1",
        "scripts/verify-auto-regression-samples.ps1",
        "scripts/verify-business-acceptance-samples.ps1",
        "scripts/verify-case-report-ui-contract.ps1",
        "scripts/verify-core-migration.ps1",
        "scripts/verify-generated-timeline-level-contract.ps1",
        "scripts/verify-generated-timeline-samples.ps1",
        "scripts/verify-history-write-rehearsal.ps1",
        "scripts/verify-launch-readiness.ps1",
        "scripts/verify-online-business-closure.ps1",
        "scripts/verify-report-csv-exports.ps1",
        "scripts/verify-report-history-queue-closure.ps1",
        "scripts/verify-report-print-archive-ledger.ps1",
        "scripts/verify-report-print-archive-samples.ps1",
        "scripts/verify-report-print-pages.ps1",
        "scripts/verify-salary-samples.ps1",
        ".gitignore"
    )

    $statusLines = @(git -c core.quotePath=false status --porcelain=v1 --untracked-files=all -- backend docs scripts .gitignore)
    $trackedFiles = @(git -c core.quotePath=false ls-files -- backend docs scripts .gitignore)
    $otherFiles = @(git -c core.quotePath=false ls-files --others --exclude-standard -- backend docs scripts .gitignore)
    $visibleFiles = @($trackedFiles + $otherFiles | Sort-Object -Unique)

    function Test-UnderPath {
        param([string]$Path, [string]$Candidate)
        $normalizedPath = $Path.Replace("\", "/").TrimEnd("/")
        $normalizedCandidate = $Candidate.Replace("\", "/").TrimEnd("/")
        return $normalizedCandidate -eq $normalizedPath -or $normalizedCandidate.StartsWith($normalizedPath + "/")
    }

    $firstBatchFiles = @($visibleFiles | Where-Object {
        $path = $_
        @($firstBatchPaths | Where-Object { Test-UnderPath -Path $_ -Candidate $path }).Count -gt 0
    } | Sort-Object -Unique)

    $forbidden = @($statusLines | Where-Object {
        $_ -match "backend/(target|BOOT-INF)(/|$)" `
            -or $_ -match "backend/[^/]+\.log$" `
            -or $_ -match "backend/[^/]+\.pid$" `
            -or $_ -match "backend/spring-run.*\.(out|err)$" `
            -or $_ -match "/~\$" `
            -or $_ -match "^.. ~\$"
    })
    if ($forbidden.Count -gt 0) {
        Write-Host "Forbidden artifacts are visible. Refusing to prepare staging list." -ForegroundColor Red
        $forbidden | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
        exit 1
    }

    $manifestFullPath = Join-Path $repoRoot $ManifestPath
    $manifestDir = Split-Path -Parent $manifestFullPath
    if (-not [string]::IsNullOrWhiteSpace($manifestDir)) {
        New-Item -ItemType Directory -Force -Path $manifestDir | Out-Null
    }
    Set-Content -LiteralPath $manifestFullPath -Value $firstBatchFiles -Encoding UTF8

    Write-Host ("First batch staging manifest written to {0}" -f $manifestFullPath) -ForegroundColor Green
    Write-Host ("First batch file count: {0}" -f $firstBatchFiles.Count) -ForegroundColor Green

    if (-not $Apply) {
        Write-Host "Preview only. Re-run with -Apply to execute git add for this manifest." -ForegroundColor Yellow
        Write-Host "Suggested command:" -ForegroundColor Yellow
        Write-Host "  powershell -NoProfile -ExecutionPolicy Bypass -File scripts\prepare-backend-first-batch-stage.ps1 -Apply" -ForegroundColor Yellow
        return
    }

    git add -- $firstBatchFiles
    Write-Host "First batch files have been staged." -ForegroundColor Green
} finally {
    Pop-Location
}
