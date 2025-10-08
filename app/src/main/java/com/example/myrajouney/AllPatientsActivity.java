package com.example.myrajouney;

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
        // Show recent 10 patients
        patientList.add(new Patient("John Doe", "45 | Rheumatoid Arthritis", R.drawable.ic_person_default));
        patientList.add(new Patient("Sarah Wilson", "38 | Osteoarthritis", R.drawable.ic_person_default));
        patientList.add(new Patient("Michael Brown", "52 | Gout", R.drawable.ic_person_default));
        patientList.add(new Patient("Emily Davis", "29 | Lupus", R.drawable.ic_person_default));
        patientList.add(new Patient("Robert Johnson", "61 | Rheumatoid Arthritis", R.drawable.ic_person_default));
        patientList.add(new Patient("Lisa Anderson", "34 | Psoriatic Arthritis", R.drawable.ic_person_default));
        patientList.add(new Patient("David Martinez", "47 | Ankylosing Spondylitis", R.drawable.ic_person_default));
        patientList.add(new Patient("Jennifer Taylor", "55 | Osteoarthritis", R.drawable.ic_person_default));
        patientList.add(new Patient("William Garcia", "42 | Rheumatoid Arthritis", R.drawable.ic_person_default));
        patientList.add(new Patient("Mary Rodriguez", "36 | Systemic Lupus", R.drawable.ic_person_default));

        filteredList = new ArrayList<>(patientList);

        adapter = new PatientsAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

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
