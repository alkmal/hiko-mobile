package com.codder.ultimate.modelclass;

import com.google.gson.annotations.SerializedName;

public class UserRoot {

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private User user;

    @SerializedName("status")
    private boolean status;

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public boolean isStatus() {
        return status;
    }

    public static class AccessibleFunction {

        @SerializedName("uploadPost")
        private boolean uploadPost;

        @SerializedName("freeCall")
        private boolean freeCall;

        @SerializedName("uploadVideo")
        private boolean uploadVideo;

        @SerializedName("cashOut")
        private boolean cashOut;

        @SerializedName("liveStreaming")
        private boolean liveStreaming;

        public boolean isUploadPost() {
            return uploadPost;
        }

        public boolean isFreeCall() {
            return freeCall;
        }

        public boolean isUploadVideo() {
            return uploadVideo;
        }

        public boolean isCashOut() {
            return cashOut;
        }

        public boolean isLiveStreaming() {
            return liveStreaming;
        }

        public void setUploadPost(boolean uploadPost) {
            this.uploadPost = uploadPost;
        }

        public void setFreeCall(boolean freeCall) {
            this.freeCall = freeCall;
        }

        public void setUploadVideo(boolean uploadVideo) {
            this.uploadVideo = uploadVideo;
        }

        public void setCashOut(boolean cashOut) {
            this.cashOut = cashOut;
        }

        public void setLiveStreaming(boolean liveStreaming) {
            this.liveStreaming = liveStreaming;
        }
    }

    public static class Ad {

        @SerializedName("date")
        private String date;

        @SerializedName("count")
        private int count;

        public String getDate() {
            return date;
        }

        public int getCount() {
            return count;
        }
    }

    public static class Level {

        @SerializedName("image")
        private String image;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("accessibleFunction")
        private AccessibleFunction accessibleFunction;

        @SerializedName("name")
        private String name;

        @SerializedName("_id")
        private String id;

        @SerializedName("coin")
        private double coin;

        @SerializedName("updatedAt")
        private String updatedAt;

