# Reset MySQL80 Root Password to Empty
# Run this script as Administrator

Write-Host "=== Reset MySQL80 Root Password ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "This will reset the MySQL root password to empty (no password)" -ForegroundColor Yellow
Write-Host ""

# Check if running as admin
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "ERROR: This script must be run as Administrator!" -ForegroundColor Red
    Write-Host "Right-click PowerShell and select 'Run as Administrator'" -ForegroundColor Yellow
    exit 1
}

# Stop MySQL80
Write-Host "Stopping MySQL80 service..." -ForegroundColor Yellow
Stop-Service MySQL80 -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

# Create temp directory if it doesn't exist
$tempDir = "C:\temp"
if (-not (Test-Path $tempDir)) {
    New-Item -ItemType Directory -Path $tempDir | Out-Null
}

# Create reset SQL file
$resetFile = "$tempDir\reset-mysql.txt"
$resetSQL = @"
ALTER USER 'root'@'localhost' IDENTIFIED BY '';
FLUSH PRIVILEGES;
"@
Set-Content $resetFile $resetSQL

Write-Host "Created reset file: $resetFile" -ForegroundColor Green

# Find MySQL bin directory
$mysqlBin = "C:\Program Files\MySQL\MySQL Server 8.0\bin"
if (-not (Test-Path $mysqlBin)) {
    Write-Host "ERROR: MySQL bin directory not found at: $mysqlBin" -ForegroundColor Red
    exit 1
}

Write-Host "Starting MySQL with init file..." -ForegroundColor Yellow
Write-Host "This will take about 10 seconds..." -ForegroundColor Yellow
Write-Host ""

# Start MySQL with init file in background
$process = Start-Process -FilePath "$mysqlBin\mysqld.exe" -ArgumentList "--init-file=$resetFile" -PassThru -WindowStyle Hidden

# Wait for MySQL to process the init file
Start-Sleep -Seconds 10

# Stop the process
Write-Host "Stopping temporary MySQL process..." -ForegroundColor Yellow
Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

# Start MySQL80 service normally
Write-Host "Starting MySQL80 service..." -ForegroundColor Yellow
Start-Service MySQL80
Start-Sleep -Seconds 3

# Test connection
Write-Host ""
Write-Host "Testing connection..." -ForegroundColor Yellow
$testResult = & mysql -u root -e "SELECT 'Password reset successful!' as status;" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "SUCCESS! MySQL root password has been reset to empty" -ForegroundColor Green
    Write-Host ""
    Write-Host "Now update phpMyAdmin config back to empty password:" -ForegroundColor Cyan
    Write-Host "  Run: .\fix-phpmyadmin-empty.ps1" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "Connection test failed. Error:" -ForegroundColor Red
    Write-Host $testResult -ForegroundColor Red
    Write-Host ""
    Write-Host "You may need to try again or reset manually." -ForegroundColor Yellow
}

# Cleanup
Remove-Item $resetFile -ErrorAction SilentlyContinue

Write-Host ""
