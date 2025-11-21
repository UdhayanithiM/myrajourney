# Start MySQL80 Service
# Run this script as Administrator

Write-Host "Starting MySQL80 service..." -ForegroundColor Yellow

try {
    Start-Service MySQL80
    Write-Host "✅ MySQL80 service started successfully" -ForegroundColor Green
    
    # Wait for MySQL to be ready
    Start-Sleep -Seconds 3
    
    # Test connection
    Write-Host "`nTesting database connection..." -ForegroundColor Yellow
    $response = Invoke-WebRequest -Uri "http://localhost/backend/public/test-connection-debug.php" -UseBasicParsing
    
    if ($response.Content -match "Connected to MySQL server") {
        Write-Host "✅ Database connection successful!" -ForegroundColor Green
        
        if ($response.Content -match "Database 'myrajourney' exists") {
            Write-Host "✅ Database 'myrajourney' exists" -ForegroundColor Green
        } else {
            Write-Host "❌ Database 'myrajourney' does not exist" -ForegroundColor Red
            Write-Host "`nNext step: Create database in phpMyAdmin" -ForegroundColor Yellow
            Write-Host "1. Open: http://localhost/phpmyadmin" -ForegroundColor Cyan
            Write-Host "2. Click 'New' to create database" -ForegroundColor Cyan
            Write-Host "3. Name: myrajourney" -ForegroundColor Cyan
            Write-Host "4. Import: C:\xampp\htdocs\backend\scripts\setup_database.sql" -ForegroundColor Cyan
        }
    } else {
        Write-Host "❌ Database connection failed" -ForegroundColor Red
        Write-Host $response.Content
    }
    
} catch {
    Write-Host "❌ Failed to start MySQL80: $_" -ForegroundColor Red
    Write-Host "`nPlease run this script as Administrator:" -ForegroundColor Yellow
    Write-Host "Right-click PowerShell → Run as Administrator" -ForegroundColor Cyan
    Write-Host "Then run: .\start-mysql.ps1" -ForegroundColor Cyan
}
