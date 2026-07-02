package com.codder.ultimate.modelclass;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BroadcastBannerRoot {

	@SerializedName("data")
	private List<BroadcastBannerItem> data;

	@SerializedName("message")
	private String message;

	@SerializedName("status")
	private boolean status;

	public List<BroadcastBannerItem> getData(){
		return data;
	}

	public String getMessage(){
		return message;
	}

	public boolean isStatus(){
		return status;
	}

	public static class BroadcastBannerItem{

		@SerializedName("createdAt")
		private String createdAt;

		@SerializedName("redirectUrl")
		private String redirectUrl;

		@SerializedName("imageUrl")
		private String imageUrl;

		@SerializedName("_id")
		private String id;

		@SerializedName("isActive")
		private boolean isActive;

		public String getCreatedAt(){
			return createdAt;
		}

		public String getRedirectUrl(){
			return redirectUrl;
		}

		public String getImageUrl(){
			return imageUrl;
		}

		public String getId(){
			return id;
		}

		public boolean isIsActive(){
			return isActive;
		}
	}
}