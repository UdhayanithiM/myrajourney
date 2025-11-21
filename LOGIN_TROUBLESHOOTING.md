# Login Troubleshooting Guide

## 🔍 Common Login Issues & Solutions

### Issue: "Network error" or "Login failed"

#### **Solution 1: Check Base URL Configuration**

The app needs to know where your backend server is located.

**For Android Emulator:**
- Base URL should be: `http://10.0.2.2/backend/public/api/v1/`
- This is already set by default

**For Physical Device:**
- Base URL should be: `http://YOUR_PC_IP/backend/public/api/v1/`
- Find your PC IP:
  1. Open CMD
  2. Run: `ipconfig`
  3. Look for "IPv4 Address" (e.g., `10.16.20.165`)
  4. Update `ApiClient.java` with your IP

**Location:** `app/src/main/java/com/example/myrajouney/api/ApiClient.java`

```java
private static final String BASE_URL_PHYSICAL = "http://10.16.20.165/backend/public/api/v1/";
```

---

#### **Solution 2: Check XAMPP is Running**

1. Open **XAMPP Control Panel**
2. Verify **Apache** shows "Running" (green)
3. Verify **MySQL** shows "Running" (green)
4. If not running, click "Start"

---

#### **Solution 3: Test Backend from Device**

**On Emulator:**
- Open browser in emulator
- Visit: `http://10.0.2.2/backend/public/api/v1/education/articles`
- Should show JSON response

**On Physical Device:**
- Open browser on device
- Visit: `http://10.16.20.165/backend/public/api/v1/education/articles`
- Should show JSON response
- If it doesn't work, check:
  - Device and PC are on same WiFi network
  - Windows Firewall allows Apache
  - PC IP address is correct

---

#### **Solution 4: Check Windows Firewall**

1. Open **Windows Defender Firewall**
2. Click **Allow an app through firewall**
3. Find **Apache HTTP Server** or **XAMPP**
4. Check both **Private** and **Public**
5. Click **OK**

Or temporarily disable firewall to test:
- If login works with firewall off, then add Apache exception

---

#### **Solution 5: Check Network Connection**

**For Physical Device:**
- Device and PC must be on **same WiFi network**
- Test: Can you ping PC from device? (Use network tools app)
- Alternative: Use USB tethering or hotspot

---

#### **Solution 6: Verify Backend is Accessible**

**Test in PowerShell:**
```powershell
# Test from your PC
Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/education/articles" -Method GET | Select-Object -ExpandProperty Content

# Test login endpoint
$body = '{"email":"test@example.com","password":"test123456"}'
Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content
```

If these work, backend is fine. Issue is with device/emulator connection.

---

#### **Solution 7: Check Logcat for Errors**

In Android Studio:
1. Open **Logcat** tab
2. Filter by: `LoginActivity` or `ApiClient`
3. Look for error messages
4. Check what base URL is being used
5. Look for connection errors

**Common Logcat messages:**
- `Using base URL: http://10.0.2.2/backend/public/api/v1/` ✅ Good
- `Unable to resolve host "10.0.2.2"` ❌ Emulator can't reach host
- `Failed to connect to /10.16.20.165` ❌ Physical device can't reach PC
- `Connection refused` ❌ Apache not running or firewall blocking

---

#### **Solution 8: Verify Database is Working**

```powershell
# Test database connection
mysql -u root -e "USE myrajourney; SELECT COUNT(*) FROM users;"
```

If this fails, fix database connection first.

---

## ✅ Quick Fix Checklist

- [ ] XAMPP Apache is running
- [ ] XAMPP MySQL is running
- [ ] Base URL is correct for your device type
- [ ] Windows Firewall allows Apache
- [ ] Device and PC are on same network (for physical device)
- [ ] Backend is accessible from browser (test URL)
- [ ] Database is working
- [ ] `.env` file is configured correctly

---

## 🎯 Step-by-Step Debugging

1. **Test backend from PC:**
   ```powershell
   Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/education/articles" -Method GET
   ```
   If this fails → Backend issue

2. **Test from emulator browser:**
   - Visit: `http://10.0.2.2/backend/public/api/v1/education/articles`
   - If this fails → Network/emulator issue

3. **Test from physical device browser:**
   - Visit: `http://YOUR_PC_IP/backend/public/api/v1/education/articles`
   - If this fails → Network/firewall issue

4. **Check Logcat:**
   - Look for base URL being used
   - Look for connection errors
   - Share error messages

---

## 📱 Device-Specific Configuration

### Android Emulator
- ✅ Already configured: `http://10.0.2.2/backend/public/api/v1/`
- ✅ Network security config allows cleartext
- Should work if XAMPP is running

### Physical Device
**Required Steps:**
1. Find PC IP: Run `ipconfig` in CMD
2. Update `ApiClient.java`:
   ```java
   private static final String BASE_URL_PHYSICAL = "http://YOUR_IP/backend/public/api/v1/";
   ```
3. Ensure device and PC on same WiFi
4. Allow Apache in Windows Firewall
5. Test in device browser first

---

## 🔧 Manual Base URL Configuration

If auto-detection doesn't work, manually set base URL:

**Edit:** `app/src/main/java/com/example/myrajouney/api/ApiClient.java`

```java
public static String getBaseUrl() {
    // Manually set your base URL here
    return "http://10.16.20.165/backend/public/api/v1/"; // Your PC IP
    // OR for emulator:
    // return "http://10.0.2.2/backend/public/api/v1/";
}
```

---

## 📝 Still Not Working?

1. **Share Logcat output** - Look for errors in Android Studio Logcat
2. **Share error message** - Exact error from login screen
3. **Test backend** - Confirm backend works from PowerShell
4. **Check device type** - Emulator or physical device?

The app will now show more detailed error messages to help debug!

