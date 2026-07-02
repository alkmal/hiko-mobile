package com.codder.ultimate.profile.modelclass;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SettingRoot {

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    @SerializedName("setting")
    private Setting setting;

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public Setting getSetting() {
        return setting;
    }

    public static class Setting {


        @SerializedName("isFlutterwaveEnabled")
        private boolean FlutterwaveAndroidEnabled;


        @SerializedName("flutterWaveId")
        private String FlutterwaveSecretKey;


        public boolean isFlutterwaveAndroidEnabled() {
            return FlutterwaveAndroidEnabled;
        }


        public String getFlutterwaveSecretKey() {
            return FlutterwaveSecretKey;
        }

        @SerializedName("paypalAndroidEnabled")
        private boolean paypalAndroidEnabled;

        @SerializedName("paypalClientId")
        private String paypalClientId;

        @SerializedName("paypalSecretKey")
        private String paypalSecretKey;

        @SerializedName("razorPayAndroidEnabled")
        private boolean razorPayAndroidEnabled;

        @SerializedName("razorPayId")
        private String razorPayId;

        @SerializedName("razorSecretKey")
        private String razorSecretKey;

        @SerializedName("cashfreeAndroidEnabled")
        private boolean cashfreeAndroidEnabled;

        @SerializedName("cashfreeClientId")
        private String cashfreeClientId;

        @SerializedName("cashfreeClientSecret")
        private String cashfreeClientSecret;

        @SerializedName("paystackAndroidEnabled")
        private boolean paystackAndroidEnabled;

        @SerializedName("paystackPublicKey")
        private String paystackPublicKey;

        @SerializedName("paystackSecretKey")
        private String paystackSecretKey;

        public boolean isPaystackAndroidEnabled() {
            return paystackAndroidEnabled;
        }

        public String getPaystackPublicKey() {
            return paystackPublicKey;
        }

        public String getPaystackSecretKey() {
            return paystackSecretKey;
        }

        public boolean isCashfreeAndroidEnabled() {
            return cashfreeAndroidEnabled;
        }

        public String getCashfreeClientId() {
            return cashfreeClientId;
        }

        public String getCashfreeClientSecret() {
            return cashfreeClientSecret;
        }

        public boolean isRazorPayAndroidEnabled() {
            return razorPayAndroidEnabled;
        }

        public String getRazorPayId() {
            return razorPayId;
        }

        public String getRazorSecretKey() {
            return razorSecretKey;
        }

        public boolean isPaypalAndroidEnabled() {
            return paypalAndroidEnabled;
        }

        public String getPaypalClientId() {
            return paypalClientId;
        }

        public String getPaypalSecretKey() {
            return paypalSecretKey;
        }

        @SerializedName("isAppActive")
        private boolean isAppActive;

        @SerializedName("callCharge")
        private double callCharge;

        @SerializedName("maxSecondForVideo")
        private int maxSecondForVideo;

        @SerializedName("googlePlayEmail")
        private String googlePlayEmail;

        @SerializedName("minRcoinForCashOut")
        private int minRcoinForCashOut;

        @SerializedName("stripeSwitch")
        private boolean stripeSwitch;

        @SerializedName("loginBonus")
        private int loginBonus;

        @SerializedName("privacyPolicyLink")
        private String privacyPolicyLink;

        @SerializedName("termsAndConditionLink")
        private String termsAndConditionLink;

        @SerializedName("aboutUsLink")
        private String aboutUsLink;

        @SerializedName("agoraKey")
        private String agoraKey;

        @SerializedName("rCoinForDiamond")
        private double rCoinForDiamond;

        @SerializedName("agoraCertificate")
        private String agoraCertificate;

        @SerializedName("googlePlaySwitch")
        private boolean googlePlaySwitch;

        @SerializedName("freeDiamondForAd")
        private int freeDiamondForAd;

        @SerializedName("privacyPolicyText")
        private String privacyPolicyText;

   /*     @SerializedName("currency")
        private Currency currency;*/

        @SerializedName("currency")
        private JsonElement currency; // Use JsonElement to handle both cases (String or Object)

        public Currency getCurrency() {
            if (currency != null) {
                if (currency.isJsonObject()) {
                    return new Gson().fromJson(currency, Currency.class);  // Deserialize as an object
                } else if (currency.isJsonPrimitive()) {
                    String currencyCode = currency.getAsString();  // If it's a string, wrap it in a Currency object
                    Currency currencyObj = new Currency();
                    currencyObj.setCurrencyCode(currencyCode);  // Set the currency code in the Currency object
                    return currencyObj;
                }
            }
            return null;  // Return null if currency is not set
        }


        @SerializedName("googlePlayKey")
        private String googlePlayKey;

        @SerializedName("referralBonus")
        private int referralBonus;

        @SerializedName("stripeSecretKey")
        private String stripeSecretKey;

        @SerializedName("stripePublishableKey")
        private String stripePublishableKey;

        @SerializedName("maxAdPerDay")
        private int maxAdPerDay;

        @SerializedName("chatCharge")
        private int chatCharge;

        @SerializedName("_id")
        private String id;

        @SerializedName("rCoinForCashOut")
        private double rCoinForCaseOut;

        @SerializedName("paymentGateway")
        private List<String> paymentGateway;

        @SerializedName("version")
        private int version;

        @SerializedName("locationApiKey")
        private String locationApiKey;


        @SerializedName("game")
        private List<Game> game;

        public String getLocationApiKey() {
            return locationApiKey;
        }

        public double getFemaleCallCharge() {
            return femaleCallCharge;
        }

        public double getMaleCallCharge() {
            return maleCallCharge;
        }

        @SerializedName("femaleCallCharge")
        private double femaleCallCharge;

        @SerializedName("maleCallCharge")
        private double maleCallCharge;

        @SerializedName("audioCallChargeMale")
        private double audioCallChargeMale;
        @SerializedName("audioCallChargeFemale")
        private double audioCallChargeFemale;

        public double getAudioCallChargeMale() {
            return audioCallChargeMale;
        }

        public double getAudioCallChargeFemale() {
            return audioCallChargeFemale;
        }

        @SerializedName("bothRandomCallRate")
        private double bothRandomCallRate;

        @SerializedName("maleRandomCallRate")
        private double maleRandomCallRate;

        @SerializedName("femaleRandomCallRate")
        private double femaleRandomCallRate;

        public double getBothRandomCallRate() {
            return bothRandomCallRate;
        }

        public double getMaleRandomCallRate() {
            return maleRandomCallRate;
        }

        public double getFemaleRandomCallRate() {
            return femaleRandomCallRate;
        }

        public boolean isIsAppActive() {
            return isAppActive;
        }

        public List<Game> getGame() {
            return game;
        }

        public double getCallCharge() {
            return callCharge;
        }

        public int getMaxSecondForVideo() {
            return maxSecondForVideo;
        }

        public String getGooglePlayEmail() {
            return googlePlayEmail;
        }

        public int getMinRcoinForCashOut() {
            return minRcoinForCashOut;
        }

        public boolean isStripeSwitch() {
            return stripeSwitch;
        }

        public int getLoginBonus() {
            return loginBonus;
        }


        public String getPrivacyPolicyLink() {
            return privacyPolicyLink;
        }

        public String getTermsAndConditionLink() {
            return termsAndConditionLink;
        }

        public String getAboutUsLink() {
            return aboutUsLink;
        }

        public String getAgoraKey() {
            return agoraKey;
        }

        public double getRCoinForDiamond() {
            return rCoinForDiamond;
        }

        public String getAgoraCertificate() {
            return agoraCertificate;
        }

        public boolean isGooglePlaySwitch() {
            return googlePlaySwitch;
        }

        public int getFreeDiamondForAd() {
            return freeDiamondForAd;
        }

        public String getPrivacyPolicyText() {
            return privacyPolicyText;
        }

//        public Currency getCurrency() {
//            return currency;
//        }


        public String getGooglePlayKey() {
            return googlePlayKey;
        }

        public int getReferralBonus() {
            return referralBonus;
        }

        public String getStripeSecretKey() {
            return stripeSecretKey;
        }

        public String getStripePublishableKey() {
            return stripePublishableKey;
        }

        public int getMaxAdPerDay() {
            return maxAdPerDay;
        }

        public int getChatCharge() {
            return chatCharge;
        }

        public String getId() {
            return id;
        }

        public double getRCoinForCaseOut() {
            return rCoinForCaseOut;
        }

        public List<String> getPaymentGateway() {
            return paymentGateway;
        }

        public int getLetestVersonCode() {
            return version;
        }
    }

    public class Game {

        @SerializedName("name")
        private String name;
        @SerializedName("link")
        private String link;
        @SerializedName("_id")
        private String id;
        @SerializedName("createdAt")
        private String createdAt;
        @SerializedName("updatedAt")
        private String updatedAt;
        @SerializedName("image")
        private String image;

        public String getName() {
            return name;
        }

        public String getLink() {
            return link;
        }

        public String getId() {
            return id;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getImage() {
            return image;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Game game = (Game) o;
            return id.equals(game.id) &&
                    name.equals(game.name) &&
                    link.equals(game.link) &&
                    createdAt.equals(game.createdAt) &&
                    updatedAt.equals(game.updatedAt) &&
                    image.equals(game.image);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(id, name, link, createdAt, updatedAt, image);
        }

    }

    public static class Currency {

        @SerializedName("symbol")
        private String symbol;

        @SerializedName("isDefault")
        private boolean isDefault;

        @SerializedName("countryCode")
        private String countryCode;

        @SerializedName("name")
        private String name;

        @SerializedName("currencyCode")
        private String currencyCode;

        public String getSymbol() {
            return symbol;
        }

        public boolean isIsDefault() {
            return isDefault;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public String getName() {
            return name;
        }

        public String getCurrencyCode() {
            return currencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }
    }
}