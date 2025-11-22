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

    @SerializedName("phone")
    private String phone;

    @SerializedName("address")
    private String address;

    // ✅ ADDED: Missing Age, Gender, and Profile Image fields
    @SerializedName("age")
    private String age;

    @SerializedName("gender")
    private String gender;

    @SerializedName("profile_image")
    private String profileImage;

    // --- Getters ---
    public int getId() { return id; }
    public String getIdString() { return String.valueOf(id); }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Integer getAssignedDoctorId() { return assignedDoctorId; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }

    // ✅ ADDED: Getters for new fields
    public String getAge() { return age; }
    public String getGender() { return gender; }
    public String getProfileImage() { return profileImage; }

    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setAssignedDoctorId(Integer assignedDoctorId) { this.assignedDoctorId = assignedDoctorId; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }

    // ✅ ADDED: Setters for new fields
    public void setAge(String age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
}