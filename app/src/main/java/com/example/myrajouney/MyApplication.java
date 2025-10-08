package com.example.myrajouney;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class MyApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Apply theme globally when app starts
        ThemeManager.applyTheme(this);
    }
}
