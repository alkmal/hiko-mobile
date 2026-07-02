package com.codder.ultimate.fake.utils;

public class FakeGiftRoot {
    public static final int IMAGE = 1, SVGA = 2, GIF = 3;
    int url;
    int coin, type;
    private int id;
    private int count;

    public FakeGiftRoot() {
    }

    public FakeGiftRoot(int id, int url, int coin, int type) {
        this.id = id;
        this.url = url;
        this.coin = coin;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUrl() {
        return url;
    }

    public void setUrl(int url) {
        this.url = url;
    }

    public int getCoin() {
        return coin;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
