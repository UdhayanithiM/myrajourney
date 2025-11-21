# Switch from MySQL80 to XAMPP MySQL
# Run as Administrator

Write-Host "=== Switching to XAMPP MySQL ===" -ForegroundColor Cyan
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

# Disable MySQL80 from auto-starting
Write-Host "Disabling MySQL80 auto-start..." -ForegroundColor Yellow
Set-Service MySQL80 -StartupType Disabled

Write-Host "MySQL80 has been stopped and disabled" -ForegroundColor Green
Write-Host ""

# Check if XAMPP MySQL is installed
$xamppMysql = "C:\xampp\mysql\bin\mysqld.exe"
if (-not (Test-Path $xamppMysql)) {
    Write-Host "ERROR: XAMPP MySQL not found at: $xamppMysql" -ForegroundColor Red
    Write-Host "Please install XAMPP first" -ForegroundColor Yellow
    exit 1
}

Write-Host "XAMPP MySQL found" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Open XAMPP Control Panel: C:\xampp\xampp-control.exe" -ForegroundColor White
Write-Host "2. Click 'Start' next to MySQL" -ForegroundColor White
Write-Host "3. Wait for it to show 'Running' in green" -ForegroundColor White
Write-Host "4. Run: .\fix-phpmyadmin-empty.ps1" -ForegroundColor White
Write-Host "5. Open: http://localhost/phpmyadmin" -ForegroundColor White
Write-Host ""
Write-Host "XAMPP MySQL uses empty password by default (no password)" -ForegroundColor Yellow
Write-Host ""
