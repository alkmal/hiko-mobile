package com.codder.ultimate.live.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GiftRoot {

    @SerializedName(value = "gift", alternate = {"data"})
    private List<GiftItem> gift;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public List<GiftItem> getGift() {
        return gift;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public static class GiftItem {

        @SerializedName("image")
        private String image;

        @SerializedName("message")
        private String message;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("_id")
        private String id;

        @SerializedName("type")
        private int type;

        @SerializedName(value = "category", alternate = {"giftCategoryId"})
        private String category;

        @SerializedName("svgaImage")
        private String svgaImage;

        @SerializedName("coin")
        private double coin;

        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }

        public String getSvgaImage() {
            return svgaImage;
        }
        @SerializedName("receiverUserName")
        private List<String> receiverUserName;

        public List<String> getReceiverUserName() {
            return receiverUserName;

        }

        public void setReceiverUserName(List<String> receiverUserName) {
            this.receiverUserName = receiverUserName;
        }

        public void setName(String name) {
            this.name = name;
        }

        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        @SerializedName("updatedAt")
        private String updatedAt;

        public String getImage() {
            return image;
        }

        public String getMessage() {
            return message;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getId() {
            return id;
        }

        public int getType() {
            return type;
        }

        public String getCategory() {
            return category;
        }

        public double getCoin() {
            return coin;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        @Override
        public String toString() {
            return "GiftItem{" +
                    "image='" + image + '\'' +
                    ", createdAt='" + createdAt + '\'' +
                    ", id='" + id + '\'' +
                    ", type=" + type +
                    ", category='" + category + '\'' +
                    ", coin=" + coin +
                    ", count=" + count +
                    ", updatedAt='" + updatedAt + '\'' +
                    '}';
        }
    }
}
