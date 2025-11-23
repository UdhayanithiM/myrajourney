package com.example.myrajourney.doctor.meds;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View; // ✅ REQUIRED IMPORT (fixes all errors)
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Medication;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorAssignMedicationActivity extends AppCompatActivity {

    private EditText searchBox, etDosage, etFrequencyPerDay, etNameOverride;
    private TextView tvSelected, tvStartDate, tvEndDate;
    private RecyclerView rvResults;
    private ProgressBar progress;
    private Button btnAssign;

    private MedSearchAdapter adapter;
    private List<Medication> results = new ArrayList<>();

    private Medication selectedMedication = null;
    private int patientId = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_assign_medication);

        patientId = getIntent().getIntExtra("patient_id", 0);

        initViews();
        setupSearchDebounce();
        setupDatePickers();

        btnAssign.setOnClickListener(v -> assignMedication());
        searchMedications("");
    }

    private void initViews() {
        searchBox = findViewById(R.id.search_box);
        rvResults = findViewById(R.id.rv_med_results);
        progress = findViewById(R.id.progress);

        tvSelected = findViewById(R.id.tv_selected_med);
        etDosage = findViewById(R.id.et_dosage);
        etFrequencyPerDay = findViewById(R.id.et_frequency_per_day);
        etNameOverride = findViewById(R.id.et_name_override);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);
        btnAssign = findViewById(R.id.btn_assign);

        adapter = new MedSearchAdapter(this, results, med -> {
            selectedMedication = med;
            tvSelected.setText("Selected: " + med.getName());
            etNameOverride.setText("");
        });

        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(adapter);
    }

    private void setupSearchDebounce() {
        Handler handler = new Handler();
        final Runnable[] last = new Runnable[1];

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (last[0] != null) handler.removeCallbacks(last[0]);
                last[0] = () -> searchMedications(s.toString().trim());
                handler.postDelayed(last[0], 300);
            }
        });
    }

    private void setupDatePickers() {
        tvStartDate.setOnClickListener(v -> pickDate(tvStartDate));
        tvEndDate.setOnClickListener(v -> pickDate(tvEndDate));
    }

    private void pickDate(TextView target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, day);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    target.setText(sdf.format(cal.getTime()));
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void searchMedications(String q) {
        progress.setVisibility(View.VISIBLE);

        ApiService api = ApiClient.getApiService(this);
        Call<ApiResponse<List<Medication>>> call = api.searchMedications(q);

        call.enqueue(new Callback<ApiResponse<List<Medication>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Medication>>> call,
                                   Response<ApiResponse<List<Medication>>> response) {

                progress.setVisibility(View.GONE);
                results.clear();

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (response.body().getData() != null)
                        results.addAll(response.body().getData());
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Medication>>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(DoctorAssignMedicationActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void assignMedication() {
        if (patientId <= 0) {
            Toast.makeText(this, "Missing patient ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer medicationId = selectedMedication != null
                ? parseIntOrNull(selectedMedication.getId())
                : null;

        String nameOverride = etNameOverride.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String freq = etFrequencyPerDay.getText().toString().trim();
        String start = tvStartDate.getText().toString().trim();
        String end = tvEndDate.getText().toString().trim();

        if (dosage.isEmpty()) {
            Toast.makeText(this, "Enter dosage", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("patient_id", patientId);
        body.put("medication_id", medicationId);
        body.put("name_override", nameOverride.isEmpty() ? null : nameOverride);
        body.put("dosage", dosage);
        body.put("frequency_per_day", freq.isEmpty() ? null : freq);
        body.put("start_date", start.isEmpty() ? null : start);
        body.put("end_date", end.isEmpty() ? null : end);

        btnAssign.setEnabled(false);
        progress.setVisibility(View.VISIBLE);

        ApiService api = ApiClient.getApiService(this);
        Call<ApiResponse<Map<String, Object>>> call = api.doctorAssignMedication(body);

        call.enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                                   Response<ApiResponse<Map<String, Object>>> response) {

                progress.setVisibility(View.GONE);
                btnAssign.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(DoctorAssignMedicationActivity.this,
                            "Medication Assigned!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(DoctorAssignMedicationActivity.this,
                            "Assign failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                btnAssign.setEnabled(true);
                Toast.makeText(DoctorAssignMedicationActivity.this,
                        "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Integer parseIntOrNull(String s) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return null; }
    }
}
