param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [int]$TimeoutSec = 30,
    [string]$Username = "admin",
    [string]$Password = "admin",
    [switch]$FailOnUnexpected,
    [string]$OutputPath = "target/permission-menu-contract-results.tsv"
)

$ErrorActionPreference = "Stop"

$outputDir = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

$webSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession

function Add-Result(
    [System.Collections.Generic.List[object]]$Rows,
    [string]$Code,
    [string]$Status,
    [string]$Message
) {
    $Rows.Add([pscustomobject]@{
        Code = $Code
        Status = $Status
        Message = $Message
    })
}

function Invoke-Api([string]$Path) {
    return Invoke-RestMethod `
        -Uri "$BaseUrl$Path" `
        -Method Get `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec `
        -ErrorAction Stop
}

function Test-Contains(
    [System.Collections.Generic.List[object]]$Rows,
    [string]$Code,
    [string]$Content,
    [string]$Pattern,
    [string]$Message
) {
    if ($Content.Contains($Pattern)) {
        Add-Result $Rows $Code "OK" $Message
    } else {
        Add-Result $Rows $Code "FAIL" ("Missing " + $Pattern)
    }
}

$results = [System.Collections.Generic.List[object]]::new()

try {
    Invoke-RestMethod `
        -Uri "$BaseUrl/api/auth/login" `
        -Method Post `
        -Body (@{ username = $Username; password = $Password } | ConvertTo-Json -Compress) `
        -ContentType "application/json; charset=utf-8" `
        -WebSession $webSession `
        -TimeoutSec $TimeoutSec `
        -ErrorAction Stop | Out-Null
    Add-Result $results "login" "OK" "Authenticated."
} catch {
    Add-Result $results "login" "REQUEST_ERROR" $_.Exception.Message
}

$requiredMenuCodes = @(
    "WORKBENCH",
    "SALARY_PERSON",
    "SALARY_TODO",
    "SALARY_DONE",
    "SALARY_TRIAL",
    "SALARY_RECONCILE",
    "SALARY_EXPORT",
    "SALARY_REPORT",
    "SALARY_HISTORY_WRITE",
    "SALARY_HISTORY_ROLLBACK",
    "MIGRATION",
    "SALARY_GOVERNANCE",
    "SALARY_ACCEPTANCE",
    "SALARY_DELIVERY_ARCHIVE",
    "APPLICATION_TODO",
    "APPLICATION_DONE",
    "SYSTEM_MENU",
    "SYSTEM_ROLE",
    "SYSTEM_USER",
    "SYSTEM_AUDIT",
    "SALARY_CONFIG"
)

try {
    $menuResponse = Invoke-Api "/api/system/menu-admin"
    $menuCodes = @($menuResponse.data | ForEach-Object { "" + $_.code })
    $missing = @($requiredMenuCodes | Where-Object { $menuCodes -notcontains $_ })
    if ($missing.Count -eq 0) {
        Add-Result $results "system-menu-required-codes" "OK" ("menus=" + $menuCodes.Count)
    } else {
        Add-Result $results "system-menu-required-codes" "FAIL" ("Missing " + ($missing -join ","))
    }
} catch {
    Add-Result $results "system-menu-required-codes" "REQUEST_ERROR" $_.Exception.Message
}

try {
    $userMenus = Invoke-Api "/api/system/menus"
    $json = $userMenus.data | ConvertTo-Json -Depth 8 -Compress
    $missing = @("WORKBENCH", "SALARY", "MIGRATION", "SYSTEM") | Where-Object { -not $json.Contains($_) }
    if ($missing.Count -eq 0) {
        Add-Result $results "current-user-menu-tree" "OK" "Workbench, salary, migration, and system roots are visible."
    } else {
        Add-Result $results "current-user-menu-tree" "FAIL" ("Missing " + ($missing -join ","))
    }
} catch {
    Add-Result $results "current-user-menu-tree" "REQUEST_ERROR" $_.Exception.Message
}

try {
    $templates = Invoke-Api "/api/system/role-templates"
    $templateMap = @{}
    foreach ($template in @($templates.data)) {
        $templateMap["" + $template.code] = @($template.menuCodes)
    }
    $templateChecks = @(
        @{ Code = "template-salary-operator"; Template = "SALARY_OPERATOR"; Menus = @("SALARY_TODO", "SALARY_DONE", "SALARY_TRIAL", "SALARY_GOVERNANCE") },
        @{ Code = "template-salary-reviewer"; Template = "SALARY_REVIEWER"; Menus = @("SALARY_DONE", "SALARY_EXPORT", "SALARY_ACCEPTANCE") },
        @{ Code = "template-salary-writer"; Template = "SALARY_WRITER"; Menus = @("SALARY_HISTORY_WRITE", "SALARY_HISTORY_ROLLBACK", "SALARY_EXPORT") },
        @{ Code = "template-rule-steward"; Template = "RULE_STEWARD"; Menus = @("SALARY_CONFIG", "SALARY_TRIAL", "SALARY_RECONCILE") },
        @{ Code = "template-system-auditor"; Template = "SYSTEM_AUDITOR"; Menus = @("SYSTEM_AUDIT") },
        @{ Code = "template-admin"; Template = "ADMIN"; Menus = $requiredMenuCodes }
    )
    foreach ($check in $templateChecks) {
        if (-not $templateMap.ContainsKey($check.Template)) {
            Add-Result $results $check.Code "FAIL" ("Missing template " + $check.Template)
            continue
        }
        $codes = @($templateMap[$check.Template])
        $missing = @($check.Menus | Where-Object { $codes -notcontains $_ })
        if ($missing.Count -eq 0) {
            Add-Result $results $check.Code "OK" ($check.Template + " menus=" + $codes.Count)
        } else {
            Add-Result $results $check.Code "FAIL" ("Missing " + ($missing -join ","))
        }
    }
} catch {
    Add-Result $results "role-template-contract" "REQUEST_ERROR" $_.Exception.Message
}

$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$appJs = Join-Path $root "backend\src\main\resources\static\app.js"
$menuService = Join-Path $root "backend\src\main\java\com\dx\rsgzgl\system\service\SystemMenuService.java"
$adminService = Join-Path $root "backend\src\main\java\com\dx\rsgzgl\system\service\SystemAdminQueryService.java"
$workbenchService = Join-Path $root "backend\src\main\java\com\dx\rsgzgl\system\service\WorkbenchService.java"
$systemController = Join-Path $root "backend\src\main\java\com\dx\rsgzgl\system\controller\SystemMenuController.java"

$app = Get-Content -Raw -Path $appJs
$menuServiceText = Get-Content -Raw -Path $menuService
$adminServiceText = Get-Content -Raw -Path $adminService
$workbenchServiceText = Get-Content -Raw -Path $workbenchService
$systemControllerText = Get-Content -Raw -Path $systemController

$menuGroupCodes = @("MIGRATION")
foreach ($code in $requiredMenuCodes) {
    $frontEndPatterns = @(
        "Permissions.has(`"$code`")",
        "Permissions.guard(`"$code`")",
        "menuCode === `"$code`"",
        "activeMenuCode === `"$code`""
    )
    if ($code -eq "SYSTEM_MENU") {
        $frontEndPatterns += "SystemPanel.loadMenus()"
    }
    if ($menuGroupCodes -contains $code) {
        Add-Result $results ("frontend-permission-" + $code) "OK" ("Menu group " + $code + " does not require a direct front-end route.")
    } else {
        $matched = @($frontEndPatterns | Where-Object { $app.Contains($_) })
        if ($matched.Count -gt 0) {
            Add-Result $results ("frontend-permission-" + $code) "OK" ("Front-end permission/menu route for " + $code)
        } else {
            Add-Result $results ("frontend-permission-" + $code) "FAIL" ("Missing front-end permission/menu route for " + $code)
        }
    }
    Test-Contains $results ("default-menu-" + $code) $menuServiceText "`"$code`"" "Default menu includes $code"
}

