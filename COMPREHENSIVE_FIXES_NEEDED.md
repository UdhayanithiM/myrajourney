# Comprehensive Fixes Needed - Static Data & Database Integration

## Issues Found:

1. **DoctorScheduleActivity** - Has 10 hardcoded sample appointments
2. **AddAppointmentActivity** - Not saving to backend, only local list
3. **ReportDetailsActivity** - Not saving diagnosis to backend
4. **SymptomLogActivity** - Not saving to backend, just showing toast
5. **UploadReportActivity** - Not uploading to backend
6. **PatientDetailsActivity** - Has hardcoded patient ID "12345"
7. **OfflineMockInterceptor** - May be interfering (but OFFLINE_MODE = false)
8. **Database connection** - Need to verify .env file exists

## Fixes Required:

### 1. DoctorScheduleActivity
- Remove hardcoded appointments (lines 40-50)
- Load appointments from `/api/v1/appointments` API
- Filter by doctor_id for doctor's schedule

### 2. AddAppointmentActivity
- Save appointment to `/api/v1/appointments` API
- Use AppointmentRequest model
- Get patient_id and doctor_id from current user
- Parse time slots to start_time and end_time format

### 3. ReportDetailsActivity
- Save diagnosis to `/api/v1/reports/{id}/notes` or similar endpoint
- Check if backend has endpoint for report notes

### 4. SymptomLogActivity
- Save symptoms to `/api/v1/symptoms` API
- Use SymptomRequest model
- Map VAS score, fatigue, stiffness to API format

### 5. UploadReportActivity
- Upload report to `/api/v1/reports` API
- Handle file upload (multipart)
- Save report metadata to database

### 6. PatientDetailsActivity
- Remove hardcoded patient ID
- Get patient ID from intent or API
- Load all patient data from backend

### 7. Database Connection
- Check if `.env` file exists in backend root
- Verify database credentials
- Test connection

