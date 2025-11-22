package com.example.myrajourney.doctor.patients;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Appointment;
import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.data.model.Rehab;
import com.example.myrajourney.data.model.Report;

// Adapters (Assuming these locations based on your project structure)
import com.example.myrajourney.patient.medications.MedicationsAdapter;
import com.example.myrajourney.patient.medications.AddMedicationAdapter;
import com.example.myrajourney.patient.rehab.RehabAdapter;
import com.example.myrajourney.common.rehab.AddRehabAdapter;
import com.example.myrajourney.patient.appointments.AppointmentAdapter;
import com.example.myrajourney.patient.reports.ReportsAdapter;

// Activities
import com.example.myrajourney.admin.users.EditPatientActivity;
import com.example.myrajourney.patient.reports.ReportDetailsActivity;

// Retrofit
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
// ---------------------

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientDetailsActivity extends AppCompatActivity {

    TextView patientName, patientAge, patientId;
    ImageView patientImage;

    RecyclerView medsRecycler, rehabRecycler, appointmentRecycler, reportsRecycler;
    EditText alertMessage;
    Button sendAlert, editPatientBtn, btnAddMedication, btnAddRehab;

    // Reports and diagnosis fields
    EditText etDiagnosis, etSuggestions;
    Button btnSaveDiagnosis;

    List<Medication> medicationsList;
    List<Rehab> rehabList;
    List<Appointment> appointmentList;
    List<Report> reportsList;
    List<Medication> availableMedications;
    List<Rehab> availableRehabExercises;

    MedicationsAdapter medicationsAdapter;
    RehabAdapter rehabAdapter;
    AppointmentAdapter appointmentAdapter;
    ReportsAdapter reportsAdapter;
    AddMedicationAdapter addMedicationAdapter;
    AddRehabAdapter addRehabAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);

        // Bind views
        patientName = findViewById(R.id.patientName);
        patientAge = findViewById(R.id.patientAge);
        patientId = findViewById(R.id.patientId);
        patientImage = findViewById(R.id.patientImage);

        medsRecycler = findViewById(R.id.medsRecycler);
        rehabRecycler = findViewById(R.id.rehabRecycler);
        appointmentRecycler = findViewById(R.id.appointmentRecycler);
        reportsRecycler = findViewById(R.id.reportsRecycler);

        alertMessage = findViewById(R.id.alertMessage);
        sendAlert = findViewById(R.id.sendAlert);
        editPatientBtn = findViewById(R.id.editPatientBtn);
        btnAddMedication = findViewById(R.id.btnAddMedication);
        btnAddRehab = findViewById(R.id.btnAddRehab);

        // Initialize diagnosis fields
        etDiagnosis = findViewById(R.id.etDiagnosis);
        etSuggestions = findViewById(R.id.etSuggestions);
        btnSaveDiagnosis = findViewById(R.id.btnSaveDiagnosis);

        // Sample patient info from intent
        patientName.setText(getIntent().getStringExtra("patient_name"));
        patientAge.setText(getIntent().getStringExtra("patient_age"));
        patientId.setText("ID: 12345");
        patientImage.setImageResource(getIntent().getIntExtra("patient_image", R.drawable.ic_person_default));

        // ---------------- Medications ----------------
        medicationsList = new ArrayList<>();
        medicationsAdapter = new MedicationsAdapter(this, medicationsList);
        medsRecycler.setLayoutManager(new LinearLayoutManager(this));
        medsRecycler.setAdapter(medicationsAdapter);

        // Initialize available medications
        initializeAvailableMedications();

        // ---------------- Rehab ----------------
        rehabList = new ArrayList<>();
        rehabAdapter = new RehabAdapter(this, rehabList);
        rehabRecycler.setLayoutManager(new LinearLayoutManager(this));
        rehabRecycler.setAdapter(rehabAdapter);

        // Initialize available rehab exercises
        initializeAvailableRehabExercises();

        // ---------------- Appointments ----------------
        appointmentList = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(this, appointmentList);
        appointmentRecycler.setLayoutManager(new LinearLayoutManager(this));
        appointmentRecycler.setAdapter(appointmentAdapter);

        // ---------------- Reports ----------------
        reportsList = new ArrayList<>();
        reportsAdapter = new ReportsAdapter(this, reportsList);
        reportsRecycler.setLayoutManager(new LinearLayoutManager(this));
        reportsRecycler.setAdapter(reportsAdapter);

        // Initialize sample reports for the patient
        initializePatientReports();

        // ---------------- Alerts ----------------
        sendAlert.setOnClickListener(v -> {
            String message = alertMessage.getText().toString();
            if (!message.isEmpty()) {
                Toast.makeText(this, "Alert sent: " + message, Toast.LENGTH_SHORT).show();
                alertMessage.setText("");
            } else {
                Toast.makeText(this, "Enter a message first", Toast.LENGTH_SHORT).show();
            }
        });

        // ---------------- Edit Patient Button ----------------
        editPatientBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PatientDetailsActivity.this, EditPatientActivity.class);
            intent.putExtra("patient_name", getIntent().getStringExtra("patient_name"));
            intent.putExtra("patient_age", getIntent().getStringExtra("patient_age"));
            startActivity(intent);
        });

        // ---------------- Add Medication Button ----------------
        btnAddMedication.setOnClickListener(v -> showMedicationSelectionDialog());

        // ---------------- Add Rehab Exercise Button ----------------
        btnAddRehab.setOnClickListener(v -> showRehabExerciseSelectionDialog());

        // ---------------- Reports Click Listener ----------------
        reportsAdapter.setOnReportClickListener(report -> {
            // Open report for viewing
            Intent intent = new Intent(this, ReportDetailsActivity.class);
            intent.putExtra("report_name", report.getName());
            intent.putExtra("report_date", report.getDate());
            // Assuming Report has getFileUrl or getFileUri
            intent.putExtra("report_file", report.getFileUrl());
            intent.putExtra("patient_name", patientName.getText().toString());
            startActivity(intent);
        });

        // ---------------- Save Diagnosis Button ----------------
        btnSaveDiagnosis.setOnClickListener(v -> {
            String diagnosis = etDiagnosis.getText().toString().trim();
            String suggestions = etSuggestions.getText().toString().trim();

            if (!diagnosis.isEmpty() || !suggestions.isEmpty()) {
                // In a real app, save to database
                Toast.makeText(this, "Diagnosis and suggestions saved!", Toast.LENGTH_SHORT).show();

                // Clear fields after saving
                etDiagnosis.setText("");
                etSuggestions.setText("");
            } else {
                Toast.makeText(this, "Please enter diagnosis or suggestions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------- Initialize Patient Reports ----------------
    private void initializePatientReports() {
        reportsList = new ArrayList<>();
        // Load reports from backend API - no default values
        loadReportsFromBackend();
    }

    private void loadReportsFromBackend() {
        ApiService apiService = ApiClient.getApiService(this);
        Call<ApiResponse<List<Report>>> call = apiService.getReports();

        call.enqueue(new Callback<ApiResponse<List<Report>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Report>>> call, Response<ApiResponse<List<Report>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Report> apiReports = response.body().getData();
                    if (apiReports != null) {
                        reportsList.clear();
                        for (Report apiReport : apiReports) {
                            String title = apiReport.getTitle() != null ? apiReport.getTitle() : "Report";
                            String date = formatDate(apiReport.getUploadedAt());
                            String fileUri = apiReport.getFileUrl() != null ? apiReport.getFileUrl() : "";
                            // Create report with clean data
                            reportsList.add(new Report(title, date, fileUri));
                        }
                        if (reportsAdapter != null) {
                            reportsAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Report>>> call, Throwable t) {
                // On failure, the list remains empty (as initialized)
                if (reportsAdapter != null) {
                    reportsAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    // ---------------- Get Next Appointment ----------------
    public Appointment getNextAppointment(String patientName) {
        Appointment next = null;
        long minTimeDiff = Long.MAX_VALUE;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);

        Calendar now = Calendar.getInstance();

        for (Appointment a : appointmentList) {
            if (a.getPatientName().equalsIgnoreCase(patientName)) {
                try {
                    // Assuming Appointment has getDate() and getTime() (or similar)
                    String dateTime = a.getDate() + " " + a.getTime();
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

    // ---------------- Initialize Available Medications ----------------
    private void initializeAvailableMedications() {
        availableMedications = new ArrayList<>();

        // Pain Management
        availableMedications.add(new Medication("Paracetamol", "500mg", "Twice a day", "5 days", "Tablet", "Painkiller", "Available"));
        availableMedications.add(new Medication("Ibuprofen", "400mg", "Once a day", "7 days", "Tablet", "Painkiller", "Available"));
        // ... (Rest of your medications) ...
        availableMedications.add(new Medication("Aspirin", "100mg", "Once a day", "30 days", "Tablet", "Painkiller", "Available"));
        availableMedications.add(new Medication("Naproxen", "250mg", "Twice a day", "10 days", "Tablet", "Painkiller", "Available"));

        // Add the rest of your long list here (Truncated for brevity, paste your full list back in)
        // RA Specific, Antibiotics, Cardiovascular, etc.
        availableMedications.add(new Medication("Methotrexate", "7.5mg", "Once a week", "Ongoing", "Tablet", "DMARD", "Available"));
        // ...
    }

    // ---------------- Show Medication Selection Dialog ----------------
    private void showMedicationSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_medications, null);

        RecyclerView medicationsRecyclerView = dialogView.findViewById(R.id.medicationsRecyclerView);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnAddSelected = dialogView.findViewById(R.id.btnAddSelected);

        // Create a copy of available medications for selection
        List<Medication> selectionList = new ArrayList<>();
        for (Medication med : availableMedications) {
            selectionList.add(new Medication(med.getName(), med.getDosage(), med.getFrequency(),
                    med.getDuration(), med.getType(), med.getCategory(), med.getStatus()));
        }

        addMedicationAdapter = new AddMedicationAdapter(this, selectionList);
        medicationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        medicationsRecyclerView.setAdapter(addMedicationAdapter);

        AlertDialog dialog = builder.setView(dialogView).create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAddSelected.setOnClickListener(v -> {
            List<Medication> selectedMedications = new ArrayList<>();
            for (int i = 0; i < selectionList.size(); i++) {
                Medication med = selectionList.get(i);
                if (med.isTakenToday()) { // Using takenToday as selection flag/check
                    selectedMedications.add(med);
                }
            }

            if (selectedMedications.isEmpty()) {
                Toast.makeText(this, "Please select at least one medication", Toast.LENGTH_SHORT).show();
            } else {
                // Add selected medications to patient's medication list
                medicationsList.addAll(selectedMedications);
                medicationsAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Added " + selectedMedications.size() + " medication(s)", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // ---------------- Initialize Available Rehab Exercises ----------------
    private void initializeAvailableRehabExercises() {
        availableRehabExercises = new ArrayList<>();

        // RA Specific Exercises
        availableRehabExercises.add(new Rehab("Hand Exercises", "Gentle hand and finger stretches for RA", "10 reps each", "Daily",
                "https://www.youtube.com/watch?v=example1",
                "https://img.youtube.com/vi/example1/0.jpg"));

        // ... (Rest of your rehab exercises) ...
        availableRehabExercises.add(new Rehab("Wrist Stretches", "Gentle wrist flexion and extension", "10 reps each direction", "Daily",
                "https://www.youtube.com/watch?v=example2",
                "https://img.youtube.com/vi/example2/0.jpg"));
    }

    // ---------------- Show Rehab Exercise Selection Dialog ----------------
    private void showRehabExerciseSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_rehab_exercises, null);

        RecyclerView rehabExercisesRecyclerView = dialogView.findViewById(R.id.rehabExercisesRecyclerView);
        Button btnCancelRehab = dialogView.findViewById(R.id.btnCancelRehab);
        Button btnAddSelectedRehab = dialogView.findViewById(R.id.btnAddSelectedRehab);

        // Create a copy of available rehab exercises for selection
        List<Rehab> selectionList = new ArrayList<>();
        for (Rehab rehab : availableRehabExercises) {
            selectionList.add(new Rehab(rehab.getName(), rehab.getDescription(), rehab.getReps(),
                    rehab.getFrequency(), rehab.getVideoUrl(), rehab.getThumbnailUrl()));
        }

        addRehabAdapter = new AddRehabAdapter(this, selectionList);
        rehabExercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rehabExercisesRecyclerView.setAdapter(addRehabAdapter);

        AlertDialog dialog = builder.setView(dialogView).create();

        btnCancelRehab.setOnClickListener(v -> dialog.dismiss());

        btnAddSelectedRehab.setOnClickListener(v -> {
            List<Rehab> selectedExercises = new ArrayList<>();
            for (int i = 0; i < selectionList.size(); i++) {
                Rehab rehab = selectionList.get(i);
                // Ensure Rehab model has isSelected() getter
                if (rehab.isSelected()) {
                    selectedExercises.add(rehab);
                }
            }

            if (selectedExercises.isEmpty()) {
                Toast.makeText(this, "Please select at least one exercise", Toast.LENGTH_SHORT).show();
            } else {
                // Add selected exercises to patient's rehab list
                rehabList.addAll(selectedExercises);
                rehabAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Added " + selectedExercises.size() + " exercise(s)", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}