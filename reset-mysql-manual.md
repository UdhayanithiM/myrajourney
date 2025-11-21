# Manual MySQL Password Reset

## Step 1: Stop MySQL Service

Open PowerShell as Administrator and run:
```powershell
net stop MySQL80
```

## Step 2: Start MySQL in Safe Mode (Skip Grant Tables)

```powershell
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"
mysqld --console --skip-grant-tables --shared-memory
```

**IMPORTANT:** Leave this window open! MySQL is now running without password protection.

## Step 3: Open a NEW PowerShell Window (as Administrator)

In the new window, run:
```powershell
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"
mysql -u root
```

## Step 4: Reset the Password

In the MySQL prompt, run these commands:
```sql
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY '';
FLUSH PRIVILEGES;
EXIT;
```

## Step 5: Stop the Safe Mode MySQL

Go back to the first PowerShell window (where mysqld is running) and press `Ctrl+C` to stop it.

## Step 6: Start MySQL Normally

```powershell
net start MySQL80
```

## Step 7: Test Connection

```powershell
mysql -u root -e "SELECT 'Success!' as status;"
```

If you see "Success!", the password has been reset to empty!

## Step 8: Update phpMyAdmin

Run:
```powershell
cd C:\Users\Admin\AndroidStudioProjects\myrajourney
.\fix-phpmyadmin-empty.ps1
```

Then try: http://localhost/phpmyadmin

---

## Alternative: Use XAMPP MySQL Instead

If the above is too complicated, you can switch to XAMPP's MySQL which has no password by default:

### 1. Disable MySQL80
```powershell
# As Administrator
net stop MySQL80
sc config MySQL80 start= disabled
```

### 2. Start XAMPP MySQL
- Open XAMPP Control Panel: `C:\xampp\xampp-control.exe`
- Click "Start" next to MySQL
- XAMPP MySQL runs on port 3306 with no password

### 3. Update phpMyAdmin
```powershell
.\fix-phpmyadmin-empty.ps1
```

### 4. Test
Open: http://localhost/phpmyadmin

---

**Which method do you prefer?**
1. Manual password reset (more steps but keeps MySQL80)
2. Switch to XAMPP MySQL (easier, uses XAMPP's MySQL)
