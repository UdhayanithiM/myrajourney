package com.example.myrajourney.common.appointments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.TokenManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Appointment;
import com.example.myrajourney.data.model.AppointmentRequest;
import com.example.myrajourney.data.model.User;
import com.example.myrajourney.patient.appointments.PatientAppointmentsActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
// ---------------------

public class AddAppointmentActivity extends AppCompatActivity {

    EditText etPatientName, etDoctorName, etReason;
    Button btnPickDate, btnAddAppointment;
    Spinner spinnerTimeSlot;
    String selectedDate = "";
    List<String> availableTimeSlots;

    // static List<Appointment> allAppointments = new ArrayList<>(); // Removed: Not needed if using backend API

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
                "11:30 AM - 12:00 PM",
                "02:00 PM - 02:30 PM",
                "02:30 PM - 03:00 PM"
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
            // Optional: Disable past dates
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        btnAddAppointment.setOnClickListener(v -> {
            String patientName = etPatientName.getText().toString().trim();
            String doctorName = etDoctorName.getText().toString().trim();
            String reason = etReason.getText().toString().trim();
            String timeSlot = spinnerTimeSlot.getSelectedItem().toString();

            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
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

        // Parse start and end time
        String[] times = timeSlot.split(" - ");
        String startTimeStr = times[0].trim();
        String endTimeStr = times.length > 1 ? times[1].trim() : startTimeStr; // Fallback

        String startTime = parseDateTime(selectedDate, startTimeStr);
        String endTime = parseDateTime(selectedDate, endTimeStr);

        // Determine IDs based on role
        String patientId = "PATIENT".equals(currentUserRole) ? currentUserId : null;
        String doctorId = "DOCTOR".equals(currentUserRole) ? currentUserId : null;

        // If ID is missing (e.g. Patient creating appt needs Doctor ID, or Admin creating for both)
        if (patientId == null || doctorId == null) {
            findUserIdsAndSave(patientName, doctorName, reason, startTime, endTime);
            return;
        }

        // Create request
        AppointmentRequest request = new AppointmentRequest(
                patientId,
                doctorId,
                reason.isEmpty() ? "General Checkup" : reason, // Default title/type
                reason,
                startTime,
                endTime
        );

        ApiService apiService = ApiClient.getApiService(this);
        Call<ApiResponse<Appointment>> call = apiService.createAppointment(request);

        call.enqueue(new Callback<ApiResponse<Appointment>>() {
            @Override
            public void onResponse(Call<ApiResponse<Appointment>> call, Response<ApiResponse<Appointment>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AddAppointmentActivity.this, "Appointment created successfully", Toast.LENGTH_SHORT).show();
                    resetForm();
                    // Navigate back or to list
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
            public void onFailure(Call<ApiResponse<Appointment>> call, Throwable t) {
                Toast.makeText(AddAppointmentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findUserIdsAndSave(String patientName, String doctorName, String reason, String startTime, String endTime) {
        ApiService apiService = ApiClient.getApiService(this);
        // Assuming getAllPatients actually returns all users or you have an endpoint for searching
        // You might need a specific endpoint like getUsers() if getAllPatients only returns patients
        Call<ApiResponse<List<User>>> call = apiService.getAllPatients();

        call.enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                String foundPatientId = null;
                String foundDoctorId = null;

                TokenManager tokenManager = TokenManager.getInstance(AddAppointmentActivity.this);
                String currentUserId = tokenManager.getUserId();
                String currentUserRole = tokenManager.getUserRole();

                // Pre-fill known ID
                if ("PATIENT".equals(currentUserRole)) foundPatientId = currentUserId;
                if ("DOCTOR".equals(currentUserRole)) foundDoctorId = currentUserId;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<User> users = response.body().getData();
                    if (users != null) {
                        for (User user : users) {
                            // Match Patient Name if we don't have ID yet
                            if (foundPatientId == null && user.getName() != null
                                    && user.getName().equalsIgnoreCase(patientName)
                                    && "PATIENT".equals(user.getRole())) {
                                foundPatientId = String.valueOf(user.getId());
                            }
                            // Match Doctor Name if we don't have ID yet
                            if (foundDoctorId == null && user.getName() != null
                                    && user.getName().equalsIgnoreCase(doctorName)
                                    && "DOCTOR".equals(user.getRole())) {
                                foundDoctorId = String.valueOf(user.getId());
                            }
                        }
                    }
                }

                if (foundPatientId == null || foundDoctorId == null) {
                    Toast.makeText(AddAppointmentActivity.this,
                            "Could not find matching user (Patient/Doctor). Check spelling.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Proceed to save
                AppointmentRequest request = new AppointmentRequest(
                        foundPatientId,
                        foundDoctorId,
                        reason.isEmpty() ? "General Checkup" : reason,
                        reason,
                        startTime,
                        endTime
                );

                apiService.createAppointment(request).enqueue(new Callback<ApiResponse<Appointment>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Appointment>> call, Response<ApiResponse<Appointment>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(AddAppointmentActivity.this, "Appointment created successfully", Toast.LENGTH_SHORT).show();
                            resetForm();
                            finish();
                        } else {
                            Toast.makeText(AddAppointmentActivity.this, "Failed to create appointment", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Appointment>> call, Throwable t) {
                        Toast.makeText(AddAppointmentActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
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
            // Fallback to current time if parse fails
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            return format.format(new Date());
        }
    }

    private void resetForm() {
        if (etPatientName != null) etPatientName.setText("");
        if (etDoctorName != null) etDoctorName.setText("");
        if (etReason != null) etReason.setText("");
        btnPickDate.setText("Pick Date");
        selectedDate = "";
    }
}