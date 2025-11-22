package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class SymptomRequest {
    @SerializedName("patient_id")
    private String patientId;

    @SerializedName("date")
    private String date;

    @SerializedName("pain_level")
    private int painLevel;

    @SerializedName("stiffness_minutes")
    private int stiffnessMinutes;

    @SerializedName("fatigue_level")
    private int fatigueLevel;

    @SerializedName("notes")
    private String notes;

    // ✅ Constructor matching the 3-arg usage
    public SymptomRequest(String patientId, String date, int painLevel) {
        this.patientId = patientId;
        this.date = date;
        this.painLevel = painLevel;
    }

    // Setters
    public void setStiffnessMinutes(int stiffnessMinutes) {
        this.stiffnessMinutes = stiffnessMinutes;
    }

    public void setFatigueLevel(int fatigueLevel) {
        this.fatigueLevel = fatigueLevel;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}