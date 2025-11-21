# Complete Setup and Fix Guide for MyRA Journey App

## 🔍 Analysis Summary

**Backend Status:**
- ✅ **PHP Backend** (`backend/`) - Fully integrated, complete implementation
- ❌ **Flask Backend** (`flask_backend/`) - Incomplete, not integrated - **WILL BE REMOVED**

**Current Issues:**
1. ❌ MySQL80 service is running but requires a password (root user)
2. ❌ Database `myrajourney` may not exist or is inaccessible
3. ❌ Android app shows "Network Error" when trying to login

## 🎯 Solution Steps

### Step 1: Fix MySQL Database Access

**Option A: Use XAMPP Control Panel (Recommended)**

1. Open XAMPP Control Panel:
   ```
   C:\xampp\xampp-control.exe
   ```

2. Stop MySQL80 service (if running):
   - Open Services (Win + R, type `services.msc`)
   - Find "MySQL80" service
   - Right-click → Stop (requires admin rights)

3. Start XAMPP MySQL:
   - In XAMPP Control Panel, click "Start" next to MySQL
   - MySQL should start on port 3306

4. Click "Admin" next to MySQL to open phpMyAdmin
   - Default login: username=`root`, password=(empty)

**Option B: Reset MySQL80 Root Password**

If you want to keep using MySQL80, reset the root password:

1. Open Command Prompt as Administrator

2. Stop MySQL80 service:
   ```cmd
   net stop MySQL80
   ```

3. Start MySQL in safe mode:
   ```cmd
   cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"
   mysqld --skip-grant-tables
   ```

4. Open another Command Prompt as Administrator:
   ```cmd
   cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"
   mysql -u root
   ```

5. Reset password:
   ```sql
   FLUSH PRIVILEGES;
   ALTER USER 'root'@'localhost' IDENTIFIED BY '';
   FLUSH PRIVILEGES;
   EXIT;
   ```

6. Stop safe mode MySQL (Ctrl+C in first window)

7. Start MySQL80 service normally:
   ```cmd
   net start MySQL80
   ```

### Step 2: Create Database and Import Schema

1. Open phpMyAdmin: http://localhost/phpmyadmin

2. Create database:
   - Click "New" in left sidebar
   - Database name: `myrajourney`
   - Collation: `utf8mb4_unicode_ci`
   - Click "Create"

3. Import schema:
   - Click on `myrajourney` database
   - Click "Import" tab
   - Choose file: `C:\xampp\htdocs\backend\scripts\setup_database.sql`
   - Click "Go"

4. Verify tables created:
   - Should see 18 tables including: users, appointments, reports, medications, etc.

### Step 3: Verify Backend Configuration

1. Check `.env` file exists:
   ```
   C:\xampp\htdocs\backend\.env
   ```

2. Verify contents:
   ```env
   APP_ENV=local
   DB_HOST=127.0.0.1
   DB_PORT=3306
   DB_NAME=myrajourney
   DB_USER=root
   DB_PASS=
   JWT_SECRET=myrajourney_dev_secret_key_2024_change_in_production_min_32_chars
   JWT_TTL_SECONDS=604800
   ```

3. If password was set in Step 1, update `DB_PASS=your_password`

### Step 4: Test Backend API

1. Test database connection:
   ```
   http://localhost/backend/public/test-connection-debug.php
   ```
   Should show: ✅ Connected to MySQL server, ✅ Database 'myrajourney' exists

2. Test public endpoint:
   ```
   http://localhost/backend/public/api/v1/education/articles
   ```
   Should return: `{"success":true,"data":[],"meta":{"total":0,"page":1,"limit":20}}`

3. Test registration:
   ```powershell
   $body = '{"email":"test@example.com","password":"test123456","role":"PATIENT","name":"Test User"}'
   Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/auth/register" -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content
   ```

### Step 5: Configure Android App

The app is already configured to use the PHP backend. It auto-detects emulator vs physical device:

**For Android Emulator:**
- Uses: `http://10.0.2.2/backend/public/api/v1/`
- No changes needed

**For Physical Device:**
- Update `ApiClient.java` line 24:
  ```java
  private static final String BASE_URL_PHYSICAL = "http://YOUR_PC_IP/backend/public/api/v1/";
  ```
- Find your PC IP:
  ```cmd
  ipconfig
  ```
  Look for "IPv4 Address" under your active network adapter

### Step 6: Test Login Flow

1. Build and run the Android app

2. Try to login with test credentials:
   - Email: `test@example.com`
   - Password: `test123456`
   - Role: Patient

