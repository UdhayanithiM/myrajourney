package com.example.myrajourney.patient.medications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.auth.LoginActivity;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.data.model.MedicationLog;
import com.example.myrajourney.data.model.MedicationLogRequest;
// ---------------------

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
    private List<Medication> allMedications;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_medications);

        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isSessionValid()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        loadTodayMedications();
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
        ApiService apiService = ApiClient.getApiService(this);
        Call<ApiResponse<List<Medication>>> call = apiService.getPatientMedications();

        call.enqueue(new Callback<ApiResponse<List<Medication>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Medication>>> call,
                                   Response<ApiResponse<List<Medication>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Medication> medications = response.body().getData();
                    allMedications = medications != null ? medications : new ArrayList<>();

                    if (medications != null && !medications.isEmpty()) {
                        // Convert API medications to local MedicationSchedule
                        for (Medication med : medications) {
                            // Check status instead of isActive() (based on your Medication model)
                            if ("Ongoing".equalsIgnoreCase(med.getStatus()) || "Active".equalsIgnoreCase(med.getStatus())) {
                                String time = parseTimeFromFrequency(med.getFrequency());

                                // Check if completed locally today
                                boolean isCompleted = checkLocalCompletionStatus(med.getName());

                                todayMedications.add(new MedicationSchedule(
                                        med.getName(), // Fixed: getName() instead of getMedicationName()
                                        med.getDosage(),
                                        time,
                                        isCompleted
                                ));
                            }
                        }
                        displayMedications();
                    } else {
                        displayMedications(); // Show empty state
                    }
                } else {
                    allMedications = new ArrayList<>();
                    displayMedications();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Medication>>> call, Throwable t) {
                Toast.makeText(PatientMedicationsActivity.this, "Failed to load medications", Toast.LENGTH_SHORT).show();
                displayMedications();
            }
        });
    }

    private String parseTimeFromFrequency(String frequency) {
        if (frequency == null || frequency.isEmpty()) {
            return "8:00 AM"; // Default
        }
        String lowerFreq = frequency.toLowerCase();
        if (lowerFreq.contains("morning") || lowerFreq.contains("once")) return "8:00 AM";
        else if (lowerFreq.contains("afternoon")) return "2:00 PM";
        else if (lowerFreq.contains("evening") || lowerFreq.contains("night")) return "8:00 PM";

        return "8:00 AM";
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
            statusButton.setEnabled(false); // Prevent multiple clicks
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
                statusButton.setEnabled(false);

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
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date time = sdf.parse(medication.getTime());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);

            Calendar today = Calendar.getInstance();
            calendar.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

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

    private boolean checkLocalCompletionStatus(String medicationName) {
        SharedPreferences prefs = getSharedPreferences("medication_status", Context.MODE_PRIVATE);
        return prefs.getBoolean(medicationName + "_" + getCurrentDate(), false);
    }

    private void saveMedicationStatus(String medicationName, boolean completed) {
        SharedPreferences prefs = getSharedPreferences("medication_status", Context.MODE_PRIVATE);
        prefs.edit().putBoolean(medicationName + "_" + getCurrentDate(), completed).apply();
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private void logMedicationIntake(String medicationName, String dosage) {
        Integer medicationId = null;
        // Match name to find ID from the loaded list
        for (Medication med : getAllMedications()) {
            if (med.getName() != null && med.getName().equals(medicationName)) {
                try {
                    if (med.getId() != null) {
                        medicationId = Integer.parseInt(med.getId());
                        break;
                    }
                } catch (NumberFormatException e) {
                    Log.e("MedicationLog", "Invalid ID format: " + med.getId());
                }
            }
        }

        if (medicationId == null) {
            Log.e("MedicationLog", "Medication ID not found for: " + medicationName);
            return;
        }

        ApiService apiService = ApiClient.getApiService(this);
        MedicationLogRequest request = new MedicationLogRequest();
        request.setPatientMedicationId(medicationId);
        request.setTakenAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        request.setDosage(dosage);
        request.setStatus("TAKEN");

        Call<ApiResponse<MedicationLog>> call = apiService.logMedicationIntake(request);

        call.enqueue(new Callback<ApiResponse<MedicationLog>>() {
            @Override
            public void onResponse(Call<ApiResponse<MedicationLog>> call, Response<ApiResponse<MedicationLog>> response) {
                // Success logging
            }

            @Override
            public void onFailure(Call<ApiResponse<MedicationLog>> call, Throwable t) {
                // Log failure logic
            }
        });
    }

    private List<Medication> getAllMedications() {
        return allMedications != null ? allMedications : new ArrayList<>();
    }

    // Inner class for simple UI model
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
        // Added hashCode for PendingIntent uniqueness
        @Override
        public int hashCode() {
            return (name + time).hashCode();
        }
    }
}