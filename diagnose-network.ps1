# Network Diagnostics for Android App

Write-Host "=== MyRA Journey Network Diagnostics ===" -ForegroundColor Cyan
Write-Host ""

# Get current IP
$ip = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object {$_.InterfaceAlias -notlike "*Loopback*" -and $_.IPAddress -notlike "169.254.*"} | Select-Object -First 1).IPAddress
Write-Host "1. Current PC IP Address: $ip" -ForegroundColor Yellow
Write-Host ""

# Check XAMPP services
Write-Host "2. Checking XAMPP Services..." -ForegroundColor Yellow
$apacheRunning = Get-Process -Name "httpd" -ErrorAction SilentlyContinue
$mysqlRunning = Get-Process -Name "mysqld" -ErrorAction SilentlyContinue

if ($apacheRunning) {
    Write-Host "   ✓ Apache is running" -ForegroundColor Green
} else {
    Write-Host "   ✗ Apache is NOT running" -ForegroundColor Red
    Write-Host "   → Start Apache from XAMPP Control Panel" -ForegroundColor Yellow
}

if ($mysqlRunning) {
    Write-Host "   ✓ MySQL is running" -ForegroundColor Green
} else {
    Write-Host "   ✗ MySQL is NOT running" -ForegroundColor Red
    Write-Host "   → Start MySQL from XAMPP Control Panel" -ForegroundColor Yellow
}
Write-Host ""

# Test localhost
Write-Host "3. Testing Backend (localhost)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/education/articles" -UseBasicParsing -TimeoutSec 5
    Write-Host "   ✓ Backend accessible on localhost" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Backend NOT accessible on localhost" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test IP address
Write-Host "4. Testing Backend (IP: $ip)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://$ip/backend/public/api/v1/education/articles" -UseBasicParsing -TimeoutSec 5
    Write-Host "   ✓ Backend accessible on $ip" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Backend NOT accessible on $ip" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test login
Write-Host "5. Testing Login Endpoint..." -ForegroundColor Yellow
try {
    $body = '{"email":"patient@test.com","password":"password"}'
    $response = Invoke-WebRequest -Uri "http://$ip/backend/public/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -UseBasicParsing -TimeoutSec 5
    Write-Host "   ✓ Login endpoint working" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Login endpoint failed" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Check ApiClient.java
Write-Host "6. Checking ApiClient.java configuration..." -ForegroundColor Yellow
$apiClientPath = "app\src\main\java\com\example\myrajouney\api\ApiClient.java"
if (Test-Path $apiClientPath) {
    $content = Get-Content $apiClientPath -Raw
    if ($content -match 'BASE_URL_PHYSICAL = "http://([0-9.]+)/') {
        $configuredIp = $matches[1]
        if ($configuredIp -eq $ip) {
            Write-Host "   ✓ ApiClient.java has correct IP: $configuredIp" -ForegroundColor Green
        } else {
            Write-Host "   ✗ ApiClient.java has WRONG IP: $configuredIp" -ForegroundColor Red
            Write-Host "   → Current IP is: $ip" -ForegroundColor Yellow
            Write-Host "   → You need to update ApiClient.java and rebuild" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "   ✗ ApiClient.java not found" -ForegroundColor Red
}
Write-Host ""

# Device type detection
Write-Host "7. Device Type Detection..." -ForegroundColor Yellow
Write-Host "   If using EMULATOR: Use http://10.0.2.2/backend/public/api/v1/" -ForegroundColor White
Write-Host "   If using PHYSICAL DEVICE: Use http://$ip/backend/public/api/v1/" -ForegroundColor White
Write-Host ""

# Summary
Write-Host "=== Summary ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backend URLs:" -ForegroundColor Yellow
Write-Host "  Emulator:        http://10.0.2.2/backend/public/api/v1/" -ForegroundColor White
Write-Host "  Physical Device: http://$ip/backend/public/api/v1/" -ForegroundColor White
Write-Host ""
Write-Host "If you're getting network errors:" -ForegroundColor Yellow
Write-Host "  1. Make sure Apache and MySQL are running in XAMPP" -ForegroundColor White
Write-Host "  2. Rebuild the app after IP changes (Build → Rebuild Project)" -ForegroundColor White
Write-Host "  3. Ensure phone is on the same WiFi network as PC" -ForegroundColor White
Write-Host "  4. Check if you're using emulator or physical device" -ForegroundColor White
Write-Host ""
