package com.codder.ultimate.post.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PostCommentRoot {

    @SerializedName("data")
    private List<CommentsItem> data;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public List<CommentsItem> getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public static class CommentsItem {
        @SerializedName("image")
        private String image;
        @SerializedName("name")
        private String name;
        @SerializedName("comment")
        private String comment;
        @SerializedName("_id")
        private String id;
        @SerializedName("time")
        private String time;
        @SerializedName("userId")
        private String userId;
        @SerializedName("username")
        private String username;
        @SerializedName("avatarFrameImage")
        private String avatarFrameImage;

        public String getAvatarFrameImage() {
            return avatarFrameImage;
        }

        @SerializedName("isVIP")
        private boolean isVIP;

        public boolean isVIP() {
            return isVIP;
        }

        public void setVIP(boolean VIP) {
            isVIP = VIP;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getId() {
            return id != null ? id : "";
        }


        public String getTime() {
            if (time.trim().equals("0 minutes ago")) {
                return "Just Now";
            }
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof CommentsItem)) return false;
            CommentsItem other = (CommentsItem) obj;

            return getId().equals(other.getId()) &&
                    (comment != null ? comment.equals(other.comment) : other.comment == null) &&
                    (time != null ? time.equals(other.time) : other.time == null) &&
                    (name != null ? name.equals(other.name) : other.name == null) &&
                    (image != null ? image.equals(other.image) : other.image == null);
        }

        @Override
        public int hashCode() {
            int result = getId().hashCode();
            result = 31 * result + (comment != null ? comment.hashCode() : 0);
            result = 31 * result + (time != null ? time.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (image != null ? image.hashCode() : 0);
            return result;
        }
    }
}