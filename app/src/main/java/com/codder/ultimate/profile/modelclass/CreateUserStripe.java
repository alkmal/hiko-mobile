package com.codder.ultimate.profile.modelclass;

import com.google.gson.annotations.SerializedName;

public class CreateUserStripe {

	@SerializedName("publishableKey")
	private String publishableKey;

	@SerializedName("ephemeralKey")
	private String ephemeralKey;

	@SerializedName("paymentIntent")
	private String paymentIntent;

	@SerializedName("status")
	private boolean status;

	@SerializedName("customer")
	private String customer;

	@SerializedName("clientSecret")
	private String clientSecret;


	public String getPublishableKey(){
		return publishableKey;
	}

	public String getEphemeralKey(){
		return ephemeralKey;
	}

	public String getPaymentIntent(){
		return paymentIntent;
	}

	public boolean isStatus(){
		return status;
	}

	public String getCustomer(){
		return customer;
	}

	public String getClientSecret() {
		return clientSecret;
	}
}