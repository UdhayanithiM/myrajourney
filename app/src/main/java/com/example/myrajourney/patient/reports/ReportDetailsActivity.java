package com.example.myrajourney.patient.reports;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.ReportNote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportDetailsActivity extends AppCompatActivity {

    private TextView patientName, reportType, reportDate, reportStatus;
    private ImageView reportImage, backButton;
    private EditText diagnosisInput, suggestionsInput;
    private Button saveDiagnosisBtn, normalBtn, abnormalBtn, reviewBtn;

    private String reportId, reportFile, mode;

    private static final String BASE_URL = "http://10.58.163.149:8000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        initViews();
        receiveIntentData();
        loadExistingNotes();
        loadReportFile();
        setupStatusButtons();
        setupSaveButton();
    }

    private void initViews() {
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

        backButton.setOnClickListener(v -> finish());
    }

    private void receiveIntentData() {

        patientName.setText(getIntent().getStringExtra("patient_name"));
        reportType.setText(getIntent().getStringExtra("report_type"));
        reportDate.setText(getIntent().getStringExtra("report_date"));
        reportStatus.setText(getIntent().getStringExtra("report_status"));

        reportId = getIntent().getStringExtra("report_id");
        reportFile = getIntent().getStringExtra("report_file");

        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "patient";

        if (mode.equals("patient")) {

            // Patient CAN SEE but CANNOT EDIT
            diagnosisInput.setEnabled(false);
            suggestionsInput.setEnabled(false);

            // Hide doctor-only actions
            saveDiagnosisBtn.setVisibility(View.GONE);
            normalBtn.setVisibility(View.GONE);
            abnormalBtn.setVisibility(View.GONE);
            reviewBtn.setVisibility(View.GONE);
        }
    }

    private void loadExistingNotes() {
        if (reportId == null) return;

        ApiService api = ApiClient.getApiService(this);

        api.getReportNotes(reportId).enqueue(new Callback<ApiResponse<List<ReportNote>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ReportNote>>> call,
                                   Response<ApiResponse<List<ReportNote>>> response) {

                if (!response.isSuccessful() || response.body() == null) return;

                List<ReportNote> notes = response.body().getData();
                if (notes == null || notes.isEmpty()) return;

                ReportNote latest = notes.get(0); // latest due to ORDER BY created_at DESC

                if (latest.getDiagnosisText() != null)
                    diagnosisInput.setText(latest.getDiagnosisText());

                if (latest.getSuggestionsText() != null)
                    suggestionsInput.setText(latest.getSuggestionsText());
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ReportNote>>> call, Throwable t) {}
        });
    }

    private void loadReportFile() {

        if (reportFile == null || reportFile.isEmpty()) {
            reportImage.setImageResource(R.drawable.ic_report_sample);
            return;
        }

        String fullUrl = reportFile.startsWith("http")
                ? reportFile
                : BASE_URL + reportFile;

        if (reportFile.endsWith(".pdf")) {

            reportImage.setImageResource(R.drawable.ic_pdf);

            reportImage.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl));
                intent.setDataAndType(Uri.parse(fullUrl), "application/pdf");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });

        } else {
            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_report_sample)
                    .error(R.drawable.ic_report_sample)
                    .into(reportImage);
        }
    }

    private void setupStatusButtons() {
        normalBtn.setOnClickListener(v -> updateStatus("Normal"));
        abnormalBtn.setOnClickListener(v -> updateStatus("Abnormal"));
        reviewBtn.setOnClickListener(v -> updateStatus("Review Required"));
    }

    private void updateStatus(String newStatus) {

        if (reportId == null) return;

        Map<String, Object> body = new HashMap<>();
        body.put("report_id", reportId);
        body.put("status", newStatus);

        ApiService api = ApiClient.getApiService(this);

        api.updateReportStatus(body).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call,
                                   Response<ApiResponse<Object>> response) {

                if (response.isSuccessful() &&
                        response.body() != null &&
                        response.body().isSuccess()) {

                    reportStatus.setText(newStatus);

                } else {
                    Toast.makeText(ReportDetailsActivity.this,
                            "Failed to update status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(ReportDetailsActivity.this,
                        "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSaveButton() {
        saveDiagnosisBtn.setOnClickListener(v -> {

            String diagnosis = diagnosisInput.getText().toString().trim();
            String suggestions = suggestionsInput.getText().toString().trim();

            if (diagnosis.isEmpty() && suggestions.isEmpty()) {
                Toast.makeText(this,
                        "Enter diagnosis or suggestions", Toast.LENGTH_SHORT).show();
                return;
            }

            saveDiagnosisToBackend(reportId, diagnosis, suggestions);
        });
    }

    private void saveDiagnosisToBackend(String reportId, String diagnosis, String suggestions) {

        Map<String, Object> body = new HashMap<>();
        body.put("report_id", reportId);
        body.put("diagnosis_text", diagnosis);
        body.put("suggestions_text", suggestions);

        ApiService api = ApiClient.getApiService(this);

        api.createReportNote(body).enqueue(new Callback<ApiResponse<ReportNote>>() {
            @Override
            public void onResponse(Call<ApiResponse<ReportNote>> call,
                                   Response<ApiResponse<ReportNote>> response) {

                if (response.isSuccessful() &&
                        response.body() != null &&
                        response.body().isSuccess()) {

                    Toast.makeText(ReportDetailsActivity.this,
                            "Saved successfully", Toast.LENGTH_LONG).show();
                    finish();

                } else {
                    Toast.makeText(ReportDetailsActivity.this,
                            "Save failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ReportNote>> call, Throwable t) {
                Toast.makeText(ReportDetailsActivity.this,
                        "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
