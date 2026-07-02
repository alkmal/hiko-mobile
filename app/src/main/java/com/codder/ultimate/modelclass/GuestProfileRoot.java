package com.codder.ultimate.modelclass;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class GuestProfileRoot {

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

    public static class User {
        @SerializedName("image")
        private String image;

        public String getCoverImage() {
            return coverImage;
        }

        public String getCountryFlagImage() {
            return countryFlagImage;
        }

        @SerializedName("coverImage")
        private String coverImage;

        @SerializedName("countryFlagImage")
        private String countryFlagImage;
        @SerializedName("country")
        private String country;
        @SerializedName("gender")
        private String gender;
        @SerializedName("level")
        private Level level;
        @SerializedName("FollowStatus")
        private FollowStatus FollowStatus;
        @SerializedName("bio")
        private String bio;
        @SerializedName("video")
        private int video;
        @SerializedName("userId")
        private String userId;
        @SerializedName("isVIP")
        private boolean isVIP;
        @SerializedName("isFollow")
        private boolean isFollow;

        @SerializedName("isBlock")
        private boolean isBlock;

        public boolean isBlock() {
            return isBlock;
        }

        public void setBlock(boolean block) {
            isBlock = block;
        }

        @SerializedName("isFake")
        private boolean isFake;
        @SerializedName("followers")
        private int followers;
        @SerializedName("post")
        private int post;
        @SerializedName("following")
        private int following;
        @SerializedName("name")
        private String name;



        @SerializedName("link")
        private String link;
        @SerializedName("_id")
        private String id;
        @SerializedName("age")
        private int age;
        @SerializedName("username")
        private String username;

        @SerializedName("uniqueId")
        private String uniqueId;
        @SerializedName("avatarFrameImage")
        private String avatarFrameImage;

        @SerializedName("isHost")
        private boolean isHost;


        @SerializedName("isAgency")
        private boolean isAgency;

        @SerializedName("isCoinSeller")
        private boolean isCoinSeller;

        public boolean isCoinSeller() {
            return isCoinSeller;
        }

        public boolean isHost() {
            return isHost;
        }


        public boolean isAgency() {
            return isAgency;
        }

        public String getAvatarFrameImage() {
            return avatarFrameImage;
        }

        public boolean isFake() {
            return isFake;
        }
        public String getLink() {
            return link;
        }
        public String getUniqueId() {
            return uniqueId;
        }

        public boolean isVIP() {
            return isVIP;
        }

        public void setVIP(boolean VIP) {
            isVIP = VIP;
        }

        public boolean isFollow() {
            return isFollow;
        }

        public void setFollow(boolean follow) {
            isFollow = follow;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public Level getLevel() {
            return level;
        }

        public FollowStatus getFollowStatus() {
            return FollowStatus;
        }

        public void setFollowStatus(FollowStatus followStatus) {
            FollowStatus = followStatus;
        }

        public void setLevel(Level level) {
            this.level = level;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public int getVideo() {
            return video;
        }

        public void setVideo(int video) {
            this.video = video;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public boolean isIsVIP() {
            return isVIP;
        }

        public boolean isIsFollow() {
            return isFollow;
        }

        public int getFollowers() {
            return followers;
        }

        public void setFollowers(int followers) {
            this.followers = followers;
        }

        public int getPost() {
            return post;
        }

        public void setPost(int post) {
            this.post = post;
        }

        public int getFollowing() {
            return following;
        }

        public void setFollowing(int following) {
            this.following = following;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            User user = (User) o;

            return video == user.video &&
                    followers == user.followers &&
                    post == user.post &&
                    following == user.following &&
                    age == user.age &&
                    isVIP == user.isVIP &&
                    isFollow == user.isFollow &&
                    isBlock == user.isBlock &&
                    isFake == user.isFake &&
                    isHost == user.isHost &&
                    isAgency == user.isAgency &&
                    isCoinSeller == user.isCoinSeller &&
                    Objects.equals(image, user.image) &&
                    Objects.equals(coverImage, user.coverImage) &&
                    Objects.equals(country, user.country) &&
                    Objects.equals(gender, user.gender) &&
                    Objects.equals(level, user.level) &&
                    Objects.equals(FollowStatus, user.FollowStatus) &&
                    Objects.equals(bio, user.bio) &&
                    Objects.equals(userId, user.userId) &&
                    Objects.equals(name, user.name) &&
                    Objects.equals(link, user.link) &&
                    Objects.equals(id, user.id) &&
                    Objects.equals(username, user.username) &&
                    Objects.equals(uniqueId, user.uniqueId) &&
                    Objects.equals(avatarFrameImage, user.avatarFrameImage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    image, coverImage, country, gender, level,FollowStatus, bio, video, userId,
                    isVIP, isFollow, isBlock, isFake, followers, post, following,
                    name, link, id, age, username, uniqueId, avatarFrameImage,
                    isHost, isAgency, isCoinSeller
            );
        }

    }

    public static class Level {

        @SerializedName("image")
        private String image;

        @SerializedName("createdAt")
        private String createdAt;

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Level level = (Level) o;

            return Double.compare(level.coin, coin) == 0 &&
                    Objects.equals(image, level.image) &&
                    Objects.equals(createdAt, level.createdAt) &&
                    Objects.equals(name, level.name) &&
                    Objects.equals(id, level.id) &&
                    Objects.equals(updatedAt, level.updatedAt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(image, createdAt, name, id, coin, updatedAt);
        }

    }

    public static class FollowStatus{

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("fromUserId")
        private String fromUserId;

        @SerializedName("_id")
        private String id;

        @SerializedName("toUserId")
        private String toUserId;

        @SerializedName("updatedAt")
        private String updatedAt;

        public String getCreatedAt(){
            return createdAt;
        }

        public String getFromUserId(){
            return fromUserId;
        }

        public String getId(){
            return id;
        }

        public String getToUserId(){
            return toUserId;
        }

        public String getUpdatedAt(){
            return updatedAt;
        }
    }
}