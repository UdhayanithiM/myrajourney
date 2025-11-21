# Comprehensive Fixes Plan

## Critical Issues to Fix

### 1. Overlapping Search Bars in Doctor Reports
- Need to check if there's a search bar in ReportsAdapter or item layout
- May need to remove duplicate search functionality

### 2. User Management
- Create specific users: 2 doctors, 1 admin, multiple patients
- Doctor 1: divyapriyaa0454.sse@saveetha.com / Divya@ida7
- Doctor 2: divyapriyaa87@gmail.com / Divya@ida7  
- Admin: divyapriyaa0454.sse@saveetha.com / Divya@ida7
- Multiple patients (created by admin/doctors)

### 3. Data Flow Issues
- Appointments not showing in patient feed
- Patients created by admin not showing in doctor feed
- Patients created by doctors not showing in doctor dashboard
- Notifications when patients enter data

### 4. Patient Feed Issues
- Remove default values
- Store symptom log, reports, rehab in database
- Show doctor prescriptions/rehab in patient feed
- Add progress graphs
- Fix health statistics

### 5. Backend Integration
- Verify all data is stored in database
- Check all API endpoints
- Ensure proper notifications

### 6. Additional Features
- Expand education hub content
- Make chatbot realtime
- Fix rehab video links

## Backend Database Location
- Database: MySQL (myrajourney)
- Location: XAMPP MySQL (usually C:\xampp\mysql\data\myrajourney)
- View via: phpMyAdmin (http://localhost/phpmyadmin)
- Or: MySQL Workbench / Command line

## Backend API Location
- Backend: PHP (backend/public/index.php)
- Base URL: http://localhost/backend/public/api/v1/
- Test: http://localhost/backend/public/test-db.php

