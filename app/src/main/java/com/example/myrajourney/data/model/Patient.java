package com.example.myrajourney.data.model;

public class Patient {
    private int id;
    private String name;
    private String email;
    private String age;
    private String gender;
    private String role; // "PATIENT"
    private Integer assignedDoctorId; // Can be null if not assigned
    private String profileImageUrl;

    // Default constructor
    public Patient() {
    }

    // Constructor for basic details
    public Patient(int id, String name, String email, String age) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.role = "PATIENT";
    }

    // Full Constructor
    public Patient(int id, String name, String email, String age, String gender, String role, Integer assignedDoctorId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.role = role;
        this.assignedDoctorId = assignedDoctorId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getAssignedDoctorId() {
        return assignedDoctorId;
    }

    public void setAssignedDoctorId(Integer assignedDoctorId) {
        this.assignedDoctorId = assignedDoctorId;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // Helper to check if assigned
    public boolean isAssignedToDoctor() {
        return assignedDoctorId != null && assignedDoctorId > 0;
    }
}