$backendPermissionChecks = @(
    @{ Code = "backend-salary-todo"; Pattern = 'hasMenu("SALARY_TODO")' },
    @{ Code = "backend-salary-done"; Pattern = 'hasMenu("SALARY_DONE")' },
    @{ Code = "backend-salary-trial"; Pattern = 'hasMenu("SALARY_TRIAL")' },
    @{ Code = "backend-salary-export"; Pattern = 'hasMenu("SALARY_EXPORT")' },
    @{ Code = "backend-salary-governance"; Pattern = 'hasMenu("SALARY_GOVERNANCE")' },
    @{ Code = "backend-salary-acceptance"; Pattern = 'hasMenu("SALARY_ACCEPTANCE")' },
    @{ Code = "backend-history-write"; Pattern = 'hasMenu("SALARY_HISTORY_WRITE")' },
    @{ Code = "backend-history-rollback"; Pattern = 'hasMenu("SALARY_HISTORY_ROLLBACK")' }
)

foreach ($check in $backendPermissionChecks) {
    Test-Contains $results $check.Code $workbenchServiceText $check.Pattern $check.Pattern
}

$controllerChecks = @(
    @{ Code = "system-menu-admin-endpoint"; Pattern = '"/menu-admin"' },
    @{ Code = "system-role-templates-endpoint"; Pattern = '"/role-templates"' },
    @{ Code = "system-role-menu-update-endpoint"; Pattern = '"/roles/{code}/menus"' },
    @{ Code = "system-user-org-update-endpoint"; Pattern = '"/users/{username}/orgs"' },
    @{ Code = "system-audit-endpoint"; Pattern = '"/audits"' }
)

foreach ($check in $controllerChecks) {
    Test-Contains $results $check.Code $systemControllerText $check.Pattern $check.Pattern
}

$adminChecks = @(
    @{ Code = "admin-role-template-list"; Pattern = "roleTemplates()" },
    @{ Code = "admin-role-menu-validation"; Pattern = "Role menus contain invalid menu code" },
    @{ Code = "admin-user-orgs"; Pattern = "updateUserOrgCodes" },
    @{ Code = "admin-role-template-audit"; Pattern = '"role-template"' }
)

foreach ($check in $adminChecks) {
    Test-Contains $results $check.Code $adminServiceText $check.Pattern $check.Pattern
}

$results | Export-Csv -Path $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8

Write-Host ""
Write-Host "Permission/menu contract summary:"
$results | Format-Table Code,Status,Message -AutoSize
Write-Host ""
Write-Host "Wrote $OutputPath"

$unexpected = @($results | Where-Object { $_.Status -in @("FAIL", "REQUEST_ERROR") })
if ($FailOnUnexpected -and $unexpected.Count -gt 0) {
    throw "Permission/menu contract verification found unexpected rows."
}
