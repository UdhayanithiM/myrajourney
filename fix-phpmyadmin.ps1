# Fix phpMyAdmin Configuration
# This script updates phpMyAdmin to use the correct MySQL password

Write-Host "=== Fixing phpMyAdmin Configuration ===" -ForegroundColor Cyan
Write-Host ""

# Backup original config
$configPath = "C:\xampp\phpMyAdmin\config.inc.php"
$backupPath = "C:\xampp\phpMyAdmin\config.inc.php.backup"

if (Test-Path $configPath) {
    Write-Host "Creating backup..." -ForegroundColor Yellow
    Copy-Item $configPath $backupPath -Force
    Write-Host "Backup created: $backupPath" -ForegroundColor Green
    Write-Host ""
}

# Read current config
$config = Get-Content $configPath -Raw

# Update the password line - change empty password to 'root'
$config = $config -replace "(\['password'\]\s*=\s*)'';", "`$1'root';"

# Update controlpass line - change empty password to 'root'  
$config = $config -replace "(\['controlpass'\]\s*=\s*)'';", "`$1'root';"

# Write updated config
Set-Content $configPath $config -NoNewline

Write-Host "phpMyAdmin configuration updated" -ForegroundColor Green
Write-Host ""
Write-Host "Changes made:" -ForegroundColor Cyan
Write-Host "  - Root password: (empty) -> 'root'" -ForegroundColor White
Write-Host "  - Control password: (empty) -> 'root'" -ForegroundColor White
Write-Host ""
Write-Host "Now try accessing: http://localhost/phpmyadmin" -ForegroundColor Yellow
Write-Host ""
Write-Host "If you still get errors, your MySQL password might be different." -ForegroundColor Yellow
Write-Host "Check your MySQL password with:" -ForegroundColor Yellow
Write-Host "  mysql -u root -p" -ForegroundColor White
Write-Host ""
