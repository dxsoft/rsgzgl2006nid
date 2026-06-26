param(
    [string]$OutputPath = ""
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Push-Location $repoRoot
try {
    if ([string]::IsNullOrWhiteSpace($OutputPath)) {
        $OutputPath = Join-Path "backend" "target\backend-submit-review.md"
    }

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

    $secondBatchPrefixes = @("scripts/")
    $thirdBatchPrefixes = @("docs/")

    $statusLines = @(git -c core.quotePath=false status --porcelain=v1 --untracked-files=all -- backend docs scripts .gitignore)
    $trackedFiles = @(git -c core.quotePath=false ls-files -- backend docs scripts .gitignore)
    $visibleFiles = New-Object System.Collections.Generic.List[string]

    foreach ($line in $statusLines) {
        $path = $line.Substring(3).Trim()
        if (-not $path.EndsWith("/")) {
            $visibleFiles.Add($path)
        }
    }
    foreach ($path in $trackedFiles) {
        if (-not $visibleFiles.Contains($path)) {
            $visibleFiles.Add($path)
        }
    }

    function Test-UnderPath {
        param([string]$Path, [string]$Candidate)
        $normalizedPath = $Path.Replace("\", "/").TrimEnd("/")
        $normalizedCandidate = $Candidate.Replace("\", "/").TrimEnd("/")
        return $normalizedCandidate -eq $normalizedPath -or $normalizedCandidate.StartsWith($normalizedPath + "/")
    }

    function Test-AnyPrefix {
        param([string]$Path, [string[]]$Prefixes)
        foreach ($prefix in $Prefixes) {
            if ($Path.StartsWith($prefix)) {
                return $true
            }
        }
        return $false
    }

    $firstBatchFiles = @($visibleFiles | Where-Object {
        $path = $_
        @($firstBatchPaths | Where-Object { Test-UnderPath -Path $_ -Candidate $path }).Count -gt 0
    } | Sort-Object -Unique)

    $secondBatchFiles = @($visibleFiles | Where-Object {
        (Test-AnyPrefix -Path $_ -Prefixes $secondBatchPrefixes) -and ($firstBatchFiles -notcontains $_)
    } | Sort-Object -Unique)

    $thirdBatchFiles = @($visibleFiles | Where-Object {
        (Test-AnyPrefix -Path $_ -Prefixes $thirdBatchPrefixes) -and ($firstBatchFiles -notcontains $_) -and ($secondBatchFiles -notcontains $_)
    } | Sort-Object -Unique)

    $classified = @($firstBatchFiles + $secondBatchFiles + $thirdBatchFiles | Sort-Object -Unique)
    $unclassified = @($visibleFiles | Where-Object { $classified -notcontains $_ } | Sort-Object -Unique)

    $forbidden = @($statusLines | Where-Object {
        $_ -match "backend/(target|BOOT-INF)(/|$)" `
            -or $_ -match "backend/[^/]+\.log$" `
            -or $_ -match "backend/[^/]+\.pid$" `
            -or $_ -match "backend/spring-run.*\.(out|err)$" `
            -or $_ -match "/~\$" `
            -or $_ -match "^.. ~\$"
    })

    $outputFullPath = Join-Path $repoRoot $OutputPath
    $outputDir = Split-Path -Parent $outputFullPath
    if (-not [string]::IsNullOrWhiteSpace($outputDir)) {
        New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
    }

    $report = New-Object System.Collections.Generic.List[string]
    $report.Add("# Backend Submit Review")
    $report.Add("")
    $report.Add(("Generated at: {0}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss")))
    $report.Add("")
    $report.Add("## Summary")
    $report.Add("")
    $report.Add(("- First batch files: {0}" -f $firstBatchFiles.Count))
    $report.Add(("- Second batch files: {0}" -f $secondBatchFiles.Count))
    $report.Add(("- Third batch files: {0}" -f $thirdBatchFiles.Count))
    $report.Add(("- Unclassified files: {0}" -f $unclassified.Count))
    $report.Add(("- Forbidden visible artifacts: {0}" -f $forbidden.Count))
    $report.Add("")

    $sections = @(
        @{Title = "First Batch Candidates"; Items = $firstBatchFiles},
        @{Title = "Second Batch Candidates"; Items = $secondBatchFiles},
        @{Title = "Third Batch Candidates"; Items = $thirdBatchFiles},
        @{Title = "Unclassified Files"; Items = $unclassified},
        @{Title = "Forbidden Visible Artifacts"; Items = $forbidden}
    )

    foreach ($section in $sections) {
        $report.Add(("## {0}" -f $section.Title))
        $report.Add("")
        if ($section.Items.Count -eq 0) {
            $report.Add("- None")
        } else {
            $section.Items | ForEach-Object { $report.Add(('- `{0}`' -f $_)) }
        }
        $report.Add("")
    }

    $report.Add("## Suggested First Batch Review")
    $report.Add("")
    $report.Add('Use `git status --short -- backend docs scripts .gitignore` and compare with this report before staging.')
    $report.Add("Do not stage build outputs, logs, PID files, or Office temporary files.")

    Set-Content -LiteralPath $outputFullPath -Value $report -Encoding UTF8
    Write-Host ("Submit review written to {0}" -f $outputFullPath) -ForegroundColor Green

    if ($forbidden.Count -gt 0) {
        Write-Host "Forbidden artifacts are visible. Review before staging." -ForegroundColor Red
        exit 1
    }
} finally {
    Pop-Location
}
