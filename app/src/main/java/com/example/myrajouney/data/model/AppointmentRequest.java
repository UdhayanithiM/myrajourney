package com.example.myrajouney.data.model;

import com.google.gson.annotations.SerializedName;

public class AppointmentRequest {
    @SerializedName("doctor_id")
    private String doctorId;

    @SerializedName("appointment_date")
    private String date;

    @SerializedName("time_slot")
    private String timeSlot;

    @SerializedName("reason")
    private String reason;

    public AppointmentRequest(String doctorId, String date, String timeSlot, String reason) {
        this.doctorId = doctorId;
        this.date = date;
        this.timeSlot = timeSlot;
        this.reason = reason;
    }
}