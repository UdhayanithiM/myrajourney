package com.example.myrajouney;

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
        // Add sample appointments (showing recent 10 entries)
        appointments.add(new Appointment("John Doe", "10:00 AM", "Rheumatoid Arthritis Checkup", "Today"));
        appointments.add(new Appointment("Sarah Wilson", "11:30 AM", "Follow-up Consultation", "Today"));
        appointments.add(new Appointment("Michael Brown", "02:30 PM", "New Patient Consultation", "Today"));
        appointments.add(new Appointment("Emily Davis", "04:00 PM", "Treatment Review", "Today"));
        appointments.add(new Appointment("Robert Johnson", "09:00 AM", "Lab Results Discussion", "Tomorrow"));
        appointments.add(new Appointment("Lisa Anderson", "10:30 AM", "Physical Therapy Review", "Tomorrow"));
        appointments.add(new Appointment("David Martinez", "01:00 PM", "Medication Adjustment", "Tomorrow"));
        appointments.add(new Appointment("Jennifer Taylor", "03:30 PM", "Routine Checkup", "Tomorrow"));
        appointments.add(new Appointment("William Garcia", "09:30 AM", "Pain Management", "Dec 8"));
        appointments.add(new Appointment("Mary Rodriguez", "11:00 AM", "Joint Examination", "Dec 8"));

        filteredList = new ArrayList<>(appointments);

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
