package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class Appointment {
    @SerializedName("id")
    private String id;

    @SerializedName("doctor_name")
    private String doctorName;

    @SerializedName("patient_name")
    private String patientName;

    @SerializedName("date")
    private String date; // API format YYYY-MM-DD

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status;

    // Local field for UI display (formatted string like "10:00 AM - 10:30 AM")
    private String timeSlot;

    // Default constructor for Retrofit
    public Appointment() {
    }

    // Constructor used by DoctorScheduleActivity (4 args)
    public Appointment(String patientName, String startTime, String title, String date) {
        this.patientName = patientName;
        this.startTime = startTime;
        this.title = title;
        this.date = date;
        // Fallback for timeSlot
        this.timeSlot = startTime;
    }

    // Constructor used by PatientAppointmentsActivity (5 args)
    public Appointment(String patientName, String doctorName, String date, String timeSlot, String title) {
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.date = date;
        this.timeSlot = timeSlot;
        this.title = title;
    }

    // Getters
    public String getId() { return id; }
    public String getDoctorName() { return doctorName; }
    public String getPatientName() { return patientName; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }

    // Helper: returns timeSlot if set (local), otherwise startTime (API)
    public String getTimeSlot() {
        return timeSlot != null ? timeSlot : startTime;
    }

    // Setters
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setTitle(String title) { this.title = title; }
    public void setDate(String date) { this.date = date; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
}