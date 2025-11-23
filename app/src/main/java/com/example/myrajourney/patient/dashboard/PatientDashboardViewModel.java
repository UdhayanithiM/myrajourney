package com.example.myrajourney.patient.dashboard;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myrajourney.data.model.Appointment;
import com.example.myrajourney.data.model.PatientOverview;
import com.example.myrajourney.data.repository.PatientRepository;
import com.example.myrajourney.doctor.dashboard.HealthMetric;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;     // ⭐ ADDED
import java.util.Comparator;      // ⭐ ADDED
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientDashboardViewModel extends AndroidViewModel {

    private final PatientRepository repository;

    private final MutableLiveData<List<HealthMetric>> healthMetrics = new MutableLiveData<>();
    private final MutableLiveData<List<Appointment>> upcomingAppointments = new MutableLiveData<>();
    private final MutableLiveData<Boolean> taskCompleted = new MutableLiveData<>();
    private final MutableLiveData<String> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public PatientDashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new PatientRepository(application);
        refreshData();   // initial load
    }

    // --- Getters ---
    public LiveData<List<HealthMetric>> getHealthMetrics() { return healthMetrics; }
    public LiveData<List<Appointment>> getUpcomingAppointments() { return upcomingAppointments; }
    public LiveData<Boolean> getTaskCompleted() { return taskCompleted; }
    public LiveData<String> getCurrentUser() { return currentUser; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // ----------------------------------------------------------
    // ⭐ ADDED — dashboard refresh onResume() in PatientDashboardActivity
    // ----------------------------------------------------------
    public void refreshAppointments() {
        loadAppointments();
    }

    // ----------------------------------------------------------
    // Main refresh
    // ----------------------------------------------------------
    public void refreshData() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.getPatientOverview(new PatientRepository.DataCallback<PatientOverview>() {
            @Override
            public void onSuccess(PatientOverview data) {
                if (data != null) {
                    currentUser.setValue(data.getPatientName());
                    mapOverviewToMetrics(data);
                }
                loadAppointments();        // continue to appointments
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue("Overview failed: " + error);
                isLoading.setValue(false);
            }
        });
    }

    // ----------------------------------------------------------
    // ⭐ FIXED — central appointment fetcher
    // ----------------------------------------------------------
    private void loadAppointments() {

        repository.getAppointments(new PatientRepository.DataCallback<List<Appointment>>() {
            @Override
            public void onSuccess(List<Appointment> data) {

                if (data == null) data = new ArrayList<>();

                // ⭐ FIXED — filter out past appointments
                List<Appointment> future = filterUpcoming(data);

                // ⭐ FIXED — sort by start_time
                sortAppointmentsByDate(future);

                upcomingAppointments.setValue(future);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue("Appointments failed: " + error);
                upcomingAppointments.setValue(new ArrayList<>());  // avoid crash
                isLoading.setValue(false);
            }
        });
    }

    // ----------------------------------------------------------
    // ⭐ ADDED — keep only future appointments
    // ----------------------------------------------------------
    private List<Appointment> filterUpcoming(List<Appointment> list) {

        List<Appointment> future = new ArrayList<>();
        long now = System.currentTimeMillis();

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (Appointment appt : list) {
            try {
                if (appt.getStartTime() == null) continue;

                Date d = f.parse(appt.getStartTime());
                if (d != null && d.getTime() >= now) {
                    future.add(appt);
                }
            } catch (Exception ignored) { }
        }

        return future;
    }

    // ----------------------------------------------------------
    // ⭐ ADDED — sort properly using real start_time
    // ----------------------------------------------------------
    private void sortAppointmentsByDate(List<Appointment> list) {

        Collections.sort(list, new Comparator<Appointment>() {
            @Override
            public int compare(Appointment a1, Appointment a2) {
                try {
                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date d1 = f.parse(a1.getStartTime());
                    Date d2 = f.parse(a2.getStartTime());
                    if (d1 == null || d2 == null) return 0;
                    return d1.compareTo(d2);
                } catch (Exception e) {
                    return 0;
                }
            }
        });
    }

    // ----------------------------------------------------------
    // Metrics mapping
    // ----------------------------------------------------------
    private void mapOverviewToMetrics(PatientOverview overview) {
        List<HealthMetric> metrics = new ArrayList<>();

        double das = overview.getDas28Score();
        String dasColor = das > 5.1 ? "#F44336" : (das < 2.6 ? "#4CAF50" : "#FF9800");
        metrics.add(new HealthMetric("DAS28 Score", String.valueOf(das), "📊", dasColor));

        int pain = overview.getPainLevel();
        String painColor = pain >= 7 ? "#F44336" : (pain >= 4 ? "#FF9800" : "#4CAF50");
        metrics.add(new HealthMetric("Pain Level", pain + "/10", "😣", painColor));

        metrics.add(new HealthMetric("Notifications",
                String.valueOf(overview.getUnreadNotifications()), "🔔", "#2196F3"));

        metrics.add(new HealthMetric("Fatigue", "Moderate", "😫", "#FF9800"));

        healthMetrics.setValue(metrics);
    }

    public void setTaskCompleted(boolean completed) {
        taskCompleted.setValue(completed);
    }
}
