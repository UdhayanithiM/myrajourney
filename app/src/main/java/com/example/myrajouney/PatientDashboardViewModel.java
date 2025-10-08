package com.example.myrajouney;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.List;

public class PatientDashboardViewModel extends AndroidViewModel {
    
    private MutableLiveData<List<HealthMetric>> healthMetrics;
    private MutableLiveData<List<Appointment>> upcomingAppointments;
    private MutableLiveData<Boolean> taskCompleted;
    private MutableLiveData<String> currentUser;
    private MutableLiveData<Boolean> isLoading;
    
    public PatientDashboardViewModel(Application application) {
        super(application);
        initializeData();
    }
    
    private void initializeData() {
        healthMetrics = new MutableLiveData<>();
        upcomingAppointments = new MutableLiveData<>();
        taskCompleted = new MutableLiveData<>();
        currentUser = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        
        // Initialize with default data
        loadHealthMetrics();
        loadUpcomingAppointments();
        taskCompleted.setValue(false);
        isLoading.setValue(false);
    }
    
    public LiveData<List<HealthMetric>> getHealthMetrics() {
        return healthMetrics;
    }
    
    public LiveData<List<Appointment>> getUpcomingAppointments() {
        return upcomingAppointments;
    }
    
    public LiveData<Boolean> getTaskCompleted() {
        return taskCompleted;
    }
    
    public LiveData<String> getCurrentUser() {
        return currentUser;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    private void loadHealthMetrics() {
        List<HealthMetric> metrics = new ArrayList<>();
        metrics.add(new HealthMetric("Heart Rate", "72 BPM", "❤️", "#F44336"));
        metrics.add(new HealthMetric("Blood Pressure", "120/80", "🩸", "#2196F3"));
        metrics.add(new HealthMetric("Temperature", "36.5°C", "🌡️", "#FF9800"));
        metrics.add(new HealthMetric("Pain Level", "3/10", "😣", "#F44336"));
        healthMetrics.setValue(metrics);
    }
    
    private void loadUpcomingAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        appointments.add(new Appointment("Patient", "Dr. Smith", "Dec 15, 2024", "10:30 AM", "Consultation"));
        appointments.add(new Appointment("Patient", "Dr. Johnson", "Dec 28, 2024", "2:00 PM", "Follow-up"));
        upcomingAppointments.setValue(appointments);
    }
    
    public void setTaskCompleted(boolean completed) {
        taskCompleted.setValue(completed);
    }
    
    public void setCurrentUser(String username) {
        currentUser.setValue(username);
    }
    
    public void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }
    
    public void refreshData() {
        setLoading(true);
        // Simulate data refresh
        new android.os.Handler().postDelayed(() -> {
            loadHealthMetrics();
            loadUpcomingAppointments();
            setLoading(false);
        }, 1000);
    }
}
