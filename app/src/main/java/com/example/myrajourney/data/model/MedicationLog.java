package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class MedicationLog {
    @SerializedName("id")
    private String id;

    @SerializedName("taken_at")
    private String takenAt;

    public String getId() { return id; }
    public String getTakenAt() { return takenAt; }
}






