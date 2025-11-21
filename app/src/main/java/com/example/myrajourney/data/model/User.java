package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private Object id; // Can be String or Int depending on backend, Object handles both safely

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    public String getIdString() {
        return String.valueOf(id);
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}






