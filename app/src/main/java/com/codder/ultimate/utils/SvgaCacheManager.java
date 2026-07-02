package com.codder.ultimate.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.opensource.svgaplayer.SVGAParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SvgaCacheManager {

    private static final String TAG = "SvgaCacheManager";
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static File getSvgaCacheDir(Context context) {
        File svgaCacheDir = new File(context.getCacheDir(), "svga_cache");
        if (!svgaCacheDir.exists()) {
            boolean created = svgaCacheDir.mkdirs();
            if (!created) {
                Log.w(TAG, "Failed to create svga_cache directory.");
            }
        }
        return svgaCacheDir;
    }

    /**
     * Downloads and caches the SVGA file asynchronously.
     * Will always run in background thread automatically.
     */
    public static void downloadAndCacheSvga(String url, Context context) {
        executor.execute(() -> {
            File svgaCacheDir = getSvgaCacheDir(context);
            File cachedFile = new File(svgaCacheDir, String.valueOf(url.hashCode()));

            if (cachedFile.exists() && cachedFile.length() > 0) {
//                Log.d(TAG, "Already cached: " + cachedFile.getAbsolutePath());
//                Log.d("svga", "✅ SVGA already cached: " + cachedFile.getAbsolutePath() + " [urlHash=" + url.hashCode() + "]");
                return;
            }

            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                URL urlObj = new URL(url);
                URLConnection connection = urlObj.openConnection();
                connection.connect();

                inputStream = connection.getInputStream();
                outputStream = new FileOutputStream(cachedFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();


            } catch (Exception e) {
                Log.e("svga", "❌ SVGA download failed from: " + url + " | Reason: " + e.getMessage(), e);
            } finally {
                try {
                    if (inputStream != null) inputStream.close();
                    if (outputStream != null) outputStream.close();
                } catch (Exception ignore) {
                }
            }
        });
    }

    public static void decodeSvgaFromCache(
            @NonNull Context context,
            @NonNull String url,
            @NonNull SVGAParser.ParseCompletion parseCompletion
    ) {
        File cachedFile = new File(getSvgaCacheDir(context), String.valueOf(url.hashCode()));

        if (!cachedFile.exists() || cachedFile.length() == 0) {
            Log.w(TAG, "No valid cache file found for: " + url);
            parseCompletion.onError();
            return;
        }

        try {
            SVGAParser parser = new SVGAParser(context);

            parser.decodeFromInputStream(
                    new BufferedInputStream(new FileInputStream(cachedFile)),
                    url,
                    parseCompletion,
                    true,
                    null,
                    null
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode from cache: " + e.getMessage(), e);

            parseCompletion.onError();
        }
    }
}