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

import com.example.myrajouney.api.ApiService;
import com.example.myrajouney.api.models.ApiResponse;
import com.example.myrajouney.api.models.AuthRequest;
import com.example.myrajouney.api.models.AuthResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // Real API authentication
        performApiLogin(username, password, role);
    }
    
    private String validateLoginInput(String username, String password, String role) {
        if (TextUtils.isEmpty(username)) {
            return "Please enter email";
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
        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            return "Please enter a valid email address";
        }
        return null;
    }
    
    private void performApiLogin(String email, String password, String role) {
        // Create API service
        ApiService apiService = com.example.myrajouney.api.ApiClient.getApiService(this);
        
        // Create login request
        AuthRequest request = new AuthRequest(email, password);
        
        // Make API call
        Call<ApiResponse<AuthResponse>> call = apiService.login(request);
        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                loadingDialog.dismiss();
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        AuthResponse authResponse = apiResponse.getData();
                        String token = authResponse.getToken();
                        com.example.myrajouney.api.models.User user = authResponse.getUser();
                        
                        if (token != null && user != null) {
                            // Save token
                            TokenManager.getInstance(LoginActivity.this).saveToken(token);
                            TokenManager.getInstance(LoginActivity.this).saveUserInfo(
                                    user.getId(), 
                                    user.getEmail(), 
                                    user.getRole()
                            );
                            
                            // Create session for backward compatibility
                            sessionManager.createSession(user.getId(), user.getEmail(), user.getRole());
                            
                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            
                            // Redirect based on user role from API
                            String userRole = user.getRole();
                            if (userRole.equals("PATIENT")) {
                                redirectToDashboard("Patient");
                            } else if (userRole.equals("DOCTOR")) {
                                redirectToDashboard("Doctor");
                            } else if (userRole.equals("ADMIN")) {
                                redirectToDashboard("Admin");
                            } else {
                                redirectToDashboard(role);
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMsg = apiResponse.getError() != null ? 
                                apiResponse.getError().getMessage() : "Login failed";
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error response
                    String errorMsg = "Login failed. Please try again.";
                    if (response.code() == 401) {
                        errorMsg = "Invalid email or password";
                    } else if (response.code() == 404) {
                        errorMsg = "Server not found. Please check your connection.";
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                loadingDialog.dismiss();
                String errorMsg = "Network error: " + t.getMessage();
                if (t.getMessage() != null && t.getMessage().contains("Unable to resolve host")) {
                    errorMsg = "Cannot connect to server. Please check your internet connection and server URL.";
                }
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
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
