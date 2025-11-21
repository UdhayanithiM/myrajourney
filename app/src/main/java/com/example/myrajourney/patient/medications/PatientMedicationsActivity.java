package com.example.myrajourney.patient.medications;

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

import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model
.ApiResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientMedicationsActivity extends AppCompatActivity {

    private LinearLayout medicationsContainer;
    private TextView noMedicationsText;
    private List<MedicationSchedule> todayMedications;
    private List<com.example.myrajourney.data.model
.Medication> allMedications;
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
        ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        Call<ApiResponse<List<com.example.myrajourney.data.model
.Medication>>> call = apiService.getPatientMedications();
        
        call.enqueue(new Callback<ApiResponse<List<com.example.myrajourney.data.model
.Medication>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<com.example.myrajourney.data.model
.Medication>>> call, 
                                 Response<ApiResponse<List<com.example.myrajourney.data.model
.Medication>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<com.example.myrajourney.data.model
.Medication> medications = response.body().getData();
                    allMedications = medications != null ? medications : new ArrayList<>();
                    if (medications != null && !medications.isEmpty()) {
                        // Convert API medications to local MedicationSchedule
                        for (com.example.myrajourney.data.model
.Medication med : medications) {
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
                    allMedications = new ArrayList<>();
                    displayMedications();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<com.example.myrajourney.data.model
.Medication>>> call, Throwable t) {
                // Network error - show empty state or fallback
                displayMedications();
            }
        });
    }
    
    private String parseTimeFromFrequency(String frequency) {
        // Simple parsing - in real app, you'd parse the frequency field properly
        if (frequency == null || frequency.isEmpty()) {
            return "8:00 AM"; // Default
        }
        
        String lowerFreq = frequency.toLowerCase();
        
        if (lowerFreq.contains("morning") || lowerFreq.contains("8:00") || lowerFreq.contains("08:00")) {
            return "8:00 AM";
        } else if (lowerFreq.contains("afternoon") || lowerFreq.contains("2:00") || lowerFreq.contains("14:00")) {
            return "2:00 PM";
        } else if (lowerFreq.contains("evening") || lowerFreq.contains("20:00") || lowerFreq.contains("8:00 pm")) {
            return "8:00 PM";
        } else if (lowerFreq.contains("night") || lowerFreq.contains("22:00")) {
            return "10:00 PM";
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
            statusButton.setText("âœ… Completed");
            statusButton.setBackgroundColor(getColor(android.R.color.holo_green_dark));
            setReminderBtn.setVisibility(View.GONE);
        } else {
            statusButton.setText("â³ Pending");
            statusButton.setBackgroundColor(getColor(android.R.color.holo_orange_light));
        }

        statusButton.setOnClickListener(v -> {
            if (!medication.isCompleted()) {
                medication.setCompleted(true);
                statusButton.setText("âœ… Completed");
                statusButton.setBackgroundColor(getColor(android.R.color.holo_green_dark));
                setReminderBtn.setVisibility(View.GONE);

                // Save completion status locally
                saveMedicationStatus(medication.getName(), true);
                
                // Log medication intake to backend
                logMedicationIntake(medication.getName(), medication.getDosage());

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
    
    private void logMedicationIntake(String medicationName, String dosage) {
        // Find medication ID from the list
        Integer medicationId = null;
        for (com.example.myrajourney.data.model
.Medication med : getAllMedications()) {
            if (med.getMedicationName() != null && med.getMedicationName().equals(medicationName)) {
                try {
                    // Convert String ID to Integer
                    medicationId = Integer.parseInt(med.getId());
                    break;
                } catch (NumberFormatException e) {
                    // If ID cannot be parsed, continue searching
                    continue;
                }
            }
        }
        
        if (medicationId == null) {
            // If we can't find the ID, skip logging
            return;
        }
        
        com.example.myrajourney.core.network.ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        com.example.myrajourney.data.model
.MedicationLogRequest request = new com.example.myrajourney.data.model
.MedicationLogRequest();
        request.setPatientMedicationId(medicationId);
        request.setTakenAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        request.setDosage(dosage);
        request.setStatus("TAKEN");
        
        retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.MedicationLog>> call = apiService.logMedicationIntake(request);
        
        call.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.MedicationLog>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.MedicationLog>> call, 
                                 retrofit2.Response<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.MedicationLog>> response) {
                // Medication logged successfully - doctor will be notified
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.MedicationLog>> call, Throwable t) {
                // Logging failed - but local status is saved
            }
        });
    }
    
    private List<com.example.myrajourney.data.model
.Medication> getAllMedications() {
        return allMedications != null ? allMedications : new ArrayList<>();
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






