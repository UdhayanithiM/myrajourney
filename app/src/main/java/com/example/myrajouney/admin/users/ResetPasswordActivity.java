package com.example.myrajouney.admin.users;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myrajouney.api.ApiService;
import com.example.myrajouney.api.models.ApiResponse;

public class ResetPasswordActivity extends AppCompatActivity {
    
    private EditText etEmail, etPassword, etConfirmPassword, etToken;
    private Button btnReset;
    private TextView tvInfo;
    private String token;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        
        // Get token from intent or URL
        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        if (token == null && intent.getData() != null) {
            // Extract token from URL
            String data = intent.getData().toString();
            if (data.contains("token=")) {
                token = data.substring(data.indexOf("token=") + 6);
                if (token.contains("&")) {
                    token = token.substring(0, token.indexOf("&"));
                }
            }
        }
        
        initializeViews();
        setupClickListeners();
    }
    
    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etToken = findViewById(R.id.etToken);
        btnReset = findViewById(R.id.btnReset);
        tvInfo = findViewById(R.id.tvInfo);
        
        // If token is provided, hide email field and show token field
        if (token != null && !token.isEmpty()) {
            etToken.setText(token);
            etToken.setEnabled(false);
            etEmail.setVisibility(android.view.View.GONE);
            findViewById(R.id.tvEmailLabel).setVisibility(android.view.View.GONE);
            tvInfo.setText("Enter your new password. Password must be at least 8 characters long.");
        } else {
            etToken.setVisibility(android.view.View.GONE);
            findViewById(R.id.tvTokenLabel).setVisibility(android.view.View.GONE);
            tvInfo.setText("Enter your email and new password. Password must be at least 8 characters long.");
        }
    }
    
    private void setupClickListeners() {
        btnReset.setOnClickListener(v -> performReset());
        
        // Back to login
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);
        if (tvBackToLogin != null) {
            tvBackToLogin.setOnClickListener(v -> {
                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
    
    private void performReset() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String resetToken = etToken.getText().toString().trim();
        
        // Validation
        if (token == null || token.isEmpty()) {
            // Email-based reset
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Use token from field if available, otherwise use intent token
        if (resetToken.isEmpty() && token != null) {
            resetToken = token;
        }
        
        // Call API
        resetPasswordAPI(email, password, resetToken);
    }
    
    private void resetPasswordAPI(String email, String password, String resetToken) {
        ApiService api = com.example.myrajouney.api.ApiClient.getApiService(this);
        
        // Create request body
        java.util.Map<String, String> requestBody = new java.util.HashMap<>();
        if (!email.isEmpty()) {
            requestBody.put("email", email);
        }
        requestBody.put("password", password);
        if (!resetToken.isEmpty()) {
            requestBody.put("token", resetToken);
        }
        
        retrofit2.Call<ApiResponse<Void>> call = api.resetPassword(requestBody);
        
        call.enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<Void>> call, retrofit2.Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ResetPasswordActivity.this, "Password reset successfully! You can now login with your new password.", Toast.LENGTH_LONG).show();
                    // Navigate to login
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Password reset failed";
                    if (response.body() != null && response.body().getError() != null) {
                        errorMsg = response.body().getError().getMessage();
                    }
                    Toast.makeText(ResetPasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(ResetPasswordActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

