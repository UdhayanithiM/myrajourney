package com.example.myrajouney;

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

            // Check for duplicate
            for (Appointment a : allAppointments) {
                if (a.getDoctorName().equalsIgnoreCase(doctorName)
                        && a.getDate().equals(selectedDate)
                        && a.getTimeSlot().equals(timeSlot)) {
                    Toast.makeText(this, "This slot is already booked", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Add appointment
            Appointment appointment = new Appointment(patientName, doctorName, selectedDate, timeSlot, reason);
            allAppointments.add(appointment);
            Toast.makeText(this, "Appointment added successfully", Toast.LENGTH_SHORT).show();

            // Reset fields
            etPatientName.setText("");
            etDoctorName.setText("");
            etReason.setText("");
            btnPickDate.setText("Pick Date");

            // Optionally open PatientAppointmentsActivity
            startActivity(new Intent(this, PatientAppointmentsActivity.class));
        });
    }
}
