package com.example.myrajouney;

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

        // Send data back to ReportList
        Intent resultIntent = new Intent();
        resultIntent.putExtra("name", name);
        resultIntent.putExtra("date", date);
        resultIntent.putExtra("fileUri", selectedFileUri.toString());
        setResult(RESULT_OK, resultIntent);
        finish(); // Close UploadReportActivity

        // Optional: Toast for confirmation
        Toast.makeText(this, "Report submitted successfully!", Toast.LENGTH_LONG).show();
    }
}
