package com.example.myrajourney.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Rehab implements Parcelable {
    private String name;
    private String description;
    private String reps;
    private String frequency;
    private String videoUrl;
    private String thumbnailUrl;

    // Helper field for UI selection
    private boolean isSelected;

    // Default constructor
    public Rehab() {
    }

    // Constructor used in activities
    public Rehab(String name, String description, String reps, String frequency, String videoUrl, String thumbnailUrl) {
        this.name = name;
        this.description = description;
        this.reps = reps;
        this.frequency = frequency;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.isSelected = false;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReps() { return reps; }
    public void setReps(String reps) { this.reps = reps; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    // --- Parcelable Implementation ---
    protected Rehab(Parcel in) {
        name = in.readString();
        description = in.readString();
        reps = in.readString();
        frequency = in.readString();
        videoUrl = in.readString();
        thumbnailUrl = in.readString();
        isSelected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(reps);
        dest.writeString(frequency);
        dest.writeString(videoUrl);
        dest.writeString(thumbnailUrl);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Rehab> CREATOR = new Creator<Rehab>() {
        @Override
        public Rehab createFromParcel(Parcel in) {
            return new Rehab(in);
        }

        @Override
        public Rehab[] newArray(int size) {
            return new Rehab[size];
        }
    };
}