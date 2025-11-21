# MyRA Journey Backend Setup Script
# This script automates the backend setup process

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "MyRA Journey Backend Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check if XAMPP is installed
Write-Host "Step 1: Checking XAMPP installation..." -ForegroundColor Yellow
if (Test-Path "C:\xampp") {
    Write-Host "✅ XAMPP is installed" -ForegroundColor Green
} else {
    Write-Host "❌ XAMPP is not installed at C:\xampp" -ForegroundColor Red
    Write-Host "Please install XAMPP from: https://www.apachefriends.org/" -ForegroundColor Yellow
    exit 1
}

# Step 2: Check if backend files are in place
Write-Host "`nStep 2: Checking backend files..." -ForegroundColor Yellow
if (Test-Path "C:\xampp\htdocs\backend") {
    Write-Host "✅ Backend files are in place" -ForegroundColor Green
} else {
    Write-Host "❌ Backend files not found at C:\xampp\htdocs\backend" -ForegroundColor Red
    Write-Host "Copying backend files..." -ForegroundColor Yellow
    Copy-Item -Path "backend" -Destination "C:\xampp\htdocs\" -Recurse -Force
    Write-Host "✅ Backend files copied" -ForegroundColor Green
}

# Step 3: Check Apache status
Write-Host "`nStep 3: Checking Apache status..." -ForegroundColor Yellow
$apacheRunning = Test-NetConnection -ComputerName localhost -Port 80 -WarningAction SilentlyContinue
if ($apacheRunning.TcpTestSucceeded) {
    Write-Host "✅ Apache is running on port 80" -ForegroundColor Green
} else {
    Write-Host "❌ Apache is not running" -ForegroundColor Red
    Write-Host "Starting Apache..." -ForegroundColor Yellow
    try {
        Start-Process "C:\xampp\apache\bin\httpd.exe" -WindowStyle Hidden
        Start-Sleep -Seconds 3
        $apacheRunning = Test-NetConnection -ComputerName localhost -Port 80 -WarningAction SilentlyContinue
        if ($apacheRunning.TcpTestSucceeded) {
            Write-Host "✅ Apache started successfully" -ForegroundColor Green
        } else {
            Write-Host "❌ Failed to start Apache" -ForegroundColor Red
            Write-Host "Please start Apache manually using XAMPP Control Panel" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ Error starting Apache: $_" -ForegroundColor Red
        Write-Host "Please start Apache manually using XAMPP Control Panel" -ForegroundColor Yellow
    }
}

# Step 4: Check MySQL status
Write-Host "`nStep 4: Checking MySQL status..." -ForegroundColor Yellow
$mysqlRunning = Test-NetConnection -ComputerName localhost -Port 3306 -WarningAction SilentlyContinue
if ($mysqlRunning.TcpTestSucceeded) {
    Write-Host "✅ MySQL is running on port 3306" -ForegroundColor Green
} else {
    Write-Host "❌ MySQL is not running" -ForegroundColor Red
    Write-Host "Please start MySQL using XAMPP Control Panel" -ForegroundColor Yellow
}

# Step 5: Test backend connection
Write-Host "`nStep 5: Testing backend connection..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost/backend/public/test-connection-debug.php" -UseBasicParsing -ErrorAction Stop
    if ($response.Content -match "Connected to MySQL server") {
        Write-Host "✅ Backend can connect to MySQL" -ForegroundColor Green
        
        if ($response.Content -match "Database 'myrajourney' exists") {
            Write-Host "✅ Database 'myrajourney' exists" -ForegroundColor Green
        } else {
            Write-Host "❌ Database 'myrajourney' does not exist" -ForegroundColor Red
            Write-Host "Please create the database:" -ForegroundColor Yellow
            Write-Host "1. Open http://localhost/phpmyadmin" -ForegroundColor Cyan
            Write-Host "2. Click 'New' to create database" -ForegroundColor Cyan
            Write-Host "3. Name: myrajourney, Collation: utf8mb4_unicode_ci" -ForegroundColor Cyan
            Write-Host "4. Import: C:\xampp\htdocs\backend\scripts\setup_database.sql" -ForegroundColor Cyan
        }
    } else {
        Write-Host "❌ Backend cannot connect to MySQL" -ForegroundColor Red
        Write-Host "Error: MySQL root user requires password" -ForegroundColor Yellow
        Write-Host "Please check SETUP_AND_FIX_GUIDE.md for password reset instructions" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Cannot access backend: $_" -ForegroundColor Red
}

# Step 6: Test API endpoint
Write-Host "`nStep 6: Testing API endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/education/articles" -UseBasicParsing -ErrorAction Stop
    $json = $response.Content | ConvertFrom-Json
    if ($json.success -eq $true) {
        Write-Host "✅ API endpoint is working" -ForegroundColor Green
    } else {
        Write-Host "❌ API returned error" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ API endpoint failed: $_" -ForegroundColor Red
}

# Step 7: Check .env file
Write-Host "`nStep 7: Checking .env configuration..." -ForegroundColor Yellow
if (Test-Path "C:\xampp\htdocs\backend\.env") {
    Write-Host "✅ .env file exists" -ForegroundColor Green
    $envContent = Get-Content "C:\xampp\htdocs\backend\.env"
    $dbHost = ($envContent | Select-String "DB_HOST=").ToString().Split("=")[1]
    $dbName = ($envContent | Select-String "DB_NAME=").ToString().Split("=")[1]
    $dbUser = ($envContent | Select-String "DB_USER=").ToString().Split("=")[1]
    Write-Host "  DB_HOST: $dbHost" -ForegroundColor Cyan
    Write-Host "  DB_NAME: $dbName" -ForegroundColor Cyan
    Write-Host "  DB_USER: $dbUser" -ForegroundColor Cyan
} else {
    Write-Host "❌ .env file not found" -ForegroundColor Red
}

# Step 8: Remove Flask backend
Write-Host "`nStep 8: Removing Flask backend..." -ForegroundColor Yellow
if (Test-Path "flask_backend") {
    try {
        Remove-Item -Path "flask_backend" -Recurse -Force
        Write-Host "✅ Flask backend removed" -ForegroundColor Green
    } catch {
        Write-Host "❌ Failed to remove Flask backend: $_" -ForegroundColor Red
    }
} else {
    Write-Host "✅ Flask backend already removed" -ForegroundColor Green
}

# Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Setup Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nNext Steps:" -ForegroundColor Yellow
Write-Host "1. If database doesn't exist, create it in phpMyAdmin" -ForegroundColor White
Write-Host "   URL: http://localhost/phpmyadmin" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. Import database schema:" -ForegroundColor White
Write-Host "   File: C:\xampp\htdocs\backend\scripts\setup_database.sql" -ForegroundColor Cyan
Write-Host ""
Write-Host "3. Test backend:" -ForegroundColor White
Write-Host "   URL: http://localhost/backend/public/test-connection-debug.php" -ForegroundColor Cyan
Write-Host ""
Write-Host "4. Build and run Android app" -ForegroundColor White
Write-Host ""
Write-Host "5. Test login with:" -ForegroundColor White
Write-Host "   Email: patient@test.com" -ForegroundColor Cyan
Write-Host "   Password: password" -ForegroundColor Cyan
Write-Host ""
Write-Host "For detailed instructions, see: SETUP_AND_FIX_GUIDE.md" -ForegroundColor Yellow
Write-Host ""
