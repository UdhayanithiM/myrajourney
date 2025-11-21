# Backend/Database Location Guide

## 📍 Where to View Your Backend/Database Entries

### 1. Database (MySQL)

#### Option A: phpMyAdmin (Easiest)
- **URL**: http://localhost/phpmyadmin
- **Steps**:
  1. Open browser
  2. Go to http://localhost/phpmyadmin
  3. Click on `myrajourney` database in left sidebar
  4. Click on any table to view data:
     - `users` - All users (patients, doctors, admin)
     - `appointments` - All appointments
     - `reports` - All uploaded reports
     - `symptoms` - Symptom logs
     - `medications` - Prescribed medications
     - `rehab_plans` - Rehabilitation exercises
     - `notifications` - All notifications
     - `education_articles` - Education hub content

#### Option B: MySQL Command Line
```bash
# Open command prompt/PowerShell
cd C:\xampp\mysql\bin
mysql.exe -u root -p

# Enter password (usually empty for XAMPP)
USE myrajourney;

# View all users
SELECT * FROM users;

# View all appointments
SELECT * FROM appointments;

# View all reports
SELECT * FROM reports;

# View notifications
SELECT * FROM notifications;
```

#### Option C: Database Files Location
- **Path**: `C:\xampp\mysql\data\myrajourney\`
- Contains `.frm` and `.ibd` files (not human-readable)
- Use phpMyAdmin or MySQL command line instead

---

### 2. Backend API

#### Base URL
- **Local**: http://localhost/backend/public/api/v1/
- **Test Connection**: http://localhost/backend/public/test-db.php
- **API Info**: http://localhost/backend/public/api-info.php

#### Key Endpoints
- `POST /api/v1/auth/login` - Login
- `GET /api/v1/patients` - Get all patients (doctor/admin)
- `GET /api/v1/appointments` - Get appointments
- `GET /api/v1/reports` - Get reports
- `GET /api/v1/notifications` - Get notifications
- `POST /api/v1/admin/users` - Create user (admin only)

#### Test Backend
1. Open browser
2. Go to http://localhost/backend/public/test-db.php
3. Should show "Database connection successful"

---

### 3. Viewing Data Flow

#### Check if Patient Data is Stored:
```sql
-- In phpMyAdmin or MySQL command line
SELECT * FROM reports WHERE patient_id = [PATIENT_ID];
SELECT * FROM symptoms WHERE patient_id = [PATIENT_ID];
SELECT * FROM medications WHERE patient_id = [PATIENT_ID];
```

#### Check if Doctor Can See Patient Data:
```sql
-- Get all patients for a doctor
SELECT DISTINCT u.* 
FROM users u
INNER JOIN appointments a ON u.id = a.patient_id
WHERE a.doctor_id = [DOCTOR_ID];
```

#### Check Notifications:
```sql
-- View all notifications
SELECT * FROM notifications ORDER BY created_at DESC;

-- View unread notifications for a user
SELECT * FROM notifications 
WHERE user_id = [USER_ID] AND read_at IS NULL;
```

---

### 4. Common Queries

#### View All Users with Roles:
```sql
SELECT id, email, name, role, status, created_at 
FROM users 
ORDER BY created_at DESC;
```

#### View Appointments with Patient/Doctor Names:
```sql
SELECT 
    a.id,
    a.title,
    a.start_time,
    u1.name as patient_name,
    u2.name as doctor_name
FROM appointments a
LEFT JOIN users u1 ON a.patient_id = u1.id
LEFT JOIN users u2 ON a.doctor_id = u2.id
ORDER BY a.start_time DESC;
```

#### View Recent Reports:
```sql
SELECT 
    r.id,
    r.title,
    r.uploaded_at,
    u.name as patient_name
FROM reports r
LEFT JOIN users u ON r.patient_id = u.id
ORDER BY r.uploaded_at DESC
LIMIT 10;
```

---

## 🔧 Troubleshooting

### Database Not Found?
- Check if XAMPP MySQL is running
- Check if database `myrajourney` exists
- Run migrations: `backend/scripts/migrations/*.sql`

### API Not Working?
- Check if Apache is running in XAMPP
- Check `backend/public/index.php` exists
- Check CORS settings
- Check database connection in `backend/src/config/DB.php`

### Data Not Showing?
- Check if data exists in database (use phpMyAdmin)
- Check API response (use browser dev tools or Postman)
- Check Android app logs for API errors
- Verify authentication token is valid

---

## 📝 Quick Reference

| What | Where |
|------|-------|
| View Database | http://localhost/phpmyadmin |
| Test Backend | http://localhost/backend/public/test-db.php |
| API Base URL | http://localhost/backend/public/api/v1/ |
| Database Name | `myrajourney` |
| Database Files | `C:\xampp\mysql\data\myrajourney\` |

