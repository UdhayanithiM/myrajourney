package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class Report {

    @SerializedName("id")
    private String id;

    @SerializedName("patient_id")
    private String patientId;

    @SerializedName("patient_name")
    private String patientName;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("file_url")
    private String fileUrl;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("uploaded_at")
    private String uploadedAt;

    @SerializedName("doctor_comment")
    private String doctorComment;

    @SerializedName("status")
    private String status;

    // Default Constructor
    public Report() {
    }

    // Constructor used in ReportList and UploadReportActivity
    public Report(String title, String createdAt, String fileUrl) {
        this.title = title;
        this.createdAt = createdAt;
        this.fileUrl = fileUrl;
        this.status = "Pending";
    }

    // Constructor with full details
    public Report(String title, String description, String fileUrl, String status) {
        this.title = title;
        this.description = description;
        this.fileUrl = fileUrl;
        this.status = status;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getCreatedAt() { return createdAt != null ? createdAt : uploadedAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getDoctorComment() { return doctorComment; }
    public void setDoctorComment(String doctorComment) { this.doctorComment = doctorComment; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // --- Helper Methods for Adapters ---
    public String getName() { return title; }
    public String getDate() { return getCreatedAt(); }
    public String getFileUri() { return fileUrl; }
    public String getReportType() { return title; }
}