        public String getImage() {
            return image;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public AccessibleFunction getAccessibleFunction() {
            return accessibleFunction;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public double getCoin() {
            return coin;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public void setAccessibleFunction(AccessibleFunction accessibleFunction) {
            this.accessibleFunction = accessibleFunction;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setCoin(double coin) {
            this.coin = coin;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    public static class Notification {

        @SerializedName("likeCommentShare")
        private boolean likeCommentShare;

        @SerializedName("newFollow")
        private boolean newFollow;

        @SerializedName("favoriteLive")
        private boolean favoriteLive;

        @SerializedName("message")
        private boolean message;

        public boolean isLikeCommentShare() {
            return likeCommentShare;
        }

        public boolean isNewFollow() {
            return newFollow;
        }

        public boolean isFavoriteLive() {
            return favoriteLive;
        }

        public boolean isMessage() {
            return message;
        }
    }

    public static class Plan {

        @SerializedName("planId")
        private String planId;

        @SerializedName("planStartDate")
        private String planStartDate;

        public String getPlanId() {
            return planId;
        }

        public String getPlanStartDate() {
            return planStartDate;
        }
    }

    public static class User {

        @SerializedName("isBd")
        private boolean isBd;

        @SerializedName("bdLoginString")
        private String bdLoginString;

        public boolean isBd() {
            return isBd;
        }

        public String getBdLoginString() {
            return bdLoginString;
        }

        @SerializedName("spentCoin")
        private double spentCoin;
        @SerializedName("analyticDate")
        private String analyticDate;

        @SerializedName("isReferral")
        private boolean isReferral;

        @SerializedName("referralCount")
        private int referralCount;

        @SerializedName("country")
        private String country;
        @SerializedName("countryFlagImage")
        private String countryFlagImage;

        public String getCountryFlagImage() {
            return countryFlagImage;
        }

        @SerializedName("lastLogin")
        private String lastLogin;

        @SerializedName("isBlock")
        private boolean isBlock;

        @SerializedName("gender")
        private String gender;

        @SerializedName("rCoin")
        private double rCoin;

        @SerializedName("loginType")
        private int loginType;

        @SerializedName("channel")
        private String channel;

        @SerializedName("bio")
        private String bio;

        @SerializedName("isOnline")
        private boolean isOnline;

        @SerializedName("video")
        private int video;

        @SerializedName("withdrawalRcoin")
        private double withdrawalRcoin;

        @SerializedName("notification")
        private Notification notification;

        public double getrCoin() {
            return rCoin;
        }

        @SerializedName("createdAt")
        private String createdAt;

        public void setrCoin(double rCoin) {
            this.rCoin = rCoin;
        }

        @SerializedName("post")
        private int post;

        @SerializedName("identity")
        private String identity;

        @SerializedName("referralCode")
        private String referralCode;

        @SerializedName("plan")
        private Plan plan;

        @SerializedName("fcmToken")
        private String fcmToken;

        @SerializedName("email")
        private String email;

        @SerializedName("updatedAt")
        private String updatedAt;

        @SerializedName("image")
        private String image;

        @SerializedName("isBusy")
        private boolean isBusy;

        @SerializedName("ad")
        private Ad ad;

        @SerializedName("level")
        private Level level;

        @SerializedName("ip")
        private String ip;

        @SerializedName("isVIP")
        private boolean isVIP;

        @SerializedName("token")
        private String token;

        @SerializedName("diamond")
        private double diamond;

        @SerializedName("followers")
        private int followers;

        @SerializedName("following")
        private int following;

        @SerializedName("name")
        private String name;

        @SerializedName("_id")
        private String id;

        @SerializedName("age")
        private int age;

        @SerializedName("username")
        private String username;

        @SerializedName("uniqueId")
        private String uniqueId;

        @SerializedName("enableToLive")
        private boolean enableToLive;

        @SerializedName("coverImage")
        private String coverImage;

        @SerializedName("avatarFrameImage")
        private String avatarFrameImage;

        @SerializedName("svgaImage")
        private String svgaImage;
        @SerializedName("avatarFrame")
        private AvatarFrame avatarFrame;
        @SerializedName("liveJoinSvga")
        private LiveJoinSvga liveJoinSvga;

        @SerializedName("isCoinSeller")
        private boolean isCoinSeller;

        @SerializedName("isAgency")
        private boolean isAgency;

        @SerializedName("isHost")
        private boolean isHost;

        @SerializedName("hostLoginString")
        private String hostLoginString;

        @SerializedName("agencyLoginString")
        private String agencyLoginString;

        public User(double spentCoin, String analyticDate, boolean isReferral, int referralCount, String country, String lastLogin, boolean isBlock, String gender, double rCoin, int loginType, String channel, String bio, boolean isOnline, int video, int withdrawalRcoin, Notification notification, String createdAt, int post, String identity, String referralCode, Plan plan, String fcmToken, String email, String updatedAt, String image, boolean isBusy, Ad ad, Level level, String ip, boolean isVIP, String token, long diamond, int followers, int following, String name, String id, int age, String username, boolean isFake, String uniqueId) {
            this.spentCoin = spentCoin;
            this.analyticDate = analyticDate;
            this.isReferral = isReferral;
            this.referralCount = referralCount;
            this.country = country;
            this.lastLogin = lastLogin;
            this.isBlock = isBlock;
            this.gender = gender;
            this.rCoin = rCoin;
            this.loginType = loginType;
            this.channel = channel;
            this.bio = bio;
            this.isOnline = isOnline;
            this.video = video;
            this.withdrawalRcoin = withdrawalRcoin;
            this.notification = notification;
            this.createdAt = createdAt;
            this.post = post;
            this.identity = identity;
            this.referralCode = referralCode;
            this.plan = plan;
            this.fcmToken = fcmToken;
            this.email = email;
            this.updatedAt = updatedAt;
            this.image = image;
            this.isBusy = isBusy;
            this.ad = ad;
            this.level = level;
            this.ip = ip;
            this.isVIP = isVIP;
            this.token = token;
            this.diamond = diamond;
            this.followers = followers;
            this.following = following;
            this.name = name;
            this.id = id;
            this.age = age;
            this.username = username;
            this.uniqueId = uniqueId;
        }

        public void setDiamond(double diamond) {
            this.diamond = diamond;
        }

        public boolean isAgency() {
            return isAgency;
        }

        public boolean isHost() {
            return isHost;
        }

        public String getHostLoginString() {
            return hostLoginString;
        }

        public String getAgencyLoginString() {
            return agencyLoginString;
        }

        public boolean isCoinSeller() {
            return isCoinSeller;
        }

        public String getSvgaImage() {
            return svgaImage;
        }

        public String getAvatarFrameImage() {
            return avatarFrameImage;
        }
        public AvatarFrame getAvatarFrame() {
            return avatarFrame;
        }

        public LiveJoinSvga getLiveJoinSvga() {
            return liveJoinSvga;
        }

        public String getCoverImage() {
            return coverImage;
        }

        public String getUniqueId() {
            return uniqueId;
        }

        public boolean isIsReferral() {
            return isReferral;
        }

        public int getReferralCount() {
            return referralCount;
        }

        public String getCountry() {
            return country;
        }

        public String getLastLogin() {
            return lastLogin;
        }

        public boolean isIsBlock() {
            return isBlock;
        }

        public String getGender() {
            return gender;
        }

        public double getRCoin() {
            return rCoin;
        }

        public int getLoginType() {
            return loginType;
        }

        public String getChannel() {
            return channel;
        }

        public String getBio() {
            return bio;
        }

        public boolean isIsOnline() {
            return isOnline;
        }

        public int getVideo() {
            return video;
        }

        public double getWithdrawalRcoin() {
            return withdrawalRcoin;
        }

        public Notification getNotification() {
            return notification;
        }

        public double getSpentCoin() {
            return spentCoin;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getAnalyticDate() {
            return analyticDate;
        }

        public int getPost() {
            return post;
        }

        public String getIdentity() {
            return identity;
        }

        public String getReferralCode() {
            return referralCode;
        }

        public Plan getPlan() {
            return plan;
        }

        public String getFcmToken() {
            return fcmToken;
        }

        public String getEmail() {
            return email;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getImage() {
            return image;
        }

        public boolean isIsBusy() {
            return isBusy;
        }

        public Ad getAd() {
            return ad;
        }

        public Level getLevel() {
            return level;
        }

        public String getIp() {
            return ip;
        }

        public boolean isIsVIP() {
            return isVIP;
        }

        public String getToken() {
            return token;
        }

        public double getDiamond() {
            return diamond;
        }



        public int getFollowers() {
            return followers;
        }

        public int getFollowing() {
            return following;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public int getAge() {
            return age;
        }

        public String getUsername() {
            return username;
        }

        public boolean getEnableToLive() {
            return enableToLive;
        }

        public void setEnableToLive(boolean enableToLive) {
            this.enableToLive = enableToLive;
        }



        public static class LiveJoinSvga {

            @SerializedName("image")
            private String image;

            @SerializedName("name")
            private String name;

            @SerializedName("_id")
            private String id;

            public String getImage() {
                return image;
            }

            public String getName() {
                return name;
            }

            public String getId() {
                return id;
            }
        }

        public static class AvatarFrame {
            @SerializedName("image")
            private String image;

            @SerializedName("name")
            private String name;

            @SerializedName("_id")
            private String id;

            public String getImage() {
                return image;
            }

            public String getName() {
                return name;
            }

            public String getId() {
                return id;
            }
        }

    }

}