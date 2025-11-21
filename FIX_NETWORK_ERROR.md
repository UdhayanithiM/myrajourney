# Fix Network Error - Complete Guide

## ✅ Backend Status: WORKING

Your backend is accessible at:
- **Emulator**: `http://10.0.2.2/backend/public/api/v1/`
- **Physical Device**: `http://10.132.68.78/backend/public/api/v1/`

## Changes Made

1. ✅ Updated `ApiClient.java` with current IP: `10.132.68.78`
2. ✅ Backend is accessible from network
3. ✅ Firewall rules are configured
4. ✅ Apache is listening on all interfaces

## Next Steps to Fix Your App

### Step 1: Rebuild the App

The IP address has been updated in the code. You need to rebuild:

```bash
# In Android Studio:
1. Click "Build" menu
2. Select "Clean Project"
3. Wait for it to finish
4. Click "Build" menu again
5. Select "Rebuild Project"
6. Wait for build to complete
7. Run the app again
```

Or via command line:
```bash
cd C:\Users\Admin\AndroidStudioProjects\myrajourney
gradlew clean assembleDebug
```

### Step 2: Check Device/Emulator

**If using Emulator:**
- URL is automatically set to: `http://10.0.2.2/backend/public/api/v1/`
- Should work without changes

**If using Physical Device:**
- Ensure your phone is on the **same WiFi network** as your PC
- URL is set to: `http://10.132.68.78/backend/public/api/v1/`
- Your phone must be able to reach `10.132.68.78`

### Step 3: Test from Phone Browser (Physical Device Only)

Before testing the app, test from your phone's browser:

1. Open Chrome/Browser on your phone
2. Go to: `http://10.132.68.78/backend/public/api/v1/education/articles`
3. You should see: `{"success":true,"data":[],...}`

If this doesn't work:
- Your phone is on a different network
- Check WiFi settings on both PC and phone

### Step 4: Check App Logs

When you run the app and try to login, check Android Studio Logcat for:

```
ApiClient: ========================================
ApiClient: API Base URL: http://...
ApiClient: Offline Mode: false
ApiClient: Device Type: EMULATOR or PHYSICAL DEVICE
ApiClient: ========================================
```

This will tell you which URL the app is using.

### Step 5: Test Login

Use these credentials:
- **Email**: `patient@test.com`
- **Password**: `password`

## Troubleshooting

### Error: "Network Error" or "Failed to connect"

**Check 1: Is backend running?**
```powershell
curl http://localhost/backend/public/api/v1/education/articles
```
Should return JSON data.

**Check 2: Is XAMPP running?**
- Open XAMPP Control Panel
- Ensure Apache and MySQL are green/running

**Check 3: Test from phone browser (physical device)**
- Open: `http://10.132.68.78/backend/public/api/v1/education/articles`
- Should show JSON data
- If not, phone can't reach your PC

**Check 4: Same WiFi network?**
- PC and phone must be on same WiFi
- Check WiFi name on both devices

**Check 5: IP changed?**
Run in PowerShell:
```powershell
ipconfig | Select-String "IPv4"
```
If IP is different from `10.132.68.78`, update `ApiClient.java` again.

### Error: "Invalid credentials"

Backend is working, but login failed. Check:
1. Email: `patient@test.com`
2. Password: `password` (lowercase)
3. Check Logcat for actual error message

### Error: "Timeout"

Network is too slow or blocked:
1. Increase timeout in `ApiClient.java` (already set to 60 seconds)
2. Check firewall isn't blocking
3. Try from emulator instead of physical device

## Quick Test Commands

### Test backend is running:
```powershell
curl http://localhost/backend/public/api/v1/education/articles
```

### Test backend from network:
```powershell
curl http://10.132.68.78/backend/public/api/v1/education/articles
```

### Test login:
```powershell
$body = '{"email":"patient@test.com","password":"password"}'
curl -Method POST -Uri "http://10.132.68.78/backend/public/api/v1/auth/login" -Body $body -ContentType "application/json"
```

### Get current IP:
```powershell
ipconfig | Select-String "IPv4"
```

## Summary

1. ✅ Backend is working and accessible
2. ✅ IP address updated in code to `10.132.68.78`
3. ⚠️ **You must rebuild the app** for changes to take effect
4. ⚠️ **Physical device must be on same WiFi** as PC

**Next Action**: Rebuild the app in Android Studio and try logging in again.
