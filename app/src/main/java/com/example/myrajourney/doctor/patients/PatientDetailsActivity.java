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

import com.example.myrajourney.R;
import com.example.myrajourney.admin.users.EditPatientActivity;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Appointment;
import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.data.model.Rehab;
import com.example.myrajourney.data.model.Report;
import com.example.myrajourney.patient.appointments.AppointmentAdapter;
import com.example.myrajourney.patient.medications.AddMedicationAdapter;
import com.example.myrajourney.patient.medications.MedicationsAdapter;
import com.example.myrajourney.patient.rehab.RehabAdapter;
import com.example.myrajourney.common.rehab.AddRehabAdapter;
import com.example.myrajourney.patient.reports.ReportDetailsActivity;
import com.example.myrajourney.patient.reports.ReportsAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientDetailsActivity extends AppCompatActivity {

    // UI Components
    private TextView patientName, patientAge, patientId;
    private ImageView patientImage;
    private RecyclerView medsRecycler, rehabRecycler, appointmentRecycler, reportsRecycler;
    private EditText alertMessage, etDiagnosis, etSuggestions;
    private Button sendAlert, editPatientBtn, btnAddMedication, btnAddRehab, btnSaveDiagnosis;

    // Data Lists
    private List<Medication> medicationsList, availableMedications;
    private List<Rehab> rehabList, availableRehabExercises;
    private List<Appointment> appointmentList;
    private List<Report> reportsList;

    // Adapters
    private MedicationsAdapter medicationsAdapter;
    private RehabAdapter rehabAdapter;
    private AppointmentAdapter appointmentAdapter;
    private ReportsAdapter reportsAdapter;

    // Variables
    private int currentPatientId;
    private String currentPatientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);

        initViews();
        setupPatientInfo(); // Critical Step
        setupRecyclerViews();
        setupButtons();
        loadReportsFromBackend();
    }

    private void initViews() {
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
        etDiagnosis = findViewById(R.id.etDiagnosis);
        etSuggestions = findViewById(R.id.etSuggestions);
        btnSaveDiagnosis = findViewById(R.id.btnSaveDiagnosis);
    }

    private void setupPatientInfo() {
        Intent intent = getIntent();
        currentPatientId = intent.getIntExtra("patient_id", 0);
        currentPatientName = intent.getStringExtra("patient_name");
        String ageRaw = intent.getStringExtra("patient_age");
        int imageRes = intent.getIntExtra("patient_image", R.drawable.ic_person_default);

        // 1. Name
        patientName.setText(currentPatientName != null ? currentPatientName : "Unknown");

        // 2. ID
        if (currentPatientId != 0) {
            patientId.setText("ID: " + currentPatientId);
        } else {
            patientId.setText("ID: --");
        }

        // 3. Age Logic (Robust)
        if (ageRaw != null && !ageRaw.trim().isEmpty() && !ageRaw.equals("N/A") && !ageRaw.equals("0")) {
            // Check if "Years" is already in the string to avoid duplication
            if (ageRaw.toLowerCase().contains("years")) {
                patientAge.setText("Age: " + ageRaw);
            } else {
                patientAge.setText("Age: " + ageRaw + " Years");
            }
        } else {
            patientAge.setText("Age: Not Provided");
        }

        // 4. Image
        patientImage.setImageResource(imageRes);
    }

    private void setupRecyclerViews() {
        // Initialize all lists to empty
        medicationsList = new ArrayList<>();
        rehabList = new ArrayList<>();
        appointmentList = new ArrayList<>();
        reportsList = new ArrayList<>();

        // Load data for selection dialogs
        initializeAvailableMedications();
        initializeAvailableRehabExercises();

        // Setup Adapters
        medicationsAdapter = new MedicationsAdapter(this, medicationsList);
        medsRecycler.setLayoutManager(new LinearLayoutManager(this));
        medsRecycler.setAdapter(medicationsAdapter);

        rehabAdapter = new RehabAdapter(this, rehabList);
        rehabRecycler.setLayoutManager(new LinearLayoutManager(this));
        rehabRecycler.setAdapter(rehabAdapter);

        appointmentAdapter = new AppointmentAdapter(this, appointmentList);
        appointmentRecycler.setLayoutManager(new LinearLayoutManager(this));
        appointmentRecycler.setAdapter(appointmentAdapter);

        reportsAdapter = new ReportsAdapter(this, reportsList);
        reportsRecycler.setLayoutManager(new LinearLayoutManager(this));
        reportsRecycler.setAdapter(reportsAdapter);
    }

    private void setupButtons() {
        editPatientBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditPatientActivity.class);
            intent.putExtra("patient_id", currentPatientId);
            intent.putExtra("patient_name", currentPatientName);
            intent.putExtra("patient_age", patientAge.getText().toString().replace("Age: ", "").replace(" Years", ""));
            startActivity(intent);
        });

        btnAddMedication.setOnClickListener(v -> showMedicationSelectionDialog());
        btnAddRehab.setOnClickListener(v -> showRehabExerciseSelectionDialog());

        sendAlert.setOnClickListener(v -> {
            if (!alertMessage.getText().toString().isEmpty()) {
                Toast.makeText(this, "Alert sent", Toast.LENGTH_SHORT).show();
                alertMessage.setText("");
            }
        });

        btnSaveDiagnosis.setOnClickListener(v -> {
            if (!etDiagnosis.getText().toString().isEmpty()) {
                Toast.makeText(this, "Diagnosis saved", Toast.LENGTH_SHORT).show();
                etDiagnosis.setText("");
                etSuggestions.setText("");
            }
        });

        reportsAdapter.setOnReportClickListener(report -> {
            Intent intent = new Intent(this, ReportDetailsActivity.class);
            intent.putExtra("report_name", report.getName());
            intent.putExtra("report_date", report.getDate());
            intent.putExtra("report_file", report.getFileUrl());
            startActivity(intent);
        });
    }

    private void loadReportsFromBackend() {
        if (currentPatientId == 0) return;

        ApiService apiService = ApiClient.getApiService(this);
        Call<ApiResponse<List<Report>>> call = apiService.getReports(); // Ideally filter by ID

        call.enqueue(new Callback<ApiResponse<List<Report>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Report>>> call, Response<ApiResponse<List<Report>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    reportsList.clear();
                    for (Report r : response.body().getData()) {
                        String date = r.getUploadedAt() != null ? r.getUploadedAt().substring(0, 10) : "Unknown";
                        reportsList.add(new Report(r.getTitle(), date, r.getFileUrl()));
                    }
                    reportsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Report>>> call, Throwable t) {
                // Handle failure silently or show toast
            }
        });
    }

    // --- Helpers ---
    private void initializeAvailableMedications() {
        availableMedications = new ArrayList<>();
        availableMedications.add(new Medication("Methotrexate", "7.5mg", "Weekly", "Ongoing", "Tablet", "DMARD", "Available"));
        availableMedications.add(new Medication("Hydroxychloroquine", "200mg", "Daily", "30 days", "Tablet", "DMARD", "Available"));
        availableMedications.add(new Medication("Ibuprofen", "400mg", "As needed", "7 days", "Tablet", "NSAID", "Available"));
    }

    private void initializeAvailableRehabExercises() {
        availableRehabExercises = new ArrayList<>();
        availableRehabExercises.add(new Rehab("Hand Squeeze", "Squeeze a soft ball", "10 reps", "Daily", "", ""));
        availableRehabExercises.add(new Rehab("Wrist Extensions", "Extend wrist", "15 reps", "Daily", "", ""));
    }

    private void showMedicationSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_select_medications, null);
        RecyclerView rv = view.findViewById(R.id.medicationsRecyclerView);

        List<Medication> selectionList = new ArrayList<>();
        for(Medication m : availableMedications) {
            selectionList.add(new Medication(m.getName(), m.getDosage(), m.getFrequency(), m.getDuration(), m.getType(), m.getCategory(), m.getStatus()));
        }

        AddMedicationAdapter adapter = new AddMedicationAdapter(this, selectionList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        AlertDialog dialog = builder.setView(view).create();

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnAddSelected).setOnClickListener(v -> {
            for(Medication m : selectionList) {
                if(m.isTakenToday()) {
                    medicationsList.add(m);
                }
            }
            medicationsAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showRehabExerciseSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_select_rehab_exercises, null);
        RecyclerView rv = view.findViewById(R.id.rehabExercisesRecyclerView);

        List<Rehab> selectionList = new ArrayList<>();
        for(Rehab r : availableRehabExercises) {
            selectionList.add(new Rehab(r.getName(), r.getDescription(), r.getReps(), r.getFrequency(), r.getVideoUrl(), r.getThumbnailUrl()));
        }

        AddRehabAdapter adapter = new AddRehabAdapter(this, selectionList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        AlertDialog dialog = builder.setView(view).create();

        view.findViewById(R.id.btnCancelRehab).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnAddSelectedRehab).setOnClickListener(v -> {
            for(Rehab r : selectionList) {
                if(r.isSelected()) {
                    rehabList.add(r);
                }
            }
            rehabAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });
        dialog.show();
    }
}