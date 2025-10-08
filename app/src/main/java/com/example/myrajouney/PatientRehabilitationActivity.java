package com.example.myrajouney;

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
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientRehabilitationActivity extends AppCompatActivity {

    private LinearLayout rehabContainer;
    private TextView noRehabText;
    private List<RehabExercise> todayExercises;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_rehabilitation);

        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn() || !sessionManager.isSessionValid()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        loadTodayExercises();
        displayExercises();
    }

    private void initializeViews() {
        rehabContainer = findViewById(R.id.rehabContainer);
        noRehabText = findViewById(R.id.noRehabText);

        // Back button
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void loadTodayExercises() {
        todayExercises = new ArrayList<>();

        // Mock data - In real app, this would come from database/server
        // based on prescriptions from doctors
        todayExercises.add(new RehabExercise(
            "Hand Exercises",
            "Gentle hand and finger exercises for RA",
            "https://www.youtube.com/watch?v=k2kMJ2hHugQ",
            3, 10, 75)); // 3 sets of 10 reps, 75% progress

        todayExercises.add(new RehabExercise(
            "Wrist Stretches",
            "Stretching exercises for wrist mobility",
            "https://www.youtube.com/watch?v=TdhvUFFv1_A",
            2, 15, 40)); // 2 sets of 15 reps, 40% progress

        todayExercises.add(new RehabExercise(
            "Shoulder Rolls",
            "Shoulder mobility and strengthening",
            "https://www.youtube.com/watch?v=k2kMJ2hHugQ",
            3, 12, 90)); // 3 sets of 12 reps, 90% progress
    }

    private void displayExercises() {
        rehabContainer.removeAllViews();

        if (todayExercises.isEmpty()) {
            noRehabText.setVisibility(View.VISIBLE);
            return;
        }

        noRehabText.setVisibility(View.GONE);

        for (RehabExercise exercise : todayExercises) {
            addExerciseCard(exercise);
        }
    }

    private void addExerciseCard(RehabExercise exercise) {
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

                // Save completion status
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
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void saveExerciseStatus(String exerciseName, boolean completed) {
        SharedPreferences prefs = getSharedPreferences("rehab_status", MODE_PRIVATE);
        prefs.edit().putBoolean(exerciseName + "_" + getCurrentDate(), completed).apply();
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // Inner class for rehab exercise
    private static class RehabExercise {
        private String name;
        private String description;
        private String videoUrl;
        private int sets;
        private int reps;
        private int progress;
        private boolean completed;

        public RehabExercise(String name, String description, String videoUrl, int sets, int reps, int progress) {
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
