package com.codder.ultimate.profile.modelclass;

import com.google.gson.annotations.SerializedName;

public class CoinRecordRoot {

    @SerializedName("diamond")
    private Diamond diamond;

    @SerializedName("rCoin")
    private RCoin rCoin;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public Diamond getDiamond() {
        return diamond;
    }

    public RCoin getRCoin() {
        return rCoin;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public static class Diamond {

        @SerializedName("income")
        private double income;

        @SerializedName("outgoing")
        private double outgoing;

        public double getIncome() {
            return income;
        }

        public double getOutgoing() {
            return outgoing;
        }
    }

    public static class RCoin {

        @SerializedName("income")
        private double income;

        @SerializedName("outgoing")
        private double outgoing;

        public double getIncome() {
            return income;
        }

        public double getOutgoing() {
            return outgoing;
        }
    }
}