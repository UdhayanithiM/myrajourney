# Database Consolidation Guide

## Problem
You have 2 databases named `myrajourney` in phpMyAdmin, and neither has the `password_resets` table.

## Solution
We have two options:

### Option 1: Fresh Start (Recommended if no important data)
This will delete both databases and create a fresh one with all tables.

### Option 2: Update Existing (Keep existing data)
This will keep your existing database and just add missing tables.

---

## Option 1: Fresh Start (Delete Duplicates & Create Fresh)

### Steps:

1. **Open phpMyAdmin**: `http://localhost/phpmyadmin`

2. **Select SQL tab** at the top

3. **Copy and paste** the contents of `backend/scripts/consolidate_database.sql`

4. **Click "Go"** to execute

This will:
- ✅ Delete duplicate databases
- ✅ Create fresh `myrajourney` database
- ✅ Create all tables including `password_resets`
- ✅ Set up all relationships and indexes

### OR use MySQL command line:

```bash
cd C:\xampp\mysql\bin
mysql.exe -u root -p < "C:\Users\Admin\AndroidStudioProjects\myrajourney\backend\scripts\consolidate_database.sql"
```

---

## Option 2: Update Existing (Keep Data)

### Steps:

1. **Open phpMyAdmin**: `http://localhost/phpmyadmin`

2. **Manually delete one duplicate database**:
   - Click on one of the `myrajourney` databases
   - Click "Operations" tab
   - Click "Drop the database (DROP)" button
   - Confirm deletion

3. **Select the remaining `myrajourney` database**

4. **Select SQL tab** at the top

5. **Copy and paste** the contents of `backend/scripts/update_existing_database.sql`

6. **Click "Go"** to execute

This will:
- ✅ Keep your existing data
- ✅ Add `password_resets` table if missing
- ✅ Add any other missing tables
- ✅ Not delete existing data

### OR use MySQL command line:

```bash
cd C:\xampp\mysql\bin
mysql.exe -u root -p myrajourney < "C:\Users\Admin\AndroidStudioProjects\myrajourney\backend\scripts\update_existing_database.sql"
```

---

## Verify Tables Were Created

After running either script, verify in phpMyAdmin:

1. Click on `myrajourney` database
2. You should see these tables:
   - ✅ `users`
   - ✅ `patients`
   - ✅ `doctors`
   - ✅ `user_settings`
   - ✅ `appointments`
   - ✅ `reports`
   - ✅ `report_notes`
   - ✅ `patient_medications`
   - ✅ `medication_logs`
   - ✅ `rehab_plans`
   - ✅ `rehab_exercises`
   - ✅ `notifications`
   - ✅ `education_articles`
   - ✅ `symptoms`
   - ✅ `health_metrics`
   - ✅ `password_resets` ← **This should now exist!**

---

## Quick SQL to Check

Run this in phpMyAdmin SQL tab:

```sql
USE myrajourney;

-- Check if password_resets table exists
SHOW TABLES LIKE 'password_resets';

-- List all tables
SHOW TABLES;

-- Check table structure
DESCRIBE password_resets;
```

---

## After Consolidation

1. **Run seed data** (optional):
   ```sql
   -- Generate password hash first
   -- In PHP: php -r "echo password_hash('Divya@ida7', PASSWORD_BCRYPT);"
   
   -- Then update and run:
   source backend/scripts/migrations/012_default_users.sql;
   source backend/scripts/migrations/013_education_seed.sql;
   source backend/scripts/migrations/014_specific_users.sql;
   ```

2. **Test password reset**:
   - Try forgot password in the app
   - Check `password_resets` table for tokens

---

## Troubleshooting

### Error: "Table already exists"
- This is OK! The script uses `CREATE TABLE IF NOT EXISTS`
- It won't overwrite existing tables

### Error: "Cannot delete database"
- Make sure no connections are using the database
- Close phpMyAdmin and reopen
- Restart XAMPP MySQL service

### Error: "Foreign key constraint fails"
- Make sure `users` table exists first
- Run migrations in order (001 → 002 → 003...)

---

## Files Created

- `backend/scripts/consolidate_database.sql` - Fresh start script
- `backend/scripts/update_existing_database.sql` - Update existing script
- `DATABASE_CONSOLIDATION_GUIDE.md` - This guide

---

## Summary

**Recommended**: Use Option 2 (Update Existing) if you have important data, or Option 1 (Fresh Start) if you want a clean slate.

Both scripts will ensure the `password_resets` table exists and all other tables are properly set up.

