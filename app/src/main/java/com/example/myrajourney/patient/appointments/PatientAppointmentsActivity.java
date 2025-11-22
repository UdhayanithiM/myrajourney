package com.example.myrajourney.patient.appointments;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Appointment;
// ---------------------

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientAppointmentsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_appointments);

        recyclerView = findViewById(R.id.recycler_appointments);

        // Load appointments from API
        loadAppointmentsFromAPI();
    }

    private void loadAppointmentsFromAPI() {
        ApiService apiService = ApiClient.getApiService(this);
        Call<ApiResponse<List<Appointment>>> call = apiService.getAppointments();

        call.enqueue(new Callback<ApiResponse<List<Appointment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Appointment>>> call, Response<ApiResponse<List<Appointment>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Appointment> apiAppointments = response.body().getData();

                    if (apiAppointments != null && !apiAppointments.isEmpty()) {
                        // Convert API appointments to local Appointment model
                        List<Appointment> localAppointments = convertToLocalAppointments(apiAppointments);

                        // Find next appointment
                        Appointment nextAppointment = getNextAppointment(localAppointments);

                        // Set up adapter
                        adapter = new AppointmentAdapter(PatientAppointmentsActivity.this, localAppointments, nextAppointment);
                        recyclerView.setLayoutManager(new LinearLayoutManager(PatientAppointmentsActivity.this));
                        recyclerView.setAdapter(adapter);
                    } else {
                        showEmptyList();
                    }
                } else {
                    showEmptyList();
                    Toast.makeText(PatientAppointmentsActivity.this, "No appointments found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Appointment>>> call, Throwable t) {
                showEmptyList();
                Toast.makeText(PatientAppointmentsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyList() {
        adapter = new AppointmentAdapter(PatientAppointmentsActivity.this, new ArrayList<>(), null);
        recyclerView.setLayoutManager(new LinearLayoutManager(PatientAppointmentsActivity.this));
        recyclerView.setAdapter(adapter);
    }

    private List<Appointment> convertToLocalAppointments(List<Appointment> apiAppointments) {
        List<Appointment> local = new ArrayList<>();

        for (Appointment apiAppt : apiAppointments) {
            // Logic to format time slot (e.g., convert "2025-11-15 10:00:00" to "10:00 AM")
            String startTimeRaw = apiAppt.getStartTime();
            String endTimeRaw = apiAppt.getEndTime();
            String date = "N/A";
            String timeSlot = "TBD";

            try {
                // Attempt to parse date and time from start_time string
                if (startTimeRaw != null && startTimeRaw.contains(" ")) {
                    String[] parts = startTimeRaw.split(" ");
                    date = parts[0]; // "2025-11-15"
                    String timePart = parts[1]; // "10:00:00"

                    // Format Time (HH:mm:ss -> h:mm a)
                    SimpleDateFormat inputTime = new SimpleDateFormat("HH:mm:ss", Locale.US);
                    SimpleDateFormat outputTime = new SimpleDateFormat("h:mm a", Locale.US);
                    String formattedStart = outputTime.format(inputTime.parse(timePart));

                    timeSlot = formattedStart;

                    // If End Time exists, append it
                    if (endTimeRaw != null && endTimeRaw.contains(" ")) {
                        String endTimePart = endTimeRaw.split(" ")[1];
                        String formattedEnd = outputTime.format(inputTime.parse(endTimePart));
                        timeSlot += " - " + formattedEnd;
                    }
                }

                // Format Date (yyyy-MM-dd -> dd-MM-yyyy)
                SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat outputDate = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                date = outputDate.format(inputDate.parse(date));

            } catch (Exception e) {
                // Fallback values if parsing fails
                if (apiAppt.getDate() != null) date = apiAppt.getDate();
                if (apiAppt.getStartTime() != null) timeSlot = apiAppt.getStartTime();
            }

            String patientName = apiAppt.getPatientName() != null ? apiAppt.getPatientName() : "Me";
            String doctorName = apiAppt.getDoctorName() != null ? apiAppt.getDoctorName() : "Dr. Assigned";
            // Use Description if available, otherwise Title
            String reason = apiAppt.getDescription() != null ? apiAppt.getDescription() : apiAppt.getTitle();

            // Using the 5-arg constructor created in Appointment.java
            Appointment localAppt = new Appointment(patientName, doctorName, date, timeSlot, reason);
            local.add(localAppt);
        }
        return local;
    }

    private Appointment getNextAppointment(List<Appointment> appointments) {
        if (appointments == null || appointments.isEmpty()) return null;

        Calendar now = Calendar.getInstance();
        Appointment next = null;
        long minDiff = Long.MAX_VALUE;

        // Updated format to match the conversion logic above (dd-MM-yyyy h:mm a)
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy h:mm a", Locale.US);

        for (Appointment appt : appointments) {
            try {
                String dateStr = appt.getDate();
                // Extract just the start time part "10:00 AM" from "10:00 AM - 10:30 AM"
                String timeStr = appt.getTimeSlot().split(" - ")[0].trim();

                String dateTimeStr = dateStr + " " + timeStr;
                Calendar appointmentTime = Calendar.getInstance();
                appointmentTime.setTime(sdf.parse(dateTimeStr));

                long diff = appointmentTime.getTimeInMillis() - now.getTimeInMillis();

                // Only consider future appointments
                if (diff > 0 && diff < minDiff) {
                    minDiff = diff;
                    next = appt;
                }
            } catch (Exception e) {
                // Skip on parse error
            }
        }
        return next;
    }
}