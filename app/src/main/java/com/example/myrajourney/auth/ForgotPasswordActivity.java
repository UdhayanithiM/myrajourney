package com.example.myrajourney.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;
import com.example.myrajourney.admin.users.ResetPasswordActivity;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.AuthRequest;
import com.example.myrajourney.data.model.AuthResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnResetHere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnResetHere = findViewById(R.id.btnResetHere);

        btnResetHere.setText("Continue");

        btnResetHere.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            checkEmailExists(email);
        });
    }

    // ---------------------------------------------------------
    // CHECK IF EMAIL EXISTS IN DATABASE
    // ---------------------------------------------------------
    private void checkEmailExists(String email) {
        ApiService api = ApiClient.getApiService(this);

        // Try login with a known-wrong password
        api.login(new AuthRequest(email, "WRONG_PASSWORD"))
                .enqueue(new Callback<ApiResponse<AuthResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AuthResponse>> call,
                                           Response<ApiResponse<AuthResponse>> res) {

                        // If email exists backend returns: 401 INVALID_CREDENTIALS
                        if (res.code() == 401) {
                            goToResetScreen(email);
                            return;
                        }

                        // Email does NOT exist
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Email not found",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Network error",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToResetScreen(String email) {
        Intent i = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
        i.putExtra("email", email);
        startActivity(i);
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email)
                && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
