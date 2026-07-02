package com.codder.ultimate.modelclass;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class SearchHistoryRoot{

	@SerializedName("total")
	private Integer total;

	@SerializedName("data")
	private List<DataItem> data;

	@SerializedName("status")
	private Boolean status;

	public Integer getTotal(){
		return total;
	}

	public List<DataItem> getData(){
		return data;
	}

	public Boolean isStatus(){
		return status;
	}

    public static class SearchedUser{

        @SerializedName("image")
        private String image;

        @SerializedName("name")
        private String name;

        @SerializedName("_id")
        private String id;

        @SerializedName("uniqueId")
        private Integer uniqueId;

        @SerializedName("username")
        private String username;

        @SerializedName("countryFlagImage")
        private String countryFlagImage;

        public String getCountryFlagImage() {
            return countryFlagImage;
        }

        public String getImage(){
            return image;
        }

        public String getName(){
            return name;
        }

        public String getId(){
            return id;
        }

        public Integer getUniqueId(){
            return uniqueId;
        }

        public String getUsername(){
            return username;
        }
    }

    public static class DataItem{

        @SerializedName("searchedBy")
        private String searchedBy;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("searchText")
        private String searchText;

        @SerializedName("searchedUser")
        private SearchedUser searchedUser;

        @SerializedName("searchCount")
        private Integer searchCount;

        @SerializedName("_id")
        private String id;

        @SerializedName("updatedAt")
        private String updatedAt;

        public String getSearchedBy(){
            return searchedBy;
        }

        public String getCreatedAt(){
            return createdAt;
        }

        public String getSearchText(){
            return searchText;
        }

        public SearchedUser getSearchedUser(){
            return searchedUser;
        }

        public Integer getSearchCount(){
            return searchCount;
        }

        public String getId(){
            return id;
        }

        public String getUpdatedAt(){
            return updatedAt;
        }
    }
}