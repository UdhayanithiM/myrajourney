package com.example.myrajouney;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class NavigationUtils {
    
    public static void navigateToActivity(Activity currentActivity, Class<?> targetActivity) {
        Intent intent = new Intent(currentActivity, targetActivity);
        currentActivity.startActivity(intent);
    }
    
    public static void navigateToActivityWithData(Activity currentActivity, Class<?> targetActivity, Bundle data) {
        Intent intent = new Intent(currentActivity, targetActivity);
        if (data != null) {
            intent.putExtras(data);
        }
        currentActivity.startActivity(intent);
    }
    
    public static void navigateToActivityWithResult(Activity currentActivity, Class<?> targetActivity, int requestCode) {
        Intent intent = new Intent(currentActivity, targetActivity);
        currentActivity.startActivityForResult(intent, requestCode);
    }
    
    public static void navigateBackWithResult(Activity currentActivity, int resultCode, Bundle data) {
        Intent intent = new Intent();
        if (data != null) {
            intent.putExtras(data);
        }
        currentActivity.setResult(resultCode, intent);
        currentActivity.finish();
    }
    
    public static void navigateBack(Activity currentActivity) {
        currentActivity.finish();
    }
    
    public static void navigateBackToActivity(Activity currentActivity, Class<?> targetActivity) {
        Intent intent = new Intent(currentActivity, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        currentActivity.startActivity(intent);
        currentActivity.finish();
    }
    
    public static void clearTaskAndNavigate(Activity currentActivity, Class<?> targetActivity) {
        Intent intent = new Intent(currentActivity, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        currentActivity.startActivity(intent);
        currentActivity.finish();
    }
    
    public static boolean handleBackPress(Activity currentActivity, Class<?> parentActivity) {
        // Check if we can go back to parent activity
        if (parentActivity != null) {
            navigateBackToActivity(currentActivity, parentActivity);
            return true;
        }
        return false;
    }
}
