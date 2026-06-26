param(
    [switch]$FailOnUntracked,
    [string]$ReportPath = ""
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Push-Location $repoRoot
try {
    if ([string]::IsNullOrWhiteSpace($ReportPath)) {
        $ReportPath = Join-Path "backend" "target\backend-version-control-check.md"
    }

    $requiredPathManifest = Join-Path "scripts" "backend-first-batch-paths.txt"
    if (-not (Test-Path -LiteralPath $requiredPathManifest)) {
        Write-Host "Required path manifest is missing: $requiredPathManifest" -ForegroundColor Red
        exit 1
    }
    $requiredPaths = @(Get-Content -LiteralPath $requiredPathManifest | Where-Object {
        -not [string]::IsNullOrWhiteSpace($_) -and -not $_.TrimStart().StartsWith("#")
    })

    $missing = @($requiredPaths | Where-Object { -not (Test-Path -LiteralPath $_) })
    if ($missing.Count -gt 0) {
        Write-Host "Missing required backend migration paths:" -ForegroundColor Red
        $missing | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
        exit 1
    }

    $statusLines = @(git -c core.quotePath=false status --porcelain=v1 --untracked-files=all -- backend docs scripts .gitignore)
    $untrackedLines = @($statusLines | Where-Object { $_.StartsWith("?? ") })
    $modifiedLines = @($statusLines | Where-Object { -not $_.StartsWith("?? ") })
    $forbidden = @($statusLines | Where-Object {
        $_ -match "backend/(target|BOOT-INF)(/|$)" `
            -or $_ -match "backend/[^/]+\.log$" `
            -or $_ -match "backend/[^/]+\.pid$" `
            -or $_ -match "backend/spring-run.*\.(out|err)$"
    })
    if ($forbidden.Count -gt 0) {
        Write-Host "Forbidden backend build/runtime artifacts are visible to Git:" -ForegroundColor Red
        $forbidden | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
        exit 1
    }

    $trackedFiles = @(git -c core.quotePath=false ls-files -- backend docs scripts .gitignore)
    $otherFiles = @(git -c core.quotePath=false ls-files --others --exclude-standard -- backend docs scripts .gitignore)
    $visibleFiles = @($trackedFiles + $otherFiles | Sort-Object -Unique)
    $textFilePattern = '\.(java|js|css|html|xml|yml|yaml|properties|sql|ps1|mjs|md|txt|tsv|csv|gitignore)$'
    $localPasswordLiteral = "dx" + "262105"
    $sensitiveLiteralFindings = @($visibleFiles | Where-Object {
        $_ -match $textFilePattern -and (Test-Path -LiteralPath $_ -PathType Leaf)
    } | Select-String -SimpleMatch $localPasswordLiteral)
    if ($sensitiveLiteralFindings.Count -gt 0) {
        Write-Host "Sensitive local password literal is visible to Git:" -ForegroundColor Red
        $sensitiveLiteralFindings | ForEach-Object {
            Write-Host ("  {0}:{1}" -f $_.Path, $_.LineNumber) -ForegroundColor Red
        }
        exit 1
    }

    $untrackedRequired = New-Object System.Collections.Generic.List[string]
    foreach ($path in $requiredPaths) {
        $normalized = $path.Replace("\", "/")
        $isTracked = $false
        if (Test-Path -LiteralPath $path -PathType Leaf) {
            $isTracked = $trackedFiles -contains $normalized
        } else {
            $prefix = $normalized.TrimEnd("/") + "/"
            $isTracked = @($trackedFiles | Where-Object { $_.StartsWith($prefix) }).Count -gt 0
        }
        if (-not $isTracked) {
            $untrackedRequired.Add($path)
        }
    }

    if ($untrackedRequired.Count -gt 0) {
        Write-Host "Required backend migration paths are not yet tracked:" -ForegroundColor Yellow
        $untrackedRequired | ForEach-Object { Write-Host "  $_" -ForegroundColor Yellow }
    } else {
        Write-Host "All required backend migration paths are tracked." -ForegroundColor Green
    }

    Write-Host "No backend build/runtime artifacts are visible to Git." -ForegroundColor Green
    Write-Host "No sensitive local password literals are visible to Git." -ForegroundColor Green
    $reportFullPath = Join-Path $repoRoot $ReportPath
    $reportDir = Split-Path -Parent $reportFullPath
    if (-not [string]::IsNullOrWhiteSpace($reportDir)) {
        New-Item -ItemType Directory -Force -Path $reportDir | Out-Null
    }
    $report = New-Object System.Collections.Generic.List[string]
    $report.Add("# Backend Version Control Check")
    $report.Add("")
    $report.Add(("Generated at: {0}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss")))
    $report.Add("")
    $report.Add("## Summary")
    $report.Add("")
    $report.Add(("- Missing required paths: {0}" -f $missing.Count))
    $report.Add(("- Required paths not tracked: {0}" -f $untrackedRequired.Count))
    $report.Add(("- Forbidden artifacts visible to Git: {0}" -f $forbidden.Count))
    $report.Add(("- Sensitive local password literal findings: {0}" -f $sensitiveLiteralFindings.Count))
    $report.Add(("- Modified tracked lines in scope: {0}" -f $modifiedLines.Count))
    $report.Add(("- Untracked lines in scope: {0}" -f $untrackedLines.Count))
    $report.Add("")
    $report.Add("## Required Paths Not Yet Tracked")
    $report.Add("")
    if ($untrackedRequired.Count -eq 0) {
        $report.Add("- None")
    } else {
        $untrackedRequired | ForEach-Object { $report.Add(('- `{0}`' -f $_)) }
    }
    $report.Add("")
    $report.Add("## Forbidden Artifacts Visible To Git")
    $report.Add("")
    if ($forbidden.Count -eq 0) {
        $report.Add("- None")
    } else {
        $forbidden | ForEach-Object { $report.Add(('- `{0}`' -f $_)) }
    }
    $report.Add("")
    $report.Add("## Sensitive Local Password Literal Findings")
    $report.Add("")
    if ($sensitiveLiteralFindings.Count -eq 0) {
        $report.Add("- None")
    } else {
        $sensitiveLiteralFindings | ForEach-Object {
            $report.Add(('- `{0}:{1}`' -f $_.Path, $_.LineNumber))
        }
    }
    $report.Add("")
    $report.Add("## Modified Tracked Files In Scope")
    $report.Add("")
    if ($modifiedLines.Count -eq 0) {
        $report.Add("- None")
    } else {
        $modifiedLines | ForEach-Object { $report.Add(('- `{0}`' -f $_)) }
    }
    $report.Add("")
    $report.Add("## Untracked Files And Directories In Scope")
    $report.Add("")
    if ($untrackedLines.Count -eq 0) {
        $report.Add("- None")
    } else {
        $untrackedLines | ForEach-Object { $report.Add(('- `{0}`' -f $_)) }
    }
    $report.Add("")
    $report.Add("## Recommended Next Step")
    $report.Add("")
    if ($untrackedRequired.Count -gt 0) {
        $report.Add("Track the required backend migration paths before creating a clean migration commit.")
        $report.Add("")
        $report.Add("Suggested review command:")
        $report.Add("")
        $report.Add('```powershell')
        $report.Add("git status --short -- backend docs scripts .gitignore")
        $report.Add('```')
    } else {
        $report.Add('Run the strict check with `-FailOnUntracked`, then stage the reviewed migration files.')
    }
    Set-Content -LiteralPath $reportFullPath -Value $report -Encoding UTF8
    Write-Host ("Report written to {0}" -f $reportFullPath) -ForegroundColor Green
    if ($FailOnUntracked -and $untrackedRequired.Count -gt 0) {
        Write-Host "Version-control check failed because required paths are not tracked." -ForegroundColor Red
        exit 1
    }
    Write-Host "Version-control check completed." -ForegroundColor Green
} finally {
    Pop-Location
}
