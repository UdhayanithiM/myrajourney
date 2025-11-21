# Database Setup Instructions

## Problem: Duplicate Databases and Missing Tables

You have 2 databases named `myrajourney` in phpMyAdmin, and the `password_resets` table is missing.

## Solution Steps

### Step 1: Identify Which Database to Keep

1. Open phpMyAdmin: `http://localhost/phpmyadmin`
2. In the left sidebar, you'll see both databases (e.g., `myrajourney` and `myrajourney_old`)
3. Click on each database and check:
   - Which one has more tables?
   - Which one has more data (check `users` table)?
   - Which one has the most recent data?

**Keep the database with more data/tables.**

### Step 2: Delete the Duplicate Database

**Option A: Using phpMyAdmin (Easiest)**
1. Click on the database you want to DELETE in the left sidebar
2. Click the "Operations" tab at the top
3. Scroll down to "Drop the database (DROP)"
4. Click "Drop the database"
5. Confirm the deletion

**Option B: Using SQL**
1. In phpMyAdmin, click "SQL" tab
2. Run this (replace `myrajourney_old` with your duplicate database name):
```sql
DROP DATABASE IF EXISTS myrajourney_old;
```

### Step 3: Create All Missing Tables

**Option A: Using phpMyAdmin (Recommended)**
1. Click on the database you kept (e.g., `myrajourney`)
2. Click the "SQL" tab
3. Open the file: `backend/scripts/setup_database.sql`
4. Copy all the SQL content
5. Paste it into the SQL tab
6. Click "Go" to execute

**Option B: Using MySQL Command Line**
```bash
cd C:\xampp\mysql\bin
mysql.exe -u root -p
# Press Enter (no password for XAMPP)
source C:\Users\Admin\AndroidStudioProjects\myrajourney\backend\scripts\setup_database.sql
```

### Step 4: Verify All Tables Are Created

Run this SQL to check:
```sql
USE myrajourney;
SHOW TABLES;
```

You should see these 18 tables:
1. users
2. password_resets ✅ (This was missing!)
3. patients
4. doctors
5. user_settings
6. appointments
7. reports
8. report_notes
9. patient_medications
10. medication_logs
11. rehab_plans
12. rehab_exercises
13. notifications
14. education_articles
15. symptoms
16. health_metrics

### Step 5: Verify password_resets Table

Run this to check the table structure:
```sql
DESCRIBE password_resets;
```

You should see:
- id
- user_id
- token
- expires_at
- created_at

### Step 6: (Optional) Add Default Users

If you want to add the default users (patient, doctor, admin):

1. First, generate password hash:
```bash
php -r "echo password_hash('Divya@ida7', PASSWORD_BCRYPT);"
```

2. Copy the generated hash

3. Open `backend/scripts/migrations/014_specific_users.sql`

4. Replace `PASSWORD_HASH_PLACEHOLDER` with the generated hash

5. Run the migration in phpMyAdmin SQL tab

## Quick Fix Script

If you want to do everything at once:

1. **Delete duplicate database** (manually in phpMyAdmin - see Step 2)

2. **Run setup script**:
   - Open phpMyAdmin
   - Click on `myrajourney` database
   - Click "SQL" tab
   - Copy and paste contents of `backend/scripts/setup_database.sql`
   - Click "Go"

3. **Verify**:
```sql
SHOW TABLES;
DESCRIBE password_resets;
```

## Troubleshooting

### If tables already exist:
The script uses `CREATE TABLE IF NOT EXISTS`, so it's safe to run even if some tables exist.

### If foreign key errors:
Make sure tables are created in the correct order (the setup script handles this).

### If you accidentally deleted the wrong database:
- Check if you have a backup
- If not, you may need to recreate data manually

## Need Help?

If you're unsure which database to keep:
1. Check the `users` table in both databases
2. Keep the one with more users or more recent data
3. If both are empty, it doesn't matter which one you keep

