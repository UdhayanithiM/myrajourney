package com.example.myrajouney;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.*;

public class PatientAppointmentsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_appointments);

        recyclerView = findViewById(R.id.recycler_appointments);

        // Use shared appointments list from AddAppointmentActivity
        List<Appointment> allAppointments = AddAppointmentActivity.allAppointments;

        // Find next appointment for "Divya" (example)
        Appointment nextAppointment = getNextAppointment("Divya", allAppointments);
        if (nextAppointment != null) {
            Toast.makeText(this,
                    "Next appointment: " + nextAppointment.getDate() + " " + nextAppointment.getTimeSlot(),
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No upcoming appointments", Toast.LENGTH_SHORT).show();
        }

        // Set up adapter
        adapter = new AppointmentAdapter(this, allAppointments, nextAppointment);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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
