package com.example.myrajourney.common.appointments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.*;

public class AddAppointmentActivity extends AppCompatActivity {

    EditText etPatientName, etDoctorName, etReason;
    Button btnPickDate, btnAddAppointment;
    Spinner spinnerTimeSlot;
    String selectedDate = "";
    List<String> availableTimeSlots;

    static List<Appointment> allAppointments = new ArrayList<>(); // shared list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment);

        etPatientName = findViewById(R.id.et_patient_name);
        etDoctorName = findViewById(R.id.et_doctor_name);
        etReason = findViewById(R.id.et_reason);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnAddAppointment = findViewById(R.id.btn_add_appointment);
        spinnerTimeSlot = findViewById(R.id.spinner_timeslot);

        // Example time slots
        availableTimeSlots = Arrays.asList(
                "10:00 AM - 10:30 AM",
                "10:30 AM - 11:00 AM",
                "11:00 AM - 11:30 AM",
                "11:30 AM - 12:00 PM"
        );

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, availableTimeSlots);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeSlot.setAdapter(adapter);

        // Pick date
        btnPickDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, monthOfYear, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                        selectedDate = sdf.format(selected.getTime());
                        btnPickDate.setText(selectedDate);
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        });

        btnAddAppointment.setOnClickListener(v -> {
            String patientName = etPatientName.getText().toString().trim();
            String doctorName = etDoctorName.getText().toString().trim();
            String reason = etReason.getText().toString().trim();
            String timeSlot = spinnerTimeSlot.getSelectedItem().toString();

            if (patientName.isEmpty() || doctorName.isEmpty() || selectedDate.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save appointment to backend API
            saveAppointmentToBackend(patientName, doctorName, reason, timeSlot);
        });
    }

    private void saveAppointmentToBackend(String patientName, String doctorName, String reason, String timeSlot) {
        TokenManager tokenManager = TokenManager.getInstance(this);
        String currentUserId = tokenManager.getUserId();
        String currentUserRole = tokenManager.getUserRole();

        if (currentUserId == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        String startTime = parseDateTime(selectedDate, timeSlot.split(" - ")[0].trim());
        String endTime = parseDateTime(selectedDate, timeSlot.split(" - ").length > 1 ? timeSlot.split(" - ")[1].trim() : timeSlot.split(" - ")[0].trim());

        String patientId = "PATIENT".equals(currentUserRole) ? currentUserId : null;
        String doctorId = "DOCTOR".equals(currentUserRole) ? currentUserId : null;

        if (patientId == null || doctorId == null) {
            findUserIdsAndSave(patientName, doctorName, reason, startTime, endTime);
            return;
        }

        com.example.myrajourney.data.model.AppointmentRequest request =
                new com.example.myrajourney.data.model.AppointmentRequest(
                        patientId,
                        doctorId,
                        reason.isEmpty() ? "Appointment" : reason,
                        reason,
                        startTime,
                        endTime
                );

        com.example.myrajourney.core.network.ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.Appointment>> call =
                apiService.createAppointment(request);

        call.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.Appointment>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.Appointment>> call,
                                   retrofit2.Response<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.Appointment>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AddAppointmentActivity.this, "Appointment created successfully", Toast.LENGTH_SHORT).show();
                    resetForm();
                    startActivity(new Intent(AddAppointmentActivity.this, PatientAppointmentsActivity.class));
                    finish();
                } else {
                    String errorMsg = "Failed to create appointment";
                    if (response.body() != null && response.body().getError() != null) {
                        errorMsg = response.body().getError().getMessage();
                    }
                    Toast.makeText(AddAppointmentActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.Appointment>> call, Throwable t) {
                Toast.makeText(AddAppointmentActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findUserIdsAndSave(String patientName, String doctorName, String reason, String startTime, String endTime) {
        com.example.myrajourney.core.network.ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.User>>> call = apiService.getAllPatients();

        call.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.User>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.User>>> call,
                                   retrofit2.Response<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.User>>> response) {
                String patientId = null;
                String doctorId = null;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<com.example.myrajourney.data.model
.User> users = response.body().getData();
                    if (users != null) {
                        for (com.example.myrajourney.data.model
.User user : users) {
                            if (user.getName() != null && user.getName().equalsIgnoreCase(patientName) && "PATIENT".equals(user.getRole())) {
                                patientId = user.getIdString();
                            }
                            if (user.getName() != null && user.getName().equalsIgnoreCase(doctorName) && "DOCTOR".equals(user.getRole())) {
                                doctorId = user.getIdString();
                            }
                        }
                    }
                }

                TokenManager tokenManager = TokenManager.getInstance(AddAppointmentActivity.this);
                String currentUserId = tokenManager.getUserId();
                String currentUserRole = tokenManager.getUserRole();

                if (patientId == null && "PATIENT".equals(currentUserRole)) {
                    patientId = currentUserId;
                }
                if (doctorId == null && "DOCTOR".equals(currentUserRole)) {
                    doctorId = currentUserId;
                }

                if (patientId == null || doctorId == null) {
                    Toast.makeText(AddAppointmentActivity.this, "Could not find patient or doctor. Please check names.", Toast.LENGTH_LONG).show();
                    return;
                }

                com.example.myrajourney.data.model
.AppointmentRequest request =
                        new com.example.myrajourney.data.model
.AppointmentRequest(
                                patientId,
                                doctorId,
                                reason.isEmpty() ? "Appointment" : reason,
                                reason,
                                startTime,
                                endTime
                        );

                retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.Appointment>> createCall =
                        apiService.createAppointment(request);

                createCall.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.Appointment>>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.Appointment>> call,
                                           retrofit2.Response<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.Appointment>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(AddAppointmentActivity.this, "Appointment created successfully", Toast.LENGTH_SHORT).show();
                            resetForm();
                            startActivity(new Intent(AddAppointmentActivity.this, PatientAppointmentsActivity.class));
                            finish();
                        } else {
                            Toast.makeText(AddAppointmentActivity.this, "Failed to create appointment", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.Appointment>> call, Throwable t) {
                        Toast.makeText(AddAppointmentActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.User>>> call, Throwable t) {
                Toast.makeText(AddAppointmentActivity.this, "Failed to find users. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String parseDateTime(String dateStr, String timeStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            Date date = dateFormat.parse(dateStr);

            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
            Date time = timeFormat.parse(timeStr);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(time);
            cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, 0);

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            return outputFormat.format(cal.getTime());
        } catch (Exception e) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            return format.format(new Date());
        }
    }

    private void resetForm() {
        etPatientName.setText("");
        etDoctorName.setText("");
        etReason.setText("");
        btnPickDate.setText("Pick Date");
    }
}






