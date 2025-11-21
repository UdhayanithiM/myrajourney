package com.example.myrajourney.doctor.reports;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorReportsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText searchBar;
    ImageView backButton;
    ImageView addReportButton;
    ImageView settingsButton;
    List<Report> reports, filteredList;
    ReportsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_reports);

        // Initialize views
        recyclerView = findViewById(R.id.reports_recycler);
        searchBar = findViewById(R.id.search_bar);
        backButton = findViewById(R.id.back_button);
        addReportButton = findViewById(R.id.add_report_button);
        settingsButton = findViewById(R.id.settings_button);

        // Set click listeners
        backButton.setOnClickListener(v -> finish());

        // Handle Add Report button click
        if (addReportButton != null) {
            addReportButton.setOnClickListener(v -> {
                startActivity(new Intent(this, UploadReportActivity.class));
            });
        }

        // Handle Settings button click
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                startActivity(new Intent(this, SettingsActivity.class));
            });
        }

        // Initialize lists
        reports = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new ReportsAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // Set click listener to open report details
        adapter.setOnReportClickListener(report -> {
            Intent intent = new Intent(DoctorReportsActivity.this, ReportDetailsActivity.class);
            intent.putExtra("patient_name", report.getPatientName());
            intent.putExtra("report_type", report.getReportType());
            intent.putExtra("report_date", report.getDate());
            intent.putExtra("report_status", report.getStatus());
            intent.putExtra("report_id", report.getId());
            startActivity(intent);
        });

        // Load data from backend
        loadReportsFromBackend();

        // Set up search functionality
        setupSearch();
    }

    private void loadReportsFromBackend() {
        ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        Call<ApiResponse<List<com.example.myrajourney.data.model.Report>>> call = apiService.getReports();
        
        call.enqueue(new Callback<ApiResponse<List<com.example.myrajourney.data.model.Report>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<com.example.myrajourney.data.model.Report>>> call, Response<ApiResponse<List<com.example.myrajourney.data.model.Report>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<com.example.myrajourney.data.model.Report> apiReports = response.body().getData();
                    if (apiReports != null) {
                        reports.clear();
                        // Convert API reports to local Report format
                        for (com.example.myrajourney.data.model.Report apiReport : apiReports) {
                            String reportId = apiReport.getId() != null ? apiReport.getId() : "";
                            String patientName = apiReport.getPatientName() != null ? apiReport.getPatientName() : "Unknown";
                            String title = apiReport.getTitle() != null ? apiReport.getTitle() : "Report";
                            String date = formatDate(apiReport.getUploadedAt());
                            String status = determineStatus(apiReport);
                            Report report = new Report(reportId, patientName, title, date, status);
                            reports.add(report);
                        }
                        filter(searchBar.getText().toString());
                    }
                } else {
                    // No reports or error - show empty list
                    reports.clear();
                    filter("");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<com.example.myrajourney.data.model.Report>>> call, Throwable t) {
                // On failure, show empty list
                reports.clear();
                filter("");
                android.util.Log.e("DoctorReports", "Failed to load reports", t);
            }
        });
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date());
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String determineStatus(com.example.myrajourney.data.model.Report apiReport) {
        // You can add logic here to determine status based on report data
        return "Normal";
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(reports);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Report r : reports) {
                if ((r.getPatientName() != null && r.getPatientName().toLowerCase().contains(lowerCaseQuery)) ||
                        (r.getReportType() != null && r.getReportType().toLowerCase().contains(lowerCaseQuery)) ||
                        (r.getStatus() != null && r.getStatus().toLowerCase().contains(lowerCaseQuery))) {
                    filteredList.add(r);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}






