package com.codder.ultimate.modelclass;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BlockedUserListRoot {

	@SerializedName("blockedUsers")
	private List<BlockedUsersItem> blockedUsers;

	@SerializedName("message")
	private String message;

	@SerializedName("status")
	private boolean status;

	@SerializedName("total")
	private int total;

	public int getTotal() {
		return total;
	}

	public List<BlockedUsersItem> getBlockedUsers(){
		return blockedUsers;
	}

	public String getMessage(){
		return message;
	}

	public boolean isStatus(){
		return status;
	}

	public static class BlockedUsersItem{

		@SerializedName("_id")
		private String id;

		@SerializedName("toUserId")
		private ToUserId toUserId;

		public String getId(){
			return id;
		}

		public ToUserId getToUserId(){
			return toUserId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			BlockedUsersItem that = (BlockedUsersItem) o;

			if (id != null ? !id.equals(that.id) : that.id != null) return false;
			return toUserId != null ? toUserId.equals(that.toUserId) : that.toUserId == null;
		}

		@Override
		public int hashCode() {
			int result = id != null ? id.hashCode() : 0;
			result = 31 * result + (toUserId != null ? toUserId.hashCode() : 0);
			return result;
		}
	}

	public static class ToUserId{

		@SerializedName("country")
		private String country;

		@SerializedName("image")
		private String image;

		@SerializedName("countryFlagImage")
		private String countryFlagImage;

		@SerializedName("name")
		private String name;

		@SerializedName("username")
		private String username;

		@SerializedName("_id")
		private String id;

		public String getCountry(){
			return country;
		}

		public String getImage(){
			return image;
		}

		public String getCountryFlagImage(){
			return countryFlagImage;
		}

		public String getName(){
			return name;
		}

		public String getUsername() {
			return username;
		}

		public String getId(){
			return id;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ToUserId toUserId = (ToUserId) o;

			if (!id.equals(toUserId.id)) return false;
			if (!name.equals(toUserId.name)) return false;
			if (!country.equals(toUserId.country)) return false;
			if (!image.equals(toUserId.image)) return false;
			return countryFlagImage.equals(toUserId.countryFlagImage);
		}

		@Override
		public int hashCode() {
			int result = id.hashCode();
			result = 31 * result + name.hashCode();
			result = 31 * result + country.hashCode();
			result = 31 * result + image.hashCode();
			result = 31 * result + countryFlagImage.hashCode();
			return result;
		}
	}

}