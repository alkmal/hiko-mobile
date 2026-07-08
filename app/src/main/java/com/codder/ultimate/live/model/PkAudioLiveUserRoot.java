package com.codder.ultimate.live.model;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public class PkAudioLiveUserRoot {

    @SerializedName("message")
    private String message;

    @SerializedName("users")
    private List<UsersItem> users;

    @SerializedName("status")
    private boolean status;

    public String getMessage() {
        return message;
    }

    public List<UsersItem> getUsers() {
        return users;
    }

    public boolean isStatus() {
        return status;
    }

    public static class UsersItem {



        public static final int TYPE_MY_ROOM = 0;
        public static final int TYPE_QUICK_JOIN = 1;
        public static final int TYPE_USER = 2;


        private String uniqueKey;

        public String getUniqueKey() {
            return uniqueKey;
        }

        public void setUniqueKey(String uniqueKey) {
            this.uniqueKey = uniqueKey;
        }

        private int itemType = TYPE_USER;

        public int getItemType() {
            return itemType;
        }

        public void setItemType(int itemType) {
            this.itemType = itemType;
        }


        @SerializedName("country")
        private String country;

        @SerializedName("rCoin")
        private double rCoin;

        @SerializedName("channel")
        private String channel;

        @SerializedName("link")
        private String link;


        @SerializedName("pkVideoArray")
        private List<String> pkVideoArray;


        @SerializedName("pkImageArray")
        private List<String> pkImageArray;

        @SerializedName("blockedUsers")
        private List<BlockUsers> blockedUsers;

        public List<BlockUsers> getBlockedUsers() {
            return blockedUsers;
        }

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("view")
        private int view;

        @SerializedName("isPublic")
        private boolean isPublic;

        @SerializedName("audio")
        private boolean audio;

        @SerializedName("isPkMode")
        private boolean isPkMode;

        @SerializedName("updatedAt")
        private String updatedAt;

        @SerializedName("image")
        private String image;



        @SerializedName("uniqueId")
        private String uniqueId;
        @SerializedName("avatarFrameImage")
        private String avatarFrameImage;


        @SerializedName("background")
        private String background;

        @SerializedName("continueLive")
        private boolean continueLive;

        @SerializedName("pkIdentity")
        private PkIdentity pkIdentity;

        public String getCountryFlagImage() {
            return countryFlagImage;
        }

        @SerializedName("countryFlagImage")
        private String countryFlagImage;

        public void setAgoraUID(int agoraUID) {
            this.agoraUID = agoraUID;
        }

        @SerializedName("agoraUID")
        private int agoraUID;

        @SerializedName("liveStreamingId")
        private String liveStreamingId;

        @SerializedName("isVIP")
        private boolean isVIP;

        @SerializedName("token")
        private String token;

        @SerializedName("liveUserId")
        private String liveUserId;

        public void setSeat(List<SeatItem> seat) {
            this.seat = seat;
        }

        @SerializedName("seat")
        private List<SeatItem> seat;

        @SerializedName("diamond")
        private double diamond;

        @SerializedName("pkEndTime")
        private String pkEndTime;

        public String getFilter() {
            return filter;
        }

        @SerializedName("filter")
        private String filter;

        @SerializedName("name")
        private String name;

        @SerializedName("_id")
        private String id;

        @SerializedName("time")
        private int time;

        @SerializedName("roomImage")
        private String roomImage;

        @SerializedName("roomName")
        private String roomName;

        @SerializedName("roomWelcome")
        private String roomWelcome;

        @SerializedName("privateCode")
        private int privateCode;

        @SerializedName("roomOwnerUniqueId")
        private String roomOwnerUniqueId;

        public String getRoomOwnerUniqueId() {
            return roomOwnerUniqueId;
        }

        public int getPrivateCode() {
            return privateCode;
        }

        public void setPrivateCode(int privateCode) {
            this.privateCode = privateCode;
        }

        public String getRoomImage() {
            return roomImage;
        }

        public void setRoomImage(String roomImage) {
            this.roomImage = roomImage;
        }

        public String getRoomName() {
            return roomName;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }

        public String getRoomWelcome() {
            return roomWelcome;
        }

        public void setRoomWelcome(String roomWelcome) {
            this.roomWelcome = roomWelcome;
        }

        public void setrCoin(double rCoin) {
            this.rCoin = rCoin;
        }

        @Override
        public String toString() {
            return "UsersItem{" +
                    "country='" + country + '\'' +
                    ", rCoin=" + rCoin +
                    ", channel='" + channel + '\'' +
                    ", link='" + link + '\'' +
                    ", createdAt='" + createdAt + '\'' +
                    ", view=" + view +
                    ", isPublic=" + isPublic +
                    ", audio=" + audio +
                    ", isPkMode=" + isPkMode +
                    ", updatedAt='" + updatedAt + '\'' +
                    ", image='" + image + '\'' +
                    ", background='" + background + '\'' +
                    ", continueLive=" + continueLive +
                    ", pkIdentity=" + pkIdentity +
                    ", agoraUID=" + agoraUID +
                    ", liveStreamingId='" + liveStreamingId + '\'' +
                    ", isVIP=" + isVIP +
                    ", token='" + token + '\'' +
                    ", liveUserId='" + liveUserId + '\'' +
                    ", seat=" + seat +
                    ", diamond=" + diamond +
                    ", pkEndTime='" + pkEndTime + '\'' +
                    ", filter='" + filter + '\'' +
                    ", name='" + name + '\'' +
                    ", id='" + id + '\'' +
                    ", time=" + time +
                    ", isFake=" + isFake +
                    ", disconnect=" + disconnect +
                    ", age=" + age +
                    ", pkConfig=" + pkConfig +
                    ", username='" + username + '\'' +
                    ", duration=" + duration +
                    ", liveStreaming=" + liveStreaming +
                    '}';
        }

        @SerializedName("isFake")
        private boolean isFake;

        public boolean isDisconnect() {
            return disconnect;
        }

        @SerializedName("disconnect")
        private boolean disconnect;

        @SerializedName("age")
        private int age;

        @SerializedName("pkConfig")
        private PkConfig pkConfig;

        @SerializedName("audioConfig")
        private AudioRoomConfig AudioRoomConfig;

        @SerializedName("username")
        private String username;

        public int getDuration() {
            return duration;
        }

        @SerializedName("duration")
        private int duration;

        @SerializedName("liveStreaming")
        private LiveStreaming liveStreaming;

        public String getAvatarFrameImage() {
            return avatarFrameImage;
        }

        public List<String> getPkVideoArray() {
            return pkVideoArray;
        }

        public String getBackground() {
            return background;
        }

        public void setBackground(String background) {
            this.background = background;
        }

        public String getUniqueId() {
            return uniqueId;
        }
        public String getCountry() {
            return country;
        }

        public double getRCoin() {
            return rCoin;
        }

        public String getChannel() {
            return channel;
        }

        public String getLink() {
            return link;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public List<String> getPkImageArray() {
            return pkImageArray;
        }

        public int getView() {
            return view;
        }

        public boolean isIsPublic() {
            return isPublic;
        }

        public boolean isAudio() {
            return audio;
        }

        public boolean isIsPkMode() {
            return isPkMode;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getImage() {
            return image;
        }

        public boolean isContinueLive() {
            return continueLive;
        }

        public PkIdentity getPkIdentity() {
            return pkIdentity;
        }

        public int getAgoraUID() {
            return agoraUID;
        }

        public String getLiveStreamingId() {
            return liveStreamingId;
        }

        public boolean isIsVIP() {
            return isVIP;
        }

        public String getToken() {
            return token;
        }

        public String getLiveUserId() {
            return liveUserId;
        }

        public List<SeatItem> getSeat() {
            return seat;
        }

        public double getDiamond() {
            return diamond;
        }

        public String getPkEndTime() {
            return pkEndTime;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getTime() {
            return time;
        }

        public boolean isIsFake() {
            return isFake;
        }

        public int getAge() {
            return age;
        }

        public PkConfig getPkConfig() {
            return pkConfig;
        }

        public String getUsername() {
            return username;
        }

        public LiveStreaming getLiveStreaming() {
            return liveStreaming;
        }

        public UsersItem.AudioRoomConfig getAudioRoomConfig() {
            return AudioRoomConfig;
        }

        public void setAudioRoomConfig(UsersItem.AudioRoomConfig audioRoomConfig) {
            AudioRoomConfig = audioRoomConfig;
        }


        public static class AudioRoomConfig {

            @SerializedName("isHostMute")
            int isHostMute;

            public AudioRoomConfig() {
            }

            public int isHostMute() {
                return isHostMute;
            }

            public void setHostMute(int hostMute) {
                isHostMute = hostMute;
            }
        }


        public static class PkConfig {

            @Override
            public String toString() {
                return "PkConfig{" +
                        "remoteRank=" + remoteRank +
                        ", host1Image='" + host1Image + '\'' +
                        ", host1Details=" + host1Details +
                        ", host2Name='" + host2Name + '\'' +
                        ", host1Channel='" + host1Channel + '\'' +
                        ", host2Channel='" + host2Channel + '\'' +
                        ", host1AgoraUID=" + host1AgoraUID +
                        ", host2AgoraUID=" + host2AgoraUID +
                        ", host2Token='" + host2Token + '\'' +
                        ", host1Name='" + host1Name + '\'' +
                        ", isWinner=" + isWinner +
                        ", host2Image='" + host2Image + '\'' +
                        ", host1LiveId='" + host1LiveId + '\'' +
                        ", host1Id='" + host1Id + '\'' +
                        ", host2Details=" + host2Details +
                        ", host2Id='" + host2Id + '\'' +
                        ", host2LiveId='" + host2LiveId + '\'' +
                        ", host1Token='" + host1Token + '\'' +
                        ", localRank=" + localRank +
                        '}';
            }

            @SerializedName("remoteRank")
            private int remoteRank;

            @SerializedName("host1Image")
            private String host1Image;

            @SerializedName("host1Details")
            private Host1Details host1Details;

            @SerializedName("host2Name")
            private String host2Name;

            @SerializedName("host1Channel")
            private String host1Channel;

            @SerializedName("host2Channel")
            private String host2Channel;

            @SerializedName("host1AgoraUID")
            private int host1AgoraUID;

            @SerializedName("host2AgoraUID")
            private int host2AgoraUID;

            @SerializedName("host2Token")
            private String host2Token;

            @SerializedName("host1Name")
            private String host1Name;

            @SerializedName("isWinner")
            private int isWinner;

            @SerializedName("host2Image")
            private String host2Image;

            @SerializedName("host1LiveId")
            private String host1LiveId;

            @SerializedName("host1Id")
            private String host1Id;

            @SerializedName("host2Details")
            private Host2Details host2Details;

            @SerializedName("host2Id")
            private String host2Id;

            @SerializedName("host2LiveId")
            private String host2LiveId;

            @SerializedName("host1Token")
            private String host1Token;

            @SerializedName("localRank")
            private int localRank;

            public int getRemoteRank() {
                return remoteRank;
            }

            public String getHost1Image() {
                return host1Image;
            }

            public Host1Details getHost1Details() {
                return host1Details;
            }

            public String getHost2Name() {
                return host2Name;
            }

            public String getHost1Channel() {
                return host1Channel;
            }

            public String getHost2Channel() {
                return host2Channel;
            }

            public int getHost1AgoraUID() {
                return host1AgoraUID;
            }

            public int getHost2AgoraUID() {
                return host2AgoraUID;
            }

            public String getHost2Token() {
                return host2Token;
            }

            public String getHost1Name() {
                return host1Name;
            }

            public int getIsWinner() {
                return isWinner;
            }

            public String getHost2Image() {
                return host2Image;
            }

            public String getHost1LiveId() {
                return host1LiveId;
            }

            public String getHost1Id() {
                return host1Id;
            }

            public Host2Details getHost2Details() {
                return host2Details;
            }

            public String getHost2Id() {
                return host2Id;
            }

            public String getHost2LiveId() {
                return host2LiveId;
            }

            public String getHost1Token() {
                return host1Token;
            }

            public int getLocalRank() {
                return localRank;
            }

            public static class Host1Details {

                @Override
                public String toString() {
                    return "Host1Details{" +
                            "image='" + image + '\'' +
                            ", country='" + country + '\'' +
                            ", rCoin=" + rCoin +
                            ", name='" + name + '\'' +
                            ", isVIP=" + isVIP +
                            '}';
                }

                @SerializedName("image")
                private String image;

                public String getAvatarFrameImage() {
                    return avatarFrameImage;
                }

                @SerializedName("avatarFrameImage")
                private String avatarFrameImage;

                @SerializedName("country")
                private String country;

                @SerializedName("rCoin")
                private double rCoin;

                @SerializedName("name")
                private String name;
                @SerializedName("uniqueId")
                private String uniqueId;

                public boolean isVIP() {
                    return isVIP;
                }

                @SerializedName("isVIP")
                private boolean isVIP;

                public String getUniqueId() {
                    return uniqueId;
                }

                public String getImage() {
                    return image;
                }

                public String getCountry() {
                    return country;
                }

                public double getRCoin() {
                    return rCoin;
                }

                public String getName() {
                    return name;
                }
            }

            public static class Host2Details {
                @Override
                public String toString() {
                    return "Host2Details{" +
                            "image='" + image + '\'' +
                            ", country='" + country + '\'' +
                            ", rCoin=" + rCoin +
                            ", name='" + name + '\'' +
                            ", isVIP=" + isVIP +
                            '}';
                }

                @SerializedName("image")
                private String image;

                public String getAvatarFrameImage() {
                    return avatarFrameImage;
                }

                @SerializedName("avatarFrameImage")
                private String avatarFrameImage;

                @SerializedName("country")
                private String country;

                @SerializedName("rCoin")
                private double rCoin;

                @SerializedName("name")
                private String name;


                @SerializedName("uniqueId")
                private String uniqueId;

                public boolean isVIP() {
                    return isVIP;
                }

                @SerializedName("isVIP")
                private boolean isVIP;

                public String getUniqueId() {
                    return uniqueId;
                }

                public String getImage() {
                    return image;
                }

                public String getCountry() {
                    return country;
                }

                public double getRCoin() {
                    return rCoin;
                }

                public String getName() {
                    return name;
                }
            }
        }

        public static class PkIdentity {

            @SerializedName("pkId")
            private String pkId;

            @Override
            public String toString() {
                return "PkIdentity{" +
                        "pkId='" + pkId + '\'' +
                        ", count=" + count +
                        '}';
            }

            @SerializedName("count")
            private int count;

            public String getPkId() {
                return pkId;
            }

            public int getCount() {
                return count;
            }
        }

        public static class SeatItem {
            private Queue<String> reactionQueue = new LinkedList<>();

            private boolean isReactionRunning;
            private String reactionImage;

            // Getters and setters

            public Queue<String> getReactionQueue() {
                if (reactionQueue == null) {
                    reactionQueue = new LinkedList<>();
                }
                return reactionQueue;
            }

            public void setReactionQueue(Queue<String> reactionQueue) {
                this.reactionQueue = reactionQueue;
            }

            public boolean isReactionRunning() {
                return isReactionRunning;
            }

            public void setReactionRunning(boolean reactionRunning) {
                isReactionRunning = reactionRunning;
            }

            public String getReactionImage() {
                return reactionImage;
            }

            public void setReactionImage(String reactionImage) {
                this.reactionImage = reactionImage;
            }
            public SeatItem(String image, String country, boolean reserved, String name, boolean lock, int agoraUid, int mute, boolean isSpeaking, String id, int position, boolean invite, String userId) {
                this.image = image;
                this.country = country;
                this.reserved = reserved;
                this.name = name;
                this.lock = lock;
                this.agoraUid = agoraUid;
                this.mute = mute;
                this.isSpeaking = isSpeaking;
                this.id = id;
                this.position = position;
                this.invite = invite;
                this.userId = userId;
            }

            @SerializedName("isSpeaking")
            private boolean isSpeaking;
            @SerializedName("avatarFrameImage")
            private String avatarFrame;
            @SerializedName("image")
            private String image;

            @SerializedName("country")
            private String country;

            @SerializedName("reserved")
            private boolean reserved;

            @SerializedName("name")
            private String name;

            @SerializedName("lock")
            private boolean lock;

            @SerializedName("agoraUid")
            private int agoraUid;

            @SerializedName("mute")
            private int mute;

            @SerializedName("_id")
            private String id;

            @SerializedName("position")
            private int position;

            @SerializedName("invite")
            private boolean invite;

            @SerializedName("userId")
            private String userId;

            private boolean loading = false;

            public boolean isLoading() {
                return loading;
            }

            public void setLoading(boolean loading) {
                this.loading = loading;
            }

            private boolean isAnimate=false;

            public boolean isAnimate() {
                return isAnimate;
            }

            public void setAnimate(boolean b) {
                isAnimate=b;
            }

            public boolean isIsSpeaking() {
                return isSpeaking;
            }

            public String getImage() {
                return image;
            }

            public String getCountry() {
                return country;
            }

            public boolean isReserved() {
                return reserved;
            }

            public void setReserved(boolean reserved) {
                this.reserved = reserved;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }

            public void setImage(String image) {
                this.image = image;
            }

            public void setAvatarFrame(String avatarFrame) {
                this.avatarFrame = avatarFrame;
            }

            public void setCountry(String country) {
                this.country = country;
            }

            public void setName(String name) {
                this.name = name;
            }

            public void setLock(boolean lock) {
                this.lock = lock;
            }

            public void setAgoraUid(int agoraUid) {
                this.agoraUid = agoraUid;
            }

            public void setSpeaking(boolean speaking) {
                isSpeaking = speaking;
            }

            public String getName() {
                return name;
            }

            public boolean isLock() {
                return lock;
            }

            public int getAgoraUid() {
                return agoraUid;
            }

            public int isMute() {
                return mute;
            }

            public void setMute(int mute) {
                this.mute = mute;
            }

            public String getId() {
                return id;
            }

            public int getPosition() {
                return position;
            }

            public boolean isInvite() {
                return invite;
            }

            @Override
            public String toString() {
                return "SeatItem{" +
                        "isSpeaking=" + isSpeaking +
                        ", image='" + image + '\'' +
                        ", country='" + country + '\'' +
                        ", reserved=" + reserved +
                        ", name='" + name + '\'' +
                        ", lock=" + lock +
                        ", agoraUid=" + agoraUid +
                        ", mute=" + mute +
                        ", id='" + id + '\'' +
                        ", position=" + position +
                        ", invite=" + invite +
                        ", userId='" + userId + '\'' +
                        '}';
            }

            public String getUserId() {
                return userId;
            }

            public String getAvatarFrame() {
                return avatarFrame;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (obj == null || getClass() != obj.getClass()) return false;
                SeatItem other = (SeatItem) obj;
                return position == other.position &&
                        reserved == other.reserved &&
                        lock == other.lock &&
                        mute == other.mute &&
                        agoraUid == other.agoraUid &&
                        isSpeaking == other.isSpeaking &&
                        invite == other.invite &&
                        isAnimate == other.isAnimate &&
                        loading == other.loading &&
                        isReactionRunning == other.isReactionRunning &&
                        Objects.equals(id, other.id) &&
                        Objects.equals(userId, other.userId) &&
                        Objects.equals(name, other.name) &&
                        Objects.equals(image, other.image) &&
                        Objects.equals(avatarFrame, other.avatarFrame) &&
                        Objects.equals(country, other.country) &&
                        Objects.equals(reactionImage, other.reactionImage);
            }

            @Override
            public int hashCode() {
                return Objects.hash(position, reserved, lock, mute, agoraUid, isSpeaking, invite, isAnimate,
                        loading, id, userId, name, image, avatarFrame, country,
                        reactionImage, isReactionRunning);
            }

        }

        public static class LiveStreaming {

            @SerializedName("comments")
            private int comments;

            @SerializedName("rCoin")
            private double rCoin;

            @Override
            public String toString() {
                return "LiveStreaming{" +
                        "comments=" + comments +
                        ", rCoin=" + rCoin +
                        ", momentStartTime='" + momentStartTime + '\'' +
                        ", userId='" + userId + '\'' +
                        ", momentEndTime='" + momentEndTime + '\'' +
                        ", fans=" + fans +
                        ", duration='" + duration + '\'' +
                        ", createdAt='" + createdAt + '\'' +
                        ", startTime='" + startTime + '\'' +
                        ", id='" + id + '\'' +
                        ", audio=" + audio +
                        ", endTime='" + endTime + '\'' +
                        ", user=" + user +
                        ", gifts=" + gifts +
                        ", updatedAt='" + updatedAt + '\'' +
                        '}';
            }

            @SerializedName("momentStartTime")
            private String momentStartTime;

            @SerializedName("userId")
            private String userId;

            @SerializedName("momentEndTime")
            private String momentEndTime;

            @SerializedName("fans")
            private int fans;

            @SerializedName("duration")
            private String duration;

            @SerializedName("createdAt")
            private String createdAt;

            @SerializedName("startTime")
            private String startTime;

            @SerializedName("_id")
            private String id;

            @SerializedName("audio")
            private boolean audio;

            @SerializedName("endTime")
            private String endTime;

            @SerializedName("user")
            private int user;

            @SerializedName("gifts")
            private int gifts;

            @SerializedName("updatedAt")
            private String updatedAt;

            public int getComments() {
                return comments;
            }

            public double getRCoin() {
                return rCoin;
            }

            public String getMomentStartTime() {
                return momentStartTime;
            }

            public String getUserId() {
                return userId;
            }

            public String getMomentEndTime() {
                return momentEndTime;
            }

            public int getFans() {
                return fans;
            }

            public String getDuration() {
                return duration;
            }

            public String getCreatedAt() {
                return createdAt;
            }

            public String getStartTime() {
                return startTime;
            }

            public String getId() {
                return id;
            }

            public boolean isAudio() {
                return audio;
            }

            public String getEndTime() {
                return endTime;
            }

            public int getUser() {
                return user;
            }

            public int getGifts() {
                return gifts;
            }

            public String getUpdatedAt() {
                return updatedAt;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            UsersItem other = (UsersItem) obj;
            // Compare only significant fields for UI, e.g. id and view
            return id.equals(other.id) &&
                    name.equals(other.name) &&
                    view == other.view &&
                    image.equals(other.image); // Add more if you wish
        }

//        @Override
//        public boolean equals(Object obj) {
//            if (this == obj) return true;
//            if (obj == null || getClass() != obj.getClass()) return false;
//
//            UsersItem other = (UsersItem) obj;
//
//            return safeEquals(id, other.id)
//                    && safeEquals(name, other.name)
//                    && view == other.view
//                    && safeEquals(image, other.image)
//                    && itemType == other.itemType;
//        }
//
//        private boolean safeEquals(Object a, Object b) {
//            return a == b || (a != null && a.equals(b));
//        }


        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + view;
            result = 31 * result + (image != null ? image.hashCode() : 0);
            return result;
        }

//        @Override
//        public int hashCode() {
//            int result = 17;
//            result = 31 * result + (id != null ? id.hashCode() : 0);
//            result = 31 * result + (name != null ? name.hashCode() : 0);
//            result = 31 * result + view;
//            result = 31 * result + (image != null ? image.hashCode() : 0);
//            result = 31 * result + itemType;
//            return result;
//        }


    }

    public static class BlockUsers{

        private String blockedUserId;
        private String blockedUntil;
        protected String blockedUntilTime;
        private String _id;

        public String getBlockedUserId() {
            return blockedUserId;
        }

        public String getBlockedUntil() {
            return blockedUntil;
        }

        public String getBlockedUntilTime() {
            return blockedUntilTime;
        }

        public String get_id() {
            return _id;
        }
    }

}
