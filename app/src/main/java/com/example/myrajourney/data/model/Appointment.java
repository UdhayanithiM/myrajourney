package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class Appointment {
    @SerializedName("id")
    private String id;

    @SerializedName("doctor_name")
    private String doctorName;

    @SerializedName("date")
    private String date;

    @SerializedName("time")
    private String time;

    @SerializedName("title")
    private String title;

    @SerializedName("status")
    private String status;

    // Getters
    public String getId() { return id; }
    public String getDoctorName() { return doctorName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
}






