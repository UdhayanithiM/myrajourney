package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RehabPlan {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("video_url")
    private String videoUrl;

    @SerializedName("exercise_name")
    private String exerciseName; // For single-exercise plans

    @SerializedName("sets_per_day")
    private Integer setsPerDay;

    @SerializedName("reps_per_set")
    private Integer repsPerSet;

    @SerializedName("exercises")
    private List<RehabExercise> exercises;

    // Inner class for individual exercises within a plan
    public static class RehabExercise {
        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;

        @SerializedName("sets")
        private Integer sets;

        @SerializedName("reps")
        private Integer reps;

        @SerializedName("completed")
        private boolean completed;

        public String getName() { return name; }
        public String getDescription() { return description; }
        public Integer getSets() { return sets; }
        public Integer getReps() { return reps; }
        public boolean isCompleted() { return completed; }
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getVideoUrl() { return videoUrl; }
    public String getExerciseName() { return exerciseName; }
    public Integer getSetsPerDay() { return setsPerDay; }
    public Integer getRepsPerSet() { return repsPerSet; }
    public List<RehabExercise> getExercises() { return exercises; }
}