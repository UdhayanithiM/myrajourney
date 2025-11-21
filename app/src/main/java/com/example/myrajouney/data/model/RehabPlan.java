package com.example.myrajouney.data.model;

import com.google.gson.annotations.SerializedName;

public class RehabPlan {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("video_url")
    private String videoUrl;

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getVideoUrl() { return videoUrl; }
}