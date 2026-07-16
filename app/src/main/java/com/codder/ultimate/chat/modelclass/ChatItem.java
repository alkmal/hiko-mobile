package com.codder.ultimate.chat.modelclass;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class ChatItem {

    @SerializedName("date")
    private String date;

    @SerializedName("image")
    private String image;

    @SerializedName("audio")
    private String audio;

    @SerializedName("audioDuration")
    private long audioDuration;

    @SerializedName("giftsvgaImage")
    private String giftsvgaImage;

    @SerializedName("giftImage")
    private String giftImage;

    @SerializedName("svgaImage")
    private String svgaImage;

    @SerializedName("type")
    private int type;


    @SerializedName("senderId")
    private String senderId;

    @SerializedName("messageType")
    private String messageType;

    @SerializedName("topic")
    private String topic;

    @SerializedName("_id")
    private String id;

    @SerializedName("message")
    private String message;

    @SerializedName("callStatus")
    private int callStatus;
    @SerializedName("callType")
    private int callType;

    @SerializedName("coin")
    private double coin;

    @SerializedName("callDuration")
    private String callDuration;


    public int getCallStatus() {
        return callStatus;
    }

    public int getCallType() {
        return callType;
    }

    public double getCoin() {
        return coin;
    }

    public String getCallDuration() {
        return callDuration;
    }

    public String getDate() {
        return date;
    }

    public String getImage() {
        return image;
    }

    public String getAudio() {
        return audio;
    }

    public long getAudioDuration() {
        return audioDuration;
    }

    public void setAudioDuration(long audioDuration) {
        this.audioDuration = audioDuration;
    }

    public String getSvgaImage() {
        return svgaImage;
    }

    public void setSvgaImage(String svgaImage) {
        this.svgaImage = svgaImage;
    }

    public String getGiftsvgaImage() {
        return giftsvgaImage;
    }

    public void setGiftsvgaImage(String giftsvgaImage) {
        this.giftsvgaImage = giftsvgaImage;
    }

    public String getGiftImage() {
        return giftImage;
    }

    public void setGiftImage(String giftImage) {
        this.giftImage = giftImage;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    public String getSenderId() {
        return senderId;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getTopic() {
        return topic;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChatItem other = (ChatItem) obj;
        return id.equals(other.id) &&
                senderId.equals(other.senderId) &&
                messageType.equals(other.messageType) &&
                ((message == null && other.message == null) || (message != null && message.equals(other.message))) &&
                ((image == null && other.image == null) || (image != null && image.equals(other.image)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, senderId, messageType, message, image);
    }

}
