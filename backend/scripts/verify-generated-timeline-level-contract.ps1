param(
    [switch]$FailOnUnexpected,
    [string]$OutputPath = "target/generated-timeline-level-contract-results.tsv"
)

$ErrorActionPreference = "Stop"

$scriptPath = (Resolve-Path $MyInvocation.MyCommand.Path).Path
$root = Split-Path -Parent (Split-Path -Parent $scriptPath)
$servicePath = Join-Path $root "src/main/java/com/dx/rsgzgl/salary/service/impl/DefaultSalaryGeneratedTimelineService.java"
$mapperPath = Join-Path $root "src/main/java/com/dx/rsgzgl/salary/mapper/LegacySalaryMapper.java"
$diagnosticsPath = Join-Path $root "scripts/build-generated-timeline-diagnostics.ps1"
$testPath = Join-Path $root "src/test/java/com/dx/rsgzgl/salary/SalaryTimelineRegressionTests.java"
$docDir = Join-Path (Split-Path -Parent $root) "docs"

function Add-Check([System.Collections.Generic.List[object]]$Rows, [string]$Code, [bool]$Ok, [string]$Message) {
    $Rows.Add([pscustomobject]@{
        Code = $Code
        Status = if ($Ok) { "OK" } else { "FAIL" }
        Message = $Message
    })
}

if (-not (Test-Path $servicePath)) {
    throw "Missing service source: $servicePath"
}

$service = Get-Content -Encoding UTF8 -Path $servicePath -Raw
$mapper = if (Test-Path $mapperPath) { Get-Content -Encoding UTF8 -Path $mapperPath -Raw } else { "" }
$diagnostics = if (Test-Path $diagnosticsPath) { Get-Content -Encoding UTF8 -Path $diagnosticsPath -Raw } else { "" }
$test = if (Test-Path $testPath) { Get-Content -Encoding UTF8 -Path $testPath -Raw } else { "" }
$doc = ""
$allDocs = ""
if (Test-Path $docDir) {
    $allDocs = (Get-ChildItem -Path $docDir -Filter "*.md" |
        ForEach-Object { Get-Content -Encoding UTF8 -Path $_.FullName -Raw }) -join "`n"
    $docFile = Get-ChildItem -Path $docDir -Filter "*.md" |
        Where-Object {
            $text = Get-Content -Encoding UTF8 -Path $_.FullName -Raw
            $text.Contains("dndkh") -and $text.Contains("01/02/04/21/22")
        } |
        Select-Object -First 1
    if ($null -ne $docFile) {
        $doc = Get-Content -Encoding UTF8 -Path $docFile.FullName -Raw
    }
}

