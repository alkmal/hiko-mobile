package com.codder.ultimate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.codder.ultimate.live.model.StickerRoot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RayziUtils {

    private static final String TAG = "RayziUtils";
    private static List<StickerRoot.StickerItem> sticker = new ArrayList<>();
    public static String formatCoin(double amount) {
        // Handle Trillion (T)
        if (amount >= 1_000_000_000_000d) {
            String s = String.format(Locale.US, "%.1f", amount / 1_000_000_000_000d);
            if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
            return s + "T";
        }

        // Handle Billion (B)
        else if (amount >= 1_000_000_000d) {
            String s = String.format(Locale.US, "%.1f", amount / 1_000_000_000d);
            if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
            return s + "B";
        }

        // Handle Million (M)
        else if (amount >= 1_000_000d) {
            String s = String.format(Locale.US, "%.1f", amount / 1_000_000d);
            if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
            return s + "M";
        }

        // Handle Thousand (K)
        else if (amount >= 1_000d) {
            String s = String.format(Locale.US, "%.1f", amount / 1_000d);
            if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
            return s + "K";
        }

        // Handle below 1000
        else {
            return Integer.toString((int) amount);
        }
    }


    public static List<StickerRoot.StickerItem> getSticker() {
        return sticker;
    }

    public static void marqueeText(TextView tvName) {
        tvName.setSelected(true);
        tvName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvName.setSingleLine(true);
        tvName.setMarqueeRepeatLimit(-1); // Set -1 for infinite marquee
        tvName.setFocusable(true);
        tvName.setFocusableInTouchMode(true);
        tvName.setWidth(150);
    }

    public static void startCustomMarquee(TextView tv, int parentWidth) {

        tv.post(() -> {

            float textWidth = tv.getPaint().measureText(tv.getText().toString());

            if (textWidth <= parentWidth) return; // No need to scroll

            long duration = (long) (textWidth * 30); // 🔥 speed control (bigger = slower)

            ObjectAnimator animator = ObjectAnimator.ofFloat(
                    tv,
                    "translationX",
                    parentWidth,
                    -textWidth
            );

            animator.setDuration(duration);
            animator.setInterpolator(new LinearInterpolator());

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tv.postDelayed(() -> animator.start(), 1000); // ✅ 1 sec pause
                }
            });

            animator.start();
        });
    }


    public static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%d:%02d:%02d", h, m, s);
    }

    public static int getImageFromNumber(int count) {
        if (count == 10) {
            return R.drawable.x10;
        } else if (count == 9) {
            return R.drawable.x9;
        } else if (count == 8) {
            return R.drawable.x8;
        } else if (count == 7) {
            return R.drawable.x7;
        } else if (count == 6) {
            return R.drawable.x6;
        } else if (count == 5) {
            return R.drawable.x5;
        } else if (count == 4) {
            return R.drawable.x4;
        } else if (count == 3) {
            return R.drawable.x3;
        } else if (count == 2) {
            return R.drawable.x2;
        } else {
            return R.drawable.x1;
        }
    }

    public static void showToast(FragmentActivity activity, String message) {
        activity.runOnUiThread(() -> Toast.makeText(activity, message, Toast.LENGTH_LONG).show());
    }

    public enum Privacy {
        PUBLIC, FOLLOWERS, PRIVATE
    }

    public static void setStickers(List<StickerRoot.StickerItem> sticker) {
        RayziUtils.sticker = sticker;
    }

    public static File createTempFileFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("upload_", ".jpg", context.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (IOException e) {
            Log.e(TAG, "createTempFileFromUri: ", e);
            return null;
        }
    }

    public abstract static class DebouncedOnClickListener implements android.view.View.OnClickListener {
        private static final long MIN_CLICK_INTERVAL = 1000; // 1 second
        private long lastClickTime = 0;

        public abstract void onDebouncedClick(android.view.View v);

        @Override
        public final void onClick(android.view.View v) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
                lastClickTime = currentTime;
                onDebouncedClick(v);
            }
        }
    }
}
