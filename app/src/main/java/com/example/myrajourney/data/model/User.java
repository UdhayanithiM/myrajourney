package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private int id; // Changed to int to match usage: userJson.getInt("id")

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    @SerializedName("assigned_doctor_id")
    private Integer assignedDoctorId;

    // --- Getters ---
    public int getId() { return id; }
    public String getIdString() { return String.valueOf(id); } // Helper for String requirements
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Integer getAssignedDoctorId() { return assignedDoctorId; }

    // --- Setters (ADDED) ---
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setAssignedDoctorId(Integer assignedDoctorId) { this.assignedDoctorId = assignedDoctorId; }
}