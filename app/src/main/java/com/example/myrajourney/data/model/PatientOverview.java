package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PatientOverview {
    @SerializedName("patient_name")
    private String patientName;

    @SerializedName("unread_notifications")
    private int unreadNotifications;

    @SerializedName("das28_score")
    private double das28Score;

    @SerializedName("pain_level")
    private int painLevel;

    @SerializedName("next_appointment")
    private Appointment nextAppointment;

    @SerializedName("recent_medications")
    private List<Medication> recentMedications;

    public String getPatientName() { return patientName; }
    public int getUnreadNotifications() { return unreadNotifications; }
    public double getDas28Score() { return das28Score; }
    public int getPainLevel() { return painLevel; }
    public Appointment getNextAppointment() { return nextAppointment; }
    public List<Medication> getRecentMedications() { return recentMedications; }
}






