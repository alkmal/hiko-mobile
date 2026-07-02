package com.codder.ultimate.live.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LiveStreamRoot {

    @SerializedName("liveUser")
    private LiveUser liveUser;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public LiveUser getLiveUser() {
        return liveUser;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public static class LiveUser {
        @SerializedName("country")
        private String country;

        @SerializedName("image")
        private String image;

        @SerializedName("rCoin")
        private int rCoin;

        @SerializedName("channel")
        private String channel;

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

        @SerializedName("seat")
        private List<SeatItem> seat;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("view")
        private int view;

        @SerializedName("diamond")
        private double diamond;

        @SerializedName("name")
        private String name;

        @SerializedName("isPublic")
        private boolean isPublic;

        @SerializedName("_id")
        private String id;

        @SerializedName("uniqueId")
        private String uniqueId;

        @SerializedName("background")
        private String background;

        @SerializedName("audio")
        private boolean audio;

        @SerializedName("age")
        private int age;

        @SerializedName("username")
        private String username;

        @SerializedName("updatedAt")
        private String updatedAt;

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

        // Additional fields from second model

        @SerializedName("pkConfig")
        private PkConfig pkConfig;

        @SerializedName("filter")
        private String filter;

        @SerializedName("isPkMode")
        private boolean isPkMode;

        @SerializedName("pkView")
        private boolean pkView;

        @SerializedName("disconnect")
        private boolean disconnect;

        @SerializedName("duration")
        private int duration;

        // Getters and setters for all fields (only some shown here for brevity)

        public String getCountry() {
            return country;
        }

        public String getImage() {
            return image;
        }

        public int getRCoin() {
            return rCoin;
        }

        public String getChannel() {
            return channel;
        }

        public int getAgoraUID() {
            return agoraUID;
        }

        public String getLiveStreamingId() {
            return liveStreamingId;
        }

        public boolean isVIP() {
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

        public void setSeat(List<SeatItem> seat) {
            this.seat = seat;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public int getView() {
            return view;
        }

        public double getDiamond() {
            return diamond;
        }

        public String getName() {
            return name;
        }

        public boolean isPublic() {
            return isPublic;
        }

        public String getId() {
            return id;
        }

        public String getUniqueId() {
            return uniqueId;
        }

        public String getBackground() {
            return background;
        }

        public void setBackground(String background) {
            this.background = background;
        }

        public boolean isAudio() {
            return audio;
        }

        public int getAge() {
            return age;
        }

        public String getUsername() {
            return username;
        }

        public String getUpdatedAt() {
            return updatedAt;
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

        public int getPrivateCode() {
            return privateCode;
        }

        public void setPrivateCode(int privateCode) {
            this.privateCode = privateCode;
        }

        public String getRoomOwnerUniqueId() {
            return roomOwnerUniqueId;
        }

        public PkConfig getPkConfig() {
            return pkConfig;
        }

        public void setPkConfig(PkConfig pkConfig) {
            this.pkConfig = pkConfig;
        }

        public String getFilter() {
            return filter;
        }

        public boolean isPkMode() {
            return isPkMode;
        }

        public boolean isPkView() {
            return pkView;
        }

        public void setPkView(boolean pkView) {
            this.pkView = pkView;
        }

        public boolean isDisconnect() {
            return disconnect;
        }

        public boolean isIsVIP() {
            return isVIP;
        }

        public boolean isIsPublic() {
            return isPublic;
        }

        public int getDuration() {
            return duration;
        }

        @Override
        public String toString() {
            return "LiveUser{" +
                    "country='" + country + '\'' +
                    ", image='" + image + '\'' +
                    ", rCoin=" + rCoin +
                    ", channel='" + channel + '\'' +
                    ", agoraUID=" + agoraUID +
                    ", liveStreamingId='" + liveStreamingId + '\'' +
                    ", isVIP=" + isVIP +
                    ", token='" + token + '\'' +
                    ", liveUserId='" + liveUserId + '\'' +
                    ", seat=" + seat +
                    ", createdAt='" + createdAt + '\'' +
                    ", view=" + view +
                    ", diamond=" + diamond +
                    ", name='" + name + '\'' +
                    ", isPublic=" + isPublic +
                    ", id='" + id + '\'' +
                    ", uniqueId='" + uniqueId + '\'' +
                    ", background='" + background + '\'' +
                    ", audio=" + audio +
                    ", age=" + age +
                    ", username='" + username + '\'' +
                    ", updatedAt='" + updatedAt + '\'' +
                    ", roomImage='" + roomImage + '\'' +
                    ", roomName='" + roomName + '\'' +
                    ", roomWelcome='" + roomWelcome + '\'' +
                    ", privateCode=" + privateCode +
                    ", roomOwnerUniqueId='" + roomOwnerUniqueId + '\'' +
                    ", pkConfig=" + pkConfig +
                    ", filter='" + filter + '\'' +
                    ", isPkMode=" + isPkMode +
                    ", pkView=" + pkView +
                    ", disconnect=" + disconnect +
                    ", duration=" + duration +
                    '}';
        }

        public static class PkConfig {
            @SerializedName("host1LiveId")
            private String host1LiveId;

            @SerializedName("host1Id")
            private String host1Id;

            @SerializedName("host1Channel")
            private String host1Channel;

            @SerializedName("host2Name")
            private String host2Name;

            @SerializedName("host1Name")
            private String host1Name;

            @SerializedName("host2Id")
            private String host2Id;

            @SerializedName("host2Channel")
            private String host2Channel;

            @SerializedName("host1AgoraUID")
            private int host1AgoraUID;

            @SerializedName("isWinner")
            private int isWinner;

            @SerializedName("host2LiveId")
            private String host2LiveId;

            @SerializedName("host2AgoraUID")
            private int host2AgoraUID;

            @SerializedName("localRank")
            private int localRank;

            @SerializedName("remoteRank")
            private int remoteRank;

            @SerializedName("host1Details")
            private Host1Details host1Details;

            @SerializedName("host2Details")
            private Host2Details host2Details;

            @SerializedName("host1Token")
            private String host1Token;

            @SerializedName("host2Token")
            private String host2Token;

            @SerializedName("host1Image")
            private String host1Image;

            @SerializedName("host2Image")
            private String host2Image;

            // Getters
            public String getHost1LiveId() {
                return host1LiveId;
            }

            public String getHost1Id() {
                return host1Id;
            }

            public String getHost1Channel() {
                return host1Channel;
            }

            public String getHost2Name() {
                return host2Name;
            }

            public String getHost1Name() {
                return host1Name;
            }

            public String getHost2Id() {
                return host2Id;
            }

            public String getHost2Channel() {
                return host2Channel;
            }

            public int getHost1AgoraUID() {
                return host1AgoraUID;
            }

            public int getIsWinner() {
                return isWinner;
            }

            public String getHost2LiveId() {
                return host2LiveId;
            }

            public int getHost2AgoraUID() {
                return host2AgoraUID;
            }

            public int getLocalRank() {
                return localRank;
            }

            public int getRemoteRank() {
                return remoteRank;
            }

            public Host1Details getHost1Details() {
                return host1Details;
            }

            public Host2Details getHost2Details() {
                return host2Details;
            }

            public String getHost1Token() {
                return host1Token;
            }

            public String getHost2Token() {
                return host2Token;
            }

            public String getHost1Image() {
                return host1Image;
            }

            public String getHost2Image() {
                return host2Image;
            }

            @Override
            public String toString() {
                return "PkConfig{" +
                        "host1LiveId='" + host1LiveId + '\'' +
                        ", host1Id='" + host1Id + '\'' +
                        ", host1Channel='" + host1Channel + '\'' +
                        ", host2Name='" + host2Name + '\'' +
                        ", host1Name='" + host1Name + '\'' +
                        ", host2Id='" + host2Id + '\'' +
                        ", host2Channel='" + host2Channel + '\'' +
                        ", host1AgoraUID=" + host1AgoraUID +
                        ", isWinner=" + isWinner +
                        ", host2LiveId='" + host2LiveId + '\'' +
                        ", host2AgoraUID=" + host2AgoraUID +
                        ", localRank=" + localRank +
                        ", remoteRank=" + remoteRank +
                        ", host1Details=" + host1Details +
                        ", host2Details=" + host2Details +
                        ", host1Token='" + host1Token + '\'' +
                        ", host2Token='" + host2Token + '\'' +
                        ", host1Image='" + host1Image + '\'' +
                        ", host2Image='" + host2Image + '\'' +
                        '}';
            }
        }

        public static class Host1Details {
            @SerializedName("image")
            private String image;

            @SerializedName("country")
            private String country;

            @SerializedName("rCOin")
            private int rCOin;

            @SerializedName("isVIP")
            private boolean isVIP;

            @SerializedName("name")
            private String name;

            public String getImage() {
                return image;
            }

            public String getCountry() {
                return country;
            }

            public int getRCOin() {
                return rCOin;
            }

            public boolean isVIP() {
                return isVIP;
            }

            public String getName() {
                return name;
            }

            @Override
            public String toString() {
                return "Host1Details{" +
                        "image='" + image + '\'' +
                        ", country='" + country + '\'' +
                        ", rCOin=" + rCOin +
                        ", isVIP=" + isVIP +
                        ", name='" + name + '\'' +
                        '}';
            }
        }

        public static class Host2Details {
            @SerializedName("image")
            private String image;

            @SerializedName("country")
            private String country;

            @SerializedName("rCOin")
            private int rCOin;

            @SerializedName("name")
            private String name;

            @SerializedName("isVIP")
            private boolean isVIP;

            public String getImage() {
                return image;
            }

            public String getCountry() {
                return country;
            }

            public int getRCOin() {
                return rCOin;
            }

            public String getName() {
                return name;
            }

            public boolean isVIP() {
                return isVIP;
            }

            @Override
            public String toString() {
                return "Host2Details{" +
                        "image='" + image + '\'' +
                        ", country='" + country + '\'' +
                        ", rCOin=" + rCOin +
                        ", name='" + name + '\'' +
                        ", isVIP=" + isVIP +
                        '}';
            }
        }
    }
}
