package com.example.myrajourney.doctor.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.data.model.Patient;
import com.example.myrajourney.doctor.patients.AllPatientsActivity; // If referenced

public class DoctorDashboardActivity extends AppCompatActivity {

    // RecyclerViews
    RecyclerView notificationsRecycler, patientsRecycler;

    // Lists
    List<String> notificationsList;
    List<Patient> patientsList;

    // UI Components
    private TextView totalPatientsText, activePatientsText, aiInsightsText, aiInsightsTitle;
    private ProgressBar insightsProgress;
    private CardView aiInsightsCard;
    private Button btnNewPatient, btnSchedule, btnReports, btnAddAppointment;
    private ImageView logoutBtn, menuIcon;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    
    // Session Manager
    private SessionManager sessionManager;
    
    // Mock data
    private int totalPatients = 0;
    private int activePatientsToday = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content view
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);
        
        // Initialize session manager
        sessionManager = new SessionManager(this);
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn() || !sessionManager.isSessionValid()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        // Initialize views
        initializeViews();
        
        // Set up click listeners
        setupClickListeners();
        
        // Load data
        loadData();
        
        // Initialize notifications and patients
        setupNotifications();
        setupPatientsList();
        
        // Generate AI insights (simulated)
        generateAIInsights();
    }
    
    private void initializeViews() {
        // Initialize session manager and check login
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn() || !sessionManager.isSessionValid()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        // Initialize UI components
        logoutBtn = findViewById(R.id.logoutBtn);
        menuIcon = findViewById(R.id.menu_icon);
        totalPatientsText = findViewById(R.id.totalPatients);
        activePatientsText = findViewById(R.id.activePatients);
        aiInsightsText = findViewById(R.id.aiInsightsText);
        aiInsightsTitle = findViewById(R.id.aiInsightsTitle);
        insightsProgress = findViewById(R.id.insightsProgress);
        aiInsightsCard = findViewById(R.id.aiInsightsCard);
        
        // Display doctor's name
        TextView welcomeDoctorText = findViewById(R.id.welcomeDoctorText);
        if (welcomeDoctorText != null) {
            String userName = sessionManager.getUserName();
            if (userName != null && !userName.isEmpty()) {
                welcomeDoctorText.setText("Welcome, Dr. " + userName + "!");
            } else {
                welcomeDoctorText.setText("Welcome, Doctor!");
            }
        }
        
        // Initialize buttons
        btnNewPatient = findViewById(R.id.btnNewPatient);
        btnSchedule = findViewById(R.id.btnSchedule);
        btnReports = findViewById(R.id.btnReports);
        btnAddAppointment = findViewById(R.id.btnAddAppointment);
        
        // Initialize drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        setupNavigationDrawer();
    }
    
    private void setupClickListeners() {
        // Menu button
        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        
        // Logout button
        logoutBtn.setOnClickListener(v -> showLogoutDialog());
        
        // Quick action buttons
        btnNewPatient.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDashboardActivity.this, CreatePatientActivity.class);
            startActivity(intent);
        });
        
        btnSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDashboardActivity.this, DoctorScheduleActivity.class);
            startActivity(intent);
        });
        
        btnReports.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDashboardActivity.this, DoctorReportsActivity.class);
            startActivity(intent);
        });

        if (btnAddAppointment != null) {
            btnAddAppointment.setOnClickListener(v -> {
                Intent intent = new Intent(DoctorDashboardActivity.this, AddAppointmentActivity.class);
                startActivity(intent);
            });
        }
        
        // View all buttons
        findViewById(R.id.view_all_notifications).setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDashboardActivity.this, AllNotificationsActivity.class);
            startActivity(intent);
        });

        View notifBtn = findViewById(R.id.notificationsBtn);
        if (notifBtn != null) {
            notifBtn.setOnClickListener(v -> startActivity(new Intent(this, AllNotificationsActivity.class)));
            TextView badge = findViewById(R.id.notifBadge);
            NotificationBadgeUpdater.update(this, badge);
        }
        
        findViewById(R.id.view_all_patients).setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDashboardActivity.this, AllPatientsActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadData() {
        // Load data from backend API
        loadStatisticsFromBackend();
    }
    
    private void loadStatisticsFromBackend() {
        com.example.myrajourney.core.network.ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.DoctorOverview>> call = apiService.getDoctorOverview();
        
        call.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model
.DoctorOverview>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.DoctorOverview>> call, retrofit2.Response<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.DoctorOverview>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    com.example.myrajourney.data.model
.DoctorOverview overview = response.body().getData();
                    if (overview != null) {
                        totalPatients = overview.getPatientsCount() != null ? overview.getPatientsCount() : 0;
                        activePatientsToday = overview.getTodaySchedule() != null ? overview.getTodaySchedule().size() : 0;
                        updateStatistics();
                    }
                } else {
                    // On error, show 0
                    totalPatients = 0;
                    activePatientsToday = 0;
                    updateStatistics();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.DoctorOverview>> call, Throwable t) {
                // On failure, show 0
                totalPatients = 0;
                activePatientsToday = 0;
                updateStatistics();
            }
        });
    }
    
    private void updateStatistics() {
        totalPatientsText.setText(String.valueOf(totalPatients));
        activePatientsText.setText(String.valueOf(activePatientsToday));
    }
    
    private void setupNotifications() {
        notificationsRecycler = findViewById(R.id.notifications_recycler);
        notificationsList = new ArrayList<>();
        
        // Load notifications from API
        loadNotificationsFromBackend();
    }
    
    private void loadNotificationsFromBackend() {
        com.example.myrajourney.core.network.ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.Notification>>> call = apiService.getNotifications(1, 5, true);
        
        call.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.Notification>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.Notification>>> call, 
                                 retrofit2.Response<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.Notification>>> response) {
                List<com.example.myrajourney.data.model
.Notification> notifications = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    notifications = response.body().getData();
                    if (notifications == null) notifications = new ArrayList<>();
                }
                
                NotificationsAdapter notificationsAdapter = new NotificationsAdapter(DoctorDashboardActivity.this, notifications);
                LinearLayoutManager notifLayoutManager = new LinearLayoutManager(DoctorDashboardActivity.this, LinearLayoutManager.VERTICAL, false);
                notificationsRecycler.setLayoutManager(notifLayoutManager);
                notificationsRecycler.setAdapter(notificationsAdapter);
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.Notification>>> call, Throwable t) {
                // On failure, show empty list
                NotificationsAdapter notificationsAdapter = new NotificationsAdapter(DoctorDashboardActivity.this, new ArrayList<>());
                LinearLayoutManager notifLayoutManager = new LinearLayoutManager(DoctorDashboardActivity.this, LinearLayoutManager.VERTICAL, false);
                notificationsRecycler.setLayoutManager(notifLayoutManager);
                notificationsRecycler.setAdapter(notificationsAdapter);
            }
        });
    }
    
    private void setupPatientsList() {
        patientsRecycler = findViewById(R.id.patients_recycler);
        patientsList = new ArrayList<>();
        
        // Load patients from backend API - no default values
        loadPatientsFromBackend();
    }
    
    private void loadPatientsFromBackend() {
        // Load all patients from patients API (not just from appointments)
        com.example.myrajourney.core.network.ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.User>>> call = apiService.getAllPatients();
        
        call.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.User>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.User>>> call, retrofit2.Response<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.User>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<com.example.myrajourney.data.model
.User> users = response.body().getData();
                    if (users != null) {
                        patientsList.clear();
                        for (com.example.myrajourney.data.model
.User user : users) {
                            if (user.getRole() != null && user.getRole().equals("PATIENT")) {
                                String name = user.getName() != null ? user.getName() : "Patient";
                                String details = user.getEmail() != null ? user.getEmail() : "";
                                patientsList.add(new Patient(name, details, R.drawable.ic_person_default));
                            }
                        }
                        
                        PatientsAdapter patientsAdapter = new PatientsAdapter(DoctorDashboardActivity.this, patientsList);
                        LinearLayoutManager patientsLayoutManager = new LinearLayoutManager(DoctorDashboardActivity.this);
                        patientsRecycler.setLayoutManager(patientsLayoutManager);
                        patientsRecycler.setAdapter(patientsAdapter);
                    }
                } else {
                    // On error, show empty list
                    PatientsAdapter patientsAdapter = new PatientsAdapter(DoctorDashboardActivity.this, patientsList);
                    LinearLayoutManager patientsLayoutManager = new LinearLayoutManager(DoctorDashboardActivity.this);
                    patientsRecycler.setLayoutManager(patientsLayoutManager);
                    patientsRecycler.setAdapter(patientsAdapter);
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<List<com.example.myrajourney.data.model
.User>>> call, Throwable t) {
                // On failure, show empty list
                PatientsAdapter patientsAdapter = new PatientsAdapter(DoctorDashboardActivity.this, patientsList);
                LinearLayoutManager patientsLayoutManager = new LinearLayoutManager(DoctorDashboardActivity.this);
                patientsRecycler.setLayoutManager(patientsLayoutManager);
                patientsRecycler.setAdapter(patientsAdapter);
            }
        });
    }
    
    private void generateAIInsights() {
        // Show loading state
        aiInsightsText.setText("Analyzing patient data for insights...");
        insightsProgress.setVisibility(View.VISIBLE);
        
        // Simulate AI processing delay
        new Handler().postDelayed(() -> {
            // Simulate AI analysis (in a real app, this would call an ML model)
            String[] possibleInsights = {
                "Trend: 20% increase in joint pain reports this week. Consider reviewing pain management strategies.",
                "Pattern: Patients with morning stiffness show 30% better outcomes with early exercise.",
                "Alert: 3 patients show signs of medication non-adherence. Follow-up recommended.",
                "Insight: Patients who engage in regular physical therapy have 40% fewer flare-ups.",
                "Recommendation: Consider adjusting medication for patients with persistent inflammation markers."
            };
            
            // Select a random insight
            String insight = possibleInsights[new Random().nextInt(possibleInsights.length)];
            
            // Update UI with the insight
            runOnUiThread(() -> {
                aiInsightsText.setText(insight);
                insightsProgress.setVisibility(View.GONE);
                
                // Set a click listener to refresh insights
                aiInsightsCard.setOnClickListener(v -> {
                    aiInsightsText.setText("Analyzing patient data for new insights...");
                    insightsProgress.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(() -> {
                        String newInsight = possibleInsights[new Random().nextInt(possibleInsights.length)];
                        aiInsightsText.setText(newInsight);
                        insightsProgress.setVisibility(View.GONE);
                    }, 1500);
                });
            });
        }, 2000);
    }
    
    private void setupNavigationDrawer() {
        // Update navigation header with user info
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.navHeaderDoctorName);
        TextView navHeaderEmail = headerView.findViewById(R.id.navHeaderDoctorEmail);
        
        String userName = sessionManager.getUserName();
        String userEmail = sessionManager.getUserEmail();
        
        if (navHeaderName != null && userName != null && !userName.isEmpty()) {
            navHeaderName.setText("Dr. " + userName);
        }
        if (navHeaderEmail != null && userEmail != null && !userEmail.isEmpty()) {
            navHeaderEmail.setText(userEmail);
        }
        
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                
                if (id == R.id.nav_add_patient) {
                    startActivity(new Intent(DoctorDashboardActivity.this, CreatePatientActivity.class));
                } else if (id == R.id.nav_all_patients) {
                    startActivity(new Intent(DoctorDashboardActivity.this, AllPatientsActivity.class));
                } else if (id == R.id.nav_schedule) {
                    startActivity(new Intent(DoctorDashboardActivity.this, DoctorScheduleActivity.class));
                } else if (id == R.id.nav_add_appointment) {
                    startActivity(new Intent(DoctorDashboardActivity.this, AddAppointmentActivity.class));
                } else if (id == R.id.nav_reports) {
                    startActivity(new Intent(DoctorDashboardActivity.this, DoctorReportsActivity.class));
                } else if (id == R.id.nav_settings) {
                    Toast.makeText(DoctorDashboardActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_dark_theme) {
                    ThemeManager.toggleTheme(DoctorDashboardActivity.this);
                    recreate();
                } else if (id == R.id.nav_logout) {
                    showLogoutDialog();
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
    
    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    sessionManager.logout(DoctorDashboardActivity.this);
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}






