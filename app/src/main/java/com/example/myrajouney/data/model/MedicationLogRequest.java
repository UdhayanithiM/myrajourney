package com.example.myrajouney.data.model;

import com.google.gson.annotations.SerializedName;

public class MedicationLogRequest {
    @SerializedName("medication_id")
    private String medicationId;

    @SerializedName("date")
    private String date;

    public MedicationLogRequest(String medicationId, String date) {
        this.medicationId = medicationId;
        this.date = date;
    }
}