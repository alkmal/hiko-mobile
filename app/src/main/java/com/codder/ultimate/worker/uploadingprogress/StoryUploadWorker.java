package com.codder.ultimate.worker.uploadingprogress;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.codder.ultimate.SessionManager;
import com.codder.ultimate.live.activity.UploadActivity;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.utils.VideoUtil;
import com.otaliastudios.transcoder.Transcoder;
import com.otaliastudios.transcoder.TranscoderListener;
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class StoryUploadWorker extends Worker implements ProgressRequestBody.UploadCallbacks {

    private static final String TAG = "StoryUploadWorker";
    private static final long TIMEOUT_SECONDS = 180;

    private final Context context;
    private final SessionManager sessionManager;

    public StoryUploadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context.getApplicationContext();
        this.sessionManager = new SessionManager(this.context);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            boolean success = uploadPostWithSyncProgress();
            return success ? Result.success() : Result.retry();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in upload", e);
            return Result.retry();
        }
    }

    private boolean uploadPostWithSyncProgress() throws InterruptedException {
        UploadActivity.LocalVideo localVideo = sessionManager.getLocalVideo();
        if (localVideo == null) {
            Log.e(TAG, "No local video found in session");
            return false;
        }

        File originalFile = safeFile(localVideo.getVideo());
        File screenshotFile = safeFile(localVideo.getScreenshot());
        File previewFile = safeFile(localVideo.getPreview());
        updateUploadProgress(1);

        if (originalFile == null || !originalFile.exists()) {
            Log.e(TAG, "Original video file is invalid or missing");
            sendBroadcast(Const.PROGRESS_DONE, "Original video file is invalid or missing");
            sessionManager.clearUploadProgress();
            return false;
        }

        // Step 1: Attempt compression if original is large
        File videoFile = originalFile; // default to original
        long originalSize = originalFile.length();
        Log.d(TAG, "Original file size: " + (originalSize / (1024 * 1024)) + " MB");

        if (originalSize > 5 * 1024 * 1024) {
            Log.d(TAG, "Attempting compression...");

            File compressedFile = new File(context.getCacheDir(), "compressed_" + System.currentTimeMillis() + ".mp4");

            CountDownLatch compressLatch = new CountDownLatch(1);
            final boolean[] compressionSuccess = {false};

            Transcoder.into(compressedFile.getAbsolutePath())
                    .addDataSource(originalFile.getAbsolutePath())
                    .setVideoTrackStrategy(DefaultVideoStrategy.atMost(720).build())
                    .setListener(new TranscoderListener() {
                        @Override
                        public void onTranscodeProgress(double progress) {
                            Log.d(TAG, "Compression progress: " + (int) (progress * 100) + "%");
                            updateUploadProgress(2 + (int) (progress * 23));
                        }

                        @Override
                        public void onTranscodeCompleted(int successCode) {
                            Log.i(TAG, "Compression completed");
                            compressionSuccess[0] = true;
                            compressLatch.countDown();
                        }

                        @Override
                        public void onTranscodeCanceled() {
                            Log.w(TAG, "Compression cancelled");
                            compressLatch.countDown();
                        }

                        @Override
                        public void onTranscodeFailed(@NonNull Throwable exception) {
                            Log.e(TAG, "Compression failed", exception);
                            compressLatch.countDown();
                        }
                    })
                    .transcode();

            compressLatch.await();

            if (compressionSuccess[0] && compressedFile.exists() && compressedFile.length() < originalFile.length()) {
                videoFile = compressedFile;
                Log.i(TAG, "Using compressed file: " + (compressedFile.length() / (1024 * 1024)) + " MB");
            } else {
                Log.w(TAG, "Compressed file not smaller, using original");
            }
        }

        // Step 2: Prepare video meta
        String songId = Objects.requireNonNullElse(localVideo.getSongId(), "");
        String description = Objects.requireNonNullElse(localVideo.getDescription(), "");
        String location = Objects.requireNonNullElse(localVideo.getLocation(), "");
        String userId = Objects.requireNonNullElse(localVideo.getUserId(), "");
        String userName = Objects.requireNonNullElse(localVideo.getUsername(), "");
        String hashtags = Objects.requireNonNullElse(localVideo.getHashtags(), "");
        String mentions = Objects.requireNonNullElse(localVideo.getMentions(), "");
        boolean hasComments = localVideo.isHasComments();
        int privacy = localVideo.getPrivacy();

        long durationMillis;
        try {
            durationMillis = VideoUtil.getDuration(context, Uri.fromFile(videoFile));
        } catch (IOException e) {
            Log.w(TAG, "Failed to get video duration, continuing with 0", e);
            durationMillis = 0;
        }
        long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis);
        updateUploadProgress(30);

        // Step 3: Prepare request body
        MultipartBody.Part videoPart = createMultipartPart("video", videoFile);
        MultipartBody.Part screenshotPart = createMultipartPart("screenshot", screenshotFile);
        MultipartBody.Part previewPart = createMultipartPart("thumbnail", previewFile);

        HashMap<String, RequestBody> params = new HashMap<>();
        params.put("userId", createTextRequestBody(userId));
        params.put("username", createTextRequestBody(userName));
        if (songId.isEmpty()) {
            params.put("isOriginalAudio", createTextRequestBody("true"));
        } else {
            params.put("isOriginalAudio", createTextRequestBody("false"));
            params.put("songId", createTextRequestBody(songId));
        }
        params.put("allowComment", createTextRequestBody(String.valueOf(hasComments)));
        params.put("caption", createTextRequestBody(description));
        params.put("showVideo", createTextRequestBody(String.valueOf(privacy)));
        params.put("location", createTextRequestBody(location));
        params.put("hashtag", createTextRequestBody(hashtags));
        params.put("mentionPeople", createTextRequestBody(mentions));
        params.put("duration", createTextRequestBody(String.valueOf(durationSeconds)));
        params.put("size", createTextRequestBody(VideoUtil.getFileSizeInMB(videoFile) + " MB"));

        // Step 4: Make network call
        final boolean[] isSuccess = {false};
        CountDownLatch latch = new CountDownLatch(1);

        CountingRequestBody.Listener progressListener = (bytesRead, contentLength) -> {
            if (contentLength <= 0) return;
            int percentage = 30 + (int) ((bytesRead * 69) / contentLength);
            onProgressUpdate(Math.min(99, percentage));
        };

        Call<RestResponse> call = RetrofitBuilder.createStoryUploadFile(progressListener)
                .uploadRelite(params, videoPart, screenshotPart, previewPart);

        call.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Log.i(TAG, "Upload successful");
                    Toast.makeText(context, "Upload successful", Toast.LENGTH_SHORT).show();
                    updateUploadProgress(100);
                    sendBroadcast(Const.PROGRESS_DONE, null);
                    sessionManager.clearUploadProgress();
                    isSuccess[0] = true;
                } else {
                    String msg = (response.body() != null) ? response.body().getMessage() : "Unknown error";
                    Log.w(TAG, "Upload failed: " + msg);
                    sendBroadcast(Const.PROGRESS_DONE, msg);
                    sessionManager.clearUploadProgress();
                }
                latch.countDown();
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {
                Log.e(TAG, "Upload failed", t);
                sendBroadcast(Const.PROGRESS_DONE, t.getLocalizedMessage());
                sessionManager.clearUploadProgress();
                latch.countDown();
            }
        });

        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!completed) {
            Log.e(TAG, "Upload timed out");
            sendBroadcast(Const.PROGRESS_DONE, "Upload timed out");
            sessionManager.clearUploadProgress();
            return false;
        }

        return isSuccess[0];
    }


    private File safeFile(String path) {
        if (path == null || path.trim().isEmpty()) return null;
        return new File(path);
    }

    private MultipartBody.Part createMultipartPart(String partName, File file) {
        if (file == null || !file.exists()) return null;
        RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), body);
    }

    private RequestBody createTextRequestBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), Objects.requireNonNullElse(value, ""));
    }

    private void sendBroadcast(String action, String message) {
        Intent intent = new Intent(action);
        if (message != null) {
            intent.putExtra("message", message);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    @Override
    public void onProgressUpdate(int percentage) {
        updateUploadProgress(percentage);
    }

    private void updateUploadProgress(int percentage) {
        int safeProgress = Math.min(100, Math.max(0, percentage));
        Log.d(TAG, "Upload progress: " + percentage + "%");
        sessionManager.setUploadProgress(safeProgress);
        Intent intent = new Intent(Const.UPLOAD_PROGRESS);
        intent.putExtra("progress", safeProgress);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onError() {
        Log.e(TAG, "Upload error occurred");
    }

    @Override
    public void onFinish() {
        Log.i(TAG, "Upload finished");
    }
}

