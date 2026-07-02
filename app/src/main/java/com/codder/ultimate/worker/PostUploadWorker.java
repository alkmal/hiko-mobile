package com.codder.ultimate.worker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.codder.ultimate.SessionManager;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.worker.uploadingprogress.CountingRequestBody;
import com.codder.ultimate.worker.uploadingprogress.ProgressRequestBody;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class PostUploadWorker extends Worker implements ProgressRequestBody.UploadCallbacks {
    private static final String TAG = "PostUploadWorker";
    private String postImage, selectedLocation, hashTag, mentionPeople, caption, isPrivate, isComment;
    private Context context;
    private SessionManager sessionManager;

    public PostUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        sessionManager = new SessionManager(context);
        postImage = workerParams.getInputData().getString(Const.POST_IMAGE_LINK);
        selectedLocation = workerParams.getInputData().getString(Const.SELECTED_LOCATION);
        hashTag = workerParams.getInputData().getString(Const.HASH_TAG);
        mentionPeople = workerParams.getInputData().getString(Const.MENTION_PEOPLE);
        caption = workerParams.getInputData().getString(Const.CAPTION);
        isPrivate = workerParams.getInputData().getString(Const.SHOW_POST);
        isComment = workerParams.getInputData().getString(Const.ALLOW_COMMENT);
    }

    @Override
    public Result doWork() {
        try {
            uploadPost();  // Now synchronous
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error", e);
            return Result.failure();
        }
    }


    private void uploadPost() {
        try {
            File originalFile = new File(postImage);

            // Log original size
            long originalSize = originalFile.length();
            Log.d(TAG, "Original image size: " + (originalSize / 1024) + " KB");

            // Compress the file
            File compressedFile = new Compressor(context)
                    .setMaxWidth(1080)
                    .setMaxHeight(1920)
                    .setQuality(75) // Compression quality
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .compressToFile(originalFile);

            // Log compressed size
            long compressedSize = compressedFile.length();
            Log.d(TAG, "Compressed image size: " + (compressedSize / 1024) + " KB");

            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), compressedFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("post", compressedFile.getName(), requestFile);

            CountingRequestBody.Listener progressListener = (bytesRead, contentLength) -> {
                if (contentLength > 0) {
                    int progress = (int) (((double) bytesRead / contentLength) * 100);
                    Log.d(TAG, "Upload progress: " + progress + "%");
                    Intent intent = new Intent(Const.UPLOAD_PROGRESS);
                    intent.putExtra("progress", progress);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            };

            HashMap<String, RequestBody> hashMap = new HashMap<>();
            hashMap.put("userId", toRequestBody(sessionManager.getUser().getId()));
            hashMap.put("location", toRequestBody(selectedLocation));
            hashMap.put("caption", toRequestBody(caption));
            hashMap.put("hashtag", toRequestBody(hashTag));
            hashMap.put("mentionPeople", toRequestBody(mentionPeople));
            hashMap.put("showPost", toRequestBody(isPrivate));
            hashMap.put("allowComment", toRequestBody(String.valueOf(isComment)));

            Call<RestResponse> call = RetrofitBuilder.createUploadFile(progressListener).uploadPost(hashMap, body);
            Response<RestResponse> response = call.execute();
            if (response.code() == 200 && response.body() != null && response.body().isStatus()) {
                Intent intent = new Intent(Const.UPLOAD_SUCCESS);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                Intent doneIntent = new Intent(Const.PROGRESS_DONE);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(doneIntent);

                Log.d(TAG, "onResponse: success");
            } else {
                Log.e(TAG, "Upload failed: response code = " + response.code());
            }

        } catch (IOException e) {
            Log.e(TAG, "Image compression failed", e);
        }
    }


    public RequestBody toRequestBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), Objects.requireNonNullElse(value, ""));
    }

    @Override
    public void onProgressUpdate(int percentage) {
        Log.d(TAG, "onProgressUpdate: percentage == " + percentage);
        Intent intent = new Intent(Const.UPLOAD_PROGRESS);
        intent.putExtra("progress", percentage);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onError() {

    }

    @Override
    public void onFinish() {

    }

}
