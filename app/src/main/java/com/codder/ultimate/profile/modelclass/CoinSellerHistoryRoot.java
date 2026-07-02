package com.codder.ultimate.profile.modelclass;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class CoinSellerHistoryRoot {

    @SerializedName("total")
    private double total;

    @SerializedName("history")
    private List<HistoryItem> history;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public double getTotal(){
        return total;
    }

    public List<HistoryItem> getHistory(){
        return history;
    }

    public String getMessage(){
        return message;
    }

    public boolean isStatus(){
        return status;
    }

    public static class HistoryItem{

        @SerializedName("date")
        private String date;

        @SerializedName("image")
        private String image;

        @SerializedName("country")
        private String country;

        @SerializedName("gender")
        private String gender;

        @SerializedName("name")
        private String name;

        @SerializedName("_id")
        private String id;

        @SerializedName("email")
        private String email;

        @SerializedName("coin")
        private double coin;

        @SerializedName("username")
        private String username;

        @SerializedName("uniqueId")
        private String uniqueId;

        public String getUniqueId() {
            return uniqueId;
        }

        public String getDate(){
            return date;
        }

        public String getImage(){
            return image;
        }

        public String getCountry(){
            return country;
        }

        public String getGender(){
            return gender;
        }

        public String getName(){
            return name;
        }

        public String getId(){
            return id;
        }

        public String getEmail(){
            return email;
        }

        public double getCoin(){
            return coin;
        }

        public String getUsername(){
            return username;
        }
    }
}