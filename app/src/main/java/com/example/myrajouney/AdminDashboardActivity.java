package com.example.myrajouney;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    // Footer navigation buttons
    private LinearLayout navUpdatePatient, navUpdateDoctor, navSettings;

    // Top buttons
    private Button btnCreatePatient, btnCreateDoctor;

    // Search bar
    private SearchView searchBar;
    
    // Logout button
    private ImageView logoutBtn;
    
    // Session Manager
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content view
        ThemeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        
        // Initialize session manager
        sessionManager = new SessionManager(this);
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn() || !sessionManager.isSessionValid()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize views
        btnCreatePatient = findViewById(R.id.btnCreatePatient);
        btnCreateDoctor = findViewById(R.id.btnCreateDoctor);
        navUpdatePatient = findViewById(R.id.navUpdatePatient);
        navUpdateDoctor = findViewById(R.id.navUpdateDoctor);
        navSettings = findViewById(R.id.navSettings);
        searchBar = findViewById(R.id.searchBar);
        logoutBtn = findViewById(R.id.logoutBtn);
        
        // Set up logout listener

        logoutBtn.setOnClickListener(v -> showLogoutDialog());

        // --------- SearchView Listener (dummy for now) ---------
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // You can implement search logic here
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // You can implement live search logic here
                return false;
            }
        });

        // --------- Top buttons: Create Patient / Doctor ---------
        btnCreatePatient.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, CreatePatientActivity.class))
        );

        btnCreateDoctor.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, CreateDoctorActivity.class))
        );

        // --------- Footer navigation: Update Patient / Doctor / Settings ---------
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
                    sessionManager.logout(AdminDashboardActivity.this);
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
