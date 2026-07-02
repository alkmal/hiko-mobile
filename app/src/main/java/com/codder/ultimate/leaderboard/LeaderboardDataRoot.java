package com.codder.ultimate.leaderboard;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LeaderboardDataRoot {

	@SerializedName("data")
	private List<DataItem> data;

	@SerializedName("message")
	private String message;

	@SerializedName("status")
	private boolean status;

	public List<DataItem> getData(){
		return data;
	}

	public String getMessage(){
		return message;
	}

	public boolean isStatus(){
		return status;
	}

	public static class DataItem{

		@SerializedName("image")
		private String image;

		@SerializedName("level")
		private String level;

		@SerializedName("name")
		private String name;

		@SerializedName("levelImage")
		private String levelImage;

		@SerializedName("totalSpentDiamond")
		private long totalSpentDiamond;

		@SerializedName("totalEarnrCoin")
		private long totalEarnrCoin;

		@SerializedName("_id")
		private String id;

		@SerializedName("userId")
		private String userId;

		@SerializedName("uniqueId")
		private int uniqueId;

		@SerializedName("finalTotalAmount")
		private long finalTotalAmount;

		public long getFinalTotalAmount() {
			return finalTotalAmount;
		}

		@SerializedName("agency")
		private Agency agency;

		public Agency getAgency() {
			return agency;
		}

		public String getImage(){
			return image;
		}

		public String getLevel(){
			return level;
		}

		public String getName(){
			return name;
		}

		public String getLevelImage(){
			return levelImage;
		}

		public long getTotalSpentDiamond(){
			return totalSpentDiamond;
		}

		public String getId(){
			return id;
		}

		public String getUserId(){
			return userId;
		}

		public int getUniqueId(){
			return uniqueId;
		}

		public long getTotalEarnrCoin() {
			return totalEarnrCoin;
		}


		public void setImage(String image) {
			this.image = image;
		}

		public void setLevel(String level) {
			this.level = level;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setLevelImage(String levelImage) {
			this.levelImage = levelImage;
		}

		public void setTotalSpentDiamond(long totalSpentDiamond) {
			this.totalSpentDiamond = totalSpentDiamond;
		}

		public void setTotalEarnrCoin(long totalEarnrCoin) {
			this.totalEarnrCoin = totalEarnrCoin;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public void setUniqueId(int uniqueId) {
			this.uniqueId = uniqueId;
		}

		public void setFinalTotalAmount(long finalTotalAmount) {
			this.finalTotalAmount = finalTotalAmount;
		}

		public void setAgency(Agency agency) {
			this.agency = agency;
		}
	}

	public static class Agency{

		@SerializedName("name")
		private String name;

		@SerializedName("image")
		private String image;

		public String getName() {
			return name;
		}

		public String getImage() {
			return image;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setImage(String image) {
			this.image = image;
		}
	}

}