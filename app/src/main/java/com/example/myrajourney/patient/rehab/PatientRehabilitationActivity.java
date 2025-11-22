package com.example.myrajourney.patient.rehab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.auth.LoginActivity;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.RehabPlan;
// ---------------------

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientRehabilitationActivity extends AppCompatActivity {

    private LinearLayout rehabContainer;
    private TextView noRehabText;
    // Using local inner class for UI representation
    private List<LocalRehabExercise> todayExercises;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_rehabilitation);

        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isSessionValid()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        loadTodayExercises();
    }

    private void initializeViews() {
        rehabContainer = findViewById(R.id.rehabContainer);
        noRehabText = findViewById(R.id.noRehabText);

        // Back button
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void loadTodayExercises() {
        todayExercises = new ArrayList<>();
        // Load rehab exercises from API
        loadRehabFromAPI();
    }

    private void loadRehabFromAPI() {
        ApiService apiService = ApiClient.getApiService(this);

        // Get current user ID safely
        Integer patientId = null;
        try {
            String userIdStr = sessionManager.getUserName(); // Or get actual ID if stored in session
            // In a real app, store ID in session. For now, passing null might let backend use token.
        } catch (Exception e) {
            e.printStackTrace();
        }

        Call<ApiResponse<List<RehabPlan>>> call = apiService.getRehabPlans(patientId);

        call.enqueue(new Callback<ApiResponse<List<RehabPlan>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<RehabPlan>>> call,
                                   Response<ApiResponse<List<RehabPlan>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<RehabPlan> plans = response.body().getData();

                    if (plans != null && !plans.isEmpty()) {
                        for (RehabPlan plan : plans) {
                            // Check if plan has explicit sub-exercises
                            if (plan.getExercises() != null && !plan.getExercises().isEmpty()) {
                                for (RehabPlan.RehabExercise ex : plan.getExercises()) {
                                    String videoUrl = plan.getVideoUrl() != null ? plan.getVideoUrl() : "https://www.youtube.com/watch?v=k2kMJ2hHugQ";
                                    int sets = ex.getSets() != null ? ex.getSets() : 3;
                                    int reps = ex.getReps() != null ? ex.getReps() : 10;
                                    int progress = calculateProgress(null, ex);

                                    String name = ex.getName() != null ? ex.getName() : "Exercise";
                                    String desc = ex.getDescription() != null ? ex.getDescription() : (plan.getDescription() != null ? plan.getDescription() : "Rehabilitation exercise");

                                    todayExercises.add(new LocalRehabExercise(name, desc, videoUrl, sets, reps, progress));
                                }
                            } else {
                                // Fallback: use plan data directly as a single exercise
                                String videoUrl = plan.getVideoUrl() != null ? plan.getVideoUrl() : "https://www.youtube.com/watch?v=k2kMJ2hHugQ";
                                int sets = plan.getSetsPerDay() != null ? plan.getSetsPerDay() : 3;
                                int reps = plan.getRepsPerSet() != null ? plan.getRepsPerSet() : 10;
                                int progress = calculateProgress(plan, null);

                                String name = plan.getExerciseName() != null ? plan.getExerciseName() : (plan.getTitle() != null ? plan.getTitle() : "Exercise");
                                String desc = plan.getDescription() != null ? plan.getDescription() : "Rehabilitation exercise";

                                todayExercises.add(new LocalRehabExercise(name, desc, videoUrl, sets, reps, progress));
                            }
                        }
                        displayExercises();
                    } else {
                        displayExercises(); // Show empty state
                    }
                } else {
                    displayExercises();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<RehabPlan>>> call, Throwable t) {
                displayExercises();
            }
        });
    }

    private int calculateProgress(RehabPlan plan, RehabPlan.RehabExercise exercise) {
        SharedPreferences prefs = getSharedPreferences("rehab_status", MODE_PRIVATE);
        String exerciseName = "";

        if (exercise != null) {
            exerciseName = exercise.getName();
        } else if (plan != null) {
            exerciseName = plan.getExerciseName() != null ? plan.getExerciseName() : plan.getTitle();
        }

        String dateKey = exerciseName + "_" + getCurrentDate();
        boolean completed = prefs.getBoolean(dateKey, false);
        return completed ? 100 : 0;
    }

    private void displayExercises() {
        rehabContainer.removeAllViews();

        if (todayExercises.isEmpty()) {
            noRehabText.setVisibility(View.VISIBLE);
            return;
        }

        noRehabText.setVisibility(View.GONE);

        for (LocalRehabExercise exercise : todayExercises) {
            addExerciseCard(exercise);
        }
    }

    private void addExerciseCard(LocalRehabExercise exercise) {
        View cardView = getLayoutInflater().inflate(R.layout.item_patient_rehab, rehabContainer, false);

        TextView exerciseName = cardView.findViewById(R.id.exerciseName);
        TextView exerciseDescription = cardView.findViewById(R.id.exerciseDescription);
        TextView exerciseSets = cardView.findViewById(R.id.exerciseSets);
        TextView exerciseGoals = cardView.findViewById(R.id.exerciseGoals);
        Button statusButton = cardView.findViewById(R.id.statusButton);
        Button watchVideoBtn = cardView.findViewById(R.id.watchVideoBtn);
        ProgressBar progressBar = cardView.findViewById(R.id.progressBar);

        exerciseName.setText(exercise.getName());
        exerciseDescription.setText(exercise.getDescription());
        exerciseSets.setText("Sets: " + exercise.getSets() + " × " + exercise.getReps() + " reps");
        exerciseGoals.setText("Goal: Complete all sets daily");

        progressBar.setProgress(exercise.getProgress());

        if (exercise.isCompleted()) {
            statusButton.setText("✅ Completed");
            statusButton.setBackgroundColor(getColor(android.R.color.holo_green_dark));
        } else {
            statusButton.setText("⏳ Pending");
            statusButton.setBackgroundColor(getColor(android.R.color.holo_orange_light));
        }

        statusButton.setOnClickListener(v -> {
            if (!exercise.isCompleted()) {
                exercise.setCompleted(true);
                statusButton.setText("✅ Completed");
                statusButton.setBackgroundColor(getColor(android.R.color.holo_green_dark));

                // Update progress
                progressBar.setProgress(100);

                // Save completion status locally
                saveExerciseStatus(exercise.getName(), true);

                Toast.makeText(this, "Exercise completed!", Toast.LENGTH_SHORT).show();
            }
        });

        watchVideoBtn.setOnClickListener(v -> {
            openYouTubeVideo(exercise.getVideoUrl());
        });

        rehabContainer.addView(cardView);
    }

    private void openYouTubeVideo(String videoUrl) {
        if (videoUrl == null || videoUrl.isEmpty()) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open video", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveExerciseStatus(String exerciseName, boolean completed) {
        SharedPreferences prefs = getSharedPreferences("rehab_status", MODE_PRIVATE);
        prefs.edit().putBoolean(exerciseName + "_" + getCurrentDate(), completed).apply();
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // Renamed inner class to avoid conflict with RehabPlan.RehabExercise
    private static class LocalRehabExercise {
        private String name;
        private String description;
        private String videoUrl;
        private int sets;
        private int reps;
        private int progress;
        private boolean completed;

        public LocalRehabExercise(String name, String description, String videoUrl, int sets, int reps, int progress) {
            this.name = name;
            this.description = description;
            this.videoUrl = videoUrl;
            this.sets = sets;
            this.reps = reps;
            this.progress = progress;
            this.completed = (progress >= 100);
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getVideoUrl() { return videoUrl; }
        public int getSets() { return sets; }
        public int getReps() { return reps; }
        public int getProgress() { return progress; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}