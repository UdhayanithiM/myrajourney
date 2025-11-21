package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class ReportNote {
    @SerializedName("id")
    private String id;

    @SerializedName("note")
    private String note;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() { return id; }
    public String getNote() { return note; }
    public String getCreatedAt() { return createdAt; }
}






