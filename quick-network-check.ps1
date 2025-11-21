# Quick Network Check
$ip = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object {$_.InterfaceAlias -notlike "*Loopback*" -and $_.IPAddress -notlike "169.254.*"} | Select-Object -First 1).IPAddress

Write-Host "Current IP: $ip" -ForegroundColor Yellow
Write-Host ""

Write-Host "Testing backend..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://$ip/backend/public/api/v1/education/articles" -UseBasicParsing -TimeoutSec 5
    Write-Host "SUCCESS - Backend is accessible" -ForegroundColor Green
} catch {
    Write-Host "FAILED - Backend not accessible" -ForegroundColor Red
}

Write-Host ""
Write-Host "Backend URLs:" -ForegroundColor Cyan
Write-Host "  Emulator: http://10.0.2.2/backend/public/api/v1/" -ForegroundColor White
Write-Host "  Physical: http://$ip/backend/public/api/v1/" -ForegroundColor White
Write-Host ""
Write-Host "IMPORTANT: Rebuild the app after IP change!" -ForegroundColor Yellow
Write-Host "  Build -> Clean Project" -ForegroundColor White
Write-Host "  Build -> Rebuild Project" -ForegroundColor White
