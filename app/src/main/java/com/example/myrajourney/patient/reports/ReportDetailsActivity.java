package com.example.myrajourney.patient.reports;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDetailsActivity extends AppCompatActivity {

    private TextView patientName, reportType, reportDate, reportStatus;
    private ImageView reportImage, backButton;
    private EditText diagnosisInput, suggestionsInput;
    private Button saveDiagnosisBtn, normalBtn, abnormalBtn, reviewBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        // Initialize views
        patientName = findViewById(R.id.patient_name_detail);
        reportType = findViewById(R.id.report_type_detail);
        reportDate = findViewById(R.id.report_date_detail);
        reportStatus = findViewById(R.id.report_status_detail);
        reportImage = findViewById(R.id.report_image);
        backButton = findViewById(R.id.back_button);
        diagnosisInput = findViewById(R.id.diagnosis_input);
        suggestionsInput = findViewById(R.id.suggestions_input);
        saveDiagnosisBtn = findViewById(R.id.save_diagnosis_btn);
        normalBtn = findViewById(R.id.btn_normal);
        abnormalBtn = findViewById(R.id.btn_abnormal);
        reviewBtn = findViewById(R.id.btn_review);

        // Get data from intent
        String patientNameStr = getIntent().getStringExtra("patient_name");
        String reportTypeStr = getIntent().getStringExtra("report_type");
        String reportDateStr = getIntent().getStringExtra("report_date");
        String reportStatusStr = getIntent().getStringExtra("report_status");
        String reportIdStr = getIntent().getStringExtra("report_id");

        // Set data
        patientName.setText(patientNameStr != null ? patientNameStr : "Patient");
        reportType.setText(reportTypeStr != null ? reportTypeStr : "Report");
        reportDate.setText(reportDateStr != null ? reportDateStr : "");
        reportStatus.setText(reportStatusStr != null ? reportStatusStr : "Review Required");
        
        // Load existing notes if report ID is available
        if (reportIdStr != null && !reportIdStr.isEmpty()) {
            loadReportNotes(reportIdStr);
        }

        // Set status color
        if (reportStatusStr != null) {
            if (reportStatusStr.equals("Normal")) {
                reportStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (reportStatusStr.equals("Abnormal")) {
                reportStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                reportStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            }
        }

        // Set sample report image (in real app, load from server/storage)
        reportImage.setImageResource(R.drawable.ic_report_sample);

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Quick set status buttons
        normalBtn.setOnClickListener(v -> reportStatus.setText("Normal"));
        abnormalBtn.setOnClickListener(v -> reportStatus.setText("Abnormal"));
        reviewBtn.setOnClickListener(v -> reportStatus.setText("Review Required"));

        // Save diagnosis button
        saveDiagnosisBtn.setOnClickListener(v -> {
            String diagnosis = diagnosisInput.getText().toString().trim();
            String suggestions = suggestionsInput.getText().toString().trim();

            if (diagnosis.isEmpty() && suggestions.isEmpty()) {
                Toast.makeText(this, "Please enter diagnosis or suggestions", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (reportIdStr == null || reportIdStr.isEmpty()) {
                Toast.makeText(this, "Report ID not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to backend
            saveDiagnosisToBackend(reportIdStr, diagnosis, suggestions);
        });
    }
    
    private void loadReportNotes(String reportId) {
        com.example.myrajourney.core.network.ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.ReportNote>>> call = apiService.getReportNotes(reportId);
        
        call.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.ReportNote>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.ReportNote>>> call,
                                 retrofit2.Response<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.ReportNote>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<com.example.myrajourney.data.model
.ReportNote> notes = response.body().getData();
                    if (notes != null && !notes.isEmpty()) {
                        // Load the most recent note
                        com.example.myrajourney.data.model
.ReportNote latestNote = notes.get(0);
                        if (latestNote.getDiagnosisText() != null) {
                            diagnosisInput.setText(latestNote.getDiagnosisText());
                        }
                        if (latestNote.getSuggestionsText() != null) {
                            suggestionsInput.setText(latestNote.getSuggestionsText());
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.ReportNote>>> call, Throwable t) {
                // Silently fail - user can still add new notes
            }
        });
    }
    
    private void saveDiagnosisToBackend(String reportId, String diagnosis, String suggestions) {
        Map<String, Object> request = new HashMap<>();
        request.put("report_id", Integer.parseInt(reportId));
        request.put("diagnosis_text", diagnosis);
        request.put("suggestions_text", suggestions);
        
        com.example.myrajourney.core.network.ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.ReportNote>> call = apiService.createReportNote(request);
        
        call.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.ReportNote>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.ReportNote>> call,
                                 retrofit2.Response<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.ReportNote>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ReportDetailsActivity.this, "Diagnosis and suggestions saved successfully! Patient will be notified.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String errorMsg = "Failed to save diagnosis";
                    if (response.body() != null && response.body().getError() != null) {
                        errorMsg = response.body().getError().getMessage();
                    }
                    Toast.makeText(ReportDetailsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.ReportNote>> call, Throwable t) {
                Toast.makeText(ReportDetailsActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}






