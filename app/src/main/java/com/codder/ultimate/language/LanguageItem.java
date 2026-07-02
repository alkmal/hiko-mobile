package com.codder.ultimate.language;

public class LanguageItem {
    private final String key;   // display key or Const.* name
    private final String code;  // "en", "ar", ...
    private final int flagRes;

    public LanguageItem(String key, String code, int flagRes) {
        this.key = key;
        this.code = code;
        this.flagRes = flagRes;
    }

    public String getKey() { return key; }
    public String getCode() { return code; }
    public int getFlagRes() { return flagRes; }
}
