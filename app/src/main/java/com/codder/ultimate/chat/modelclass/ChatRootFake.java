package com.codder.ultimate.chat.modelclass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Model representing a fake chat message.
 */
public class ChatRootFake {

    private final int flag; // 1 = sender, 2 = receiver
    private final String message;
    private final String image;
    private final String avtarImage;

    public ChatRootFake(int flag, @NonNull String message, @Nullable String image, @Nullable String avtarImage) {
        this.flag = flag;
        this.message = message;
        this.image = image;
        this.avtarImage = avtarImage;
    }

    public int getFlag() {
        return flag;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    @Nullable
    public String getImage() {
        return image;
    }

    public String getAvtarImage() {
        return avtarImage;
    }

    @NonNull
    @Override
    public String toString() {
        return "ChatRootFake{" +
                "flag=" + flag +
                ", message='" + message + '\'' +
                ", image='" + image + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChatRootFake)) return false;
        ChatRootFake other = (ChatRootFake) obj;
        return this.flag == other.flag &&
                this.message.equals(other.message) &&
                ((this.image == null && other.image == null) || (this.image != null && this.image.equals(other.image)));
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(flag);
        result = 31 * result + message.hashCode();
        result = 31 * result + (image != null ? image.hashCode() : 0);
        return result;
    }
}

