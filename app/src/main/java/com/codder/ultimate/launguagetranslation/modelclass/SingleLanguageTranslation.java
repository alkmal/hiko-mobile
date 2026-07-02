package com.codder.ultimate.launguagetranslation.modelclass;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class SingleLanguageTranslation {

	@SerializedName("doc")
	private Doc doc;

	@SerializedName("message")
	private String message;

	@SerializedName("version")
	private String version;

	@SerializedName("status")
	private boolean status;

	public Doc getDoc() { return doc; }
	public String getMessage() { return message; }
	public String getVersion() { return version; }
	public boolean isStatus() { return status; }

	public static class Doc {

		@SerializedName("createdAt")
		private String createdAt;

		@SerializedName("translations")
		private Map<String, String> translations; // ← Fixed class ની જગ્યાએ Map

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

		public String getCreatedAt() { return createdAt; }
		public Map<String, String> getTranslations() { return translations; } // ← Map return
		public String getModule() { return module; }
		public String getId() { return id; }
		public String getLanguageCode() { return languageCode; }
		public Version getVersion() { return version; }
		public String getUpdatedAt() { return updatedAt; }
	}

	public static class Version {

		@SerializedName("patch")
		private int patch;

		@SerializedName("major")
		private int major;

		@SerializedName("minor")
		private int minor;

		public int getPatch() { return patch; }
		public int getMajor() { return major; }
		public int getMinor() { return minor; }

		public String getVersionString() {
			return major + "." + minor + "." + patch;
		}
	}
}