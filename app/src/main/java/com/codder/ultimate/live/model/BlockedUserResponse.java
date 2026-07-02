package com.codder.ultimate.live.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class BlockedUserResponse{

	@SerializedName("total")
	private int total;

	@SerializedName("blockedByUsers")
	private List<BlockedByUsersItem> blockedByUsers;

	@SerializedName("message")
	private String message;

	@SerializedName("status")
	private boolean status;

	public int getTotal(){
		return total;
	}

	public List<BlockedByUsersItem> getBlockedByUsers(){
		return blockedByUsers;
	}

	public String getMessage(){
		return message;
	}

	public boolean isStatus(){
		return status;
	}

	public static class BlockedByUsersItem{

		@SerializedName("_id")
		private String id;

		@SerializedName("userId")
		private UserId userId;

		public String getId(){
			return id;
		}

		public UserId getUserId(){
			return userId;
		}
	}

	public static class UserId{

		@SerializedName("image")
		private String image;

		@SerializedName("country")
		private String country;

		@SerializedName("countryFlagImage")
		private String countryFlagImage;

		@SerializedName("name")
		private String name;

		@SerializedName("_id")
		private String id;

		public String getImage(){
			return image;
		}

		public String getCountry(){
			return country;
		}

		public String getCountryFlagImage(){
			return countryFlagImage;
		}

		public String getName(){
			return name;
		}

		public String getId(){
			return id;
		}
	}
}