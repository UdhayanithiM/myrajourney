package com.example.myrajourney.patient.reports;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.TokenManager;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Report;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportList extends AppCompatActivity {

    RecyclerView recyclerView;
    ReportsAdapter adapter;
    List<Report> fullList = new ArrayList<>();
    List<Report> filteredList = new ArrayList<>();
    EditText searchBar;
    ImageButton addButton;

    ActivityResultLauncher<Intent> uploadLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list);

        recyclerView = findViewById(R.id.recyclerView);
        searchBar = findViewById(R.id.searchBar);
        addButton = findViewById(R.id.addButton);

        adapter = new ReportsAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Click → open details
        adapter.setOnReportClickListener(report -> {
            Intent i = new Intent(this, ReportDetailsActivity.class);
            i.putExtra("patient_name", "You");
            i.putExtra("report_type", report.getTitle());
            i.putExtra("report_date", report.getCreatedAt());
            i.putExtra("report_status", report.getStatus());
            i.putExtra("report_id", report.getId());
            i.putExtra("report_file", report.getFileUrl());
            startActivity(i);
        });

        // Launcher for uploading
        uploadLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadReports(); // refresh from backend
                    }
                }
        );

        addButton.setOnClickListener(v -> {
            Intent i = new Intent(this, UploadReportActivity.class);
            uploadLauncher.launch(i);
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                filter(s.toString());
            }
        });

        loadReports();
    }

    private void loadReports() {
        TokenManager tm = TokenManager.getInstance(this);
        String patientId = tm.getUserId();

        if (patientId == null) return;

        ApiService api = ApiClient.getApiService(this);

        api.getReports().enqueue(new Callback<ApiResponse<List<Report>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Report>>> call,
                                   Response<ApiResponse<List<Report>>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                    List<Report> list = response.body().getData();

                    // filter only this patient’s reports
                    fullList.clear();
                    for (Report r : list) {
                        if (String.valueOf(r.getPatientId()).equals(patientId)) {
                            fullList.add(r);
                        }
                    }

                    filter(searchBar.getText().toString());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Report>>> call, Throwable t) {}
        });
    }

    private void filter(String query) {
        filteredList.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            String q = query.toLowerCase();

            for (Report r : fullList) {
                if (r.getTitle().toLowerCase().contains(q) ||
                        r.getCreatedAt().toLowerCase().contains(q)) {
                    filteredList.add(r);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}
