package com.example.myrajouney.data.model;
import com.google.gson.annotations.SerializedName;
public class Notification {
    @SerializedName("id") private String id;
    @SerializedName("title") private String title;
    @SerializedName("message") private String message;
    @SerializedName("is_read") private boolean isRead;
    @SerializedName("created_at") private String createdAt;

    public String getTitle() { return title; }
    public String getMessage() { return message; }
}