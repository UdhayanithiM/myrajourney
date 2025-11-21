# Test Network Access for Android App
# This script tests if the backend is accessible from network

Write-Host "=== Testing Backend Network Access ===" -ForegroundColor Cyan
Write-Host ""

# Get current IP
$ip = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object {$_.InterfaceAlias -notlike "*Loopback*" -and $_.IPAddress -notlike "169.254.*"} | Select-Object -First 1).IPAddress
Write-Host "Your PC IP: $ip" -ForegroundColor Yellow
Write-Host ""

# Test localhost
Write-Host "1. Testing localhost..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/education/articles" -UseBasicParsing -TimeoutSec 5
    Write-Host "   SUCCESS - localhost works" -ForegroundColor Green
} catch {
    Write-Host "   FAILED - localhost not accessible" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test IP address
Write-Host "2. Testing IP address ($ip)..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://$ip/backend/public/api/v1/education/articles" -UseBasicParsing -TimeoutSec 5
    Write-Host "   SUCCESS - IP address works" -ForegroundColor Green
} catch {
    Write-Host "   FAILED - IP address not accessible" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test login endpoint
Write-Host "3. Testing login endpoint..." -ForegroundColor Cyan
try {
    $body = '{"email":"patient@test.com","password":"password"}'
    $response = Invoke-WebRequest -Uri "http://$ip/backend/public/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -UseBasicParsing -TimeoutSec 5
    Write-Host "   SUCCESS - Login endpoint works" -ForegroundColor Green
    Write-Host "   Response: $($response.Content.Substring(0, [Math]::Min(100, $response.Content.Length)))..." -ForegroundColor Gray
} catch {
    Write-Host "   FAILED - Login endpoint not accessible" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Check firewall rules
Write-Host "4. Checking Windows Firewall..." -ForegroundColor Cyan
$firewallRules = Get-NetFirewallRule | Where-Object {$_.DisplayName -like "*Apache*" -or $_.DisplayName -like "*XAMPP*" -or $_.DisplayName -like "*httpd*"}
if ($firewallRules) {
    Write-Host "   Found firewall rules for Apache/XAMPP:" -ForegroundColor Yellow
    $firewallRules | ForEach-Object {
        Write-Host "   - $($_.DisplayName): $($_.Enabled) ($($_.Direction))" -ForegroundColor Gray
    }
} else {
    Write-Host "   WARNING: No firewall rules found for Apache" -ForegroundColor Yellow
    Write-Host "   You may need to add a firewall rule" -ForegroundColor Yellow
}
Write-Host ""

# Check if Apache is listening on all interfaces
Write-Host "5. Checking Apache listening ports..." -ForegroundColor Cyan
$listening = netstat -an | Select-String ":80 " | Select-String "LISTENING"
if ($listening) {
    Write-Host "   Apache is listening on port 80:" -ForegroundColor Green
    $listening | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
} else {
    Write-Host "   WARNING: Apache not listening on port 80" -ForegroundColor Red
}
Write-Host ""

# Summary
Write-Host "=== Summary ===" -ForegroundColor Cyan
Write-Host "Backend URL for Android app:" -ForegroundColor Yellow
Write-Host "  Emulator: http://10.0.2.2/backend/public/api/v1/" -ForegroundColor White
Write-Host "  Physical Device: http://$ip/backend/public/api/v1/" -ForegroundColor White
Write-Host ""
Write-Host "If tests failed, you may need to:" -ForegroundColor Yellow
Write-Host "1. Add Windows Firewall rule for Apache" -ForegroundColor White
Write-Host "2. Ensure Apache is listening on 0.0.0.0 (all interfaces)" -ForegroundColor White
Write-Host "3. Check your phone is on the same WiFi network" -ForegroundColor White
Write-Host ""
