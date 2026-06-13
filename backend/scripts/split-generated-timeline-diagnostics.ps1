param(
    [string]$InputPath = "target/generated-timeline-diagnostics.tsv",
    [string]$OutputDir = "target/diagnostics"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $InputPath)) {
    throw "Input diagnostics file not found: $InputPath"
}

if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

$rows = @(Import-Csv -Path $InputPath -Delimiter "`t")

$categories = @(
    "POST_BASE",
    "EDUCATION_BASE",
    "ASSESSMENT_STATE",
    "HISTORY_NEARBY",
    "BASIC_INFO",
    "MISSING_HISTORY",
    "AMOUNT_DIFF",
    "TRIAL_ERROR",
    "REQUEST_ERROR",
    "OTHER"
)

foreach ($category in $categories) {
    $categoryRows = @($rows | Where-Object { $_.DiagnosisCategory -eq $category })
    if ($categoryRows.Count -eq 0) {
        continue
    }
    $safeName = $category.ToLowerInvariant().Replace("_", "-")
    $categoryRows |
        Sort-Object PersonCode, @{ Expression = { [int]($_.Year -as [int]) } }, @{ Expression = { [int]($_.Month -as [int]) } }, Source, SourceId |
        Export-Csv -Path (Join-Path $OutputDir "$safeName.tsv") -Delimiter "`t" -NoTypeInformation -Encoding UTF8
}

$personSummary = $rows |
    Group-Object PersonCode |
    ForEach-Object {
        $personRows = @($_.Group)
        $categoriesForPerson = @($personRows | Select-Object -ExpandProperty DiagnosisCategory -Unique | Sort-Object)
        $statusesForPerson = @($personRows | Select-Object -ExpandProperty Status -Unique | Sort-Object)
        [pscustomobject]@{
            PersonCode = $_.Name
            IssueCount = $personRows.Count
            Categories = ($categoriesForPerson -join ",")
            Statuses = ($statusesForPerson -join ",")
            FirstYear = ($personRows | Sort-Object @{ Expression = { [int]($_.Year -as [int]) } }, @{ Expression = { [int]($_.Month -as [int]) } } | Select-Object -First 1).Year
            FirstMonth = ($personRows | Sort-Object @{ Expression = { [int]($_.Year -as [int]) } }, @{ Expression = { [int]($_.Month -as [int]) } } | Select-Object -First 1).Month
            FirstCategory = ($personRows | Sort-Object @{ Expression = { [int]($_.Year -as [int]) } }, @{ Expression = { [int]($_.Month -as [int]) } } | Select-Object -First 1).DiagnosisCategory
            FirstStatus = ($personRows | Sort-Object @{ Expression = { [int]($_.Year -as [int]) } }, @{ Expression = { [int]($_.Month -as [int]) } } | Select-Object -First 1).Status
            FirstChangeTypeEsc = ($personRows | Sort-Object @{ Expression = { [int]($_.Year -as [int]) } }, @{ Expression = { [int]($_.Month -as [int]) } } | Select-Object -First 1).ChangeTypeEsc
            FirstMessageEsc = ($personRows | Sort-Object @{ Expression = { [int]($_.Year -as [int]) } }, @{ Expression = { [int]($_.Month -as [int]) } } | Select-Object -First 1).MessageEsc
        }
    } |
    Sort-Object @{ Expression = { [int]$_.IssueCount }; Descending = $true }, PersonCode

$personSummary | Export-Csv -Path (Join-Path $OutputDir "person-summary.tsv") -Delimiter "`t" -NoTypeInformation -Encoding UTF8

$rows |
    Group-Object DiagnosisCategory |
    Sort-Object Count -Descending |
    Select-Object Count, Name |
    Export-Csv -Path (Join-Path $OutputDir "category-summary.tsv") -Delimiter "`t" -NoTypeInformation -Encoding UTF8

$rows |
    Group-Object DiagnosisCategory, Status |
    Sort-Object Count -Descending |
    Select-Object Count, Name |
    Export-Csv -Path (Join-Path $OutputDir "category-status-summary.tsv") -Delimiter "`t" -NoTypeInformation -Encoding UTF8

Write-Host "Wrote split diagnostics to $OutputDir"
Write-Host ""
$rows | Group-Object DiagnosisCategory | Sort-Object Count -Descending | Select-Object Count,Name | Format-Table -AutoSize
