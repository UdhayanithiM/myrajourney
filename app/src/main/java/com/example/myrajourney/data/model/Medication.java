package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class Medication {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("dosage")
    private String dosage;

    @SerializedName("frequency")
    private String frequency; // e.g. "Morning", "Twice a day", "8:00 AM"

    @SerializedName("duration")
    private String duration;

    @SerializedName("type")
    private String type;

    @SerializedName("category")
    private String category;

    @SerializedName("status")
    private String status;

    @SerializedName("timing")
    private String timing; // Real backend timing field

    @SerializedName("instructions")
    private String instructions;

    // Helper (not from backend)
    private boolean isTakenToday = false;

    // Empty Constructor
    public Medication() {}

    // Full Constructor (for doctor local selection list)
    public Medication(String name,
                      String dosage,
                      String frequency,
                      String duration,
                      String type,
                      String category,
                      String status) {

        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.type = type;
        this.category = category;
        this.status = status;
    }

    // ---------------- Getters & Setters ----------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = safe(id);
    }

    public String getName() {
        return safe(name);
    }

    public void setName(String name) {
        this.name = safe(name);
    }

    public String getDosage() {
        return safe(dosage);
    }

    public void setDosage(String dosage) {
        this.dosage = safe(dosage);
    }

    public String getFrequency() {
        return safe(frequency);
    }

    public void setFrequency(String frequency) {
        this.frequency = safe(frequency);
    }

    public String getDuration() {
        return safe(duration);
    }

    public void setDuration(String duration) {
        this.duration = safe(duration);
    }

    public String getType() {
        return safe(type);
    }

    public void setType(String type) {
        this.type = safe(type);
    }

    public String getCategory() {
        return safe(category);
    }

    public void setCategory(String category) {
        this.category = safe(category);
    }

    public String getStatus() {
        if (status == null) return "Active";
        return status;
    }

    public void setStatus(String status) {
        this.status = safe(status);
    }

    public boolean isActive() {
        return "Active".equalsIgnoreCase(status) ||
                "Ongoing".equalsIgnoreCase(status) ||
                status == null;
    }

    public boolean isTakenToday() {
        return isTakenToday;
    }

    public void setTakenToday(boolean takenToday) {
        this.isTakenToday = takenToday;
    }

    public String getTiming() {
        return safe(timing);
    }

    public void setTiming(String timing) {
        this.timing = safe(timing);
    }

    public String getInstructions() {
        return safe(instructions);
    }

    public void setInstructions(String instructions) {
        this.instructions = safe(instructions);
    }

    // ---------------- Helper ----------------

    private String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * Smart formatting: converts loose text to a time
     */
    public String getFormattedTime() {
        String text = getFrequency().toLowerCase();

        if (text.contains("morning")) return "8:00 AM";
        if (text.contains("afternoon")) return "2:00 PM";
        if (text.contains("evening") || text.contains("night")) return "8:00 PM";

        if (text.contains(":")) return getFrequency();

        return "8:00 AM"; // default fallback
    }
}
