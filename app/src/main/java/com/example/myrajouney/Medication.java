package com.example.myrajouney;

import android.os.Parcel;
import android.os.Parcelable;

public class Medication implements Parcelable {
    private String name;
    private String dosage;
    private String frequency;
    private String duration;
    private String type;
    private String category;
    private String status;

    private boolean takenToday; // <-- NEW FIELD

    // Constructor
    public Medication(String name, String dosage, String frequency, String duration,
                      String type, String category, String status) {
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.type = type;
        this.category = category;
        this.status = status;
        this.takenToday = false; // default
    }

    // Getters & Setters
    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getFrequency() { return frequency; }
    public String getDuration() { return duration; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }

    // --- For Checkbox ---
    public boolean isTakenToday() {
        return takenToday;
    }

    public void setTakenToday(boolean takenToday) {
        this.takenToday = takenToday;
    }

    // Parcelable implementation
    protected Medication(Parcel in) {
        name = in.readString();
        dosage = in.readString();
        frequency = in.readString();
        duration = in.readString();
        type = in.readString();
        category = in.readString();
        status = in.readString();
        takenToday = in.readByte() != 0; // read boolean
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(dosage);
        dest.writeString(frequency);
        dest.writeString(duration);
        dest.writeString(type);
        dest.writeString(category);
        dest.writeString(status);
        dest.writeByte((byte) (takenToday ? 1 : 0)); // write boolean
    }

    @Override
    public int describeContents() {
        return 0;
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
}
