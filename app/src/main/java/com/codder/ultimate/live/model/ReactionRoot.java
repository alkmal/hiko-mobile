package com.codder.ultimate.live.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReactionRoot {

    @SerializedName("data")
    private List<DataItem> data;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public List<DataItem> getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public static class DataItem {

        @SerializedName("image")
        private String image;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("_id")
        private String id;

        @SerializedName("updatedAt")
        private String updatedAt;

        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }

        public String getImage() {
            return image;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getId() {
            return id;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }
    }
}