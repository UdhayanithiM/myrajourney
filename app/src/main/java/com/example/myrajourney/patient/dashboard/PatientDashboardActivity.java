package com.example.myrajourney.patient.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.common.messaging.ChatAdapter;
import com.example.myrajourney.common.messaging.ChatBot;
import com.example.myrajourney.common.messaging.ChatMessage;
import com.example.myrajourney.core.navigation.NavigationManager;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.Appointment;

import com.example.myrajourney.patient.appointments.PatientAppointmentsActivity;
import com.example.myrajourney.patient.education.EducationHubActivity;
import com.example.myrajourney.patient.medications.PatientMedicationsActivity;
import com.example.myrajourney.patient.rehab.PatientRehabilitationActivity;
import com.example.myrajourney.patient.reports.ReportList;
import com.example.myrajourney.patient.symptoms.SymptomLogActivity;
import com.example.myrajourney.doctor.dashboard.HealthStatsActivity;
import com.example.myrajourney.admin.logs.AllNotificationsActivity;
import com.example.myrajourney.admin.dashboard.SettingsActivity;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class PatientDashboardActivity extends AppCompatActivity {

    private Button yesBtn, noBtn;
    private ImageView menuIcon, logoutBtn;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private PatientDashboardViewModel viewModel;

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private ChatBot chatBot;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard_new);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isSessionValid()) {
            NavigationManager.goToLogin(this);
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(PatientDashboardViewModel.class);

        initializeViews();
        initializeChat();
        setupClickListeners();
        observeViewModel();
    }

    // ⭐ ADDED — refresh appointments when user returns
    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshAppointments();
    }

    private void observeViewModel() {

        viewModel.getCurrentUser().observe(this, name -> {
            TextView welcomeText = findViewById(R.id.welcomeText);
            if (welcomeText != null) {
                welcomeText.setText(name != null ? "Welcome, " + name + "!" : "Welcome Back!");
            }
        });

        viewModel.getHealthMetrics().observe(this, metrics -> {
            // Future: update health cards
        });

        viewModel.getUpcomingAppointments().observe(this, this::updateAppointmentCards);

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }

    private void updateAppointmentCards(List<Appointment> appointments) {

        TextView consultationTitle = findViewById(R.id.consultationTitle);
        TextView consultationDate = findViewById(R.id.consultationDate);
        TextView followupTitle = findViewById(R.id.followupTitle);
        TextView followupDate = findViewById(R.id.followupDate);
        View consultationCard = findViewById(R.id.consultationCard);
        View followupCard = findViewById(R.id.followupCard);

        if (appointments == null || appointments.isEmpty()) {
            if (consultationCard != null) consultationCard.setVisibility(View.GONE);
            if (followupCard != null) followupCard.setVisibility(View.GONE);
            return;
        }

        List<Appointment> upcoming = new ArrayList<>(appointments);

        // ⭐ FIXED — use formatted fields
        if (!upcoming.isEmpty() && consultationTitle != null) {
            Appointment first = upcoming.get(0);

            consultationTitle.setText(first.getTitle() != null ? first.getTitle() : "Appointment");

            // ⭐ FIXED — correct date/time output
            consultationDate.setText(
                    first.getFormattedDate() + "  " + first.getFormattedTimeSlot()
            );

            if (consultationCard != null) consultationCard.setVisibility(View.VISIBLE);
        }

        if (upcoming.size() > 1 && followupTitle != null) {
            Appointment second = upcoming.get(1);

            followupTitle.setText(second.getTitle() != null ? second.getTitle() : "Appointment");

            // ⭐ FIXED
            followupDate.setText(
                    second.getFormattedDate() + "  " + second.getFormattedTimeSlot()
            );

            if (followupCard != null) followupCard.setVisibility(View.VISIBLE);
        }
    }

    private void initializeViews() {
        yesBtn = findViewById(R.id.yesBtn);
        noBtn = findViewById(R.id.noBtn);
        menuIcon = findViewById(R.id.menu_icon);
        logoutBtn = findViewById(R.id.logoutBtn);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        setupNavigationDrawer();
    }

    private void initializeChat() {
        chatBot = new ChatBot(this);
        chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage("Hello! I'm your RA assistant. How can I help you today?", false));

        chatAdapter = new ChatAdapter(this, chatMessages);
    }

    private void setupClickListeners() {

        if (yesBtn != null) {
            yesBtn.setOnClickListener(v -> {
                viewModel.setTaskCompleted(true);
                Toast.makeText(this, "Great! Task completed ✔", Toast.LENGTH_SHORT).show();
            });
        }

        if (noBtn != null) {
            noBtn.setOnClickListener(v ->
                    Toast.makeText(this, "Please complete your task ❌", Toast.LENGTH_SHORT).show());
        }

        View chatCard = findViewById(R.id.chatCard);
        if (chatCard != null) chatCard.setOnClickListener(v -> showChatDialog());

        View healthCard = findViewById(R.id.healthStatsCard);
        if (healthCard != null) healthCard.setOnClickListener(v ->
                startActivity(new Intent(this, HealthStatsActivity.class)));

        View consultationBtn = findViewById(R.id.consultationDetailsBtn);
        if (consultationBtn != null)
            consultationBtn.setOnClickListener(v ->
                    startActivity(new Intent(this, PatientAppointmentsActivity.class)));

        View followupBtn = findViewById(R.id.followupDetailsBtn);
        if (followupBtn != null)
            followupBtn.setOnClickListener(v ->
                    startActivity(new Intent(this, PatientAppointmentsActivity.class)));

        if (menuIcon != null) {
            menuIcon.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START))
                        drawerLayout.closeDrawer(GravityCompat.START);
                    else drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        if (logoutBtn != null) logoutBtn.setOnClickListener(v -> logout());

        setupQuickActions();
    }

    private void setupQuickActions() {

        View symptomBtn = findViewById(R.id.symptomLogBtn);
        if (symptomBtn != null)
            symptomBtn.setOnClickListener(v ->
                    startActivity(new Intent(this, SymptomLogActivity.class)));

        View medsBtn = findViewById(R.id.medicationsBtn);
        if (medsBtn != null)
            medsBtn.setOnClickListener(v ->
                    startActivity(new Intent(this, PatientMedicationsActivity.class)));

        View rehabBtn = findViewById(R.id.rehabBtn);
        if (rehabBtn != null)
            rehabBtn.setOnClickListener(v ->
                    startActivity(new Intent(this, PatientRehabilitationActivity.class)));
    }

    private void showChatDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_chat, null);

        chatRecyclerView = dialogView.findViewById(R.id.chatRecyclerView);
        EditText messageInput = dialogView.findViewById(R.id.messageInput);
        Button sendBtn = dialogView.findViewById(R.id.sendBtn);
        Button closeBtn = dialogView.findViewById(R.id.closeBtn);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        AlertDialog dialog = builder.setView(dialogView).create();

        sendBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                addChatMessage(message, true);
                String response = chatBot.getResponse(message);
                addChatMessage(response, false);
                messageInput.setText("");
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            }
        });

        closeBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        if (chatMessages.size() > 0)
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
    }

    private void addChatMessage(String message, boolean fromUser) {
        chatMessages.add(new ChatMessage(message, fromUser));
        if (chatAdapter != null) chatAdapter.notifyItemInserted(chatMessages.size() - 1);
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    sessionManager.logout();
                    NavigationManager.goToLogin(this);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void setupNavigationDrawer() {

        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.navHeaderPatientName);
        TextView navHeaderEmail = headerView.findViewById(R.id.navHeaderPatientEmail);

        String userName = sessionManager.getUserName();
        String userEmail = sessionManager.getUserEmail();

        if (navHeaderName != null && userName != null)
            navHeaderName.setText(userName);

        if (navHeaderEmail != null && userEmail != null)
            navHeaderEmail.setText(userEmail);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else if (id == R.id.nav_medications) {
                startActivity(new Intent(this, PatientMedicationsActivity.class));
            } else if (id == R.id.nav_rehab) {
                startActivity(new Intent(this, PatientRehabilitationActivity.class));
            } else if (id == R.id.nav_education) {
                startActivity(new Intent(this, EducationHubActivity.class));
            } else if (id == R.id.nav_appointments) {
                startActivity(new Intent(this, PatientAppointmentsActivity.class));
            } else if (id == R.id.nav_symptoms) {
                startActivity(new Intent(this, SymptomLogActivity.class));
            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(this, ReportList.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.nav_dark_theme) {
                ThemeManager.toggleTheme(this);
                recreate();
            } else if (id == R.id.nav_logout) {
                logout();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
}
