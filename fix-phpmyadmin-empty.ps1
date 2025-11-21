# Fix phpMyAdmin to use empty password
# Run this after resetting MySQL password to empty

Write-Host "=== Fixing phpMyAdmin for Empty Password ===" -ForegroundColor Cyan
Write-Host ""

$configPath = "C:\xampp\phpMyAdmin\config.inc.php"

if (-not (Test-Path $configPath)) {
    Write-Host "ERROR: phpMyAdmin config not found at: $configPath" -ForegroundColor Red
    exit 1
}

# Read current config
$config = Get-Content $configPath -Raw

# Update password to empty
$config = $config -replace "(\['password'\]\s*=\s*)'[^']*';", "`$1'';"

# Update controlpass to empty
$config = $config -replace "(\['controlpass'\]\s*=\s*)'[^']*';", "`$1'';"

# Write updated config
Set-Content $configPath $config -NoNewline

Write-Host "phpMyAdmin configuration updated to use empty password" -ForegroundColor Green
Write-Host ""
Write-Host "Now try accessing: http://localhost/phpmyadmin" -ForegroundColor Yellow
Write-Host ""
