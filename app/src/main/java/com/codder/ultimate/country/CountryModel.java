package com.codder.ultimate.country;

public class CountryModel {
    private String name;
    private String flag;
    private String phoneCode;
    private Integer phoneLength;

    // Constructor
    public CountryModel(String name, String flag, String phoneCode, Integer phoneLength) {
        this.name = name;
        this.flag = flag;
        this.phoneCode = phoneCode;
        this.phoneLength = phoneLength;
    }

    // Default constructor (optional)
    public CountryModel() {}

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    public Integer getPhoneLength() {
        return phoneLength;
    }

    public void setPhoneLength(Integer phoneLength) {
        this.phoneLength = phoneLength;
    }
}

