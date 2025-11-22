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
    private String frequency;

    @SerializedName("duration")
    private String duration;

    @SerializedName("type")
    private String type; // e.g., Tablet, Capsule

    @SerializedName("category")
    private String category; // e.g., Painkiller, Antibiotic

    @SerializedName("status")
    private String status; // e.g., Ongoing, Completed, Available

    @SerializedName("timing")
    private String timing;

    @SerializedName("instructions")
    private String instructions;

    // Helper field for UI (Checkbox state)
    private boolean isTakenToday;

    // Default Constructor
    public Medication() {
    }

    // Constructor used in AllMedicationsActivity and PatientDetailsActivity
    public Medication(String name, String dosage, String frequency, String duration, String type, String category, String status) {
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.type = type;
        this.category = category;
        this.status = status;
        this.isTakenToday = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTiming() { return timing; }
    public void setTiming(String timing) { this.timing = timing; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public boolean isTakenToday() { return isTakenToday; }
    public void setTakenToday(boolean takenToday) { isTakenToday = takenToday; }
}