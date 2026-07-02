package com.codder.ultimate.live.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class ThemeRoot {

    @SerializedName("theme")
    private List<ThemeItem> theme;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public List<ThemeItem> getTheme() {
        return theme;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public static class ThemeItem {

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("theme")
        private String theme;

        @SerializedName("_id")
        private String id;

        @SerializedName("type")
        private int type;

        @SerializedName("isDefault")
        private boolean isDefault;

        @SerializedName("updatedAt")
        private String updatedAt;

        public String getCreatedAt() {
            return createdAt;
        }

        public String getTheme() {
            return theme;
        }

        public String getId() {
            return id;
        }

        public int getType() {
            return type;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public boolean isDefault() {
            return isDefault;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ThemeItem that = (ThemeItem) o;

            return type == that.type &&
                    isDefault == that.isDefault &&
                    Objects.equals(id, that.id) &&
                    Objects.equals(theme, that.theme) &&
                    Objects.equals(createdAt, that.createdAt) &&
                    Objects.equals(updatedAt, that.updatedAt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, theme, createdAt, updatedAt, type, isDefault);
        }

    }
}