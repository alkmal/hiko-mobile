package com.codder.ultimate.reels.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class ReliteRoot {

    @SerializedName("video")
    private List<VideoItem> video;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public List<VideoItem> getVideo() {
        return video;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public static class VideoItem {

        @SerializedName("song")
        private Song song;

        @SerializedName("thumbnail")
        private String thumbnail;


        @SerializedName("name")
        private String name;

        @SerializedName("userImage")
        private String userImage;

        public String getAvatarFrameImage() {
            return avatarFrameImage;
        }

        @SerializedName("avatarFrameImage")
        private String avatarFrameImage;

        @SerializedName("time")
        private String time;

        @SerializedName("isVIP")
        private boolean isVIP;

        @SerializedName("allowComment")
        private boolean allowComment;

        public void setAllowComment(boolean allowComment) {
            this.allowComment = allowComment;
        }

        public String getName() {
            return name;
        }

        public String getUserImage() {
            return userImage;
        }

        public String getTime() {
            return time;
        }

        public boolean isVIP() {
            return isVIP;
        }

        public int getLike() {
            return like;
        }

        @SerializedName("isLike")
        private boolean isLike;

        public boolean isOriginalAudio() {
            return isOriginalAudio;
        }

        @SerializedName("like")
        private int like;

        @SerializedName("caption")
        private String caption;


        @SerializedName("video")
        private String video;

        @SerializedName("screenshot")
        private String screenshot;

        @SerializedName("mentionPeople")
        private List<String> mentionPeople;

        @SerializedName("userId")
        private String userId;

        @SerializedName("isOriginalAudio")
        private boolean isOriginalAudio;

        @SerializedName("showVideo")
        private int showVideo;

        @SerializedName("comment")
        private int comment;

        @SerializedName("location")
        private String location;

        @SerializedName("_id")
        private String id;

        @SerializedName("isFollow")
        private boolean isFollow;

        public boolean isFollow() {
            return isFollow;
        }


        public void setFollow(boolean follow) {
            isFollow = follow;
        }

        @SerializedName("hashtag")
        private List<String> hashtag;

        public Song getSong() {
            return song;
        }

        public String getThumbnail() {
            return thumbnail;
        }


        public int getLikeCount() {
            return like;
        }

        public String getCaption() {
            return caption;
        }

        public boolean isAllowComment() {
            return allowComment;
        }

        public String getVideo() {
            return video;
        }

        public String getScreenshot() {
            return screenshot;
        }

        public List<String> getMentionPeople() {
            return mentionPeople;
        }

        public String getUserId() {
            return userId;
        }

        public boolean isIsOriginalAudio() {
            return isOriginalAudio;
        }

        public int getShowVideo() {
            return showVideo;
        }

        public int getComment() {
            return comment;
        }

        public String getLocation() {
            return location;
        }

        public void setLikeCount(int like) {
            this.like = like;
        }

        public boolean isLike() {
            return isLike;
        }

        public void setLike(boolean like) {
            isLike = like;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<String> getHashtag() {
            return hashtag;
        }

        public static class Song {

            @SerializedName("song")
            private String song;

            @SerializedName("image")
            private String image;

            @SerializedName("createdAt")
            private String createdAt;

            @SerializedName("singer")
            private String singer;

            @SerializedName("isDelete")
            private boolean isDelete;

            @SerializedName("_id")
            private String id;

            @SerializedName("title")
            private String title;

            @SerializedName("updatedAt")
            private String updatedAt;

            public String getSong() {
                return song;
            }

            public String getImage() {
                return image;
            }

            public String getCreatedAt() {
                return createdAt;
            }

            public String getSinger() {
                return singer;
            }

            public boolean isIsDelete() {
                return isDelete;
            }

            public String getId() {
                return id;
            }

            public String getTitle() {
                return title;
            }

            public String getUpdatedAt() {
                return updatedAt;
            }
        }

        public VideoItem() {
        }

        public VideoItem(VideoItem source) {
            this.id = source.id;
            this.name = source.name;
            this.userImage = source.userImage;
            this.avatarFrameImage = source.avatarFrameImage;
            this.caption = source.caption;
            this.video = source.video;
            this.screenshot = source.screenshot;
            this.mentionPeople = source.mentionPeople;
            this.userId = source.userId;
            this.isOriginalAudio = source.isOriginalAudio;
            this.showVideo = source.showVideo;
            this.comment = source.comment;
            this.location = source.location;
            this.song = source.song;
            this.thumbnail = source.thumbnail;
            this.time = source.time;
            this.isVIP = source.isVIP;
            this.allowComment = source.allowComment;
            this.isLike = source.isLike;
            this.like = source.like;
            this.hashtag = source.hashtag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VideoItem)) return false;

            VideoItem that = (VideoItem) o;

            return like == that.like &&
                    isLike == that.isLike &&
                    isOriginalAudio == that.isOriginalAudio &&
                    isVIP == that.isVIP &&
                    allowComment == that.allowComment &&
                    showVideo == that.showVideo &&
                    comment == that.comment &&
                    Objects.equals(id, that.id) &&
                    Objects.equals(name, that.name) &&
                    Objects.equals(userImage, that.userImage) &&
                    Objects.equals(avatarFrameImage, that.avatarFrameImage) &&
                    Objects.equals(time, that.time) &&
                    Objects.equals(caption, that.caption) &&
                    Objects.equals(video, that.video) &&
                    Objects.equals(screenshot, that.screenshot) &&
                    Objects.equals(thumbnail, that.thumbnail) &&
                    Objects.equals(location, that.location) &&
                    Objects.equals(userId, that.userId) &&
                    Objects.equals(mentionPeople, that.mentionPeople) &&
                    Objects.equals(hashtag, that.hashtag) &&
                    Objects.equals(song, that.song);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    id, name, userImage, avatarFrameImage, time, isVIP, allowComment, like, isLike,
                    caption, video, screenshot, mentionPeople, userId, isOriginalAudio, showVideo,
                    comment, location, hashtag, thumbnail, song
            );
        }

    }
}