$rows = [System.Collections.Generic.List[object]]::new()
$trialAllowanceText = -join ([char[]](0x5BA1, 0x5224, 0x6D25, 0x8D34))
$prosecutorAllowanceText = -join ([char[]](0x68C0, 0x5BDF, 0x6D25, 0x8D34))
$supervisorAllowanceText = -join ([char[]](0x76D1, 0x5BDF, 0x6D25, 0x8D34))
Add-Check $rows "level-prefix-scope" ($service.Contains("NORMAL_LEVEL_PREFIXES") -and $service.Contains('"01", "02", "04", "21", "22"')) "Normal level candidate prefixes are explicit."
Add-Check $rows "assessment-source" ($service.Contains("FROM dndkh") -and $service.Contains("TRIM(khjg) IN")) "Normal level candidates come from qualified assessment rows."
Add-Check $rows "five-year-cycle" ($service.Contains("qualifiedCount < 5") -and $service.Contains("qualifiedCount % 5")) "Normal level candidates require a five-year qualified cycle."
Add-Check $rows "state-machine-precheck" ($service.Contains("normalLevelPromotionWouldApply") -and $service.Contains("normalGradeTrialService.trial")) "Normal level candidates reuse the normal promotion state machine."
Add-Check $rows "real-level-only" ($service.Contains('startsWith("\u7ea7\u522b\u664b\u5347\uff1a")')) "Highest-level grade turnover is not treated as a normal level promotion."
Add-Check $rows "deduplicate-history-expanded" ($service.Contains("explicitAssessmentLevels") -and $service.Contains("!explicitAssessmentLevels.contains")) "History-assisted expansion does not duplicate explicit base level candidates."
Add-Check $rows "regression-test" ($test.Contains("generatedTimelineDoesNotReportHighestLevelGradeTurnoverAsMissingNormalLevel")) "Regression test covers highest-level grade turnover."
Add-Check $rows "documentation" ($doc.Contains("dndkh") -and $doc.Contains("01/02/04/21/22") -and $doc.Contains("5")) "Documentation records the normal level generation policy."
Add-Check $rows "judicial-conversion-source" ($mapper.Contains("LEFT(TRIM(zwbm), 2) = '03'") -and $mapper.Contains("\u6cd5\u68c0\u5957\u6539")) "Judicial 03-prefix entrance is generated as judicial conversion."
Add-Check $rows "judicial-conversion-post-derived" ($service.Contains("CHANGE_JUDICIAL_CONVERSION.equals(value)") -and $service.Contains("|| CHANGE_JUDICIAL_CONVERSION.equals(value)")) "Judicial conversion participates in post-derived de-duplication."
Add-Check $rows "judicial-conversion-regression" ($test.Contains("generatedTimelineCreatesJudicialConversionWhenPostEntersJudicialPrefix")) "Regression test covers judicial conversion generation."
Add-Check $rows "judicial-conversion-documentation" ($doc.Contains("03") -and $doc.Contains("bz06_zwgz_fj")) "Documentation records judicial 03-prefix generation policy."
Add-Check $rows "rank-allowance-source" ($mapper.Contains("FROM jx") -and $mapper.Contains("previousRank") -and $mapper.Contains("TRIM(jx)") -and $mapper.Contains("LIKE '%\u8b66%'") -and $mapper.Contains("LIKE '%\u6cd5%'") -and $mapper.Contains("LIKE '%\u68c0%'") -and $mapper.Contains("LIKE '%\u76d1%'")) "Rank and allowance candidates are generated from jx.jx content."
Add-Check $rows "rank-allowance-type-alignment" ($service.Contains("alignRankEventsToNearbyHistoryType") -and $service.Contains("CHANGE_POLICE_RANK_ALLOWANCE") -and $service.Contains("CHANGE_JUDGE_ALLOWANCE") -and $service.Contains("CHANGE_PROSECUTOR_ALLOWANCE") -and $service.Contains("CHANGE_SUPERVISOR_ALLOWANCE")) "Generated rank events align to legacy allowance wording."
Add-Check $rows "rank-allowance-regression" ($test.Contains("generatedTimelineCreatesRankAllowanceEventFromRankTable")) "Regression test covers rank and allowance generation."
Add-Check $rows "rank-allowance-documentation" ($allDocs.Contains("jx.jx") -and $allDocs.Contains($trialAllowanceText) -and $allDocs.Contains($prosecutorAllowanceText) -and $allDocs.Contains($supervisorAllowanceText) -and $allDocs.Contains("jxjtbz") -and $allDocs.Contains("JXJT")) "Documentation records rank and allowance generation policy."
Add-Check $rows "generated-diagnostics-sources" ($diagnostics.Contains('Source -eq "jx"') -and $diagnostics.Contains('Source -eq "hjxx"') -and $diagnostics.Contains("RANK_BASE") -and $diagnostics.Contains("REWARD_PUNISHMENT_BASE")) "Generated timeline diagnostics classify jx and hjxx sources."

$outputDir = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}
$rows | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8

Write-Host ""
Write-Host "Generated timeline level contract summary:"
$rows | Format-Table Code,Status,Message -AutoSize
Write-Host ""
Write-Host "Wrote $OutputPath"

if ($FailOnUnexpected -and @($rows | Where-Object { $_.Status -ne "OK" }).Count -gt 0) {
    throw "Generated timeline normal level contract failed."
}
