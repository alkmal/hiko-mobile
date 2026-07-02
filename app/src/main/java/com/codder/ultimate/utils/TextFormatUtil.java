package com.codder.ultimate.utils;

import android.annotation.SuppressLint;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TextFormatUtil {

    @NotNull
    public static String toMMSS(long millis) {
        long mm = TimeUnit.MILLISECONDS.toMinutes(millis)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
        long ss = TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        return String.format(Locale.US, "%02d:%02d", mm, ss);
    }

    @NotNull
    @SuppressLint("DefaultLocale")
    public static String toShortNumber(long count) {
        if (count < 1000) {
            return count + "";
        }
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format(
                "%.1f %c",
                count / Math.pow(1000, exp),
                "kMGTPE".charAt(exp - 1)
        );
    }

}
