package com.example.myrajourney.auth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.core.navigation.NavigationManager;
import com.example.myrajourney.core.session.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int DELAY = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::decide, DELAY);
    }

    private void decide() {
        SessionManager sm = new SessionManager(this);

        // 1 â€” Onboarding
        if (!sm.isOnboardingCompleted()) {
            NavigationManager.goToOnboarding(this);
            finish();
            return;
        }

        // 2 â€” Logged in?
        if (sm.isSessionValid()) {
            NavigationManager.goToDashboardForRole(this, sm.getRole());
            finish();
            return;
        }

        NavigationManager.goToLogin(this);
        finish();
    }
}






