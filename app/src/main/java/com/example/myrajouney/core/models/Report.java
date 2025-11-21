package com.example.myrajouney.data.model;

import com.google.gson.annotations.SerializedName;

public class Report {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String name; // Mapped to 'title' in DB

    @SerializedName("created_at")
    private String date;

    @SerializedName("file_url")
    private String fileUri;

    @SerializedName("patient_name")
    private String patientName;

    @SerializedName("type")
    private String reportType;

    @SerializedName("status")
    private String status;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getFileUri() { return fileUri; }
    public String getPatientName() { return patientName; }
    public String getReportType() { return reportType; }
    public String getStatus() { return status; }
}