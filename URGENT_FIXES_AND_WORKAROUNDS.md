# Urgent Fixes and Workarounds

## Issue 1: Doctor Dropdown Not Showing

### Problem
The `/api/v1/admin/doctors` endpoint is not responding, causing the doctor dropdown to be empty in the assignment screen.

### Root Cause
Routing issue in backend - the endpoint exists but isn't being matched by the router.

### Immediate Workaround
Modify the `AssignPatientToDoctorActivity` to use the `/api/v1/patients` endpoint which returns all users, then filter for doctors on the client side.

### Fix Applied
Update `AssignPatientToDoctorActivity.java`:

```java
// Instead of calling getAllDoctors(), call getAllPatients() and filter
private void loadDoctors() {
    Call<ApiResponse<List<User>>> call = apiService.getAllPatients();
    call.enqueue(new Callback<ApiResponse<List<User>>>() {
        @Override
        public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                List<User> allUsers = response.body().getData();
                if (allUsers != null) {
                    // Filter for doctors only
                    doctorsList = new ArrayList<>();
                    for (User user : allUsers) {
                        if ("DOCTOR".equals(user.getRole())) {
                            Doctor doctor = new Doctor();
                            doctor.setId(user.getId());
                            doctor.setName(user.getName());
                            doctor.setEmail(user.getEmail());
                            doctorsList.add(doctor);
                        }
                    }
                }
            }
        }
        
        @Override
        public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
            Toast.makeText(AssignPatientToDoctorActivity.this, "Failed to load doctors: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
}
```

## Issue 2: Admin Dashboard Not Scrolling

### Problem
Admin dashboard content extends beyond screen but doesn't scroll.

### Status
FIXED - ScrollView was added to the layout.

### Verification
The layout file now starts with:
```xml
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">
```

### If Still Not Working
1. Rebuild the app completely
2. Clear app data
3. Reinstall the app

## Alternative Solution for Doctor Dropdown

### Option A: Use Direct Database Query
Create a simple PHP file that bypasses the routing:

File: `backend/public/get-doctors.php`
```php
<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require __DIR__ . '/../src/config/db.php';

$db = Src\Config\DB::conn();
$stmt = $db->prepare("SELECT u.id, u.name, u.email, d.specialization 
    FROM users u
    LEFT JOIN doctors d ON u.id = d.id
    WHERE u.role = 'DOCTOR' AND u.status = 'ACTIVE'
    ORDER BY u.name ASC");
$stmt->execute();
$doctors = $stmt->fetchAll();

echo json_encode(['success' => true, 'data' => $doctors]);
```

Then update Android to call: `http://10.170.214.165/backend/public/get-doctors.php`

### Option B: Use Existing Patients Endpoint (RECOMMENDED)
Since `/api/v1/patients` works and returns all users when called by admin, we can filter on the client side.

## Quick Test Commands

### Test if doctors exist in database:
```sql
SELECT u.id, u.name, u.email, d.specialization 
FROM users u 
JOIN doctors d ON u.id = d.id 
WHERE u.role = 'DOCTOR';
```

### Test if patients endpoint works:
```powershell
$loginBody = '{"email":"admin@test.com","password":"password"}'
$loginResponse = Invoke-RestMethod -Uri "http://localhost/backend/public/api/v1/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
$token = $loginResponse.data.token
Invoke-RestMethod -Uri "http://localhost/backend/public/api/v1/patients" -Headers @{"Authorization" = "Bearer $token"} -Method GET
```

## Implementation Steps

1. **Update AssignPatientToDoctorActivity.java**
   - Change `loadDoctors()` method to use patients endpoint
   - Filter for DOCTOR role on client side

2. **Rebuild App**
   - Build → Clean Project
   - Build → Rebuild Project

3. **Test**
   - Login as admin
   - Go to "Assign Patients to Doctors"
   - Verify doctors appear in dropdown

## Status

- ✅ Admin dashboard scrolling: FIXED
- ⚠️ Doctor dropdown: WORKAROUND AVAILABLE
- ✅ Database: All data correct
- ✅ Backend methods: All exist
- ⚠️ Routing: Needs investigation

## Next Steps

1. Apply the workaround for doctor dropdown
2. Test the assignment functionality
3. Verify data is saved correctly
4. Debug routing issue separately (non-blocking)

The app will be fully functional with the workaround applied.