3. If you get "Network Error":
   - Check Apache is running (XAMPP Control Panel)
   - Check MySQL is running
   - Check backend URL is correct
   - Check firewall isn't blocking port 80

### Step 7: Create Test Users

Run this in phpMyAdmin SQL tab:

```sql
-- Create test patient
INSERT INTO users (email, password_hash, role, name, status, created_at, updated_at)
VALUES (
    'patient@test.com',
    '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- password: password
    'PATIENT',
    'Test Patient',
    'ACTIVE',
    NOW(),
    NOW()
);

-- Create test doctor
INSERT INTO users (email, password_hash, role, name, status, created_at, updated_at)
VALUES (
    'doctor@test.com',
    '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- password: password
    'DOCTOR',
    'Dr. Test',
    'ACTIVE',
    NOW(),
    NOW()
);

-- Create test admin
INSERT INTO users (email, password_hash, role, name, status, created_at, updated_at)
VALUES (
    'admin@test.com',
    '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- password: password
    'ADMIN',
    'Admin User',
    'ACTIVE',
    NOW(),
    NOW()
);
```

Test login credentials:
- Patient: `patient@test.com` / `password`
- Doctor: `doctor@test.com` / `password`
- Admin: `admin@test.com` / `password`

## 🗑️ Removing Flask Backend

The Flask backend is incomplete and not integrated. It will be removed:

```powershell
Remove-Item -Recurse -Force flask_backend
```

## ✅ Verification Checklist

- [ ] XAMPP Apache is running
- [ ] MySQL is running and accessible
- [ ] Database `myrajourney` exists with all tables
- [ ] Backend `.env` file is configured correctly
- [ ] Test endpoint returns valid JSON
- [ ] Can register new user via API
- [ ] Can login via API and receive token
- [ ] Android app can connect to backend
- [ ] Can login from Android app
- [ ] All 3 roles (Patient, Doctor, Admin) can login
- [ ] Data is stored in database correctly

## 🐛 Troubleshooting

### "Network Error" in Android App

**Cause:** Cannot connect to backend server

**Solutions:**
1. Check Apache is running in XAMPP
2. Test backend URL in browser
3. For physical device, ensure PC and device are on same network
4. Check Windows Firewall isn't blocking port 80
5. Verify backend URL in `ApiClient.java`

### "DB connection failed"

**Cause:** Cannot connect to MySQL database

**Solutions:**
1. Check MySQL is running
2. Verify `.env` file has correct credentials
3. Test connection: http://localhost/backend/public/test-connection-debug.php
4. Check database exists in phpMyAdmin

### "Invalid email or password"

**Cause:** User doesn't exist or password is wrong

**Solutions:**
1. Register new user via API or app
2. Use test credentials from Step 7
3. Check users table in phpMyAdmin

### "Endpoint not found"

**Cause:** Backend routing issue

**Solutions:**
1. Check `.htaccess` file exists in `backend/public/`
2. Verify Apache mod_rewrite is enabled
3. Check backend URL is correct (must include `/api/v1/`)

## 📊 Database Verification

Check data storage in phpMyAdmin:

```sql
-- View all users
SELECT id, email, name, role, status FROM users;

-- View appointments
SELECT a.*, u1.name as patient, u2.name as doctor 
FROM appointments a
LEFT JOIN users u1 ON a.patient_id = u1.id
LEFT JOIN users u2 ON a.doctor_id = u2.id;

-- View reports
SELECT r.*, u.name as patient 
FROM reports r
LEFT JOIN users u ON r.patient_id = u.id;

-- View medications
SELECT pm.*, u.name as patient, m.name as medication
FROM patient_medications pm
LEFT JOIN users u ON pm.patient_id = u.id
LEFT JOIN medications m ON pm.medication_id = m.id;
```

## 🎉 Success Indicators

When everything is working:

1. ✅ Backend test page shows all green checkmarks
2. ✅ API endpoints return valid JSON responses
3. ✅ Android app connects without "Network Error"
4. ✅ Can login with all 3 roles
5. ✅ Data appears in phpMyAdmin after actions in app
6. ✅ Notifications, appointments, reports all work

## 📝 Next Steps After Setup

1. Test all app features:
   - Patient dashboard
   - Doctor dashboard
   - Admin dashboard
   - Appointments
   - Medications
   - Reports upload
   - Notifications

2. Verify data flow:
   - Patient uploads report → appears in database
   - Doctor adds medication → patient sees it
   - Appointments created → both users notified

3. Check all 3 login flows work correctly

---

**Last Updated:** November 2024
**Status:** Ready for setup
