package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("is_read")
    private boolean isRead;

    @SerializedName("created_at")
    private String createdAt;

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }

    // The adapter calls getBody(), but the field is message.
    // Returning message here fixes the symbol error.
    public String getBody() { return message; }
    public String getMessage() { return message; }

    public String getCreatedAt() { return createdAt; }

    public boolean isRead() { return isRead; }

    // Helper for UI logic
    public boolean isUnread() { return !isRead; }
}