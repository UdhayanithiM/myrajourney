package com.example.myrajourney.admin.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.myrajourney.R;
import com.example.myrajourney.admin.assignments.AssignPatientToDoctorActivity;
import com.example.myrajourney.admin.users.CreateDoctorActivity;
import com.example.myrajourney.admin.users.CreatePatientActivity;
import com.example.myrajourney.admin.users.EditDoctorActivity;
import com.example.myrajourney.admin.users.EditPatientActivity;
import com.example.myrajourney.auth.LoginActivity;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.doctor.patients.AllPatientsActivity;
import com.google.android.material.navigation.NavigationView;

public class AdminDashboardActivity extends AppCompatActivity {

    // Views
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView iconMenu;
    private LinearLayout navUpdatePatient, navUpdateDoctor, navSettings;
    private Button btnCreatePatient, btnCreateDoctor, btnAssignPatients, btnViewAllPatients, btnViewAllDoctors;
    private SearchView searchBar;
    private ImageView logoutBtn;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new SessionManager(this);

        // Check login
        if (!sessionManager.isLoggedIn() || !sessionManager.isSessionValid()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize Drawer
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        iconMenu = findViewById(R.id.iconMenu);

        // Initialize Other Views
        btnCreatePatient = findViewById(R.id.btnCreatePatient);
        btnCreateDoctor = findViewById(R.id.btnCreateDoctor);
        btnAssignPatients = findViewById(R.id.btnAssignPatients);
        btnViewAllPatients = findViewById(R.id.btnViewAllPatients);
        btnViewAllDoctors = findViewById(R.id.btnViewAllDoctors);
        navUpdatePatient = findViewById(R.id.navUpdatePatient);
        navUpdateDoctor = findViewById(R.id.navUpdateDoctor);
        navSettings = findViewById(R.id.navSettings);
        searchBar = findViewById(R.id.searchBar);
        logoutBtn = findViewById(R.id.logoutBtn);

        // --- DRAWER LOGIC ---
        // Open drawer when menu icon is clicked
        iconMenu.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Handle Navigation Drawer Item Clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Set up Logout
        logoutBtn.setOnClickListener(v -> showLogoutDialog());

        // Set up Buttons
        setupButtons();

        // --- BACK PRESS DISPATCHER LOGIC ---
        // This replaces the deprecated onBackPressed()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If drawer is open, close it
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // CRITICAL FIX: Prevent going back to Login Activity
                    // finishAffinity() closes the entire app stack, exiting the app.
                    // This ensures the user doesn't accidentally land back on the login screen.
                    finishAffinity();
                }
            }
        });
    }

    private void setupButtons() {
        // Create Patient
        btnCreatePatient.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, CreatePatientActivity.class))
        );

        // Create Doctor
        btnCreateDoctor.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, CreateDoctorActivity.class))
        );

        // Assign Patients
        if (btnAssignPatients != null) {
            btnAssignPatients.setOnClickListener(v ->
                    startActivity(new Intent(AdminDashboardActivity.this, AssignPatientToDoctorActivity.class))
            );
        }

        // View Patients
        if (btnViewAllPatients != null) {
            btnViewAllPatients.setOnClickListener(v ->
                    startActivity(new Intent(AdminDashboardActivity.this, AllPatientsActivity.class))
            );
        }

        // View Doctors
        if (btnViewAllDoctors != null) {
            btnViewAllDoctors.setOnClickListener(v -> {
                Toast.makeText(AdminDashboardActivity.this, "View All Doctors - Coming Soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Footer Nav
        navUpdatePatient.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, EditPatientActivity.class))
        );

        navUpdateDoctor.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, EditDoctorActivity.class))
        );

        navSettings.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, SettingsActivity.class))
        );
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    sessionManager.logout();
                    // Clear task ensures we can't go back to AdminDashboard after logout
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}