package com.example.myrajourney.doctor.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.navigation.NavigationView;
import com.example.myrajourney.R;
import com.example.myrajourney.auth.LoginActivity;
import com.example.myrajourney.admin.users.CreatePatientActivity;
import com.example.myrajourney.doctor.appointments.DoctorScheduleActivity;
import com.example.myrajourney.doctor.reports.DoctorReportsActivity;
import com.example.myrajourney.common.appointments.AddAppointmentActivity;
import com.example.myrajourney.admin.logs.AllNotificationsActivity;
import com.example.myrajourney.doctor.patients.AllPatientsActivity;
import com.example.myrajourney.admin.logs.NotificationBadgeUpdater;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.data.model.Patient;
import com.example.myrajourney.admin.logs.NotificationsAdapter;
import com.example.myrajourney.doctor.patients.PatientsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DoctorDashboardActivity extends AppCompatActivity {

    RecyclerView notificationsRecycler, patientsRecycler;

    List<com.example.myrajourney.data.model.Notification> notificationsList;
    List<Patient> patientsList;

    private TextView totalPatientsText, activePatientsText, aiInsightsText, aiInsightsTitle;
    private ProgressBar insightsProgress;
    private CardView aiInsightsCard;
    private View btnNewPatient, btnSchedule, btnReports, btnAddAppointment;
    private ImageView logoutBtn, menuIcon, notificationsBtn;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private View bottomDashboard, bottomPatients, bottomSchedule, bottomReports;

    private SessionManager sessionManager;

    private int totalPatients = 0;
    private int activePatientsToday = 0;

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
        loadData();
        setupNotifications();
        setupPatientsList();
        generateAIInsights();
        setupBackHandler();

        // ⭐ ADDED: initialize drawer header + menu actions
        setupNavigationDrawer();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuIcon = findViewById(R.id.menu_icon);
        logoutBtn = findViewById(R.id.logoutBtn);
        notificationsBtn = findViewById(R.id.notificationsBtn);

        totalPatientsText = findViewById(R.id.totalPatients);
        activePatientsText = findViewById(R.id.activePatients);
        aiInsightsText = findViewById(R.id.aiInsightsText);
        aiInsightsTitle = findViewById(R.id.aiInsightsTitle);
        insightsProgress = findViewById(R.id.insightsProgress);
        aiInsightsCard = findViewById(R.id.aiInsightsCard);

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

        if (btnNewPatient != null) btnNewPatient.setOnClickListener(v ->
                startActivity(new Intent(DoctorDashboardActivity.this, CreatePatientActivity.class)));

        if (btnSchedule != null) btnSchedule.setOnClickListener(v -> {
            startActivity(new Intent(DoctorDashboardActivity.this, DoctorScheduleActivity.class));

            // ⭐ ADDED: update bottom nav status
            setBottomNavActive(bottomSchedule);
        });

        if (btnReports != null) btnReports.setOnClickListener(v -> {
            startActivity(new Intent(DoctorDashboardActivity.this, DoctorReportsActivity.class));
            setBottomNavActive(bottomReports);
        });

        if (btnAddAppointment != null) {
            btnAddAppointment.setOnClickListener(v -> {
                Intent intent = new Intent(DoctorDashboardActivity.this, AddAppointmentActivity.class);

                // ⭐ ADDED: pass doctor_id explicitly (helps with UI logic)
                intent.putExtra("context", "DOCTOR");
                intent.putExtra("doctor_id", sessionManager.getUserId());

                startActivity(intent);
            });
        }

        View viewAllNotifs = findViewById(R.id.view_all_notifications);
        if (viewAllNotifs != null)
            viewAllNotifs.setOnClickListener(v ->
                    startActivity(new Intent(DoctorDashboardActivity.this, AllNotificationsActivity.class)));

        if (notificationsBtn != null) {
            notificationsBtn.setOnClickListener(v ->
                    startActivity(new Intent(DoctorDashboardActivity.this, AllNotificationsActivity.class)));
            TextView badge = findViewById(R.id.notifBadge);
            NotificationBadgeUpdater.update(this, badge);
        }

        View viewAllPatients = findViewById(R.id.view_all_patients);
        if (viewAllPatients != null) {
            viewAllPatients.setOnClickListener(v ->
                    startActivity(new Intent(DoctorDashboardActivity.this, AllPatientsActivity.class)));
        }

        if (bottomDashboard != null) {
            bottomDashboard.setOnClickListener(v -> {
                findViewById(R.id.mainScroll).scrollTo(0, 0);
                setBottomNavActive(bottomDashboard);
            });
        }

        if (bottomPatients != null) {
            bottomPatients.setOnClickListener(v -> {
                View patientsAnchor = findViewById(R.id.patients_recycler);
                if (patientsAnchor != null) {
                    patientsAnchor.getParent().requestChildFocus(patientsAnchor, patientsAnchor);
                    NestedScrollView scroll = findViewById(R.id.mainScroll);
                    scroll.smoothScrollTo(0, patientsAnchor.getTop());
                }
                setBottomNavActive(bottomPatients);
            });
        }

        if (bottomSchedule != null) {
            bottomSchedule.setOnClickListener(v -> {
                startActivity(new Intent(DoctorDashboardActivity.this, DoctorScheduleActivity.class));
                setBottomNavActive(bottomSchedule);
            });
        }

        if (bottomReports != null) {
            bottomReports.setOnClickListener(v -> {
                startActivity(new Intent(DoctorDashboardActivity.this, DoctorReportsActivity.class));
                setBottomNavActive(bottomReports);
            });
        }
    }

    private void setBottomNavActive(View active) {
        View[] items = new View[]{bottomDashboard, bottomPatients, bottomSchedule, bottomReports};
        for (View it : items) {
            if (it == null) continue;
            it.setSelected(it == active);
            it.setElevation(it == active ? 8f : 0f);
        }
    }

    private void loadData() {
        loadStatisticsFromBackend();
    }

    private void loadStatisticsFromBackend() {
        com.example.myrajourney.core.network.ApiService apiService =
                com.example.myrajourney.core.network.ApiClient.getApiService(this);

        retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.DoctorOverview>> call =
                apiService.getDoctorOverview();

        call.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.DoctorOverview>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.DoctorOverview>> call,
                                   retrofit2.Response<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.DoctorOverview>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    com.example.myrajourney.data.model.DoctorOverview overview = response.body().getData();
                    if (overview != null) {
                        totalPatients = overview.getPatientsCount() != null ? overview.getPatientsCount() : 0;
                        activePatientsToday = overview.getTodaySchedule() != null ? overview.getTodaySchedule().size() : 0;
                        updateStatistics();
                    }
                } else {
                    totalPatients = 0;
                    activePatientsToday = 0;
                    updateStatistics();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.DoctorOverview>> call,
                                  Throwable t) {
                totalPatients = 0;
                activePatientsToday = 0;
                updateStatistics();
            }
        });
    }

    private void updateStatistics() {
        if (totalPatientsText != null) totalPatientsText.setText(String.valueOf(totalPatients));
        if (activePatientsText != null) activePatientsText.setText(String.valueOf(activePatientsToday));
    }

    private void setupNotifications() {
        notificationsRecycler = findViewById(R.id.notifications_recycler);
        notificationsList = new ArrayList<>();
        loadNotificationsFromBackend();
    }

    private void loadNotificationsFromBackend() {
        com.example.myrajourney.core.network.ApiService apiService =
                com.example.myrajourney.core.network.ApiClient.getApiService(this);

        retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.Notification>>> call =
                apiService.getNotifications(1, 5, true);

        call.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.Notification>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.Notification>>> call,
                                   retrofit2.Response<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.Notification>>> response) {

                List<com.example.myrajourney.data.model.Notification> notifications = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    notifications = response.body().getData();
                    if (notifications == null) notifications = new ArrayList<>();
                }

                if (notificationsRecycler != null) {
                    NotificationsAdapter notificationsAdapter =
                            new NotificationsAdapter(DoctorDashboardActivity.this, notifications);

                    notificationsRecycler.setLayoutManager(
                            new LinearLayoutManager(DoctorDashboardActivity.this, LinearLayoutManager.VERTICAL, false)
                    );
                    notificationsRecycler.setAdapter(notificationsAdapter);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.Notification>>> call,
                                  Throwable t) {
                if (notificationsRecycler != null) {
                    NotificationsAdapter notificationsAdapter =
                            new NotificationsAdapter(DoctorDashboardActivity.this, new ArrayList<>());

                    notificationsRecycler.setLayoutManager(
                            new LinearLayoutManager(DoctorDashboardActivity.this, LinearLayoutManager.VERTICAL, false)
                    );
                    notificationsRecycler.setAdapter(notificationsAdapter);
                }
            }
        });
    }

    private void setupPatientsList() {
        patientsRecycler = findViewById(R.id.patients_recycler);
        patientsList = new ArrayList<>();
        loadPatientsFromBackend();
    }

    private void loadPatientsFromBackend() {
        com.example.myrajourney.core.network.ApiService apiService =
                com.example.myrajourney.core.network.ApiClient.getApiService(this);

        retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.User>>> call =
                apiService.getAllPatients();

        call.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.User>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.User>>> call,
                                   retrofit2.Response<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.User>>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<com.example.myrajourney.data.model.User> users = response.body().getData();

                    if (users != null) {
                        patientsList.clear();

                        for (com.example.myrajourney.data.model.User user : users) {
                            if ("PATIENT".equals(user.getRole())) {

                                String name = user.getName() != null ? user.getName() : "Patient";
                                String email = user.getEmail() != null ? user.getEmail() : "";
                                String age = user.getAge() != null ? user.getAge() : "N/A";

                                Patient p = new Patient(
                                        user.getId(),
                                        name,
                                        email,
                                        age
                                );
                                p.setImageResId(R.drawable.ic_person_default);

                                patientsList.add(p);
                            }
                        }

                        if (patientsRecycler != null) {
                            PatientsAdapter patientsAdapter =
                                    new PatientsAdapter(DoctorDashboardActivity.this, patientsList);

                            patientsRecycler.setLayoutManager(new LinearLayoutManager(DoctorDashboardActivity.this));
                            patientsRecycler.setAdapter(patientsAdapter);
                        }
                    }
                } else {
                    setEmptyPatientList();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<List<com.example.myrajourney.data.model.User>>> call,
                                  Throwable t) {
                setEmptyPatientList();
            }
        });
    }

    private void setEmptyPatientList() {
        if (patientsRecycler != null) {
            PatientsAdapter patientsAdapter =
                    new PatientsAdapter(DoctorDashboardActivity.this, new ArrayList<>());

            patientsRecycler.setLayoutManager(new LinearLayoutManager(DoctorDashboardActivity.this));
            patientsRecycler.setAdapter(patientsAdapter);
        }
    }

    private void generateAIInsights() {
        if (aiInsightsText != null) aiInsightsText.setText("Analyzing patient data for insights...");
        if (insightsProgress != null) insightsProgress.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
            String[] possibleInsights = {
                    "Trend: 20% increase in joint pain reports this week.",
                    "Pattern: Patients with morning stiffness improve with exercise.",
                    "Alert: 3 patients show medication non-adherence.",
                    "Insight: PT adherence reduces flare-ups by 40%.",
                    "Recommendation: Adjust medication for high inflammation."
            };

            String insight = possibleInsights[new Random().nextInt(possibleInsights.length)];

            runOnUiThread(() -> {
                if (aiInsightsText != null) aiInsightsText.setText(insight);
                if (insightsProgress != null) insightsProgress.setVisibility(View.GONE);

                if (aiInsightsCard != null) {
                    aiInsightsCard.setOnClickListener(v -> {
                        if (aiInsightsText != null) aiInsightsText.setText("Refreshing...");
                        if (insightsProgress != null) insightsProgress.setVisibility(View.VISIBLE);

                        new Handler().postDelayed(() -> {
                            String newInsight = possibleInsights[new Random().nextInt(possibleInsights.length)];
                            if (aiInsightsText != null) aiInsightsText.setText(newInsight);
                            if (insightsProgress != null) insightsProgress.setVisibility(View.GONE);
                        }, 1500);
                    });
                }
            });
        }, 2000);
    }

    // ⭐ ADDED: drawer setup call was missing
    private void setupNavigationDrawer() {
        if (navigationView == null) return;

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

        navigationView.setNavigationItemSelectedListener(item -> {
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

            if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
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
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finishAffinity();
                }
            }
        });
    }
}
