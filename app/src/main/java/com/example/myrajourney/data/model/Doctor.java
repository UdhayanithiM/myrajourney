package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class Doctor {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("specialization")
    private String specialization;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
}






