package com.example.myrajourney.data.model;
import com.google.gson.annotations.SerializedName;
public class DoctorOverview {
    @SerializedName("active_patients") private int activePatients;
    @SerializedName("pending_appointments") private int pendingAppointments;
    public int getActivePatients() { return activePatients; }
    public int getPendingAppointments() { return pendingAppointments; }
}






