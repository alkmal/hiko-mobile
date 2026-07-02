package com.codder.ultimate.chat.modelclass;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class ChatSuggestion{

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

		@SerializedName("createdAt")
		private String createdAt;

		@SerializedName("_id")
		private String id;

		@SerializedName("message")
		private String message;

		@SerializedName("updatedAt")
		private String updatedAt;

		public String getCreatedAt(){
			return createdAt;
		}

		public String getId(){
			return id;
		}

		public String getMessage(){
			return message;
		}

		public String getUpdatedAt(){
			return updatedAt;
		}
	}
}