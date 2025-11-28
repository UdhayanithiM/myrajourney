package com.example.myrajourney.doctor.patients;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Added this import
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.admin.users.EditPatientActivity;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.data.model.Report;
import com.example.myrajourney.patient.medications.MedicationsAdapter;
import com.example.myrajourney.patient.rehab.RehabAdapter;
import com.example.myrajourney.patient.appointments.AppointmentAdapter;
import com.example.myrajourney.patient.reports.ReportDetailsActivity;
import com.example.myrajourney.patient.reports.ReportsAdapter;
import com.example.myrajourney.data.model.Appointment;
import com.example.myrajourney.data.model.Rehab;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientDetailsActivity extends AppCompatActivity {

    private static final int REQ_ADD_REHAB = 2001;

    private TextView patientName, patientAge, patientId;
    private ImageView patientImage;
    private RecyclerView medsRecycler, rehabRecycler, appointmentRecycler, reportsRecycler;
    private EditText alertMessage, etDiagnosis, etSuggestions;

    private Button sendAlert, editPatientBtn, btnAssignMedication, btnAddRehab, btnSaveDiagnosis;

    private List<Medication> medicationsList;
    private List<Rehab> rehabList;
    private List<Appointment> appointmentList;
    private List<Report> reportsList;

    private MedicationsAdapter medicationsAdapter;
    private RehabAdapter rehabAdapter;
    private AppointmentAdapter appointmentAdapter;
    private ReportsAdapter reportsAdapter;

    private int currentPatientId;
    private String currentPatientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);

        initViews();
        setupPatientInfo();
        setupRecyclerViews();
        setupButtons();

        loadReportsFromBackend();
        loadMedicationsFromBackend();
        loadRehabFromBackend();
        loadAppointmentsFromBackend();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to screen
        loadMedicationsFromBackend();
        loadRehabFromBackend();
        loadAppointmentsFromBackend();
    }

    // ✅ FIXED: Removed syntax error in parameters
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ADD_REHAB && resultCode == RESULT_OK && data != null) {
            // Get the list of exercises the doctor selected
            ArrayList<Rehab> selectedExercises = data.getParcelableArrayListExtra("selected_rehab");

            // Send them to the backend
            if (selectedExercises != null && !selectedExercises.isEmpty()) {
                saveRehabPlanToBackend(selectedExercises);
            } else {
                Toast.makeText(this, "No exercises selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveRehabPlanToBackend(List<Rehab> exercises) {
        // 1. Prepare the Payload (JSON Body)
        Map<String, Object> payload = new HashMap<>();
        payload.put("patient_id", currentPatientId);
        payload.put("title", "Assigned Therapy Plan");
        payload.put("description", "Exercises assigned by Doctor for " + currentPatientName);

        // Convert List<Rehab> to List<Map> for JSON
        List<Map<String, Object>> exercisesList = new ArrayList<>();
        for (Rehab r : exercises) {
            Map<String, Object> exMap = new HashMap<>();
            exMap.put("name", r.getName());
            exMap.put("description", r.getDescription());
            exMap.put("reps", r.getReps());
            exMap.put("frequency_per_week", r.getFrequency());
            exMap.put("sets", 3); // Default if not provided

            exercisesList.add(exMap);
        }
        payload.put("exercises", exercisesList);

        Toast.makeText(this, "Saving plan...", Toast.LENGTH_SHORT).show();

        // 2. Call the API
        ApiService api = ApiClient.getApiService(this);
        api.createRehabPlan(payload).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(PatientDetailsActivity.this, "Rehab Plan Assigned Successfully!", Toast.LENGTH_LONG).show();

                    // Refresh the list on screen
                    loadRehabFromBackend();
                } else {
                    String error = "Failed to save";
                    if(response.body() != null && response.body().getError() != null) {
                        error = response.body().getError().getMessage();
                    }
                    Toast.makeText(PatientDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(PatientDetailsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

        btnAssignMedication = findViewById(R.id.btnAssignMedication);
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

        patientName.setText(currentPatientName != null ? currentPatientName : "Unknown");

        patientId.setText("ID: " + (currentPatientId == 0 ? "--" : currentPatientId));

        if (ageRaw != null && !ageRaw.trim().isEmpty()) {
            patientAge.setText("Age: " + ageRaw.replace("Years", "").trim() + " Years");
        } else {
            patientAge.setText("Age: Not Provided");
        }

        patientImage.setImageResource(imageRes);
    }

    private void setupRecyclerViews() {
        medicationsList = new ArrayList<>();
        rehabList = new ArrayList<>();
        appointmentList = new ArrayList<>();
        reportsList = new ArrayList<>();

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
            intent.putExtra("patient_age", patientAge.getText().toString().replace("Age: ", ""));
            startActivity(intent);
        });

        btnAssignMedication.setOnClickListener(v -> {
            if (currentPatientId == 0) {
                Toast.makeText(this, "Invalid patient", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, com.example.myrajourney.doctor.meds.DoctorAssignMedicationActivity.class);
            intent.putExtra("patient_id", currentPatientId);
            intent.putExtra("patient_name", currentPatientName);
            startActivity(intent);
        });

        btnAddRehab.setOnClickListener(v -> {
            if (currentPatientId == 0) {
                Toast.makeText(this, "Invalid patient", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, com.example.myrajourney.common.rehab.AddRehabActivity.class);
            startActivityForResult(intent, REQ_ADD_REHAB);
        });

        sendAlert.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(alertMessage.getText().toString().trim())) {
                Toast.makeText(this, "Alert sent", Toast.LENGTH_SHORT).show();
                alertMessage.setText("");
            }
        });

        btnSaveDiagnosis.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(etDiagnosis.getText().toString().trim())) {
                Toast.makeText(this, "Diagnosis saved", Toast.LENGTH_SHORT).show();
                etDiagnosis.setText("");
                etSuggestions.setText("");
            }
        });

        reportsAdapter.setOnReportClickListener(report -> {
            Intent intent = new Intent(this, ReportDetailsActivity.class);
            intent.putExtra("patient_name", currentPatientName);
            intent.putExtra("report_type", report.getTitle());
            intent.putExtra("report_date", report.getCreatedAt());
            intent.putExtra("report_status", report.getStatus());
            intent.putExtra("report_id", report.getId());
            intent.putExtra("report_file", report.getFileUrl());
            startActivity(intent);
        });
    }

    // ---------- APPOINTMENTS ----------
    private void loadAppointmentsFromBackend() {
        appointmentList.clear();
        appointmentAdapter.notifyDataSetChanged();

        if (currentPatientId == 0) return;

        ApiService api = ApiClient.getApiService(this);

        api.getAppointments(currentPatientId, null).enqueue(new Callback<ApiResponse<List<Appointment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Appointment>>> call,
                                   Response<ApiResponse<List<Appointment>>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Appointment> data = response.body().getData();
                    if (data != null) appointmentList.addAll(data);
                }
                appointmentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Appointment>>> call, Throwable t) {
                appointmentAdapter.notifyDataSetChanged();
            }
        });
    }

    // ---------- MEDICATIONS ----------
    private void loadMedicationsFromBackend() {
        if (currentPatientId == 0) {
            medicationsList.clear();
            medicationsAdapter.notifyDataSetChanged();
            return;
        }

        ApiService api = ApiClient.getApiService(this);

        api.getPatientMedications(currentPatientId)
                .enqueue(new Callback<ApiResponse<List<Medication>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Medication>>> call,
                                           Response<ApiResponse<List<Medication>>> response) {

                        medicationsList.clear();

                        if (response.isSuccessful() &&
                                response.body() != null &&
                                response.body().isSuccess()) {

                            List<Medication> list = response.body().getData();
                            if (list != null) medicationsList.addAll(list);
                        }
                        medicationsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Medication>>> call, Throwable t) {
                        medicationsList.clear();
                        medicationsAdapter.notifyDataSetChanged();
                    }
                });
    }

    // ---------- REHAB ----------
    private void loadRehabFromBackend() {
        rehabList.clear();
        rehabAdapter.notifyDataSetChanged();

        if (currentPatientId == 0) return;

        ApiService api = ApiClient.getApiService(this);

        api.getRehabPlans(currentPatientId)
                .enqueue(new Callback<ApiResponse<List<com.example.myrajourney.data.model.RehabPlan>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<com.example.myrajourney.data.model.RehabPlan>>> call,
                                           Response<ApiResponse<List<com.example.myrajourney.data.model.RehabPlan>>> response) {

                        if (response.isSuccessful() &&
                                response.body() != null &&
                                response.body().isSuccess()) {

                            List<com.example.myrajourney.data.model.RehabPlan> plans = response.body().getData();

                            if (plans != null) {
                                for (com.example.myrajourney.data.model.RehabPlan plan : plans) {

                                    List<com.example.myrajourney.data.model.RehabPlan.RehabExercise> exercises =
                                            plan.getExercises();

                                    if (exercises != null && !exercises.isEmpty()) {
                                        for (com.example.myrajourney.data.model.RehabPlan.RehabExercise ex : exercises) {
                                            rehabList.add(
                                                    new Rehab(
                                                            ex.getName(),
                                                            ex.getDescription(),
                                                            String.valueOf(ex.getReps()),
                                                            "1 set/day",
                                                            plan.getVideoUrl(),
                                                            ""
                                                    )
                                            );
                                        }
                                    }
                                }
                            }
                        }

                        rehabAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<com.example.myrajourney.data.model.RehabPlan>>> call, Throwable t) {
                        rehabAdapter.notifyDataSetChanged();
                    }
                });
    }

    // ---------- REPORTS ----------
    private void loadReportsFromBackend() {
        reportsList.clear();
        reportsAdapter.notifyDataSetChanged();

        if (currentPatientId == 0) return;

        ApiService api = ApiClient.getApiService(this);

        api.getReports().enqueue(new Callback<ApiResponse<List<Report>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Report>>> call,
                                   Response<ApiResponse<List<Report>>> response) {

                if (response.isSuccessful() &&
                        response.body() != null &&
                        response.body().getData() != null) {

                    for (Report r : response.body().getData()) {

                        if (String.valueOf(r.getPatientId()).equals(String.valueOf(currentPatientId))) {

                            Report model = new Report();
                            model.setId(r.getId());
                            model.setPatientId(r.getPatientId());
                            model.setPatientName(currentPatientName);
                            model.setTitle(r.getTitle());
                            model.setFileUrl(r.getFileUrl());
                            model.setStatus(r.getStatus());
                            model.setCreatedAt(r.getCreatedAt());

                            reportsList.add(model);
                        }
                    }
                    reportsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Report>>> call, Throwable t) { }
        });
    }
}