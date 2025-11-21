package com.example.myrajouney.common.app;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AppNotificationManager {
    private static final String CHANNEL_ID = "medication_reminders";
    private static final String CHANNEL_NAME = "Medication Reminders";
    private static final String CHANNEL_DESCRIPTION = "Notifications for medication reminders";
    
    private Context context;
    private NotificationManagerCompat notificationManager;
    
    public AppNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            
            android.app.NotificationManager manager = 
                context.getSystemService(android.app.NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    public void showMedicationReminder(String medicationName, String dosage, int notificationId) {
        Intent intent = new Intent(context, PatientDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            notificationId, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💊 Medication Reminder")
            .setContentText("Time to take " + medicationName + " (" + dosage + ")")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("It's time to take your " + medicationName + " medication. " +
                        "Dosage: " + dosage + ". Don't forget to log this in your app!"));
        
        notificationManager.notify(notificationId, builder.build());
    }
    
    public void showExerciseReminder(String exerciseName, int notificationId) {
        Intent intent = new Intent(context, PatientDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            notificationId, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🏃‍♀️ Exercise Reminder")
            .setContentText("Time for your " + exerciseName + " exercise")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("It's time for your " + exerciseName + " exercise. " +
                        "Regular exercise helps manage your RA symptoms effectively!"));
        
        notificationManager.notify(notificationId, builder.build());
    }
    
    public void showAppointmentReminder(String doctorName, String time, int notificationId) {
        Intent intent = new Intent(context, PatientDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            notificationId, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📅 Appointment Reminder")
            .setContentText("You have an appointment with " + doctorName + " at " + time)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("You have an appointment with " + doctorName + " at " + time + ". " +
                        "Please arrive 15 minutes early and bring your symptom log."));
        
        notificationManager.notify(notificationId, builder.build());
    }
    
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }
    
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}
