package com.example.myrajourney.doctor.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.admin.logs.AllNotificationsActivity;
import com.example.myrajourney.admin.logs.NotificationBadgeUpdater;
import com.example.myrajourney.admin.logs.NotificationsAdapter;
import com.example.myrajourney.admin.users.CreatePatientActivity;
import com.example.myrajourney.auth.LoginActivity;
import com.example.myrajourney.common.appointments.AddAppointmentActivity;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.Patient;
import com.example.myrajourney.doctor.appointments.DoctorScheduleActivity;
import com.example.myrajourney.doctor.patients.AllPatientsActivity;
import com.example.myrajourney.doctor.patients.PatientsAdapter;
import com.example.myrajourney.doctor.reports.DoctorReportsActivity;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class DoctorDashboardActivity extends AppCompatActivity {

    RecyclerView notificationsRecycler, patientsRecycler;

    List<com.example.myrajourney.data.model.Notification> notificationsList;
    List<Patient> patientsList;

    private View btnNewPatient, btnSchedule, btnReports, btnAddAppointment;
    private ImageView logoutBtn, menuIcon, notificationsBtn;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private View bottomDashboard, bottomPatients, bottomSchedule, bottomReports;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn() || !sessionManager.isSessionValid()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        setupNotifications();
        setupPatientsList();
        setupBackHandler();
        setupNavigationDrawer(); // drawer init
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuIcon = findViewById(R.id.menu_icon);
        logoutBtn = findViewById(R.id.logoutBtn);
        notificationsBtn = findViewById(R.id.notificationsBtn);

        btnNewPatient = findViewById(R.id.btnNewPatient);
        btnSchedule = findViewById(R.id.btnSchedule);
        btnReports = findViewById(R.id.btnReports);
        btnAddAppointment = findViewById(R.id.btnAddAppointment);

        notificationsRecycler = findViewById(R.id.notifications_recycler);
        patientsRecycler = findViewById(R.id.patients_recycler);

        bottomDashboard = findViewById(R.id.bottom_dashboard);
        bottomPatients = findViewById(R.id.bottom_patients);
        bottomSchedule = findViewById(R.id.bottom_schedule);
        bottomReports = findViewById(R.id.bottom_reports);
    }

    private void setupClickListeners() {

        if (menuIcon != null) {
            menuIcon.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
            });
        }

        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> showLogoutDialog());
        }

        if (btnNewPatient != null)
            btnNewPatient.setOnClickListener(v ->
                    startActivity(new Intent(this, CreatePatientActivity.class)));

        if (btnSchedule != null)
            btnSchedule.setOnClickListener(v -> {
                startActivity(new Intent(this, DoctorScheduleActivity.class));
                setBottomNavActive(bottomSchedule);
            });

        if (btnReports != null)
            btnReports.setOnClickListener(v -> {
                startActivity(new Intent(this, DoctorReportsActivity.class));
                setBottomNavActive(bottomReports);
            });

        if (btnAddAppointment != null)
            btnAddAppointment.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddAppointmentActivity.class);
                intent.putExtra("context", "DOCTOR");
                intent.putExtra("doctor_id", sessionManager.getUserId());
                startActivity(intent);
            });

        View viewAllNotifs = findViewById(R.id.view_all_notifications);
        if (viewAllNotifs != null)
            viewAllNotifs.setOnClickListener(v ->
                    startActivity(new Intent(this, AllNotificationsActivity.class)));

        if (notificationsBtn != null) {
            notificationsBtn.setOnClickListener(v ->
                    startActivity(new Intent(this, AllNotificationsActivity.class)));
            TextView badge = findViewById(R.id.notifBadge);
            NotificationBadgeUpdater.update(this, badge);
        }

        View viewAllPatients = findViewById(R.id.view_all_patients);
        if (viewAllPatients != null)
            viewAllPatients.setOnClickListener(v ->
                    startActivity(new Intent(this, AllPatientsActivity.class)));

        if (bottomDashboard != null)
            bottomDashboard.setOnClickListener(v -> {
                findViewById(R.id.mainScroll).scrollTo(0, 0);
                setBottomNavActive(bottomDashboard);
            });

        if (bottomPatients != null)
            bottomPatients.setOnClickListener(v -> {
                RecyclerView patientsAnchor = findViewById(R.id.patients_recycler);
                if (patientsAnchor != null) {
                    NestedScrollView scroll = findViewById(R.id.mainScroll);
                    scroll.smoothScrollTo(0, patientsAnchor.getTop());
                }
                setBottomNavActive(bottomPatients);
            });

        if (bottomSchedule != null)
            bottomSchedule.setOnClickListener(v -> {
                startActivity(new Intent(this, DoctorScheduleActivity.class));
                setBottomNavActive(bottomSchedule);
            });

        if (bottomReports != null)
            bottomReports.setOnClickListener(v -> {
                startActivity(new Intent(this, DoctorReportsActivity.class));
                setBottomNavActive(bottomReports);
            });
    }

    private void setBottomNavActive(View active) {
        View[] items = new View[]{bottomDashboard, bottomPatients, bottomSchedule, bottomReports};
        for (View it : items) {
            if (it == null) continue;
            it.setSelected(it == active);
            it.setElevation(it == active ? 8f : 0f);
        }
    }

    private void setupNotifications() {
        notificationsList = new ArrayList<>();

        com.example.myrajourney.core.network.ApiService apiService =
                com.example.myrajourney.core.network.ApiClient.getApiService(this);

        apiService.getNotifications(1, 5, true)
                .enqueue(new retrofit2.Callback<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.Notification>>>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.Notification>>> call,
                                           retrofit2.Response<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.Notification>>> response) {

                        List<com.example.myrajourney.data.model.Notification> data = new ArrayList<>();

                        if (response.isSuccessful() &&
                                response.body() != null &&
                                response.body().isSuccess()) {
                            data = response.body().getData();
                            if (data == null) data = new ArrayList<>();
                        }

                        NotificationsAdapter adapter =
                                new NotificationsAdapter(DoctorDashboardActivity.this, data);

                        notificationsRecycler.setLayoutManager(
                                new LinearLayoutManager(DoctorDashboardActivity.this)
                        );
                        notificationsRecycler.setAdapter(adapter);
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.Notification>>> call,
                                          Throwable t) {

                        notificationsRecycler.setLayoutManager(
                                new LinearLayoutManager(DoctorDashboardActivity.this)
                        );
                        notificationsRecycler.setAdapter(
                                new NotificationsAdapter(DoctorDashboardActivity.this, new ArrayList<>())
                        );
                    }
                });
    }

    private void setupPatientsList() {
        patientsList = new ArrayList<>();

        com.example.myrajourney.core.network.ApiService apiService =
                com.example.myrajourney.core.network.ApiClient.getApiService(this);

        apiService.getAllPatients()
                .enqueue(new retrofit2.Callback<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.User>>>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.User>>> call,
                                           retrofit2.Response<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.User>>> response) {

                        List<com.example.myrajourney.data.model.User> users = new ArrayList<>();

                        if (response.isSuccessful() &&
                                response.body() != null &&
                                response.body().isSuccess()) {
                            users = response.body().getData();
                            if (users == null) users = new ArrayList<>();
                        }

                        patientsList.clear();

                        for (com.example.myrajourney.data.model.User user : users) {
                            if ("PATIENT".equals(user.getRole())) {
                                Patient p = new Patient(
                                        user.getId(),
                                        user.getName() != null ? user.getName() : "Patient",
                                        user.getEmail() != null ? user.getEmail() : "",
                                        user.getAge() != null ? user.getAge() : "N/A"
                                );
                                p.setImageResId(R.drawable.ic_person_default);
                                patientsList.add(p);
                            }
                        }

                        PatientsAdapter adapter =
                                new PatientsAdapter(DoctorDashboardActivity.this, patientsList);

                        patientsRecycler.setLayoutManager(
                                new LinearLayoutManager(DoctorDashboardActivity.this)
                        );
                        patientsRecycler.setAdapter(adapter);
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.User>>> call,
                                          Throwable t) {

                        patientsRecycler.setLayoutManager(
                                new LinearLayoutManager(DoctorDashboardActivity.this)
                        );
                        patientsRecycler.setAdapter(
                                new PatientsAdapter(DoctorDashboardActivity.this, new ArrayList<>())
                        );
                    }
                });
    }

    private void setupNavigationDrawer() {
        if (navigationView == null) return;

        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.navHeaderDoctorName);
        TextView navHeaderEmail = headerView.findViewById(R.id.navHeaderDoctorEmail);

        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        if (navHeaderName != null && name != null) navHeaderName.setText("Dr. " + name);
        if (navHeaderEmail != null && email != null) navHeaderEmail.setText(email);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_add_patient) {
                startActivity(new Intent(this, CreatePatientActivity.class));

            } else if (id == R.id.nav_all_patients) {
                startActivity(new Intent(this, AllPatientsActivity.class));

            } else if (id == R.id.nav_schedule) {
                startActivity(new Intent(this, DoctorScheduleActivity.class));

            } else if (id == R.id.nav_add_appointment) {
                startActivity(new Intent(this, AddAppointmentActivity.class));

            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(this, DoctorReportsActivity.class));

            } else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();

            } else if (id == R.id.nav_dark_theme) {
                ThemeManager.toggleTheme(this);
                recreate();

            } else if (id == R.id.nav_logout) {
                showLogoutDialog();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
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

    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (drawerLayout != null
                                && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            finishAffinity();
                        }
                    }
                });
    }
}
