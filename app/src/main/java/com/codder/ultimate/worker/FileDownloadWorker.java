package com.codder.ultimate.worker;

import android.app.Notification;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.codder.ultimate.R;
import com.codder.ultimate.utils.SharedConstants;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class FileDownloadWorker extends Worker {

    public static final String KEY_INPUT = "input";
    public static final String KEY_OUTPUT = "output";
    public static final String KEY_NOTIFICATION = "notification";
    private static final String TAG = "FileDownloadWorker";

    private final OkHttpClient mHttpClient = new OkHttpClient();

    public FileDownloadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    private ForegroundInfo createForegroundInfo(Context context) {
        Notification notification =
                new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id))
                        .setContentTitle(context.getString(R.string.notification_download_title))
                        .setTicker(context.getString(R.string.notification_download_title))
                        .setContentText(context.getString(R.string.notification_download_description))
                        .setSmallIcon(R.drawable.ic_baseline_save_alt_24)
                        .setOngoing(true)
                        .setOnlyAlertOnce(true)
                        .build();
        return new ForegroundInfo(SharedConstants.NOTIFICATION_DOWNLOAD, notification);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (getInputData().getBoolean(KEY_NOTIFICATION, false)) {
            setForegroundAsync(createForegroundInfo(getApplicationContext()));
        }

        String inputUrl = getInputData().getString(KEY_INPUT);
        String outputPath = getInputData().getString(KEY_OUTPUT);

        if (inputUrl == null || inputUrl.trim().isEmpty()) {
            Log.e(TAG, "Input URL is null or empty.");
            return Result.failure();
        }
        if (outputPath == null || outputPath.trim().isEmpty()) {
            Log.e(TAG, "Output file path is null or empty.");
            return Result.failure();
        }

        File output = new File(outputPath);
        File temp = null;
        boolean success = false;
        try {
            temp = File.createTempFile("DLD", ".tmp", getApplicationContext().getCacheDir());
            success = doActualWork(inputUrl, output, temp);
        } catch (Exception e) {
            Log.e(TAG, "Exception in main doWork", e);
        }

        if (temp != null && temp.exists() && !temp.delete()) {
            Log.w(TAG, "Could not delete temp file: " + temp);
        }

        if (!success && output.exists() && !output.delete()) {
            Log.w(TAG, "Could not delete failed output file: " + output);
        }

        return success ? Result.success() : Result.failure();
    }

    private boolean doActualWork(String url, File into, File temp) {
        Response response = null;
        try {
            Request request = new Request.Builder().url(url).build();
            response = mHttpClient.newCall(request).execute();

            if (response != null && response.isSuccessful() && response.body() != null) {
                try (BufferedSink out = Okio.buffer(Okio.sink(temp))) {
                    out.writeAll(response.body().source());
                    out.flush();
                }
                FileUtils.copyFile(temp, into);
                return true;
            } else {
                Log.e(TAG, "HTTP request failed or response body is null. Code: " +
                        (response != null ? response.code() : "no response"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during file download or saving.", e);
        } finally {
            if (response != null) {
                response.close();
            }
            if (temp != null && temp.exists() && !temp.delete()) {
                Log.w(TAG, "Could not delete temporary download: " + temp);
            }
        }
        return false;
    }
}

