package com.example.myrajouney;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajouney.api.ApiService;
import com.example.myrajouney.api.models.ApiResponse;
import com.example.myrajouney.api.models.PatientOverview;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientDashboardActivity extends AppCompatActivity {

    Button yesBtn, noBtn;
    ImageView menuIcon, logoutBtn;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    
    // Chat functionality
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private ChatBot chatBot;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard_new);

        // Initialize session manager
        sessionManager = new SessionManager(this);
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn() || !sessionManager.isSessionValid()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize Views
        initializeViews();
        
        // Initialize chat
        initializeChat();
        
        // Set up click listeners
        setupClickListeners();
        
        // Load patient overview data from API
        loadPatientOverview();
        
        // Add welcome message
        addWelcomeMessage();
    }
    
    private void loadPatientOverview() {
        ApiService apiService = com.example.myrajouney.api.ApiClient.getApiService(this);
        Call<ApiResponse<PatientOverview>> call = apiService.getPatientOverview();
        
        call.enqueue(new Callback<ApiResponse<PatientOverview>>() {
            @Override
            public void onResponse(Call<ApiResponse<PatientOverview>> call, Response<ApiResponse<PatientOverview>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    PatientOverview overview = response.body().getData();
                    if (overview != null) {
                        // Update UI with real data
                        updateDashboard(overview);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<PatientOverview>> call, Throwable t) {
                // Handle error silently - app will work with empty data
            }
        });
    }
    
    private void updateDashboard(PatientOverview overview) {
        // Update unread notifications count if there's a TextView for it
        // You can add UI updates here based on the overview data
        if (overview.getUnreadNotifications() > 0) {
            // Show notification badge or update text
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
        chatAdapter = new ChatAdapter(this, chatMessages);
        
        // Add initial bot message
        chatMessages.add(new ChatMessage("Hello! I'm your RA assistant. How can I help you today?", false));
    }
    
    private void setupClickListeners() {
        // Task buttons
        yesBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Great! Task completed ✅", Toast.LENGTH_SHORT).show();
            addChatMessage("Task completed successfully!", false);
        });

        noBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Please complete your task ❌", Toast.LENGTH_SHORT).show();
            addChatMessage("Remember to complete your daily tasks for better health management.", false);
        });

        // Chat button
        findViewById(R.id.chatCard).setOnClickListener(v -> showChatDialog());

        // Health stats button
        findViewById(R.id.healthStatsCard).setOnClickListener(v -> {
            startActivity(new Intent(this, HealthStatsActivity.class));
        });

        // Appointment cards - View Details
        View consultationBtn = findViewById(R.id.consultationDetailsBtn);
        if (consultationBtn != null) {
            consultationBtn.setOnClickListener(v -> {
                Intent i = new Intent(this, AppointmentDetailsActivity.class);
                i.putExtra("title", "Consultation");
                i.putExtra("datetime", "Dec 15, 2024 - 10:30 AM");
                i.putExtra("details", "General consultation regarding RA management.");
                startActivity(i);
            });
        }

        View followupBtn = findViewById(R.id.followupDetailsBtn);
        if (followupBtn != null) {
            followupBtn.setOnClickListener(v -> {
                Intent i = new Intent(this, AppointmentDetailsActivity.class);
                i.putExtra("title", "Follow-up");
                i.putExtra("datetime", "Dec 28, 2024 - 2:00 PM");
                i.putExtra("details", "Follow-up discussion to review progress and labs.");
                startActivity(i);
            });
        }


        // Menu button
        if (menuIcon != null) {
            menuIcon.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            });
        }

        // Ensure logout button triggers logout dialog
        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> logout());
        }

        // Bottom navigation buttons
        findViewById(R.id.symptomLogBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, SymptomLogActivity.class));
        });

        findViewById(R.id.medicationsBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, PatientMedicationsActivity.class));
        });

        findViewById(R.id.rehabBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, PatientRehabilitationActivity.class));
        });

        // Search
        // searchBar.setOnEditorActionListener((v, actionId, event) -> {
        //     String query = searchBar.getText().toString();
        //     if (!TextUtils.isEmpty(query)) {
        //         Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
        //         addChatMessage("I can help you search for information about: " + query, false);
        //     }
        //     return true;
        // });
    }
    
    private void addWelcomeMessage() {
        String username = sessionManager.getUsername();
        if (username != null) {
            addChatMessage("Welcome back, " + username + "! How are you feeling today?", false);
        }
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
        
        // Scroll to bottom
        if (chatMessages.size() > 0) {
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
        }
    }
    
    private void addChatMessage(String message, boolean fromUser) {
        chatMessages.add(new ChatMessage(message, fromUser));
        if (chatAdapter != null) {
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        }
    }
    
    private void showMenuDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Menu")
                .setItems(new String[]{"Profile", "Notifications", "Help", "Logout"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            showHelpDialog();
                            break;
                        case 3:
                            logout();
                            break;
                    }
                });
        builder.show();
    }
    
    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Help")
                .setMessage("This app helps you manage your Rheumatoid Arthritis journey. " +
                        "Use the chatbot for quick assistance, log your symptoms, " +
                        "and track your medications and exercises.")
                .setPositiveButton("OK", null)
                .show();
    }
    
    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    sessionManager.logout(PatientDashboardActivity.this);
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
    
    private void showLogoutDialog() {
        logout();
    }
    
    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                
                if (id == R.id.nav_dashboard) {
                    // Already on dashboard
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_medications) {
                    startActivity(new Intent(PatientDashboardActivity.this, PatientMedicationsActivity.class));
                } else if (id == R.id.nav_rehab) {
                    startActivity(new Intent(PatientDashboardActivity.this, PatientRehabilitationActivity.class));
                } else if (id == R.id.nav_education) {
                    startActivity(new Intent(PatientDashboardActivity.this, EducationHubActivity.class));
                } else if (id == R.id.nav_appointments) {
                    startActivity(new Intent(PatientDashboardActivity.this, PatientAppointmentsActivity.class));
                } else if (id == R.id.nav_symptoms) {
                    startActivity(new Intent(PatientDashboardActivity.this, SymptomLogActivity.class));
                } else if (id == R.id.nav_reports) {
                    startActivity(new Intent(PatientDashboardActivity.this, ReportList.class));
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(PatientDashboardActivity.this, SettingsActivity.class));
                } else if (id == R.id.nav_dark_theme) {
                    ThemeManager.toggleTheme(PatientDashboardActivity.this);
                    recreate();
                } else if (id == R.id.nav_logout) {
                    logout();
                }
                
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }
}
