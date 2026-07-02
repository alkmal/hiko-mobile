package com.codder.ultimate.post.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import kotlin.jvm.Transient;

public class PostRoot {

    @SerializedName("post")
    private List<PostItem> post;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public List<PostItem> getPost() {
        return post;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public static class PostItem {
        // User's like status (renamed to avoid confusion with like count)

        private transient int displayLikeCount = -1;
        private transient int displayCommentCount = -1;

        public int getDisplayLikeCount() {
            if (displayLikeCount == -1) {
                // likeCount 0 હોય તો random, નહીં તો real value
                displayLikeCount = (likeCount == 0)
                        ? new Random().nextInt(9901) + 100  // 100–10000
                        : likeCount;
            }
            return displayLikeCount;
        }

        public int getDisplayCommentCount() {
            if (displayCommentCount == -1) {
                displayCommentCount = (comment == 0)
                        ? new Random().nextInt(491) + 10   // 10–500
                        : comment;
            }
            return displayCommentCount;
        }

        public void incrementDisplayLikeCount() {
            if (displayLikeCount == -1) getDisplayLikeCount();
            displayLikeCount++;
        }

        public void decrementDisplayLikeCount() {
            if (displayLikeCount == -1) getDisplayLikeCount();
            if (displayLikeCount > 0) displayLikeCount--;
        }

        public void incrementDisplayCommentCount() {
            if (displayCommentCount == -1) getDisplayCommentCount();
            displayCommentCount++;
        }

        public PostItem(PostItem other) {
            this.userLiked = other.userLiked;
            this.likeCount = other.likeCount;
            this.caption = other.caption;
            this.userId = other.userId;
            this.isVIP = other.isVIP;
            this.allowComment = other.allowComment;
            this.createdAt = other.createdAt;
            this.userImage = other.userImage;
            this.avatarFrameImage = other.avatarFrameImage;
            this.post = other.post;
            this.name = other.name;
            this.comment = other.comment;
            this.location = other.location;
            this.id = other.id;
            this.time = other.time;
            this.likeInProgress = false; // never carry over UI-only state
        }

        @SerializedName("isLike")
        private boolean userLiked;

        // Total number of likes
        @SerializedName("like")
        private int likeCount;

        @SerializedName("caption")
        private String caption;

        @SerializedName("userId")
        private String userId;

        @SerializedName("isVIP")
        private boolean isVIP;

        @SerializedName("allowComment")
        private boolean allowComment;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("userImage")
        private String userImage;

        @SerializedName("avatarFrameImage")
        private String avatarFrameImage;

        @SerializedName("post")
        private String post;

        @SerializedName("name")
        private String name;

        @SerializedName("comment")
        private int comment;

        @SerializedName("location")
        private String location;

        @SerializedName("_id")
        private String id;

        @SerializedName("time")
        private String time;

        @SerializedName("isFollow")
        private boolean isFollow;

        public boolean isFollow() {
            return isFollow;
        }


        public void setFollow(boolean follow) {
            isFollow = follow;
        }

        private transient boolean likeInProgress = false;

        public boolean isLikeInProgress() {
            return likeInProgress;
        }

        public void setLikeInProgress(boolean likeInProgress) {
            this.likeInProgress = likeInProgress;
        }

        // --- Getters and Setters ---

        public boolean isUserLiked() {
            return userLiked;
        }

        public void setUserLiked(boolean userLiked) {
            this.userLiked = userLiked;
        }

        public int getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(int likeCount) {
            this.likeCount = likeCount;
        }

        public String getCaption() {
            return caption;
        }

        public String getUserId() {
            return userId;
        }

        public boolean isVIP() {
            return isVIP;
        }

        public boolean isAllowComment() {
            return allowComment;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUserImage() {
            return userImage;
        }

        public String getAvatarFrameImage() {
            return avatarFrameImage;
        }

        public String getPost() {
            return post;
        }

        public String getName() {
            return name;
        }

        public int getComment() {
            return comment;
        }

        public String getLocation() {
            return location;
        }

        public String getId() {
            return id;
        }

        public String getTime() {
            return (time != null && time.trim().equals("0 minutes ago")) ? "Just Now" : time;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PostItem postItem = (PostItem) o;

            return isUserLiked() == postItem.isUserLiked() &&
                    likeCount == postItem.likeCount &&
                    isVIP == postItem.isVIP &&
                    allowComment == postItem.allowComment &&
                    comment == postItem.comment &&
                    Objects.equals(caption, postItem.caption) &&
                    Objects.equals(userId, postItem.userId) &&
                    Objects.equals(createdAt, postItem.createdAt) &&
                    Objects.equals(userImage, postItem.userImage) &&
                    Objects.equals(post, postItem.post) &&
                    Objects.equals(name, postItem.name) &&
                    Objects.equals(location, postItem.location) &&
                    Objects.equals(id, postItem.id) &&
                    Objects.equals(time, postItem.time);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    isUserLiked(),
                    likeCount,
                    caption,
                    userId,
                    isVIP,
                    allowComment,
                    createdAt,
                    userImage,
                    post,
                    name,
                    comment,
                    location,
                    id,
                    time
            );
        }

        @Transient
        private boolean isLikeUpdating = false;

        public boolean isLikeUpdating() {
            return isLikeUpdating;
        }

        public void setLikeUpdating(boolean likeUpdating) {
            isLikeUpdating = likeUpdating;
        }
    }
}
