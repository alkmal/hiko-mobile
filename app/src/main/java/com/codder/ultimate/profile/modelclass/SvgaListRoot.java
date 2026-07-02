package com.codder.ultimate.profile.modelclass;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class SvgaListRoot {

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

        @SerializedName("diamond")
        private double diamond;

        @Override
        public String toString() {
            return "DataItem{" +
                    "image='" + image + '\'' +
                    ", createdAt='" + createdAt + '\'' +
                    ", diamond=" + diamond +
                    ", name='" + name + '\'' +
                    ", isSelected=" + isSelected +
                    ", id='" + id + '\'' +
                    ", isPurchase=" + isPurchase +
                    ", type='" + type + '\'' +
                    ", updatedAt='" + updatedAt + '\'' +
                    '}';
        }

        @SerializedName("name")
        private String name;

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        @SerializedName("isSelected")
        private boolean isSelected;

        @SerializedName("_id")
        private String id;

        public void setPurchase(boolean purchase) {
            isPurchase = purchase;
        }

        @SerializedName("isPurchase")
        private boolean isPurchase;

        @SerializedName("type")
        private String type;

        @SerializedName("updatedAt")
        private String updatedAt;

        public String getValidationTag() {
            return validationTag;
        }

        @SerializedName("validationTag")
        private String validationTag;

        public String getThumbnail() {
            return thumbnail;
        }

        @SerializedName("thumbnail")
        private String thumbnail;


        public String getImage() {
            return image;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public double getDiamond() {
            return diamond;
        }

        public String getName() {
            return name;
        }

        public boolean isIsSelected() {
            return isSelected;
        }

        public String getId() {
            return id;
        }

        public boolean isIsPurchase() {
            return isPurchase;
        }

        public String getType() {
            return type;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataItem dataItem = (DataItem) o;
            return isIsPurchase() == dataItem.isIsPurchase() &&
                    isIsSelected() == dataItem.isIsSelected() &&
                    Objects.equals(id, dataItem.id) &&
                    Objects.equals(name, dataItem.name); // add other fields as needed
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, isIsPurchase(), isIsSelected(), name);
        }

    }

}