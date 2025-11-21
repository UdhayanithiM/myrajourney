package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class SymptomRequest {
    @SerializedName("pain_level")
    private int painLevel;

    @SerializedName("stiffness")
    private int stiffness;

    @SerializedName("notes")
    private String notes;

    public SymptomRequest(int painLevel, int stiffness, String notes) {
        this.painLevel = painLevel;
        this.stiffness = stiffness;
        this.notes = notes;
    }
}






