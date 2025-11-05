package com.example.myrajouney;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientMedicationsActivity extends AppCompatActivity {

    private LinearLayout medicationsContainer;
    private TextView noMedicationsText;
    private List<MedicationSchedule> todayMedications;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_medications);

        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn() || !sessionManager.isSessionValid()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        loadTodayMedications();
        displayMedications();
    }

    private void initializeViews() {
        medicationsContainer = findViewById(R.id.medicationsContainer);
        noMedicationsText = findViewById(R.id.noMedicationsText);

        // Back button
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void loadTodayMedications() {
        todayMedications = new ArrayList<>();
        
        // Load medications from API
        loadMedicationsFromAPI();
    }
    
    private void loadMedicationsFromAPI() {
        ApiService apiService = com.example.myrajouney.api.ApiClient.getApiService(this);
        Call<ApiResponse<List<com.example.myrajouney.api.models.Medication>>> call = apiService.getPatientMedications();
        
        call.enqueue(new Callback<ApiResponse<List<com.example.myrajouney.api.models.Medication>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<com.example.myrajouney.api.models.Medication>>> call, 
                                 Response<ApiResponse<List<com.example.myrajouney.api.models.Medication>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<com.example.myrajouney.api.models.Medication> medications = response.body().getData();
                    if (medications != null && !medications.isEmpty()) {
                        // Convert API medications to local MedicationSchedule
                        for (com.example.myrajouney.api.models.Medication med : medications) {
                            if (med.isActive()) {
                                // Parse frequency to get time (simplified - you may need more complex parsing)
                                String time = parseTimeFromFrequency(med.getFrequency());
                                todayMedications.add(new MedicationSchedule(
                                    med.getMedicationName(),
                                    med.getDosage(),
                                    time,
                                    false // Check from logs
                                ));
                            }
                        }
                        displayMedications();
                    } else {
                        // No medications - show empty state
                        displayMedications();
                    }
                } else {
                    // Error loading - show empty state
                    displayMedications();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<com.example.myrajouney.api.models.Medication>>> call, Throwable t) {
                // Network error - show empty state or fallback
                displayMedications();
            }
        });
    }
    
    private String parseTimeFromFrequency(String frequency) {
        // Simple parsing - in real app, you'd parse the frequency field properly
        if (frequency != null && frequency.contains("8:00") || frequency.contains("morning")) {
            return "8:00 AM";
        } else if (frequency != null && frequency.contains("2:00") || frequency.contains("afternoon")) {
            return "2:00 PM";
        } else if (frequency != null && frequency.contains("8:00") || frequency.contains("evening")) {
            return "8:00 PM";
        }
        return "8:00 AM"; // Default
    }

    private void displayMedications() {
        medicationsContainer.removeAllViews();

        if (todayMedications.isEmpty()) {
            noMedicationsText.setVisibility(View.VISIBLE);
            return;
        }

        noMedicationsText.setVisibility(View.GONE);

        for (MedicationSchedule med : todayMedications) {
            addMedicationCard(med);
        }
    }

    private void addMedicationCard(MedicationSchedule medication) {
        View cardView = getLayoutInflater().inflate(R.layout.item_patient_medication, medicationsContainer, false);

        TextView medName = cardView.findViewById(R.id.medicineName);
        TextView medDosage = cardView.findViewById(R.id.medicineDosage);
        TextView medTime = cardView.findViewById(R.id.medicineTime);
        Button statusButton = cardView.findViewById(R.id.statusButton);
        Button setReminderBtn = cardView.findViewById(R.id.setReminderBtn);

        medName.setText(medication.getName());
        medDosage.setText(medication.getDosage());
        medTime.setText("Due: " + medication.getTime());

        if (medication.isCompleted()) {
            statusButton.setText("✅ Completed");
            statusButton.setBackgroundColor(getColor(android.R.color.holo_green_dark));
            setReminderBtn.setVisibility(View.GONE);
        } else {
            statusButton.setText("⏳ Pending");
            statusButton.setBackgroundColor(getColor(android.R.color.holo_orange_light));
        }

        statusButton.setOnClickListener(v -> {
            if (!medication.isCompleted()) {
                medication.setCompleted(true);
                statusButton.setText("✅ Completed");
                statusButton.setBackgroundColor(getColor(android.R.color.holo_green_dark));
                setReminderBtn.setVisibility(View.GONE);

                // Save completion status
                saveMedicationStatus(medication.getName(), true);

                Toast.makeText(this, "Medication marked as completed!", Toast.LENGTH_SHORT).show();
            }
        });

        setReminderBtn.setOnClickListener(v -> {
            setMedicationReminder(medication);
        });

        medicationsContainer.addView(cardView);
    }

    private void setMedicationReminder(MedicationSchedule medication) {
        // Parse time and set alarm
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date time = sdf.parse(medication.getTime());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);

            // Set for today
            Calendar today = Calendar.getInstance();
            calendar.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

            // If time has passed today, set for tomorrow
            if (calendar.before(today)) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, MedicationReminderReceiver.class);
            intent.putExtra("medication_name", medication.getName());
            intent.putExtra("medication_dosage", medication.getDosage());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, medication.hashCode(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }

            Toast.makeText(this, "Reminder set for " + medication.getName(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error setting reminder", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveMedicationStatus(String medicationName, boolean completed) {
        SharedPreferences prefs = getSharedPreferences("medication_status", Context.MODE_PRIVATE);
        prefs.edit().putBoolean(medicationName + "_" + getCurrentDate(), completed).apply();
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // Inner class for medication schedule
    private static class MedicationSchedule {
        private String name;
        private String dosage;
        private String time;
        private boolean completed;

        public MedicationSchedule(String name, String dosage, String time, boolean completed) {
            this.name = name;
            this.dosage = dosage;
            this.time = time;
            this.completed = completed;
        }

        public String getName() { return name; }
        public String getDosage() { return dosage; }
        public String getTime() { return time; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}
