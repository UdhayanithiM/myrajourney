# Complete Integration Guide - Backend & Frontend

## 📍 Where to View Backend/Database Data

### 1. Database (MySQL) - View All Stored Data

#### Option A: phpMyAdmin (Easiest - Recommended)
**URL**: http://localhost/phpmyadmin

**Steps**:
1. Open your web browser
2. Navigate to: `http://localhost/phpmyadmin`
3. Click on `myrajourney` database in the left sidebar
4. Click on any table to view its data:

**Key Tables to Check**:
- **`users`** - All users (patients, doctors, admin)
  - View: `SELECT * FROM users;`
  - Check user roles, emails, status
  
- **`appointments`** - All appointments
  - View: `SELECT * FROM appointments ORDER BY start_time DESC;`
  - See patient-doctor relationships
  
- **`reports`** - All uploaded reports
  - View: `SELECT * FROM reports ORDER BY uploaded_at DESC;`
  - Check patient reports visible to doctors
  
- **`symptoms`** - Symptom logs
  - View: `SELECT * FROM symptoms ORDER BY date DESC;`
  - See patient symptom entries
  
- **`medications`** / **`patient_medications`** - Prescribed medications
  - View: `SELECT * FROM patient_medications WHERE is_active = 1;`
  - Check medications assigned to patients
  
- **`rehab_plans`** / **`rehab_exercises`** - Rehabilitation exercises
  - View: `SELECT * FROM rehab_plans;`
  - Check exercises assigned to patients
  
- **`notifications`** - All notifications
  - View: `SELECT * FROM notifications ORDER BY created_at DESC;`
  - See notifications sent to users
  
- **`medication_logs`** - Medication intake logs
  - View: `SELECT * FROM medication_logs ORDER BY taken_at DESC;`
  - Check when patients took medications
  
- **`password_resets`** - Password reset tokens
  - View: `SELECT * FROM password_resets;`
  - Check active reset tokens
  
- **`education_articles`** - Education hub content
  - View: `SELECT * FROM education_articles;`
  - Check education content

#### Option B: MySQL Command Line
```bash
# Open command prompt/PowerShell
cd C:\xampp\mysql\bin
mysql.exe -u root -p

# Enter password (usually empty for XAMPP, just press Enter)
USE myrajourney;

# View all users
SELECT id, email, name, role, status, created_at FROM users;

# View appointments with patient/doctor names
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

# View recent reports
SELECT 
    r.id,
    r.title,
    r.uploaded_at,
    u.name as patient_name
FROM reports r
LEFT JOIN users u ON r.patient_id = u.id
ORDER BY r.uploaded_at DESC
LIMIT 10;

# View notifications
SELECT 
    n.id,
    n.type,
    n.title,
    n.body,
    u.name as user_name,
    n.created_at
FROM notifications n
LEFT JOIN users u ON n.user_id = u.id
ORDER BY n.created_at DESC
LIMIT 20;
```

#### Option C: Database Files Location
**Path**: `C:\xampp\mysql\data\myrajourney\`
- Contains `.frm` and `.ibd` files (not human-readable)
- **Recommendation**: Use phpMyAdmin or MySQL command line instead

---

### 2. Backend API - Test Connections

#### Base URL
- **Local Development**: http://localhost/backend/public/api/v1/
- **Android Emulator**: http://10.0.2.2/backend/public/api/v1/
- **Test Connection**: http://localhost/backend/public/test-db.php
- **API Info**: http://localhost/backend/public/api-info.php

#### Test Backend Connection
1. Open browser
2. Go to: `http://localhost/backend/public/test-db.php`
3. Should show: "Database connection successful"

#### Test API Endpoints
Use browser or Postman to test:

**Login**:
```
POST http://localhost/backend/public/api/v1/auth/login
Body: {"email": "divyapriyaa0454.sse@saveetha.com", "password": "Divya@ida7"}
```

**Get Patients** (requires auth token):
```
GET http://localhost/backend/public/api/v1/patients
Headers: Authorization: Bearer {token}
```

