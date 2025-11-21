package com.example.myrajouney.doctor.patients;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.List;

public class AllPatientsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText searchBar;
    List<Patient> patientList, filteredList;
    PatientsAdapter adapter;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView menuIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_patients);

        recyclerView = findViewById(R.id.all_patients_recycler);
        searchBar = findViewById(R.id.search_bar);
        menuIcon = findViewById(R.id.menu_icon);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        
        // Menu button
        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        
        // Back button
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        // Setup navigation drawer
        setupNavigationDrawer();

        patientList = new ArrayList<>();
        filteredList = new ArrayList<>();
        
        // Load patients from backend API - no default values
        loadPatientsFromBackend();

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

    private void loadPatientsFromBackend() {
        // Use direct API endpoint that works correctly with role-based filtering
        // NOTE: If network fails, update IP in res/values/network_config.xml and rebuild
        String baseIp = getString(R.string.api_base_ip);
        String url = "http://" + baseIp + "/myra-admin.php?action=users";
        
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder()
                .url(url)
                .get();
        
        // Add auth token
        TokenManager tokenManager = TokenManager.getInstance(this);
        String token = tokenManager.getToken();
        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }
        
        okhttp3.Request request = requestBuilder.build();
        
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(AllPatientsActivity.this, "Failed to load patients: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    filteredList = new ArrayList<>();
                    adapter = new PatientsAdapter(AllPatientsActivity.this, filteredList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(AllPatientsActivity.this));
                    recyclerView.setAdapter(adapter);
                });
            }
            
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(responseData);
                        org.json.JSONArray users = json.getJSONArray("data");
                        
                        patientList = new ArrayList<>();
                        for (int i = 0; i < users.length(); i++) {
                            org.json.JSONObject user = users.getJSONObject(i);
                            if ("PATIENT".equals(user.optString("role"))) {
                                String name = user.optString("name", "Patient");
                                String email = user.optString("email", "");
                                patientList.add(new Patient(name, email, R.drawable.ic_person_default));
                            }
                        }
                        
                        runOnUiThread(() -> {
                            filteredList = new ArrayList<>(patientList);
                            adapter = new PatientsAdapter(AllPatientsActivity.this, filteredList);
                            recyclerView.setLayoutManager(new LinearLayoutManager(AllPatientsActivity.this));
                            recyclerView.setAdapter(adapter);
                        });
                    } catch (org.json.JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(AllPatientsActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                            filteredList = new ArrayList<>();
                            adapter = new PatientsAdapter(AllPatientsActivity.this, filteredList);
                            recyclerView.setLayoutManager(new LinearLayoutManager(AllPatientsActivity.this));
                            recyclerView.setAdapter(adapter);
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(AllPatientsActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                        filteredList = new ArrayList<>();
                        adapter = new PatientsAdapter(AllPatientsActivity.this, filteredList);
                        recyclerView.setLayoutManager(new LinearLayoutManager(AllPatientsActivity.this));
                        recyclerView.setAdapter(adapter);
                    });
                }
            }
        });
    }
    
    private void filter(String query) {
        filteredList.clear();
        for (Patient p : patientList) {
            if (p.getName().toLowerCase().contains(query.toLowerCase()) ||
                    p.getDetails().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(p);
            }
        }
        adapter.notifyDataSetChanged();
    }
    
    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                
                if (id == R.id.nav_add_patient) {
                    startActivity(new Intent(AllPatientsActivity.this, CreatePatientActivity.class));
                } else if (id == R.id.nav_all_patients) {
                    // Already on this page
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_schedule) {
                    startActivity(new Intent(AllPatientsActivity.this, DoctorScheduleActivity.class));
                } else if (id == R.id.nav_reports) {
                    startActivity(new Intent(AllPatientsActivity.this, DoctorReportsActivity.class));
                } else if (id == R.id.nav_settings) {
                    Toast.makeText(AllPatientsActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_dark_theme) {
                    ThemeManager.toggleTheme(AllPatientsActivity.this);
                    recreate();
                } else if (id == R.id.nav_logout) {
                    finish();
                }
                
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
