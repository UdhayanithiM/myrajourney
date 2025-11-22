package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    @SerializedName("assigned_doctor_id")
    private Integer assignedDoctorId;

    // ✅ ADDED: Missing Phone and Address fields
    @SerializedName("phone")
    private String phone;

    @SerializedName("address")
    private String address;

    // --- Getters ---
    public int getId() { return id; }
    public String getIdString() { return String.valueOf(id); }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Integer getAssignedDoctorId() { return assignedDoctorId; }

    // ✅ ADDED: Getters for Phone and Address
    public String getPhone() { return phone; }
    public String getAddress() { return address; }

    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setAssignedDoctorId(Integer assignedDoctorId) { this.assignedDoctorId = assignedDoctorId; }

    // ✅ ADDED: Setters for Phone and Address
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
}