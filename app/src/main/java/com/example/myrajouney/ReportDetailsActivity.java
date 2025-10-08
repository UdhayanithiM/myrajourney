package com.example.myrajouney;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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

        // Set data
        patientName.setText(patientNameStr);
        reportType.setText(reportTypeStr);
        reportDate.setText(reportDateStr);
        reportStatus.setText(reportStatusStr);

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

            // In real app, save to database
            Toast.makeText(this, "Diagnosis and suggestions saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
