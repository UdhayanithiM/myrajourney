package com.example.myrajouney;

public class Appointment {
    private String patientName;
    private String doctorName;
    private String date;
    private String timeSlot;
    private String reason;
    private String appointmentType;

    // Constructor for doctor view (with time, type, date)
    public Appointment(String patientName, String timeSlot, String appointmentType, String date) {
        this.patientName = patientName;
        this.timeSlot = timeSlot;
        this.appointmentType = appointmentType;
        this.date = date;
    }

    // Original constructor
    public Appointment(String patientName, String doctorName, String date, String timeSlot, String reason) {
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.date = date;
        this.timeSlot = timeSlot;
        this.reason = reason;
    }

    // Getters
    public String getPatientName() {
        return patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getDate() {
        return date;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public String getReason() {
        return reason;
    }

    public String getAppointmentType() {
        return appointmentType != null ? appointmentType : reason;
    }
}
