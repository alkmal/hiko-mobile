package com.codder.ultimate.profile.modelclass;

import android.text.format.DateUtils;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ReedemListRoot {

    @SerializedName("redeem")
    private List<RedeemItem> redeem;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public List<RedeemItem> getRedeem() {
        return redeem;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public static class RedeemItem {

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("rCoin")
        private double rCoin;

        @SerializedName("description")
        private String description;

        @SerializedName("_id")
        private String id;

        @SerializedName("userId")
        private String userId;

        @SerializedName("status")
        private String status;

        @SerializedName("paymentGateway")
        private String paymentGateway;

        @SerializedName("date")
        private String date;

        @SerializedName("updatedAt")
        private String updatedAt;

        public String getCreatedAt() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                long time = sdf.parse(createdAt).getTime();
                long now = System.currentTimeMillis();
                CharSequence ago =
                        DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);

                return ago.toString();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (createdAt.equals("0 minutes ago")) {
                createdAt = "Just Now";
            }
            return createdAt;
        }

        public double getRCoin() {
            return rCoin;
        }

        public String getDescription() {
            return description;
        }

        public String getId() {
            return id;
        }

        public String getUserId() {
            return userId;
        }

        public String getStatus() {
            return status;
        }

        public String getPaymentGateway() {
            return paymentGateway;
        }

        public String getDate() {
            return date;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RedeemItem that = (RedeemItem) o;

            return Double.compare(that.rCoin, rCoin) == 0 &&
                    safeEquals(id, that.id) &&
                    safeEquals(userId, that.userId) &&
                    safeEquals(status, that.status) &&
                    safeEquals(createdAt, that.createdAt) &&
                    safeEquals(updatedAt, that.updatedAt) &&
                    safeEquals(paymentGateway, that.paymentGateway) &&
                    safeEquals(description, that.description);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(
                    id, userId, rCoin, status, createdAt, updatedAt, paymentGateway, description
            );
        }

        private boolean safeEquals(String a, String b) {
            return a == null ? b == null : a.equals(b);
        }

    }
}