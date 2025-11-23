package com.example.myrajourney.admin.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private ImageView iconMenu, logoutBtn;

    private LinearLayout navUpdatePatient, navUpdateDoctor, navSettings;
    private Button btnCreatePatient, btnCreateDoctor, btnAssignPatients, btnViewAllPatients, btnViewAllDoctors;

    private SearchView searchBar;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new SessionManager(this);

        // Validate login
        if (!sessionManager.isLoggedIn() || !sessionManager.isSessionValid()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupDrawer();
        setupButtons();
        setupBackPressHandler();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        iconMenu = findViewById(R.id.iconMenu);
        logoutBtn = findViewById(R.id.logoutBtn);

        btnCreatePatient = findViewById(R.id.btnCreatePatient);
        btnCreateDoctor = findViewById(R.id.btnCreateDoctor);
        btnAssignPatients = findViewById(R.id.btnAssignPatients);
        btnViewAllPatients = findViewById(R.id.btnViewAllPatients);
        btnViewAllDoctors = findViewById(R.id.btnViewAllDoctors);

        navUpdatePatient = findViewById(R.id.navUpdatePatient);
        navUpdateDoctor = findViewById(R.id.navUpdateDoctor);
        navSettings = findViewById(R.id.navSettings);

        searchBar = findViewById(R.id.searchBar); // FIXED — correct SearchView class
    }

    private void setupDrawer() {

        // Open drawer
        if (iconMenu != null) {
            iconMenu.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        // Handle drawer menu
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }

        // Logout
        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> showLogoutDialog());
        }
    }

    private void setupButtons() {

        if (btnCreatePatient != null)
            btnCreatePatient.setOnClickListener(v ->
                    startActivity(new Intent(this, CreatePatientActivity.class))
            );

        if (btnCreateDoctor != null)
            btnCreateDoctor.setOnClickListener(v ->
                    startActivity(new Intent(this, CreateDoctorActivity.class))
            );

        if (btnAssignPatients != null)
            btnAssignPatients.setOnClickListener(v ->
                    startActivity(new Intent(this, AssignPatientToDoctorActivity.class))
            );

        if (btnViewAllPatients != null)
            btnViewAllPatients.setOnClickListener(v ->
                    startActivity(new Intent(this, AllPatientsActivity.class))
            );

        if (btnViewAllDoctors != null)
            btnViewAllDoctors.setOnClickListener(v ->
                    Toast.makeText(this, "View All Doctors - Coming Soon", Toast.LENGTH_SHORT).show()
            );

        if (navUpdatePatient != null)
            navUpdatePatient.setOnClickListener(v ->
                    startActivity(new Intent(this, EditPatientActivity.class))
            );

        if (navUpdateDoctor != null)
            navUpdateDoctor.setOnClickListener(v ->
                    startActivity(new Intent(this, EditDoctorActivity.class))
            );

        if (navSettings != null)
            navSettings.setOnClickListener(v ->
                    startActivity(new Intent(this, SettingsActivity.class))
            );
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        // Close drawer first
                        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                            return;
                        }

                        // Clean exit without returning to login
                        finishAffinity();
                    }
                }
        );
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
