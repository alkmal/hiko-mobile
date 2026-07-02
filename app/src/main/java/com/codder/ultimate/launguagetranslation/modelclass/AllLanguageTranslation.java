package com.codder.ultimate.launguagetranslation.modelclass;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class AllLanguageTranslation {

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

		@SerializedName("translations")
		private Translations translations;

		@SerializedName("module")
		private String module;

		@SerializedName("_id")
		private String id;

		@SerializedName("languageCode")
		private String languageCode;

		@SerializedName("version")
		private Version version;

		@SerializedName("updatedAt")
		private String updatedAt;

		public String getCreatedAt(){
			return createdAt;
		}

		public Translations getTranslations(){
			return translations;
		}

		public String getModule(){
			return module;
		}

		public String getId(){
			return id;
		}

		public String getLanguageCode(){
			return languageCode;
		}

		public Version getVersion(){
			return version;
		}

		public String getUpdatedAt(){
			return updatedAt;
		}
	}

	public static class Translations{

		@SerializedName("thank_you")
		private String thankYou;

		@SerializedName("hello")
		private String hello;

		@SerializedName("welcome")
		private String welcome;

		@SerializedName("bye")
		private String bye;

		public String getThankYou(){
			return thankYou;
		}

		public String getHello(){
			return hello;
		}

		public String getWelcome(){
			return welcome;
		}

		public String getBye(){
			return bye;
		}
	}

	public static class Version{

		@SerializedName("patch")
		private int patch;

		@SerializedName("major")
		private int major;

		@SerializedName("minor")
		private int minor;

		public int getPatch(){
			return patch;
		}

		public int getMajor(){
			return major;
		}

		public int getMinor(){
			return minor;
		}
	}
}