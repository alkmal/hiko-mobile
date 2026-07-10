package com.codder.ultimate.utils;

import com.codder.ultimate.BuildConfig;

public final class ImageUrlUtil {

    private ImageUrlUtil() {
    }

    public static String normalize(String value) {
        if (value == null) return "";
        String raw = value.trim();
        if (raw.isEmpty()) return "";
        if (raw.startsWith("http://")
                || raw.startsWith("https://")
                || raw.startsWith("content://")
                || raw.startsWith("file://")
                || raw.startsWith("android.resource://")) {
            return raw;
        }

        String baseUrl = BuildConfig.BASE_URL.replaceAll("/+$", "");
        return raw.startsWith("/") ? baseUrl + raw : baseUrl + "/" + raw;
    }
}
