package com.codder.ultimate.live.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchLocationRoot {

	@SerializedName("data")
	private List<DataItem> data;

	public List<DataItem> getData(){
		return data;
	}

	public static class DataItem{

		@SerializedName("continent")
		private String continent;

		@SerializedName("country")
		private String country;

		@SerializedName("county")
		private String county;

		@SerializedName("locality")
		private String locality;
		@SerializedName("label")
		private String label;

		@SerializedName("type")
		private String type;

		@SerializedName("country_code")
		private String countryCode;

		@SerializedName("name")
		private String name;

		@SerializedName("region")
		private String region;

		@SerializedName("region_code")
		private String regionCode;

		public String getContinent(){
			return continent;
		}

		public String getCountry(){
			return country;
		}

		public String getCounty(){
			return county;
		}

		public String getLocality(){
			return locality;
		}

		public String getLabel(){
			return label;
		}

		public String getType(){
			return type;
		}

		public String getCountryCode(){
			return countryCode;
		}

		public String getName(){
			return name;
		}

		public String getRegion(){
			return region;
		}

		public String getRegionCode(){
			return regionCode;
		}
	}
}