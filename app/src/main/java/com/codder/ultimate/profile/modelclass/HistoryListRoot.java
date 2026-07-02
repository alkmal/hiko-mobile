package com.codder.ultimate.profile.modelclass;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HistoryListRoot {

    @SerializedName("outgoingTotal")
    private double outgoingTotal;

    @SerializedName("incomeTotal")
    private double incomeTotal;

    @SerializedName("history")
    private List<HistoryItem> history;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public double getOutgoingTotal() {
        return outgoingTotal;
    }

    public double getIncomeTotal() {
        return incomeTotal;
    }

    public List<HistoryItem> getHistory() {
        return history;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public static class HistoryItem {
        @Override
        public String toString() {
            return "HistoryItem{" +
                    "date='" + date + '\'' +
                    ", diamond=" + diamond +
                    ", rCoin=" + rCoin +
                    ", id='" + id + '\'' +
                    ", isAdd=" + isAdd +
                    ", type=" + type +
                    ", userName='" + userName + '\'' +
                    ", userId='" + userId + '\'' +
                    ", paymentGateway=" + paymentGateway +
                    '}';
        }

        @SerializedName("date")
        private String date;

        @SerializedName("diamond")
        private double diamond = 0;

        @SerializedName("rCoin")
        private double rCoin = 0;

        @SerializedName("_id")
        private String id;

        @SerializedName("isAdd")
        private boolean isAdd;

        @SerializedName("type")
        private int type;

        @SerializedName("userName")
        private String userName;

        @SerializedName("userId")
        private String userId;

        @SerializedName("paymentGateway")
        private Object paymentGateway;

        public String getDate() {
            return date;
        }

        public double getDiamond() {
            return diamond;
        }

        public double getRCoin() {
            return rCoin;
        }

        public String getId() {
            return id;
        }

        public boolean isIsAdd() {
            return isAdd;
        }

        public int getType() {
            return type;
        }

        public String getUserName() {
            return userName;
        }

        public String getUserId() {
            return userId;
        }

        public Object getPaymentGateway() {
            return paymentGateway;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HistoryItem that = (HistoryItem) o;

            return Double.compare(that.diamond, diamond) == 0 &&
                    Double.compare(that.rCoin, rCoin) == 0 &&
                    isAdd == that.isAdd &&
                    type == that.type &&
                    id.equals(that.id) &&
                    date.equals(that.date) &&
                    ((userName == null && that.userName == null) || (userName != null && userName.equals(that.userName))) &&
                    ((userId == null && that.userId == null) || (userId != null && userId.equals(that.userId)));
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(id, date, diamond, rCoin, isAdd, type, userName, userId);
        }

    }
}