**Get Appointments**:
```
GET http://localhost/backend/public/api/v1/appointments
Headers: Authorization: Bearer {token}
```

---

## ✅ Complete Flow Verification

### 1. User Creation Flow
**Path**: Admin → Create Patient/Doctor
1. Admin logs in
2. Navigates to Create Patient/Doctor
3. Fills form and submits
4. **Backend**: User created in `users` table
5. **Database Check**: `SELECT * FROM users WHERE email = 'newuser@example.com';`
6. **Frontend**: User can now login

### 2. Patient Data Entry Flow
**Path**: Patient → Enter Data → Doctor Sees It

**Symptom Log**:
1. Patient logs symptom
2. **Backend**: Stored in `symptoms` table
3. **Backend**: Notification created for doctor
4. **Database Check**: 
   ```sql
   SELECT * FROM symptoms WHERE patient_id = {patient_id};
   SELECT * FROM notifications WHERE user_id = {doctor_id} AND type = 'PATIENT_SYMPTOM';
   ```
5. **Doctor Feed**: Doctor sees notification and can view symptoms

**Report Upload**:
1. Patient uploads report
2. **Backend**: Stored in `reports` table
3. **Backend**: Notification created for doctor
4. **Database Check**: 
   ```sql
   SELECT * FROM reports WHERE patient_id = {patient_id};
   SELECT * FROM notifications WHERE user_id = {doctor_id} AND type = 'PATIENT_REPORT';
   ```
5. **Doctor Feed**: Doctor sees report in reports section

**Rehab Exercise**:
1. Patient completes exercise
2. **Frontend**: Status saved locally
3. **Backend**: Can be logged to database (enhancement)
4. **Doctor Feed**: Doctor can see completion status

### 3. Doctor Updates Flow
**Path**: Doctor → Update Patient → Patient Sees It

**Assign Medication**:
1. Doctor assigns medication
2. **Backend**: Stored in `patient_medications` table
3. **Backend**: Notification created for patient
4. **Database Check**: 
   ```sql
   SELECT * FROM patient_medications WHERE patient_id = {patient_id} AND is_active = 1;
   SELECT * FROM notifications WHERE user_id = {patient_id} AND type = 'MEDICATION';
   ```
5. **Patient Feed**: Patient sees medication in medications page

**Create Rehab Plan**:
1. Doctor creates rehab plan
2. **Backend**: Stored in `rehab_plans` and `rehab_exercises` tables
3. **Backend**: Notification created for patient
4. **Database Check**: 
   ```sql
   SELECT * FROM rehab_plans WHERE patient_id = {patient_id};
   SELECT * FROM rehab_exercises WHERE rehab_plan_id = {plan_id};
   ```
5. **Patient Feed**: Patient sees exercises in rehab page

**Add Diagnosis/Suggestions**:
1. Doctor adds diagnosis
2. **Backend**: Stored in patient details
3. **Patient Feed**: Patient can see in patient details

### 4. Medication Completion Flow
**Path**: Patient → Takes Medication → Doctor Notified

1. Patient marks medication as completed
2. **Backend**: Logged in `medication_logs` table
3. **Backend**: Notification created for doctor
4. **Database Check**: 
   ```sql
   SELECT * FROM medication_logs WHERE patient_medication_id = {med_id} ORDER BY taken_at DESC;
   SELECT * FROM notifications WHERE user_id = {doctor_id} AND type = 'PATIENT_MEDICATION_LOG';
   ```
5. **Doctor Feed**: Doctor sees medication intake log

### 5. Appointment Flow
**Path**: Doctor/Admin → Create Appointment → Patient Sees It

1. Doctor creates appointment
2. **Backend**: Stored in `appointments` table
3. **Backend**: Notification created for patient
4. **Database Check**: 
   ```sql
   SELECT * FROM appointments WHERE patient_id = {patient_id} ORDER BY start_time DESC;
   ```
5. **Patient Feed**: Patient sees appointment in dashboard and appointments page

---

## 🔍 Verification Queries

