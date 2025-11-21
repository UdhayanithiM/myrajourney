package com.example.myrajourney.patient.appointments;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model
.ApiResponse;

import java.text.SimpleDateFormat;
import java.util.*;

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
        ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        Call<ApiResponse<List<com.example.myrajourney.data.model
.Appointment>>> call = apiService.getAppointments();
        
        call.enqueue(new Callback<ApiResponse<List<com.example.myrajourney.data.model
.Appointment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<com.example.myrajourney.data.model
.Appointment>>> call,
                                 Response<ApiResponse<List<com.example.myrajourney.data.model
.Appointment>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<com.example.myrajourney.data.model
.Appointment> apiAppointments = response.body().getData();
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
                        // No appointments
                        Toast.makeText(PatientAppointmentsActivity.this, "No appointments found", Toast.LENGTH_SHORT).show();
                        adapter = new AppointmentAdapter(PatientAppointmentsActivity.this, new ArrayList<>(), null);
                        recyclerView.setLayoutManager(new LinearLayoutManager(PatientAppointmentsActivity.this));
                        recyclerView.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<com.example.myrajourney.data.model
.Appointment>>> call, Throwable t) {
                // Fallback to local data or show error
                List<Appointment> allAppointments = AddAppointmentActivity.allAppointments != null ? 
                    AddAppointmentActivity.allAppointments : new ArrayList<>();
                adapter = new AppointmentAdapter(PatientAppointmentsActivity.this, allAppointments, null);
                recyclerView.setLayoutManager(new LinearLayoutManager(PatientAppointmentsActivity.this));
                recyclerView.setAdapter(adapter);
            }
        });
    }
    
    private List<Appointment> convertToLocalAppointments(List<com.example.myrajourney.data.model
.Appointment> apiAppointments) {
        List<Appointment> local = new ArrayList<>();
        for (com.example.myrajourney.data.model
.Appointment apiAppt : apiAppointments) {
            // Parse date and time from start_time
            String startTime = apiAppt.getStartTime();
            String date = "";
            String timeSlot = "";
            
            try {
                // Parse ISO format: "2025-11-15 10:00:00"
                if (startTime != null && !startTime.isEmpty()) {
                    String[] parts = startTime.split(" ");
                    if (parts.length >= 2) {
                        date = parts[0]; // "2025-11-15"
                        String timePart = parts[1]; // "10:00:00"
                        String[] timeParts = timePart.split(":");
                        if (timeParts.length >= 2) {
                            int hour = Integer.parseInt(timeParts[0]);
                            int minute = Integer.parseInt(timeParts[1]);
                            String amPm = hour >= 12 ? "PM" : "AM";
                            int displayHour = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
                            timeSlot = String.format("%d:%02d %s", displayHour, minute, amPm);
                        }
                    }
                }
            } catch (Exception e) {
                // Fallback to original format
                timeSlot = startTime != null ? startTime : "10:00 AM";
                date = "2025-11-15";
            }
            
            // Format date to DD-MM-YYYY
            String formattedDate = date;
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                if (date != null && !date.isEmpty()) {
                    formattedDate = outputFormat.format(inputFormat.parse(date));
                }
            } catch (Exception e) {
                // Keep original format if parsing fails
            }
            
            // Get end time for time slot
            String endTime = apiAppt.getEndTime();
            if (endTime != null && !endTime.isEmpty()) {
                try {
                    String[] endParts = endTime.split(" ");
                    if (endParts.length >= 2) {
                        String[] timeParts = endParts[1].split(":");
                        if (timeParts.length >= 2) {
                            int hour = Integer.parseInt(timeParts[0]);
                            int minute = Integer.parseInt(timeParts[1]);
                            String amPm = hour >= 12 ? "PM" : "AM";
                            int displayHour = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
                            String endTimeSlot = String.format("%d:%02d %s", displayHour, minute, amPm);
                            timeSlot = timeSlot + " - " + endTimeSlot;
                        }
                    }
                } catch (Exception e) {
                    // Use single time if end time parsing fails
                }
            }
            
            // Use the constructor that matches: (patientName, doctorName, date, timeSlot, reason)
            // For now, we'll use "Patient" as placeholder for patientName and doctorName
            // You may need to fetch actual names from user data
            String patientName = "Patient"; // TODO: Get actual patient name
            String doctorName = "Doctor"; // TODO: Get actual doctor name
            String reason = apiAppt.getDescription() != null ? apiAppt.getDescription() : apiAppt.getTitle();
            
            Appointment localAppt = new Appointment(patientName, doctorName, formattedDate, timeSlot, reason);
            local.add(localAppt);
        }
        return local;
    }
    
    private Appointment getNextAppointment(List<Appointment> appointments) {
        if (appointments == null || appointments.isEmpty()) return null;
        
        // Find next upcoming appointment
        Calendar now = Calendar.getInstance();
        Appointment next = null;
        long minDiff = Long.MAX_VALUE;
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm a", Locale.US);
        
        for (Appointment appt : appointments) {
            try {
                // Parse appointment date and time
                String dateStr = appt.getDate(); // "dd-MM-yyyy"
                String timeStr = appt.getTimeSlot().split(" - ")[0].trim(); // "10:00 AM"
                
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
                // If parsing fails, skip this appointment
                e.printStackTrace();
            }
        }
        return next;
    }

    private Appointment getNextAppointment(String patientName, List<Appointment> allAppointments) {
        Appointment next = null;
        long minTimeDiff = Long.MAX_VALUE;

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US);
        Calendar now = Calendar.getInstance();

        for (Appointment a : allAppointments) {
            if (a.getPatientName().equalsIgnoreCase(patientName)) {
                try {
                    String startTime = a.getTimeSlot().split(" - ")[0];
                    String dateTime = a.getDate() + " " + startTime;

                    Calendar appointmentTime = Calendar.getInstance();
                    appointmentTime.setTime(sdf.parse(dateTime));

                    long diff = appointmentTime.getTimeInMillis() - now.getTimeInMillis();
                    if (diff > 0 && diff < minTimeDiff) {
                        minTimeDiff = diff;
                        next = a;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return next;
    }
}






