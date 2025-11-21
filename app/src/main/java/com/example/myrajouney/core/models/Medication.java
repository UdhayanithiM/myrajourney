package com.example.myrajouney.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class Medication implements Parcelable {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("dosage")
    private String dosage;

    @SerializedName("frequency")
    private String frequency;

    @SerializedName("duration")
    private String duration;

    @SerializedName("type")
    private String type; // pill, injection, etc.

    @SerializedName("category")
    private String category;

    @SerializedName("status")
    private String status; // active, inactive

    private boolean takenToday; // Local UI flag

    // Default constructor
    public Medication() {}

    // UI Constructor
    public Medication(String name, String dosage, String frequency, String status) {
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.status = status;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getFrequency() { return frequency; }
    public String getDuration() { return duration; }
    public String getStatus() { return status; }
    public boolean isTakenToday() { return takenToday; }
    public void setTakenToday(boolean takenToday) { this.takenToday = takenToday; }

    // Parcelable Implementation
    protected Medication(Parcel in) {
        id = in.readString();
        name = in.readString();
        dosage = in.readString();
        frequency = in.readString();
        duration = in.readString();
        type = in.readString();
        category = in.readString();
        status = in.readString();
        takenToday = in.readByte() != 0;
    }

    public static final Creator<Medication> CREATOR = new Creator<Medication>() {
        @Override
        public Medication createFromParcel(Parcel in) {
            return new Medication(in);
        }
        @Override
        public Medication[] newArray(int size) {
            return new Medication[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(dosage);
        dest.writeString(frequency);
        dest.writeString(duration);
        dest.writeString(type);
        dest.writeString(category);
        dest.writeString(status);
        dest.writeByte((byte) (takenToday ? 1 : 0));
    }
}