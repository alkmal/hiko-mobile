package com.codder.ultimate.live.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.codder.ultimate.SessionManager;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.JsonSyntaxException;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Periodically calls host API for user.
 * Handles nulls and avoids leaks.
 */
public final class HostAPICall {

    private static final String TAG = "HostAPICall";
    private static final long API_INTERVAL_MS = 60 * 1000L; // 1 minute

    private final Handler apiHandler;
    private final WeakReference<Context> contextRef;
    private final String type;
    private final SessionManager sessionManager;
    private Runnable apiRunnable;

    public HostAPICall(@NonNull Context context, @NonNull String type) {
        this.contextRef = new WeakReference<>(context.getApplicationContext());
        this.type = type;
        this.sessionManager = new SessionManager(context.getApplicationContext());
        this.apiHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Starts periodic API calls every 60 seconds.
     */
    public void startApiCallLoop() {
        stopApiCallLoop(); // Ensure previous is stopped
        apiRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    callApi();
                } catch (Exception e) {
                    Log.e(TAG, "Exception in callApi: " + e.getMessage(), e);
                }
                apiHandler.postDelayed(this, API_INTERVAL_MS);
            }
        };
        apiHandler.postDelayed(apiRunnable, API_INTERVAL_MS);
    }

    /**
     * Calls the host API with proper null checks.
     */
    public void callApi() {
        String date = getIndianDate();
        Log.d(TAG, "callApi: date = " + date);

        if (sessionManager.getUser() != null) {
            Call<RestResponse> call = RetrofitBuilder.create().getHostApi(sessionManager.getUser().getId(), type, date);
            call.enqueue(new Callback<RestResponse>() {
                @Override
                public void onResponse(@NonNull Call<RestResponse> call, @NonNull Response<RestResponse> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "API call successful: " + response.body());
                    } else {
                        Log.w(TAG, "API call unsuccessful, code: " + response.code());
                    }
                }
                @Override public void onFailure(Call<RestResponse> call, Throwable t) {
                    if (t instanceof JsonSyntaxException || t instanceof IllegalStateException) {
                        Log.e(TAG, "JSON mismatch; likely non-JSON or wrong shape", t);
                        // Optional: fire a one-off diagnostic request using Call<ResponseBody> to log raw text
                    } else {
                        Log.e(TAG, "Network/other failure", t);
                    }
                }

            });
        } else {
            Log.w(TAG, "callApi: user is null, skipping API call");
        }
    }

    /**
     * Gets today's date in India timezone, crash free.
     */
    @NonNull
    private String getIndianDate() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy", Locale.ENGLISH);
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            return dateFormat.format(new Date());
        } catch (Exception e) {
            Log.e(TAG, "Failed to get Indian date: ", e);
            return "";
        }
    }

    /**
     * Stops the periodic API call loop.
     */
    public void stopApiCallLoop() {
        if (apiHandler != null && apiRunnable != null) {
            Log.d(TAG, "stopApiCallLoop: removing callbacks");
            apiHandler.removeCallbacks(apiRunnable);
        }
    }
}

