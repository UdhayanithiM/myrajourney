package com.example.myrajouney.doctor.patients;

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
            intent.putExtra("report_file", report.getFileUri());
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
        com.example.myrajouney.api.ApiService apiService = com.example.myrajouney.api.ApiClient.getApiService(this);
        retrofit2.Call<com.example.myrajouney.api.models.ApiResponse<List<com.example.myrajouney.api.models.Report>>> call = apiService.getReports();
        
        call.enqueue(new retrofit2.Callback<com.example.myrajouney.api.models.ApiResponse<List<com.example.myrajouney.api.models.Report>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajouney.api.models.ApiResponse<List<com.example.myrajouney.api.models.Report>>> call, retrofit2.Response<com.example.myrajouney.api.models.ApiResponse<List<com.example.myrajouney.api.models.Report>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<com.example.myrajouney.api.models.Report> apiReports = response.body().getData();
                    if (apiReports != null) {
                        reportsList.clear();
                        for (com.example.myrajouney.api.models.Report apiReport : apiReports) {
                            String title = apiReport.getTitle() != null ? apiReport.getTitle() : "Report";
                            String date = formatDate(apiReport.getUploadedAt());
                            String fileUri = apiReport.getFileUrl() != null ? apiReport.getFileUrl() : "";
                            reportsList.add(new Report(title, date, fileUri));
                        }
                        if (reportsAdapter != null) {
                            reportsAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.myrajouney.api.models.ApiResponse<List<com.example.myrajouney.api.models.Report>>> call, Throwable t) {
                // On failure, show empty list
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
                    String dateTime = a.getDate() + " " + a.getTimeSlot();
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
        availableMedications.add(new Medication("Aspirin", "100mg", "Once a day", "30 days", "Tablet", "Painkiller", "Available"));
        availableMedications.add(new Medication("Naproxen", "250mg", "Twice a day", "10 days", "Tablet", "Painkiller", "Available"));

        // RA Specific Medications
        availableMedications.add(new Medication("Methotrexate", "7.5mg", "Once a week", "Ongoing", "Tablet", "DMARD", "Available"));
        availableMedications.add(new Medication("Prednisolone", "5mg", "Once a day", "As needed", "Tablet", "Corticosteroid", "Available"));
        availableMedications.add(new Medication("Hydroxychloroquine", "200mg", "Twice a day", "Ongoing", "Tablet", "DMARD", "Available"));
        availableMedications.add(new Medication("Sulfasalazine", "500mg", "Twice a day", "Ongoing", "Tablet", "DMARD", "Available"));

        // Antibiotics
        availableMedications.add(new Medication("Amoxicillin", "250mg", "Thrice a day", "10 days", "Capsule", "Antibiotic", "Available"));
        availableMedications.add(new Medication("Ciprofloxacin", "500mg", "Twice a day", "7 days", "Tablet", "Antibiotic", "Available"));
        availableMedications.add(new Medication("Azithromycin", "500mg", "Once a day", "5 days", "Tablet", "Antibiotic", "Available"));
        availableMedications.add(new Medication("Doxycycline", "100mg", "Twice a day", "14 days", "Capsule", "Antibiotic", "Available"));

        // Cardiovascular
        availableMedications.add(new Medication("Metoprolol", "50mg", "Twice a day", "30 days", "Tablet", "Beta Blocker", "Available"));
        availableMedications.add(new Medication("Lisinopril", "10mg", "Once a day", "30 days", "Tablet", "ACE Inhibitor", "Available"));
        availableMedications.add(new Medication("Atorvastatin", "20mg", "Once a day", "30 days", "Tablet", "Statin", "Available"));
        availableMedications.add(new Medication("Amlodipine", "5mg", "Once a day", "30 days", "Tablet", "Calcium Channel Blocker", "Available"));

        // Diabetes Management
        availableMedications.add(new Medication("Metformin", "500mg", "Twice a day", "30 days", "Tablet", "Antidiabetic", "Available"));
        availableMedications.add(new Medication("Insulin Glargine", "10 units", "Once a day", "30 days", "Injection", "Insulin", "Available"));
        availableMedications.add(new Medication("Gliclazide", "80mg", "Once a day", "30 days", "Tablet", "Antidiabetic", "Available"));

        // Respiratory
        availableMedications.add(new Medication("Salbutamol", "100mcg", "As needed", "30 days", "Inhaler", "Bronchodilator", "Available"));
        availableMedications.add(new Medication("Budesonide", "200mcg", "Twice a day", "30 days", "Inhaler", "Corticosteroid", "Available"));
        availableMedications.add(new Medication("Montelukast", "10mg", "Once a day", "30 days", "Tablet", "Leukotriene Inhibitor", "Available"));

        // Gastrointestinal
        availableMedications.add(new Medication("Omeprazole", "20mg", "Once a day", "30 days", "Capsule", "Proton Pump Inhibitor", "Available"));
        availableMedications.add(new Medication("Ranitidine", "150mg", "Twice a day", "30 days", "Tablet", "H2 Blocker", "Available"));
        availableMedications.add(new Medication("Domperidone", "10mg", "Thrice a day", "7 days", "Tablet", "Prokinetic", "Available"));

        // Neurological
        availableMedications.add(new Medication("Gabapentin", "300mg", "Thrice a day", "30 days", "Capsule", "Anticonvulsant", "Available"));
        availableMedications.add(new Medication("Pregabalin", "75mg", "Twice a day", "30 days", "Capsule", "Anticonvulsant", "Available"));
        availableMedications.add(new Medication("Diazepam", "5mg", "As needed", "30 days", "Tablet", "Benzodiazepine", "Available"));

        // Vitamins & Supplements
        availableMedications.add(new Medication("Vitamin D3", "1000 IU", "Once a day", "30 days", "Capsule", "Vitamin", "Available"));
        availableMedications.add(new Medication("Calcium Carbonate", "500mg", "Twice a day", "30 days", "Tablet", "Mineral", "Available"));
        availableMedications.add(new Medication("Iron Supplement", "65mg", "Once a day", "30 days", "Tablet", "Mineral", "Available"));
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
                if (med.isTakenToday()) { // Using takenToday as selection flag
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

        availableRehabExercises.add(new Rehab("Wrist Stretches", "Gentle wrist flexion and extension", "10 reps each direction", "Daily",
                "https://www.youtube.com/watch?v=example2",
                "https://img.youtube.com/vi/example2/0.jpg"));

        availableRehabExercises.add(new Rehab("Shoulder Rolls", "Roll shoulders forward and backward in circular motion", "10 reps each direction", "Daily",
                "https://www.youtube.com/watch?v=Z59hwWeVtOc",
                "https://img.youtube.com/vi/Z59hwWeVtOc/0.jpg"));

        availableRehabExercises.add(new Rehab("Ankle Pumps", "Point and flex ankles while sitting", "15 reps each direction", "Daily",
                "https://www.youtube.com/watch?v=example3",
                "https://img.youtube.com/vi/example3/0.jpg"));

        // Upper Body Exercises
        availableRehabExercises.add(new Rehab("Fist Squeeze", "Squeeze a soft ball or stress ball to strengthen grip", "10 reps, 3 sets", "Daily",
                "https://www.youtube.com/watch?v=5qny4scQqHc",
                "https://img.youtube.com/vi/5qny4scQqHc/0.jpg"));

        availableRehabExercises.add(new Rehab("Finger Spread", "Spread fingers apart and hold, then bring together", "5 reps, 3 sets", "Daily",
                "https://www.youtube.com/watch?v=DRr4qzxCSqY",
                "https://img.youtube.com/vi/DRr4qzxCSqY/0.jpg"));

        availableRehabExercises.add(new Rehab("Wrist Flex", "Flex wrist upward and downward slowly", "10 reps, 3 sets", "Daily",
                "https://www.youtube.com/watch?v=NXbtJ6qCdbs",
                "https://img.youtube.com/vi/NXbtJ6qCdbs/0.jpg"));

        availableRehabExercises.add(new Rehab("Arm Raises", "Raise arms to shoulder height and hold", "5 reps, 3 sets", "Daily",
                "https://www.youtube.com/watch?v=8QZqJqJqJqJ",
                "https://img.youtube.com/vi/8QZqJqJqJqJ/0.jpg"));

        // Lower Body Exercises
        availableRehabExercises.add(new Rehab("Ankle Circles", "Rotate ankle in circular motion clockwise and counterclockwise", "10 reps each direction", "Daily",
                "https://www.youtube.com/watch?v=9QZqJqJqJqJ",
                "https://img.youtube.com/vi/9QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Knee Flexion", "Bend and straighten knee while sitting", "10 reps, 3 sets", "Daily",
                "https://www.youtube.com/watch?v=1QZqJqJqJqJ",
                "https://img.youtube.com/vi/1QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Hip Abduction", "Move leg away from body while lying on side", "10 reps, 3 sets", "Daily",
                "https://www.youtube.com/watch?v=2QZqJqJqJqJ",
                "https://img.youtube.com/vi/2QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Calf Raises", "Rise up on toes and lower slowly", "15 reps, 3 sets", "Daily",
                "https://www.youtube.com/watch?v=3QZqJqJqJqJ",
                "https://img.youtube.com/vi/3QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Quad Sets", "Tighten thigh muscles while sitting", "10 reps, hold 5 seconds", "Daily",
                "https://www.youtube.com/watch?v=4QZqJqJqJqJ",
                "https://img.youtube.com/vi/4QZqJqJqJqJ/0.jpg"));

        // Balance and Coordination
        availableRehabExercises.add(new Rehab("Single Leg Stand", "Stand on one leg while holding support", "30 seconds each leg", "Daily",
                "https://www.youtube.com/watch?v=5QZqJqJqJqJ",
                "https://img.youtube.com/vi/5QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Heel-to-Toe Walk", "Walk placing heel directly in front of toe", "10 steps forward, 10 back", "Daily",
                "https://www.youtube.com/watch?v=6QZqJqJqJqJ",
                "https://img.youtube.com/vi/6QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Marching in Place", "Lift knees high while marching in place", "20 steps", "Daily",
                "https://www.youtube.com/watch?v=7QZqJqJqJqJ",
                "https://img.youtube.com/vi/7QZqJqJqJqJ/0.jpg"));

        // Core Strengthening
        availableRehabExercises.add(new Rehab("Pelvic Tilts", "Lie on back and tilt pelvis up and down", "10 reps, 3 sets", "Daily",
                "https://www.youtube.com/watch?v=8QZqJqJqJqJ",
                "https://img.youtube.com/vi/8QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Dead Bug", "Lie on back, extend opposite arm and leg", "10 reps each side", "Daily",
                "https://www.youtube.com/watch?v=9QZqJqJqJqJ",
                "https://img.youtube.com/vi/9QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Bird Dog", "On hands and knees, extend opposite arm and leg", "10 reps each side", "Daily",
                "https://www.youtube.com/watch?v=0QZqJqJqJqJ",
                "https://img.youtube.com/vi/0QZqJqJqJqJ/0.jpg"));

        // Flexibility and Range of Motion
        availableRehabExercises.add(new Rehab("Neck Stretches", "Gently turn head left and right, up and down", "5 reps each direction", "Daily",
                "https://www.youtube.com/watch?v=1QZqJqJqJqJ",
                "https://img.youtube.com/vi/1QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Spinal Twists", "Sit and gently twist spine left and right", "5 reps each side", "Daily",
                "https://www.youtube.com/watch?v=2QZqJqJqJqJ",
                "https://img.youtube.com/vi/2QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Hip Flexor Stretch", "Lunge forward to stretch hip flexors", "30 seconds each leg", "Daily",
                "https://www.youtube.com/watch?v=3QZqJqJqJqJ",
                "https://img.youtube.com/vi/3QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Hamstring Stretch", "Sit and reach forward to stretch hamstrings", "30 seconds, 3 sets", "Daily",
                "https://www.youtube.com/watch?v=4QZqJqJqJqJ",
                "https://img.youtube.com/vi/4QZqJqJqJqJ/0.jpg"));

        // Cardiovascular
        availableRehabExercises.add(new Rehab("Seated Marching", "March in place while sitting", "2 minutes", "Daily",
                "https://www.youtube.com/watch?v=5QZqJqJqJqJ",
                "https://img.youtube.com/vi/5QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Arm Circles", "Make large circles with arms", "10 reps each direction", "Daily",
                "https://www.youtube.com/watch?v=6QZqJqJqJqJ",
                "https://img.youtube.com/vi/6QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Step Ups", "Step up and down on low platform", "10 reps each leg", "Daily",
                "https://www.youtube.com/watch?v=7QZqJqJqJqJ",
                "https://img.youtube.com/vi/7QZqJqJqJqJ/0.jpg"));

        // Post-Surgery Specific
        availableRehabExercises.add(new Rehab("Scapular Squeezes", "Squeeze shoulder blades together", "10 reps, hold 5 seconds", "Daily",
                "https://www.youtube.com/watch?v=8QZqJqJqJqJ",
                "https://img.youtube.com/vi/8QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Wall Slides", "Slide up and down wall with back", "10 reps, 3 sets", "Daily",
                "https://www.youtube.com/watch?v=9QZqJqJqJqJ",
                "https://img.youtube.com/vi/9QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Clamshells", "Lie on side, lift top leg while keeping feet together", "10 reps each side", "Daily",
                "https://www.youtube.com/watch?v=0QZqJqJqJqJ",
                "https://img.youtube.com/vi/0QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Bridge Exercise", "Lift hips up while lying on back", "10 reps, hold 5 seconds", "Daily",
                "https://www.youtube.com/watch?v=1QZqJqJqJqJ",
                "https://img.youtube.com/vi/1QZqJqJqJqJ/0.jpg"));

        availableRehabExercises.add(new Rehab("Walking Heel Raises", "Walk on toes for short distance", "10 steps", "Daily",
                "https://www.youtube.com/watch?v=2QZqJqJqJqJ",
                "https://img.youtube.com/vi/2QZqJqJqJqJ/0.jpg"));
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
