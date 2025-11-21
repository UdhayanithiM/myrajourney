# Database Setup Steps

## Step 1: Create Database

In phpMyAdmin (http://localhost/phpmyadmin):

1. Click "New" in the left sidebar
2. Database name: `myrajourney`
3. Collation: `utf8mb4_unicode_ci`
4. Click "Create"

## Step 2: Import Database Schema

1. Click on the `myrajourney` database in the left sidebar
2. Click the "Import" tab at the top
3. Click "Choose File"
4. Navigate to: `C:\xampp\htdocs\backend\scripts\setup_database.sql`
5. Click "Go" at the bottom

You should see: "Import has been successfully finished, 18 queries executed."

## Step 3: Verify Tables

Click on the `myrajourney` database in the left sidebar. You should see 18 tables:
- appointments
- articles
- chat_messages
- community_posts
- community_post_comments
- community_post_likes
- doctors
- doctor_availability
- doctor_specializations
- educational_resources
- menstrual_cycles
- notifications
- patients
- patient_health_records
- symptom_logs
- users
- user_sessions
- wellness_tips

## Step 4: Test Backend Connection

Open in browser: http://localhost/backend/public/test-connection-debug.php

Should show:
- ✅ Connected to MySQL server
- ✅ Database 'myrajourney' exists
- ✅ All 18 tables exist

## Step 5: Test API

Open in browser: http://localhost/backend/public/api/v1/education/articles

Should return:
```json
{"success":true,"data":[],"message":"Articles retrieved successfully"}
```

## Step 6: Create Test Users (Optional)

In phpMyAdmin:
1. Click on `myrajourney` database
2. Click "SQL" tab
3. Paste this SQL:

```sql
INSERT INTO users (email, password_hash, role, name, status, created_at, updated_at)
VALUES 
('patient@test.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'PATIENT', 'Test Patient', 'ACTIVE', NOW(), NOW()),
('doctor@test.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'DOCTOR', 'Dr. Test', 'ACTIVE', NOW(), NOW()),
('admin@test.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN', 'Admin User', 'ACTIVE', NOW(), NOW());
```

4. Click "Go"

Test login credentials:
- Email: `patient@test.com`
- Password: `password`

## Done!

Your backend is now ready. You can:
- Access phpMyAdmin: http://localhost/phpmyadmin
- Test backend: http://localhost/backend/public/test-connection-debug.php
- Use API: http://localhost/backend/public/api/v1/...
- Login to your app with test users
