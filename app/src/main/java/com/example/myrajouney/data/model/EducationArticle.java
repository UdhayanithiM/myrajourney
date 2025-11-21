package com.example.myrajouney.data.model;

import com.google.gson.annotations.SerializedName;

public class EducationArticle {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("thumbnail")
    private String thumbnail;

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getThumbnail() { return thumbnail; }
}