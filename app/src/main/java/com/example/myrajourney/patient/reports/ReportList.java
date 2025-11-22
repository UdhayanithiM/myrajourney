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

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.Report;
// ---------------------

import java.util.ArrayList;
import java.util.List;

public class ReportList extends AppCompatActivity {

    RecyclerView recyclerView;
    ReportAdapter adapter;
    ArrayList<Report> reportList;
    EditText searchBar;
    ImageButton addButton;

    // ActivityResultLauncher for UploadReportActivity
    private ActivityResultLauncher<Intent> uploadReportLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list);

        recyclerView = findViewById(R.id.recyclerView);
        searchBar = findViewById(R.id.searchBar);
        addButton = findViewById(R.id.addButton);

        reportList = new ArrayList<>();
        // Initialize with some sample data or load from API if needed
        // reportList.add(new Report("Sample Report", "2023-10-01", ""));

        adapter = new ReportAdapter(this, reportList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initialize ActivityResultLauncher
        uploadReportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        // Ensure these keys match what UploadReportActivity sends back
                        String name = data.getStringExtra("name");
                        String date = data.getStringExtra("date");
                        String fileUri = data.getStringExtra("fileUri");

                        reportList.add(new Report(name, date, fileUri));
                        adapter.notifyDataSetChanged();
                    }
                }
        );

        // Launch UploadReportActivity using the launcher
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReportList.this, UploadReportActivity.class);
            uploadReportLauncher.launch(intent);
        });

        // Search filter
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
        });
    }

    private void filter(String text) {
        List<Report> filteredList = new ArrayList<>();
        for (Report report : reportList) {
            // Safe null check
            String name = report.getName() != null ? report.getName() : "";
            String date = report.getDate() != null ? report.getDate() : "";

            if (name.toLowerCase().contains(text.toLowerCase()) ||
                    date.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(report);
            }
        }
        adapter.filterList(filteredList);
    }
}