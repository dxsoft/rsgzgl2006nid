param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [int]$MaxMilliseconds = 3000,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [string]$OutputPath = "target/person-code-options-results.tsv"
)

$ErrorActionPreference = "Stop"

$outputDir = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

$webSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession

function Count-Selectable {
    param([object[]]$Nodes)

    $count = 0
    foreach ($node in @($Nodes)) {
        if ($node.selectable -eq $true) {
            $count++
        }
        if ($null -ne $node.children) {
            $count += Count-Selectable -Nodes @($node.children)
        }
    }
    return $count
}

function Invoke-CodeOptionCheck {
    param(
        [string]$FieldName,
        [int]$MinSelectable
    )

    $sw = [Diagnostics.Stopwatch]::StartNew()
    try {
        $response = Invoke-RestMethod `
            -Uri "$BaseUrl/api/persons/code-options/$FieldName" `
            -Method Get `
            -WebSession $webSession `
            -TimeoutSec $TimeoutSec `
            -ErrorAction Stop
        $sw.Stop()

        if ($response.success -ne $true) {
            throw "API returned success=false"
        }
        if ($sw.ElapsedMilliseconds -gt $MaxMilliseconds) {
            throw "API exceeded ${MaxMilliseconds}ms"
        }

        $rootCount = @($response.data).Count
        $selectableCount = Count-Selectable -Nodes @($response.data)
        if ($selectableCount -lt $MinSelectable) {
            throw "Selectable option count $selectableCount is less than $MinSelectable"
        }

        return [pscustomobject]@{
            Field = $FieldName
            Status = "PASS"
            Milliseconds = $sw.ElapsedMilliseconds
            RootCount = $rootCount
            SelectableCount = $selectableCount
            Message = ""
        }
    } catch {
        $sw.Stop()
        return [pscustomobject]@{
            Field = $FieldName
            Status = "FAIL"
            Milliseconds = $sw.ElapsedMilliseconds
            RootCount = ""
            SelectableCount = ""
            Message = $_.Exception.Message
        }
    }
}

if (-not [string]::IsNullOrWhiteSpace($Username)) {
    $loginBody = @{ username = $Username; password = $Password } | ConvertTo-Json -Compress
    Invoke-RestMethod `
        -Uri "$BaseUrl/api/auth/login" `
        -Method Post `
        -Body $loginBody `
        -ContentType "application/json; charset=utf-8" `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec `
        -ErrorAction Stop | Out-Null
}

$checks = @(
    @{ Field = "ryfl"; Min = 1 },
    @{ Field = "gwfl"; Min = 1 },
    @{ Field = "xlbm"; Min = 1 },
    @{ Field = "zjbm"; Min = 1 },
    @{ Field = "xrzw"; Min = 1 },
    @{ Field = "zwbm"; Min = 1 },
    @{ Field = "xrzwbm"; Min = 1 }
)

$rows = New-Object System.Collections.Generic.List[object]
foreach ($check in $checks) {
    $rows.Add((Invoke-CodeOptionCheck -FieldName $check.Field -MinSelectable $check.Min))
}

$rows | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8
$rows | Format-Table -AutoSize

$failed = @($rows | Where-Object { $_.Status -ne "PASS" })
if ($failed.Count -gt 0) {
    throw "Person code option verification failed: $($failed.Count) check(s). See $OutputPath"
}

Write-Host "Person code option verification passed. Report: $OutputPath"
