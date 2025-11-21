package com.example.myrajourney.doctor.appointments;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DoctorScheduleActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText searchBar;
    ImageView backButton;
    List<Appointment> appointments, filteredList;
    AppointmentsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_schedule);

        recyclerView = findViewById(R.id.schedule_recycler);
        searchBar = findViewById(R.id.search_bar);
        backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> finish());
        ImageView addButton = findViewById(R.id.add_button);
        if (addButton != null) {
            addButton.setOnClickListener(v -> startActivity(new Intent(this, AddAppointmentActivity.class)));
        }

        appointments = new ArrayList<>();
        filteredList = new ArrayList<>();
        
        // Load appointments from backend API
        loadAppointmentsFromBackend();

        adapter = new AppointmentsAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Quick access to add appointment: long-press back icon (secondary)
        backButton.setOnLongClickListener(v -> {
            startActivity(new Intent(this, AddAppointmentActivity.class));
            Toast.makeText(this, "Open Add Appointment", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void loadAppointmentsFromBackend() {
        com.example.myrajourney.core.network.ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.Appointment>>> call = apiService.getAppointments();
        
        call.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.Appointment>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.Appointment>>> call, 
                                 retrofit2.Response<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.Appointment>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<com.example.myrajourney.data.model
.Appointment> apiAppointments = response.body().getData();
                    if (apiAppointments != null) {
                        appointments.clear();
                        // Convert API appointments to local Appointment format
                        for (com.example.myrajourney.data.model
.Appointment apiAppt : apiAppointments) {
                            String patientName = apiAppt.getPatientName() != null ? apiAppt.getPatientName() : "Patient";
                            String title = apiAppt.getTitle() != null ? apiAppt.getTitle() : "Appointment";
                            String date = formatDate(apiAppt.getStartTime());
                            String time = formatTime(apiAppt.getStartTime());
                            appointments.add(new Appointment(patientName, time, title, date));
                        }
                        filteredList = new ArrayList<>(appointments);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    // No appointments or error
                    appointments.clear();
                    filteredList.clear();
                    adapter.notifyDataSetChanged();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.Appointment>>> call, Throwable t) {
                // On failure, show empty list
                appointments.clear();
                filteredList.clear();
                adapter.notifyDataSetChanged();
                Toast.makeText(DoctorScheduleActivity.this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private String formatDate(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) return "Today";
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(dateTime);
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return "Today";
        }
    }
    
    private String formatTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) return "10:00 AM";
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(dateTime);
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return "10:00 AM";
        }
    }

    private void filter(String query) {
        filteredList.clear();
        for (Appointment a : appointments) {
            if (a.getPatientName().toLowerCase().contains(query.toLowerCase()) ||
                    a.getAppointmentType().toLowerCase().contains(query.toLowerCase()) ||
                    a.getDate().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(a);
            }
        }
        adapter.notifyDataSetChanged();
    }
}






