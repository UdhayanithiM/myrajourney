package com.example.myrajouney.core.session;

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

    // ONBOARDING
    public void setOnboardingCompleted(boolean done) {
        prefs.edit().putBoolean(KEY_ONBOARD, done).apply();
    }

    public boolean isOnboardingCompleted() {
        return prefs.getBoolean(KEY_ONBOARD, false);
    }

    // SESSION
    public void createSession(String name, String email, String role) {
        prefs.edit()
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public boolean isSessionValid() {
        return com.example.myrajouney.core.session.TokenManager
                .getInstance(appContext)
                .getToken() != null;
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public void logout() {
        boolean onboard = isOnboardingCompleted();

        prefs.edit().clear().apply();

        com.example.myrajouney.core.session.TokenManager
                .getInstance(appContext)
                .clear();

        setOnboardingCompleted(onboard);
    }
}