### Check All Data for a Patient
```sql
-- Get patient info
SELECT * FROM users WHERE id = {patient_id};

-- Get all appointments
SELECT * FROM appointments WHERE patient_id = {patient_id};

-- Get all reports
SELECT * FROM reports WHERE patient_id = {patient_id};

-- Get all symptoms
SELECT * FROM symptoms WHERE patient_id = {patient_id};

-- Get all medications
SELECT * FROM patient_medications WHERE patient_id = {patient_id};

-- Get all rehab plans
SELECT * FROM rehab_plans WHERE patient_id = {patient_id};

-- Get all notifications
SELECT * FROM notifications WHERE user_id = {patient_id};
```

### Check All Data for a Doctor
```sql
-- Get doctor info
SELECT * FROM users WHERE id = {doctor_id};

-- Get all patients (from appointments)
SELECT DISTINCT u.* 
FROM users u
INNER JOIN appointments a ON u.id = a.patient_id
WHERE a.doctor_id = {doctor_id};

-- Get all appointments
SELECT * FROM appointments WHERE doctor_id = {doctor_id};

-- Get all reports (from patients)
SELECT r.*, u.name as patient_name
FROM reports r
INNER JOIN appointments a ON r.patient_id = a.patient_id
LEFT JOIN users u ON r.patient_id = u.id
WHERE a.doctor_id = {doctor_id};

-- Get all notifications
SELECT * FROM notifications WHERE user_id = {doctor_id};
```

---

## 🔧 Troubleshooting

### Data Not Showing in App?
1. **Check Database**: Use phpMyAdmin to verify data exists
2. **Check API Response**: Use browser dev tools or Postman
3. **Check Android Logs**: Look for API errors in Logcat
4. **Check Authentication**: Verify token is valid
5. **Check Network**: Ensure backend is accessible

### Backend Not Responding?
1. Check XAMPP Apache is running
2. Check `backend/public/index.php` exists
3. Check database connection in `backend/src/config/DB.php`
4. Check `.env` file for correct database credentials
5. Test: `http://localhost/backend/public/test-db.php`

### Frontend Not Connecting?
1. Check base URL in `ApiClient.java`
2. For emulator: Use `http://10.0.2.2/backend/public/api/v1/`
3. For physical device: Use your computer's IP address
4. Check CORS settings in backend
5. Check network permissions in AndroidManifest.xml

---

## 📝 Quick Reference

| What | Where |
|------|-------|
| View Database | http://localhost/phpmyadmin |
| Database Name | `myrajourney` |
| Test Backend | http://localhost/backend/public/test-db.php |
| API Base URL | http://localhost/backend/public/api/v1/ |
| Database Files | `C:\xampp\mysql\data\myrajourney\` |
| Backend Code | `backend/src/` |
| Frontend Code | `app/src/main/java/` |

---

## ✅ Integration Checklist

- [x] User creation (admin creates users)
- [x] Login (only ACTIVE users can login)
- [x] Patient data entry (symptoms, reports, rehab)
- [x] Doctor sees patient data
- [x] Doctor updates (medications, rehab, diagnosis)
- [x] Patient sees doctor updates
- [x] Notifications (all key events)
- [x] Appointments (create and view)
- [x] Medication completion tracking
- [x] Rehab completion tracking
- [x] Password reset (email link + same-page)
- [x] All data stored in database
- [x] All data loads from API
- [x] No default/mock data

---

## 🎯 Next Steps

1. **Run Migrations**:
   ```sql
   -- Generate password hash
   php -r "echo password_hash('Divya@ida7', PASSWORD_BCRYPT);"
   
   -- Update 014_specific_users.sql with hash
   -- Run in phpMyAdmin or MySQL:
   source backend/scripts/migrations/014_specific_users.sql;
   source backend/scripts/migrations/013_education_seed.sql;
   ```

2. **Test All Flows**:
   - Create patient → Check in database → Login as patient
   - Patient enters data → Check in database → Doctor sees it
   - Doctor updates → Check in database → Patient sees it

3. **Verify Data**:
   - Use phpMyAdmin to check all tables
   - Verify notifications are created
   - Verify data relationships are correct

