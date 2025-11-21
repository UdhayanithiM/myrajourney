# Fix MySQL Password Issue

## Problem
MySQL root user requires a password, but the backend is configured with an empty password.

## Solution Options

### Option 1: Reset MySQL80 Root Password to Empty (Recommended)

1. **Open Command Prompt as Administrator**
   - Press Win + X
   - Select "Command Prompt (Admin)" or "PowerShell (Admin)"

2. **Stop MySQL80 service:**
   ```cmd
   net stop MySQL80
   ```

3. **Create a temporary SQL file:**
   - Create file: `C:\temp\reset-mysql.txt`
   - Content:
   ```sql
   ALTER USER 'root'@'localhost' IDENTIFIED BY '';
   FLUSH PRIVILEGES;
   ```

4. **Start MySQL in safe mode:**
   ```cmd
   cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"
   mysqld --init-file=C:\temp\reset-mysql.txt
   ```

5. **Wait 10 seconds, then press Ctrl+C to stop**

6. **Start MySQL80 service normally:**
   ```cmd
   net start MySQL80
   ```

7. **Test connection:**
   - Open: http://localhost/backend/public/test-connection-debug.php
   - Should show: ✅ Connected to MySQL server

### Option 2: Update Backend .env File with Password

If you know the MySQL root password:

1. **Open file:** `C:\xampp\htdocs\backend\.env`

2. **Update line:**
   ```env
   DB_PASS=your_mysql_password
   ```

3. **Save file**

4. **Test connection:**
   - Open: http://localhost/backend/public/test-connection-debug.php

### Option 3: Use XAMPP MySQL Instead

1. **Stop MySQL80 service:**
   - Open Services (Win + R, type `services.msc`)
   - Find "MySQL80"
   - Right-click → Stop
   - Right-click → Properties → Startup type: Disabled

2. **Start XAMPP MySQL:**
   - Open XAMPP Control Panel: `C:\xampp\xampp-control.exe`
   - Click "Start" next to MySQL
   - XAMPP MySQL uses empty password by default

3. **Test connection:**
   - Open: http://localhost/backend/public/test-connection-debug.php

## After Fixing Password

1. **Create Database:**
   - Open: http://localhost/phpmyadmin
   - Click "New"
   - Database name: `myrajourney`
   - Collation: `utf8mb4_unicode_ci`
   - Click "Create"

2. **Import Schema:**
   - Click on `myrajourney` database
   - Click "Import" tab
   - Choose file: `C:\xampp\htdocs\backend\scripts\setup_database.sql`
   - Click "Go"

3. **Verify:**
   - Should see 18 tables
   - Open: http://localhost/backend/public/api/v1/education/articles
   - Should return: `{"success":true,"data":[],...}`

4. **Create Test Users:**
   - In phpMyAdmin, click on `myrajourney` database
   - Click "SQL" tab
   - Paste and run:
   ```sql
   INSERT INTO users (email, password_hash, role, name, status, created_at, updated_at)
   VALUES 
   ('patient@test.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'PATIENT', 'Test Patient', 'ACTIVE', NOW(), NOW()),
   ('doctor@test.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'DOCTOR', 'Dr. Test', 'ACTIVE', NOW(), NOW()),
   ('admin@test.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN', 'Admin User', 'ACTIVE', NOW(), NOW());
   ```

5. **Test Login:**
   - Email: `patient@test.com`
   - Password: `password`
   - Role: Patient

## Quick Test Commands

```powershell
# Test database connection
Invoke-WebRequest -Uri "http://localhost/backend/public/test-connection-debug.php" -UseBasicParsing | Select-Object -ExpandProperty Content

# Test API endpoint
Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/education/articles" -UseBasicParsing | Select-Object -ExpandProperty Content

# Test registration
$body = '{"email":"newuser@test.com","password":"test123456","role":"PATIENT","name":"New User"}'
Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/auth/register" -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content

# Test login
$body = '{"email":"patient@test.com","password":"password"}'
Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content
```

## Troubleshooting

### "Access denied for user 'root'@'localhost'"
- MySQL root user has a password
- Follow Option 1 or Option 2 above

### "Can't connect to MySQL server"
- MySQL is not running
- Start MySQL using XAMPP Control Panel or Services

### "Unknown database 'myrajourney'"
- Database doesn't exist
- Create it in phpMyAdmin (see "After Fixing Password" section)

### "Table 'myrajourney.users' doesn't exist"
- Schema not imported
- Import `setup_database.sql` in phpMyAdmin

---

**Need Help?**
See: SETUP_AND_FIX_GUIDE.md for complete instructions
