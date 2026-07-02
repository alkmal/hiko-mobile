package com.codder.ultimate.launguagetranslation.modelclass;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class ActiveLanguageRoot {

	@SerializedName("total")
	private int total;

	@SerializedName("docs")
	private List<DocsItem> docs;

	@SerializedName("message")
	private String message;

	@SerializedName("status")
	private boolean status;

	public int getTotal(){
		return total;
	}

	public List<DocsItem> getDocs(){
		return docs;
	}

	public String getMessage(){
		return message;
	}

	public boolean isStatus(){
		return status;
	}

	public static class DocsItem{

		@SerializedName("createdAt")
		private String createdAt;

		@SerializedName("isDefault")
		private boolean isDefault;

		@SerializedName("languageIcon")
		private String languageIcon;

		@SerializedName("_id")
		private String id;

		@SerializedName("languageTitle")
		private String languageTitle;

		@SerializedName("languageCode")
		private String languageCode;

		@SerializedName("isActive")
		private boolean isActive;

		@SerializedName("localLanguageTitle")
		private String localLanguageTitle;

		@SerializedName("errorCount")
		private int errorCount;

		@SerializedName("updatedAt")
		private String updatedAt;

		public String getCreatedAt(){
			return createdAt;
		}

		public boolean isIsDefault(){
			return isDefault;
		}

		public String getLanguageIcon(){
			return languageIcon;
		}

		public String getId(){
			return id;
		}

		public String getLanguageTitle(){
			return languageTitle;
		}

		public String getLanguageCode(){
			return languageCode;
		}

		public boolean isIsActive(){
			return isActive;
		}

		public String getLocalLanguageTitle(){
			return localLanguageTitle;
		}

		public int getErrorCount(){
			return errorCount;
		}

		public String getUpdatedAt(){
			return updatedAt;
		}
	}
}