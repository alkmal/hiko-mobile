package com.codder.ultimate.musicfunction;

import android.os.Parcel;
import android.os.Parcelable;

public class AudioDetails implements Parcelable {
    private String name;
    private String duration;
    private String thumbnailPath;
    private String songPath;


    public AudioDetails(String name, String duration, String thumbnailPath, String songPath) {
        this.name = name;
        this.duration = duration;
        this.thumbnailPath = thumbnailPath;
        this.songPath = songPath;
    }

    protected AudioDetails(Parcel in) {
        name = in.readString();
        duration = in.readString();
        thumbnailPath = in.readString();
        songPath = in.readString();
    }

    public static final Creator<AudioDetails> CREATOR = new Creator<AudioDetails>() {
        @Override
        public AudioDetails createFromParcel(Parcel in) {
            return new AudioDetails(in);
        }

        @Override
        public AudioDetails[] newArray(int size) {
            return new AudioDetails[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getDuration() {
        return duration;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public String getSongPath() {
        return songPath;
    }

    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(duration);
        dest.writeString(thumbnailPath);
        dest.writeString(songPath);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
