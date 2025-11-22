package com.example.myrajourney.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.R;

// ✅ FIX: Import ResetPasswordActivity from the admin/users package
import com.example.myrajourney.admin.users.ResetPasswordActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendLink, btnResetHere;
    private TextView tvOr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnSendLink = findViewById(R.id.btnSendLink);
        btnResetHere = findViewById(R.id.btnResetHere);
        tvOr = findViewById(R.id.tvOr);

        // Option 1: Send reset link via email
        btnSendLink.setOnClickListener(v -> sendResetLink());

        // Option 2: Reset password directly on this page
        btnResetHere.setOnClickListener(v -> resetPasswordHere());
    }

    private void sendResetLink() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        // Ensure AuthRequest constructor matches your model definition
        com.example.myrajourney.data.model.AuthRequest req = new com.example.myrajourney.data.model.AuthRequest(email, "");
        retrofit2.Call<ApiResponse<Void>> call = api.forgotPassword(req);

        call.enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<Void>> call, retrofit2.Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Password reset link has been sent to your email. Please check your inbox and click the link to reset your password.", Toast.LENGTH_LONG).show();
                } else {
                    String errorMsg = "Failed to send reset link";
                    if (response.body() != null && response.body().getError() != null) {
                        errorMsg = response.body().getError().getMessage();
                    }
                    Toast.makeText(ForgotPasswordActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPasswordHere() {
        String email = etEmail.getText().toString().trim();
        // Optional: You might want to allow navigation without email if the user intends to enter it on the next screen
        // But keeping validation if you want to pass the email forward is fine.
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to reset password page
        Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }
}