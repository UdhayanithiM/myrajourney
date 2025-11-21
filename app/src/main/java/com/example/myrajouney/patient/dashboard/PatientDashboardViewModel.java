package com.example.myrajouney.patient.dashboard;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myrajouney.data.model.Appointment;
import com.example.myrajouney.data.model.PatientOverview;
import com.example.myrajouney.data.repository.PatientRepository;
import com.example.myrajouney.doctor.dashboard.HealthMetric;
// ^ NOTE: Ensure HealthMetric is in this package. If it's in core/ui or root, update this import.

import java.util.ArrayList;
import java.util.List;

public class PatientDashboardViewModel extends AndroidViewModel {

    private final PatientRepository repository;

    // UI State Holders
    private final MutableLiveData<List<HealthMetric>> healthMetrics = new MutableLiveData<>();
    private final MutableLiveData<List<Appointment>> upcomingAppointments = new MutableLiveData<>();
    private final MutableLiveData<Boolean> taskCompleted = new MutableLiveData<>();
    private final MutableLiveData<String> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public PatientDashboardViewModel(@NonNull Application application) {
        super(application);
        // Initialize Repository
        repository = new PatientRepository(application);

        // Load initial data
        refreshData();
    }

    // --- Getters for UI Observation ---
    public LiveData<List<HealthMetric>> getHealthMetrics() { return healthMetrics; }
    public LiveData<List<Appointment>> getUpcomingAppointments() { return upcomingAppointments; }
    public LiveData<Boolean> getTaskCompleted() { return taskCompleted; }
    public LiveData<String> getCurrentUser() { return currentUser; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // --- Actions ---

    public void refreshData() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // 1. Fetch Overview (Metrics & User Info)
        repository.getPatientOverview(new PatientRepository.DataCallback<PatientOverview>() {
            @Override
            public void onSuccess(PatientOverview data) {
                if (data != null) {
                    currentUser.setValue(data.getPatientName());
                    mapOverviewToMetrics(data);
                }
                // Don't stop loading yet, fetch appointments next
                loadAppointments();
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue("Overview failed: " + error);
                isLoading.setValue(false); // Stop loading on critical error
            }
        });
    }

    private void loadAppointments() {
        // 2. Fetch Appointments List
        repository.getAppointments(new PatientRepository.DataCallback<List<Appointment>>() {
            @Override
            public void onSuccess(List<Appointment> data) {
                upcomingAppointments.setValue(data);
                isLoading.setValue(false); // All done
            }

            @Override
            public void onError(String error) {
                // Non-critical error (we still show metrics)
                errorMessage.setValue("Appointments failed: " + error);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Maps the raw API response (PatientOverview) to the UI cards (HealthMetric).
     */
    private void mapOverviewToMetrics(PatientOverview overview) {
        List<HealthMetric> metrics = new ArrayList<>();

        // 1. DAS28 Score (Color coded)
        double das = overview.getDas28Score();
        String dasColor = das > 5.1 ? "#F44336" : (das < 2.6 ? "#4CAF50" : "#FF9800");
        metrics.add(new HealthMetric("DAS28 Score", String.valueOf(das), "📊", dasColor));

        // 2. Pain Level
        int pain = overview.getPainLevel();
        String painColor = pain >= 7 ? "#F44336" : (pain >= 4 ? "#FF9800" : "#4CAF50");
        metrics.add(new HealthMetric("Pain Level", pain + "/10", "😣", painColor));

        // 3. Notifications
        int notifs = overview.getUnreadNotifications();
        metrics.add(new HealthMetric("Notifications", String.valueOf(notifs), "🔔", "#2196F3"));

        // 4. Fatigue (Placeholder or from API if available later)
        metrics.add(new HealthMetric("Fatigue", "Moderate", "😫", "#FF9800"));

        healthMetrics.setValue(metrics);
    }

    public void setTaskCompleted(boolean completed) {
        taskCompleted.setValue(completed);
    }
}