# Quick Start Guide - MyRA Journey

## 🚀 Get Your App Running in 5 Minutes

### Prerequisites
- ✅ XAMPP installed at `C:\xampp`
- ✅ Android Studio installed
- ✅ Backend files at `C:\xampp\htdocs\backend`

---

## Step 1: Fix MySQL Password (2 minutes)

**Open Command Prompt as Administrator** (Win + X → "Command Prompt (Admin)")

```cmd
net stop MySQL80
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"
```

Create file `C:\temp\reset.txt` with content:
```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY '';
FLUSH PRIVILEGES;
```

Then run:
```cmd
mysqld --init-file=C:\temp\reset.txt
```

Wait 10 seconds, press Ctrl+C, then:
```cmd
net start MySQL80
```

---

## Step 2: Create Database (2 minutes)

1. Open: http://localhost/phpmyadmin
2. Click "New" → Database name: `myrajourney` → Create
3. Click "Import" → Choose file: `C:\xampp\htdocs\backend\scripts\setup_database.sql` → Go
4. Verify 18 tables are created

---

## Step 3: Create Test Users (30 seconds)

In phpMyAdmin, click "SQL" tab and run:

```sql
INSERT INTO users (email, password_hash, role, name, status, created_at, updated_at)
VALUES 
('patient@test.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'PATIENT', 'Test Patient', 'ACTIVE', NOW(), NOW()),
('doctor@test.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'DOCTOR', 'Dr. Test', 'ACTIVE', NOW(), NOW()),
('admin@test.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN', 'Admin User', 'ACTIVE', NOW(), NOW());
```

---

## Step 4: Test Backend (30 seconds)

Open in browser: http://localhost/backend/public/test-connection-debug.php

Should show:
- ✅ Connected to MySQL server
- ✅ Database 'myrajourney' exists
- ✅ 18 tables listed

---

## Step 5: Run Android App

1. Open project in Android Studio
2. Build and run (Shift + F10)
3. Login with:
   - Email: `patient@test.com`
   - Password: `password`
   - Role: Patient

---

## ✅ Success!

If you see the Patient Dashboard, everything is working!

### Test Other Roles:
- **Doctor:** `doctor@test.com` / `password`
- **Admin:** `admin@test.com` / `password`

---

## 🐛 Troubleshooting

### "Network Error" in app?
- Check Apache is running (XAMPP Control Panel)
- Test: http://localhost/backend/public/api/v1/education/articles

### "DB connection failed"?
- Repeat Step 1 (MySQL password reset)
- Test: http://localhost/backend/public/test-connection-debug.php

### "Invalid email or password"?
- Repeat Step 3 (create test users)
- Check users exist in phpMyAdmin → `myrajourney` → `users` table

---

## 📚 Need More Help?

- **Complete Guide:** `SETUP_AND_FIX_GUIDE.md`
- **MySQL Issues:** `fix-mysql-password.md`
- **Full Summary:** `INTEGRATION_COMPLETE_SUMMARY.md`

---

**That's it! Your app should now be running. 🎉**
