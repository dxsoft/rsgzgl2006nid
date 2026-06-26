param(
    [string]$DbUrl = $env:DB_URL,
    [string]$DbUsername = $env:DB_USERNAME,
    [string]$DbPassword = $env:DB_PASSWORD
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"

if ([string]::IsNullOrWhiteSpace($DbUsername)) {
    $DbUsername = "root"
}

$previousMigrationEnabled = $env:DB_MIGRATION_ENABLED
$previousWebType = $env:SPRING_MAIN_WEB_APPLICATION_TYPE
$previousDbUrl = $env:DB_URL
$previousDbUsername = $env:DB_USERNAME
$previousDbPassword = $env:DB_PASSWORD

try {
    $env:DB_MIGRATION_ENABLED = "true"
    $env:SPRING_MAIN_WEB_APPLICATION_TYPE = "none"
    if (-not [string]::IsNullOrWhiteSpace($DbUrl)) {
        $env:DB_URL = $DbUrl
    }
    if (-not [string]::IsNullOrWhiteSpace($DbUsername)) {
        $env:DB_USERNAME = $DbUsername
    }
    if (-not [string]::IsNullOrWhiteSpace($DbPassword)) {
        $env:DB_PASSWORD = $DbPassword
    }

    Push-Location $backendDir
    try {
        mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.main.web-application-type=none"
    } finally {
        Pop-Location
    }
} finally {
    $env:DB_MIGRATION_ENABLED = $previousMigrationEnabled
    $env:SPRING_MAIN_WEB_APPLICATION_TYPE = $previousWebType
    $env:DB_URL = $previousDbUrl
    $env:DB_USERNAME = $previousDbUsername
    $env:DB_PASSWORD = $previousDbPassword
}
