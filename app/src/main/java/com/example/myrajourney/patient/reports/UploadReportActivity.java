package com.example.myrajourney.patient.reports;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UploadReportActivity extends AppCompatActivity {

    private TextInputEditText reportName;
    private TextView reportDate, uploadText;
    private Button submitButton;

    private Calendar selectedDateTime = Calendar.getInstance();
    private Uri selectedFileUri;

    // ActivityResultLauncher for file picker
    private final ActivityResultLauncher<String[]> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri != null) {
                            selectedFileUri = uri;
                            uploadText.setText("File selected: " + uri.getLastPathSegment());
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_report);

        reportName = findViewById(R.id.reportName);
        reportDate = findViewById(R.id.reportDate);
        uploadText = findViewById(R.id.uploadText);
        submitButton = findViewById(R.id.submitButton);

        // Date & Time picker
        reportDate.setOnClickListener(v -> showDateTimePicker());

        // Upload area click
        findViewById(R.id.uploadArea).setOnClickListener(v -> openFilePicker());

        // Submit button
        submitButton.setOnClickListener(v -> submitReport());
    }

    private void showDateTimePicker() {
        Calendar now = Calendar.getInstance();

        // Date picker
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(year, month, dayOfMonth);

                    // Time picker
                    TimePickerDialog timePicker = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedDateTime.set(Calendar.MINUTE, minute);

                                // Update TextView
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                reportDate.setText(sdf.format(selectedDateTime.getTime()));
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true);
                    timePicker.show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }

    private void openFilePicker() {
        filePickerLauncher.launch(new String[]{"application/pdf", "image/*"});
    }

    private void submitReport() {
        String name = reportName.getText().toString().trim();
        String date = reportDate.getText().toString().trim();

        if (name.isEmpty()) {
            reportName.setError("Report name required");
            return;
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload to backend
        uploadReportToBackend(name, date);
    }
    
    private void uploadReportToBackend(String title, String date) {
        // Get patient ID
        TokenManager tokenManager = TokenManager.getInstance(this);
        String patientId = tokenManager.getUserId();
        
        if (patientId == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Read file from URI
            android.content.ContentResolver resolver = getContentResolver();
            android.os.ParcelFileDescriptor pfd = resolver.openFileDescriptor(selectedFileUri, "r");
            if (pfd == null) {
                Toast.makeText(this, "Could not read file", Toast.LENGTH_SHORT).show();
                return;
            }
            
            java.io.FileInputStream fis = new java.io.FileInputStream(pfd.getFileDescriptor());
            byte[] fileBytes = new byte[(int)pfd.getStatSize()];
            fis.read(fileBytes);
            fis.close();
            pfd.close();
            
            // Get file name and MIME type
            String fileName = "report_" + System.currentTimeMillis() + ".pdf";
            String mimeType = resolver.getType(selectedFileUri);
            if (mimeType == null) mimeType = "application/pdf";
            
            // Create multipart request body
            okhttp3.RequestBody patientIdBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), patientId);
            okhttp3.RequestBody titleBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), title);
            okhttp3.RequestBody descriptionBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), "Uploaded on " + date);
            
            okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse(mimeType), fileBytes);
            okhttp3.MultipartBody.Part filePart = okhttp3.MultipartBody.Part.createFormData("file", fileName, fileBody);
            
            // Upload to backend
            com.example.myrajourney.core.network.ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
            retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model
.Report>> call =
                apiService.createReport(patientIdBody, titleBody, descriptionBody, filePart);
            
            submitButton.setEnabled(false);
            submitButton.setText("Uploading...");
            
            call.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.Report>>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.Report>> call,
                                     retrofit2.Response<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.Report>> response) {
                    submitButton.setEnabled(true);
                    submitButton.setText("Submit");
                    
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(UploadReportActivity.this, "Report uploaded successfully! Doctor will be notified.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorMsg = "Failed to upload report";
                        if (response.body() != null && response.body().getError() != null) {
                            errorMsg = response.body().getError().getMessage();
                        }
                        Toast.makeText(UploadReportActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.Report>> call, Throwable t) {
                    submitButton.setEnabled(true);
                    submitButton.setText("Submit");
                    Toast.makeText(UploadReportActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error reading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}






