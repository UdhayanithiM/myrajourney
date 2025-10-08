package com.example.myrajouney;

public class Report {
    private String name;
    private String date;
    private String fileUri;
    private String patientName;
    private String reportType;
    private String status;

    // Constructor for patient reports
    public Report(String name, String date, String fileUri) {
        this.name = name;
        this.date = date;
        this.fileUri = fileUri;
    }

    // Constructor for doctor reports view
    public Report(String patientName, String reportType, String date, String status) {
        this.patientName = patientName;
        this.reportType = reportType;
        this.date = date;
        this.status = status;
        this.name = reportType;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getFileUri() {
        return fileUri;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getReportType() {
        return reportType;
    }

    public String getStatus() {
        return status;
    }
}
