# Comprehensive Fix Plan

## Issues Identified

### 1. Navigation Drawer Shows Default Names ❌
- Patient drawer shows "Divya" instead of logged-in patient name
- Doctor drawer shows "Dr.Avinash" instead of logged-in doctor name

### 2. Doctors Can't See Assigned Patients ❌
- AllPatientsActivity loads ALL patients
- Should only show patients assigned to that doctor
- Backend already has filtering, but app doesn't use it correctly

### 3. Symptom Logging May Fail ❌
- Need to verify patient ID is correctly passed
- Need to ensure authentication token is sent

### 4. Doctor Can't Add Medications/Rehab ❌
- Need to implement medication assignment UI
- Need to implement rehab assignment UI
- Need patient selection for assignments

### 5. Patient Can't View Doctor's Feedback ❌
- Need to show doctor's notes/feedback
- Need to show assigned medications
- Need to show assigned rehab plans

## Solutions

### Fix 1: Navigation Drawer Names ✅
**Files**: 
- `nav_header_patient.xml` - Add IDs to TextViews
- `nav_header_doctor.xml` - Add IDs to TextViews
- `PatientDashboardActivity.java` - Update header with user info
- `DoctorDashboardActivity.java` - Update header with user info

**Implementation**: DONE

### Fix 2: Doctor Patient Filtering ✅
**Backend**: Already implemented in `PatientController.php`
- Doctors see only assigned patients
- Admins see all patients

**App Fix Needed**:
- Ensure authentication token is sent with API calls
- Backend will automatically filter based on doctor ID from token

### Fix 3: Symptom Logging
**Check**:
- SymptomLogActivity already uses TokenManager
- Already sends patient ID
- Should work if token is valid

### Fix 4: Doctor Medication/Rehab Assignment
**Need to Create**:
- Medication assignment screen for doctors
- Rehab assignment screen for doctors
- Patient selection in these screens

### Fix 5: Patient View Doctor Feedback
**Need to Update**:
- PatientMedicationsActivity - show assigned meds
- PatientRehabilitationActivity - show assigned rehab
- Add feedback/notes section

## Implementation Order

1. ✅ Fix navigation drawer names
2. ⏳ Verify API authentication
3. ⏳ Test patient filtering for doctors
4. ⏳ Create doctor medication assignment UI
5. ⏳ Create doctor rehab assignment UI
6. ⏳ Update patient medication view
7. ⏳ Update patient rehab view
8. ⏳ Add doctor feedback display

