package com.example.myrajourney.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;
import com.example.myrajourney.core.navigation.NavigationManager;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.core.session.TokenManager;
import com.example.myrajourney.core.ui.LoadingDialog;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.AuthRequest;
import com.example.myrajourney.data.model.AuthResponse;
import com.example.myrajourney.data.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailET, passET;
    private Button loginBtn;
    private TextView tvForgotPassword;
    private LoadingDialog dialog;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_login);

        session = new SessionManager(this);
        dialog = new LoadingDialog(this);

        // Bind Views
        emailET = findViewById(R.id.etUsername);
        passET = findViewById(R.id.etPassword);
        loginBtn = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Setup Listeners
        loginBtn.setOnClickListener(v -> login());

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void login() {
        String email = emailET.getText().toString().trim();
        String pass = passET.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailET.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            passET.setError("Password is required");
            return;
        }

        dialog.show("Authenticating...");

        ApiService api = ApiClient.getApiService(this);
        AuthRequest req = new AuthRequest(email, pass);

        api.login(req).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> res) {
                dialog.dismiss();

                if (!res.isSuccessful() || res.body() == null || !res.body().isSuccess()) {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthResponse auth = res.body().getData();
                User u = auth.getUser();

                if (u != null) {
                    // Save secure token
                    TokenManager.getInstance(LoginActivity.this).saveToken(auth.getToken());
                    // Save basic user info
                    TokenManager.getInstance(LoginActivity.this).saveUserInfo(
                            String.valueOf(u.getId()),
                            u.getEmail(),
                            u.getRole()
                    );

                    String name = (u.getName() == null) ? u.getEmail() : u.getName();
                    session.createSession(name, u.getEmail(), u.getRole());

                    // Navigate based on role
                    NavigationManager.goToDashboardForRole(LoginActivity.this, u.getRole());
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed: User data missing", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}