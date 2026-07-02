package com.codder.ultimate.modelclass;

import java.util.Objects;

public class ProfileRoot {

    private int getImages;
    private String getText;

    public ProfileRoot(int getImages, String getText) {
        this.getImages = getImages;
        this.getText = getText;
    }

    public int getGetImages() {
        return getImages;
    }

    public void setGetImages(int getImages) {
        this.getImages = getImages;
    }

    public String getGetText() {
        return getText;
    }

    public void setGetText(String getText) {
        this.getText = getText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfileRoot)) return false;
        ProfileRoot that = (ProfileRoot) o;
        return getImages == that.getImages &&
                Objects.equals(getText, that.getText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getImages, getText);
    }
}
