package com.example.myrajourney.doctor.appointments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Appointment;
// Using the common AddAppointmentActivity
import com.example.myrajourney.common.appointments.AddAppointmentActivity;
// Using the adapter from patient package (assuming shared)
import com.example.myrajourney.patient.appointments.AppointmentsAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
// ---------------------

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DoctorScheduleActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText searchBar;
    ImageView backButton;
    List<Appointment> appointments, filteredList;
    AppointmentsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply Theme
        ThemeManager.applyTheme(this);

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

        // Setup Adapter
        adapter = new AppointmentsAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load appointments from backend API
        loadAppointmentsFromBackend();

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
        ApiService apiService = ApiClient.getApiService(this);
        Call<ApiResponse<List<Appointment>>> call = apiService.getAppointments();

        call.enqueue(new Callback<ApiResponse<List<Appointment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Appointment>>> call, Response<ApiResponse<List<Appointment>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Appointment> apiAppointments = response.body().getData();

                    appointments.clear();

                    if (apiAppointments != null) {
                        // Ensure the Appointment model has a constructor:
                        // new Appointment(patientName, time, title, date)
                        // OR use setters if that constructor doesn't exist.
                        for (Appointment apiAppt : apiAppointments) {
                            String patientName = apiAppt.getPatientName() != null ? apiAppt.getPatientName() : "Patient";
                            String title = apiAppt.getTitle() != null ? apiAppt.getTitle() : "Appointment";
                            String date = formatDate(apiAppt.getStartTime());
                            String time = formatTime(apiAppt.getStartTime());

                            // Creating a new local appointment object for UI display
                            Appointment displayAppt = new Appointment();
                            displayAppt.setPatientName(patientName);
                            displayAppt.setTitle(title);
                            displayAppt.setDate(date);
                            // Assuming you have a field for time or use startTime
                            displayAppt.setStartTime(apiAppt.getStartTime());

                            appointments.add(displayAppt);
                        }

                        filteredList.clear();
                        filteredList.addAll(appointments);
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
            public void onFailure(Call<ApiResponse<List<Appointment>>> call, Throwable t) {
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
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateTime);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return "Today";
        }
    }

    private String formatTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) return "10:00 AM";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateTime);
            SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return "10:00 AM";
        }
    }

    private void filter(String query) {
        filteredList.clear();
        for (Appointment a : appointments) {
            // Safe check for nulls before filtering
            String pName = a.getPatientName() != null ? a.getPatientName() : "";
            String aType = a.getTitle() != null ? a.getTitle() : ""; // Using Title as Type based on your logic
            String aDate = a.getDate() != null ? a.getDate() : "";

            if (pName.toLowerCase().contains(query.toLowerCase()) ||
                    aType.toLowerCase().contains(query.toLowerCase()) ||
                    aDate.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(a);
            }
        }
        adapter.notifyDataSetChanged();
    }
}