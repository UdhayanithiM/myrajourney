package com.example.myrajourney.core.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF = "mj_session_prefs";

    private static final String KEY_ONBOARD = "onboarding_done";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_ROLE = "user_role";

    private final SharedPreferences prefs;
    private final Context appContext;

    public SessionManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.prefs = appContext.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    // --- ONBOARDING ---
    public void setOnboardingCompleted(boolean done) {
        prefs.edit().putBoolean(KEY_ONBOARD, done).apply();
    }

    public boolean isOnboardingCompleted() {
        return prefs.getBoolean(KEY_ONBOARD, false);
    }

    // --- SESSION MANAGEMENT ---
    public void createSession(String name, String email, String role) {
        prefs.edit()
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .apply();
    }

    // Check if Token exists via TokenManager
    public boolean isSessionValid() {
        return TokenManager.getInstance(appContext).getToken() != null;
    }

    // Helper for legacy calls check
    public boolean isLoggedIn() {
        return isSessionValid();
    }

    // --- GETTERS ---
    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public String getUserName() {
        return prefs.getString(KEY_NAME, "User");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    // --- LOGOUT ---
    // ✅ Corrected: No parameters needed. Clears session generically.
    public void logout() {
        boolean onboard = isOnboardingCompleted();

        // Clear all session data
        prefs.edit().clear().apply();

        // Clear Auth Token
        TokenManager.getInstance(appContext).clear();

        // Restore onboarding status
        setOnboardingCompleted(onboard);
    }
}