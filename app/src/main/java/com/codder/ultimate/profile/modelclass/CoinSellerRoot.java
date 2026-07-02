package com.codder.ultimate.profile.modelclass;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CoinSellerRoot {

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    @SerializedName("coinSeller")
    private List<CoinSellerItem> coinSeller;

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public List<CoinSellerItem> getCoinSeller() {
        return coinSeller;
    }

    public static class CoinSellerItem {

        @SerializedName("lastLogin")
        private String lastLogin;

        @SerializedName("image")
        private String image;

        @SerializedName("spendCoin")
        private int spendCoin;

        @SerializedName("mobileNumber")
        private String mobileNo;

        @SerializedName("receiveCoin")
        private int receiveCoin;

        @SerializedName("isShow")
        private boolean isShow;

        @SerializedName("isDisable")
        private boolean isDisable;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("password")
        private String password;

        @SerializedName("countryCode")
        private String countryCode;

        @SerializedName("name")
        private String name;

        @SerializedName("_id")
        private String id;

        @SerializedName("email")
        private String email;

        @SerializedName("coin")
        private double coin;

        @SerializedName("updatedAt")
        private String updatedAt;

        public String getLastLogin() {
            return lastLogin;
        }

        public String getImage() {
            return image;
        }

        public int getSpendCoin() {
            return spendCoin;
        }

        public String getMobileNo() {
            return mobileNo;
        }

        public int getReceiveCoin() {
            return receiveCoin;
        }

        public boolean isIsShow() {
            return isShow;
        }

        public boolean isIsDisable() {
            return isDisable;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getPassword() {
            return password;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public double getCoin() {
            return coin;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CoinSellerItem that = (CoinSellerItem) o;
            return coin == that.coin &&
                    spendCoin == that.spendCoin &&
                    receiveCoin == that.receiveCoin &&
                    isShow == that.isShow &&
                    isDisable == that.isDisable &&
                    safeEquals(id, that.id) &&
                    safeEquals(name, that.name) &&
                    safeEquals(email, that.email) &&
                    safeEquals(image, that.image) &&
                    safeEquals(mobileNo, that.mobileNo) &&
                    safeEquals(createdAt, that.createdAt) &&
                    safeEquals(updatedAt, that.updatedAt) &&
                    safeEquals(lastLogin, that.lastLogin) &&
                    safeEquals(countryCode, that.countryCode);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(id, name, email, image, mobileNo, coin, spendCoin, receiveCoin,
                    isShow, isDisable, createdAt, updatedAt, lastLogin, countryCode);
        }

        private static boolean safeEquals(Object a, Object b) {
            return a == null ? b == null : a.equals(b);
        }
    }
}
