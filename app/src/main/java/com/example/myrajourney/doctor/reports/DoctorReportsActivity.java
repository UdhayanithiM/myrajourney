package com.example.myrajourney.doctor.reports;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Report;
import com.example.myrajourney.patient.reports.ReportsAdapter;
import com.example.myrajourney.patient.reports.ReportDetailsActivity;
import com.example.myrajourney.patient.reports.UploadReportActivity;
import com.example.myrajourney.admin.dashboard.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorReportsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText searchBar;
    ImageView backButton, addReportButton, settingsButton;

    List<Report> reportList = new ArrayList<>();
    List<Report> filteredList = new ArrayList<>();

    ReportsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_reports);

        recyclerView = findViewById(R.id.reports_recycler);
        searchBar = findViewById(R.id.search_bar);
        backButton = findViewById(R.id.back_button);
        addReportButton = findViewById(R.id.add_report_button);
        settingsButton = findViewById(R.id.settings_button);

        backButton.setOnClickListener(v -> finish());
        addReportButton.setOnClickListener(v -> startActivity(new Intent(this, UploadReportActivity.class)));
        settingsButton.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        adapter = new ReportsAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnReportClickListener(report -> {
            Intent i = new Intent(this, ReportDetailsActivity.class);

            i.putExtra("report_id", report.getId());
            i.putExtra("patient_id", report.getPatientId());
            i.putExtra("patient_name", report.getPatientName());
            i.putExtra("report_type", report.getTitle());
            i.putExtra("report_date", report.getCreatedAt());
            i.putExtra("report_file", report.getFileUrl());
            i.putExtra("report_status", report.getStatus());

            // ⭐ DOCTOR MODE
            i.putExtra("mode", "doctor");

            startActivity(i);
        });

        loadReports();
        setupSearch();
    }

    private void loadReports() {

        ApiService api = ApiClient.getApiService(this);
        Call<ApiResponse<List<Report>>> call = api.getReports();

        call.enqueue(new Callback<ApiResponse<List<Report>>>() {

            @Override
            public void onResponse(Call<ApiResponse<List<Report>>> call, Response<ApiResponse<List<Report>>> resp) {

                if (!resp.isSuccessful() || resp.body() == null || !resp.body().isSuccess()) {
                    reportList.clear();
                    filteredList.clear();
                    adapter.notifyDataSetChanged();
                    return;
                }

                reportList = resp.body().getData();
                if (reportList == null) reportList = new ArrayList<>();

                filter(searchBar.getText().toString());
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Report>>> call, Throwable t) {
                Log.e("DoctorReports", "Failed to load reports", t);
                reportList.clear();
                filteredList.clear();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
        });
    }

    private void filter(String keyword) {

        filteredList.clear();

        if (keyword == null || keyword.trim().isEmpty()) {
            filteredList.addAll(reportList);
        } else {

            String text = keyword.toLowerCase().trim();

            for (Report r : reportList) {
                boolean matchName = r.getPatientName() != null && r.getPatientName().toLowerCase().contains(text);
                boolean matchTitle = r.getTitle() != null && r.getTitle().toLowerCase().contains(text);
                boolean matchStatus = r.getStatus() != null && r.getStatus().toLowerCase().contains(text);

                if (matchName || matchTitle || matchStatus) {
                    filteredList.add(r);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}
