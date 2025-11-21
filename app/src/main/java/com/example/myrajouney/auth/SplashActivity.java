package com.example.myrajouney.auth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajouney.core.navigation.NavigationManager;
import com.example.myrajouney.core.session.SessionManager;

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

        // 1 — Onboarding
        if (!sm.isOnboardingCompleted()) {
            NavigationManager.goToOnboarding(this);
            finish();
            return;
        }

        // 2 — Logged in?
        if (sm.isSessionValid()) {
            NavigationManager.goToDashboardForRole(this, sm.getRole());
            finish();
            return;
        }

        NavigationManager.goToLogin(this);
        finish();
    }
}
