package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class AppointmentRequest {
    @SerializedName("patient_id")
    private String patientId;

    @SerializedName("doctor_id")
    private String doctorId;

    @SerializedName("title")
    private String title;

    @SerializedName("reason")
    private String reason;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    public AppointmentRequest(String patientId, String doctorId, String title, String reason, String startTime, String endTime) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.title = title;
        this.reason = reason;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}