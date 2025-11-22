package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class ReportRequest {

    @SerializedName("patient_id")
    private String patientId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    // Constructor
    public ReportRequest(String patientId, String title, String description) {
        this.patientId = patientId;
        this.title = title;
        this.description = description;
    }

    // Getters and Setters
    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}