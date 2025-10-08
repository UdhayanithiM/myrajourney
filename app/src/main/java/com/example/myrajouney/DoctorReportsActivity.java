package com.example.myrajouney;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast; // Import Toast for placeholder action
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DoctorReportsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText searchBar;
    ImageView backButton;
    ImageView addReportButton;
    ImageView settingsButton; // <-- Add this
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
        settingsButton = findViewById(R.id.settings_button); // <-- Add this

        // Set click listeners
        backButton.setOnClickListener(v -> finish());

        // Handle Add Report button click
        if (addReportButton != null) {
            addReportButton.setOnClickListener(v -> {
                startActivity(new Intent(this, UploadReportActivity.class));
            });
        }

        // --- RECTIFIED: Handle Settings button click ---
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                // TODO: Replace with your actual SettingsActivity
                // For now, we'll show a Toast message as a placeholder.
                // Intent settingsIntent = new Intent(this, SettingsActivity.class);
                // startActivity(settingsIntent);
                Toast.makeText(this, "Settings clicked!", Toast.LENGTH_SHORT).show();
            });
        }

        // Load data and set up RecyclerView
        setupRecyclerView();

        // Set up search functionality
        setupSearch();
    }

    private void setupRecyclerView() {
        reports = new ArrayList<>();
        // Add sample reports
        reports.add(new Report("John Doe", "Blood Test Results", "Dec 5, 2024", "Normal"));
        reports.add(new Report("Sarah Wilson", "X-Ray Report", "Dec 4, 2024", "Review Required"));
        reports.add(new Report("Michael Brown", "MRI Scan", "Dec 3, 2024", "Normal"));
        reports.add(new Report("Emily Davis", "Lab Results", "Dec 2, 2024", "Abnormal"));
        reports.add(new Report("Robert Johnson", "Ultrasound", "Dec 1, 2024", "Normal"));
        reports.add(new Report("Lisa Anderson", "CT Scan", "Nov 30, 2024", "Normal"));
        reports.add(new Report("David Martinez", "Blood Test Results", "Nov 29, 2024", "Review Required"));
        reports.add(new Report("Jennifer Taylor", "Bone Density Scan", "Nov 28, 2024", "Normal"));
        reports.add(new Report("William Garcia", "Joint X-Ray", "Nov 27, 2024", "Abnormal"));
        reports.add(new Report("Mary Rodriguez", "Lab Panel", "Nov 26, 2024", "Normal"));

        filteredList = new ArrayList<>(reports);

        adapter = new ReportsAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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
                if (r.getPatientName().toLowerCase().contains(lowerCaseQuery) ||
                        r.getReportType().toLowerCase().contains(lowerCaseQuery) ||
                        r.getStatus().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(r);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
