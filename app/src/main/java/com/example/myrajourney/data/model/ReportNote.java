package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class ReportNote {
    @SerializedName("id")
    private String id;

    @SerializedName("note")
    private String note; // General note if needed

    @SerializedName("diagnosis_text")
    private String diagnosisText;

    @SerializedName("suggestions_text")
    private String suggestionsText;

    @SerializedName("created_at")
    private String createdAt;

    // Getters
    public String getId() { return id; }
    public String getNote() { return note; }
    public String getCreatedAt() { return createdAt; }

    public String getDiagnosisText() { return diagnosisText; }
    public String getSuggestionsText() { return suggestionsText; }

    // Setters
    public void setDiagnosisText(String diagnosisText) { this.diagnosisText = diagnosisText; }
    public void setSuggestionsText(String suggestionsText) { this.suggestionsText = suggestionsText; }
}