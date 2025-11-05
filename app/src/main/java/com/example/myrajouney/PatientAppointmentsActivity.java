package com.example.myrajouney;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajouney.api.ApiService;
import com.example.myrajouney.api.models.ApiResponse;

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
        ApiService apiService = com.example.myrajouney.api.ApiClient.getApiService(this);
        Call<ApiResponse<List<com.example.myrajouney.api.models.Appointment>>> call = apiService.getAppointments();
        
        call.enqueue(new Callback<ApiResponse<List<com.example.myrajouney.api.models.Appointment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<com.example.myrajouney.api.models.Appointment>>> call,
                                 Response<ApiResponse<List<com.example.myrajouney.api.models.Appointment>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<com.example.myrajouney.api.models.Appointment> apiAppointments = response.body().getData();
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
            public void onFailure(Call<ApiResponse<List<com.example.myrajouney.api.models.Appointment>>> call, Throwable t) {
                // Fallback to local data or show error
                List<Appointment> allAppointments = AddAppointmentActivity.allAppointments != null ? 
                    AddAppointmentActivity.allAppointments : new ArrayList<>();
                adapter = new AppointmentAdapter(PatientAppointmentsActivity.this, allAppointments, null);
                recyclerView.setLayoutManager(new LinearLayoutManager(PatientAppointmentsActivity.this));
                recyclerView.setAdapter(adapter);
            }
        });
    }
    
    private List<Appointment> convertToLocalAppointments(List<com.example.myrajouney.api.models.Appointment> apiAppointments) {
        List<Appointment> local = new ArrayList<>();
        for (com.example.myrajouney.api.models.Appointment apiAppt : apiAppointments) {
            // Convert API appointment to local model
            Appointment localAppt = new Appointment();
            // Set properties based on your local Appointment model structure
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
        
        for (Appointment appt : appointments) {
            // Parse appointment date/time and compare
            // This is simplified - adjust based on your Appointment model
            try {
                // Add date/time parsing logic here
                if (next == null) next = appt;
            } catch (Exception e) {
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
