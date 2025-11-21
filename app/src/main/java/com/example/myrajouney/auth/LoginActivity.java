package com.example.myrajouney.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajouney.api.models.ApiResponse;
import com.example.myrajouney.api.models.AuthRequest;
import com.example.myrajouney.api.models.AuthResponse;
import com.example.myrajouney.api.models.User;
import com.example.myrajouney.core.navigation.NavigationManager;
import com.example.myrajouney.core.session.SessionManager;
import com.example.myrajouney.core.session.TokenManager;
import com.example.myrajouney.core.ui.LoadingDialog;
import com.example.myrajouney.api.ApiClient;
import com.example.myrajouney.api.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailET, passET;
    private Button loginBtn;
    private LoadingDialog dialog;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_login);

        session = new SessionManager(this);
        dialog = new LoadingDialog(this);

        emailET = findViewById(R.id.etUsername);
        passET = findViewById(R.id.etPassword);
        loginBtn = findViewById(R.id.btnLogin);

        loginBtn.setOnClickListener(v -> login());
    }

    private void login() {

        String email = emailET.getText().toString().trim();
        String pass = passET.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
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

                TokenManager.getInstance(LoginActivity.this).saveToken(auth.getToken());
                TokenManager.getInstance(LoginActivity.this).saveUserInfo(u.getIdString(), u.getEmail(), u.getRole());

                String name = (u.getName() == null) ? u.getEmail() : u.getName();
                session.createSession(name, u.getEmail(), u.getRole());

                NavigationManager.goToDashboardForRole(LoginActivity.this, u.getRole());
                finish();
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
