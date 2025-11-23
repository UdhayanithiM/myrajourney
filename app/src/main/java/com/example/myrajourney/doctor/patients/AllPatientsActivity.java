package com.example.myrajourney.doctor.patients;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.admin.users.CreatePatientActivity;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.TokenManager;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Patient;
import com.example.myrajourney.data.model.User;
import com.example.myrajourney.doctor.appointments.DoctorScheduleActivity;
import com.example.myrajourney.doctor.reports.DoctorReportsActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllPatientsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchBar;
    private ProgressBar progressBar;
    private LinearLayout emptyView;
    private ExtendedFloatingActionButton fabAddPatient;

    private List<Patient> patientList, filteredList;
    private PatientsAdapter adapter;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_patients);

        initViews();
        setupNavigation();

        // Initialize List
        patientList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new PatientsAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load Data
        loadPatientsFromBackend();

        // Search Logic
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void initViews() {
        recyclerView = findViewById(R.id.all_patients_recycler);
        searchBar = findViewById(R.id.search_bar);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.empty_view);
        fabAddPatient = findViewById(R.id.fab_add_patient);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        fabAddPatient.setOnClickListener(v -> startActivity(new Intent(this, CreatePatientActivity.class)));
    }

    private void loadPatientsFromBackend() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        ApiService apiService = ApiClient.getApiService(this);

        // ✅ CORRECTED: Use getAllPatients() to fetch assigned patients
        // This maps to GET /api/v1/patients which is allowed for Doctors
        Call<ApiResponse<List<User>>> call = apiService.getAllPatients();

        call.enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Response<ApiResponse<List<User>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<User> users = response.body().getData();
                    patientList.clear();

                    if (users != null) {
                        for (User user : users) {
                            if ("PATIENT".equalsIgnoreCase(user.getRole())) {
                                String age = (user.getAge() != null && !user.getAge().isEmpty()) ? user.getAge() : "N/A";

                                Patient p = new Patient(
                                        user.getId(),
                                        user.getName(),
                                        user.getEmail(),
                                        age
                                );
                                patientList.add(p);
                            }
                        }
                    }

                    filteredList.clear();
                    filteredList.addAll(patientList);
                    adapter.notifyDataSetChanged();
                    updateEmptyView();
                } else {
                    String errorMsg = "Failed to load patients";
                    if (response.code() == 403) errorMsg = "Access Denied: Unauthorized";
                    Toast.makeText(AllPatientsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    updateEmptyView();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AllPatientsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyView();
            }
        });
    }

    private void updateEmptyView() {
        if (filteredList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void filter(String query) {
        filteredList.clear();
        String lowerQuery = query.toLowerCase();
        for (Patient p : patientList) {
            if (p.getName().toLowerCase().contains(lowerQuery) ||
                    (p.getEmail() != null && p.getEmail().toLowerCase().contains(lowerQuery))) {
                filteredList.add(p);
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void setupNavigation() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_add_patient) startActivity(new Intent(this, CreatePatientActivity.class));
            else if (id == R.id.nav_schedule) startActivity(new Intent(this, DoctorScheduleActivity.class));
            else if (id == R.id.nav_reports) startActivity(new Intent(this, DoctorReportsActivity.class));
            else if (id == R.id.nav_logout) {
                TokenManager.getInstance(this).clearSession();
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START);
                else finish();
            }
        });
    }
}