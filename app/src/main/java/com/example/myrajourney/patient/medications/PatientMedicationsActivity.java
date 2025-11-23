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

import com.example.myrajourney.R;
import com.example.myrajourney.auth.LoginActivity;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.data.model.MedicationLog;
import com.example.myrajourney.data.model.MedicationLogRequest;

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
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_medications);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isSessionValid()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        loadMedicationsFromAPI();
    }

    private void initializeViews() {
        medicationsContainer = findViewById(R.id.medicationsContainer);
        noMedicationsText = findViewById(R.id.noMedicationsText);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void loadMedicationsFromAPI() {
        todayMedications = new ArrayList<>();
        ApiService apiService = ApiClient.getApiService(this);

        // GET /api/v1/patient-medications
        Call<ApiResponse<List<Medication>>> call = apiService.getPatientMedications();

        call.enqueue(new Callback<ApiResponse<List<Medication>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Medication>>> call, Response<ApiResponse<List<Medication>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Medication> medications = response.body().getData();

                    if (medications != null && !medications.isEmpty()) {
                        for (Medication med : medications) {
                            if (med.isActive()) {
                                // Check local SharedPreferences for today's status
                                boolean isCompleted = checkLocalCompletionStatus(med.getId(), med.getName());

                                // ✅ CRITICAL: Pass the API ID (med.getId()) to the schedule object
                                todayMedications.add(new MedicationSchedule(
                                        med.getId(),
                                        med.getName(),
                                        med.getDosage(),
                                        med.getFormattedTime(),
                                        isCompleted
                                ));
                            }
                        }
                        displayMedications();
                    } else {
                        showEmptyState();
                    }
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Medication>>> call, Throwable t) {
                Toast.makeText(PatientMedicationsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        medicationsContainer.removeAllViews();
        noMedicationsText.setVisibility(View.VISIBLE);
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

    private void addMedicationCard(MedicationSchedule med) {
        View cardView = getLayoutInflater().inflate(R.layout.item_patient_medication, medicationsContainer, false);

        TextView medName = cardView.findViewById(R.id.medicineName);
        TextView medDosage = cardView.findViewById(R.id.medicineDosage);
        TextView medTime = cardView.findViewById(R.id.medicineTime);
        Button statusButton = cardView.findViewById(R.id.statusButton);
        Button setReminderBtn = cardView.findViewById(R.id.setReminderBtn);

        medName.setText(med.getName());
        medDosage.setText(med.getDosage());
        medTime.setText("Due: " + med.getTime());

        if (med.isCompleted()) {
            statusButton.setText("✅ Completed");
            statusButton.setBackgroundColor(getColor(android.R.color.holo_green_dark));
            setReminderBtn.setVisibility(View.GONE);
            statusButton.setEnabled(false);
        } else {
            statusButton.setText("⏳ Mark Taken");
            statusButton.setBackgroundColor(getColor(android.R.color.holo_orange_light));
            setReminderBtn.setVisibility(View.VISIBLE);
            statusButton.setEnabled(true);
        }

        statusButton.setOnClickListener(v -> {
            if (!med.isCompleted()) {
                med.setCompleted(true);

                // Update UI immediately
                statusButton.setText("✅ Taken");
                statusButton.setBackgroundColor(getColor(android.R.color.holo_green_dark));
                setReminderBtn.setVisibility(View.GONE);
                statusButton.setEnabled(false);

                // 1. Save locally
                saveMedicationStatus(med.getApiId(), med.getName(), true);

                // 2. Send to Backend
                logMedicationIntake(med.getApiId(), med.getDosage());

                Toast.makeText(this, "Marked as taken!", Toast.LENGTH_SHORT).show();
            }
        });

        setReminderBtn.setOnClickListener(v -> setMedicationReminder(med));

        medicationsContainer.addView(cardView);
    }

    private void logMedicationIntake(String patientMedicationId, String dosage) {
        if (patientMedicationId == null) return;

        ApiService apiService = ApiClient.getApiService(this);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // ✅ Uses the ID directly from the object, no name-lookup required
        MedicationLogRequest request = new MedicationLogRequest(patientMedicationId, timestamp);
        request.setDosage(dosage);
        request.setStatus("TAKEN");

        apiService.logMedicationIntake(request).enqueue(new Callback<ApiResponse<MedicationLog>>() {
            @Override
            public void onResponse(Call<ApiResponse<MedicationLog>> call, Response<ApiResponse<MedicationLog>> response) {
                if (!response.isSuccessful()) {
                    Log.e("MedLog", "Failed to sync: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<MedicationLog>> call, Throwable t) {
                Log.e("MedLog", "Sync error", t);
            }
        });
    }

    private void setMedicationReminder(MedicationSchedule med) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date time = sdf.parse(med.getTime());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);

            // Set for today
            Calendar reminderTime = Calendar.getInstance();
            reminderTime.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
            reminderTime.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
            reminderTime.set(Calendar.SECOND, 0);

            if (reminderTime.before(Calendar.getInstance())) {
                reminderTime.add(Calendar.DAY_OF_MONTH, 1); // Set for tomorrow if time passed
            }

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, MedicationReminderReceiver.class);
            intent.putExtra("medication_name", med.getName());
            intent.putExtra("medication_dosage", med.getDosage());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, med.hashCode(),
                    intent, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "Reminder set for " + med.getTime(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Could not set reminder", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Local Storage Helpers ---
    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private boolean checkLocalCompletionStatus(String id, String name) {
        SharedPreferences prefs = getSharedPreferences("med_status", Context.MODE_PRIVATE);
        // Fallback to name if ID is null (for legacy/dummy data compatibility)
        String key = (id != null ? id : name) + "_" + getCurrentDate();
        return prefs.getBoolean(key, false);
    }

    private void saveMedicationStatus(String id, String name, boolean completed) {
        SharedPreferences prefs = getSharedPreferences("med_status", Context.MODE_PRIVATE);
        String key = (id != null ? id : name) + "_" + getCurrentDate();
        prefs.edit().putBoolean(key, completed).apply();
    }

    // --- Inner Model ---
    private static class MedicationSchedule {
        private String apiId; // Added this field
        private String name;
        private String dosage;
        private String time;
        private boolean completed;

        public MedicationSchedule(String apiId, String name, String dosage, String time, boolean completed) {
            this.apiId = apiId;
            this.name = name;
            this.dosage = dosage;
            this.time = time;
            this.completed = completed;
        }

        public String getApiId() { return apiId; }
        public String getName() { return name; }
        public String getDosage() { return dosage; }
        public String getTime() { return time; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }

        @Override
        public int hashCode() { return (name + time).hashCode(); }
    }
}