package com.example.myrajouney;

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
        
        findViewById(R.id.view_all_patients).setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDashboardActivity.this, AllPatientsActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadData() {
        // In a real app, this would load data from a database or API
        totalPatients = 42;  // Mock data
        activePatientsToday = 8;  // Mock data
        
        // Update UI with loaded data
        updateStatistics();
    }
    
    private void updateStatistics() {
        totalPatientsText.setText(String.valueOf(totalPatients));
        activePatientsText.setText(String.valueOf(activePatientsToday));
    }
    
    private void setupNotifications() {
        notificationsRecycler = findViewById(R.id.notifications_recycler);
        notificationsList = new ArrayList<>();
        // Add some sample notifications
        notificationsList.add("Patient John Doe's test results are ready");
        notificationsList.add("New message from Sarah Wilson");
        notificationsList.add("Appointment reminder: 2:30 PM with Michael Brown");
        notificationsList.add("3 new patient records added");
        
        NotificationsAdapter notificationsAdapter = new NotificationsAdapter(this, notificationsList);
        LinearLayoutManager notifLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        notificationsRecycler.setLayoutManager(notifLayoutManager);
        notificationsRecycler.setAdapter(notificationsAdapter);
    }
    
    private void setupPatientsList() {
        patientsRecycler = findViewById(R.id.patients_recycler);
        patientsList = new ArrayList<>();
        
        // Add sample patients
        patientsList.add(new Patient("John Doe", "45 | Rheumatoid Arthritis", R.drawable.ic_person_default));
        patientsList.add(new Patient("Sarah Wilson", "38 | Osteoarthritis", R.drawable.ic_person_default));
        patientsList.add(new Patient("Michael Brown", "52 | Gout", R.drawable.ic_person_default));
        patientsList.add(new Patient("Emily Davis", "29 | Lupus", R.drawable.ic_person_default));
        
        PatientsAdapter patientsAdapter = new PatientsAdapter(this, patientsList);
        LinearLayoutManager patientsLayoutManager = new LinearLayoutManager(this);
        patientsRecycler.setLayoutManager(patientsLayoutManager);
        patientsRecycler.setAdapter(patientsAdapter);
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
