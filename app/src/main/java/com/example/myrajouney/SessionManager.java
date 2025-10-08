package com.example.myrajouney;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "session_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_LOGIN_TIME = "login_time";
    
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    
    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }
    
    public void createSession(String userId, String username, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
        editor.apply();
    }
    
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }
    
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }
    
    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }
    
    public long getLoginTime() {
        return prefs.getLong(KEY_LOGIN_TIME, 0);
    }
    
    public void logout() {
        editor.clear();
        editor.apply();
    }
    
    public boolean isSessionValid() {
        if (!isLoggedIn()) return false;
        
        long loginTime = getLoginTime();
        long currentTime = System.currentTimeMillis();
        long sessionDuration = 24 * 60 * 60 * 1000; // 24 hours
        
        return (currentTime - loginTime) < sessionDuration;
    }
}
