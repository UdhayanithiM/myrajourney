package com.example.myrajouney;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Spinner spinnerRole;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private SessionManager sessionManager;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Force full screen mode to remove black bars
        getWindow().getDecorView().setSystemUiVisibility(
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN |
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        
        // Apply theme
        ThemeManager.applyTheme(this);
        
        setContentView(R.layout.activity_login);

        // Initialize session manager
        sessionManager = new SessionManager(this);
        loadingDialog = new LoadingDialog(this);

        // Check if already logged in
        if (sessionManager.isLoggedIn() && sessionManager.isSessionValid()) {
            redirectToDashboard(sessionManager.getRole());
            return;
        }

        initializeViews();
        setupClickListeners();
    }
    
    private void initializeViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Setup dropdown values
        String[] roles = {"Select Role", "Doctor", "Patient", "Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        spinnerRole.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        // Login click
        btnLogin.setOnClickListener(v -> performLogin());

        // Forgot password
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }
    
    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        // Validation
        String validationError = validateLoginInput(username, password, role);
        if (validationError != null) {
            Toast.makeText(LoginActivity.this, validationError, Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        loadingDialog.show("Logging in...");

        // Simulate authentication (replace with real authentication)
        simulateAuthentication(username, password, role);
    }
    
    private String validateLoginInput(String username, String password, String role) {
        if (TextUtils.isEmpty(username)) {
            return "Please enter username";
        }
        if (TextUtils.isEmpty(password)) {
            return "Please enter password";
        }
        if (role.equals("Select Role")) {
            return "Please select a role";
        }
        if (!ValidationUtils.isValidPassword(password)) {
            return "Password must be at least 6 characters";
        }
        return null;
    }
    
    private void simulateAuthentication(String username, String password, String role) {
        // Simulate network delay
        new android.os.Handler().postDelayed(() -> {
            loadingDialog.dismiss();
            
            // Simple authentication logic (replace with real authentication)
            if (authenticateUser(username, password, role)) {
                // Create session
                String userId = generateUserId(username, role);
                sessionManager.createSession(userId, username, role);
                
                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                redirectToDashboard(role);
            } else {
                Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        }, 1500);
    }
    
    private boolean authenticateUser(String username, String password, String role) {
        // Simple authentication logic (replace with real authentication)
        // For demo purposes, accept any username/password combination
        return !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password);
    }
    
    private String generateUserId(String username, String role) {
        // Generate a simple user ID
        return role.toUpperCase() + "_" + username.hashCode();
    }
    
    private void redirectToDashboard(String role) {
        Intent intent;
        switch (role) {
            case "Doctor":
                intent = new Intent(LoginActivity.this, DoctorDashboardActivity.class);
                break;
            case "Patient":
                intent = new Intent(LoginActivity.this, PatientDashboardActivity.class);
                break;
            case "Admin":
                intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                break;
            default:
                Toast.makeText(this, "Invalid role", Toast.LENGTH_SHORT).show();
                return;
        }
        startActivity(intent);
        finish();
    }
}
