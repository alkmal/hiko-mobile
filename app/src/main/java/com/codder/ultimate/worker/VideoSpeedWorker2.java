package com.codder.ultimate.worker;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.otaliastudios.transcoder.Transcoder;
import com.otaliastudios.transcoder.TranscoderListener;
import com.otaliastudios.transcoder.TranscoderOptions;
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategies;

import java.io.File;

public class VideoSpeedWorker2 extends ListenableWorker {

    public static final String KEY_INPUT = "input";
    public static final String KEY_OUTPUT = "output";
    public static final String KEY_SPEED = "speed";
    private static final String TAG = "VideoSpeedWorker2";

    public VideoSpeedWorker2(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            String inputPath = getInputData().getString(KEY_INPUT);
            String outputPath = getInputData().getString(KEY_OUTPUT);
            float speed = getInputData().getFloat(KEY_SPEED, 1f);

            if (inputPath == null || inputPath.trim().isEmpty()) {
                Log.e(TAG, "Input path is null or empty.");
                completer.set(Result.failure(
                        new Data.Builder().putString("error", "Input path is missing.").build()));
                return null;
            }
            if (outputPath == null || outputPath.trim().isEmpty()) {
                Log.e(TAG, "Output path is null or empty.");
                completer.set(Result.failure(
                        new Data.Builder().putString("error", "Output path is missing.").build()));
                return null;
            }
            File input = new File(inputPath);
            File output = new File(outputPath);
            if (!input.exists() || !input.canRead()) {
                Log.e(TAG, "Input file does not exist or is not readable: " + inputPath);
                completer.set(Result.failure(
                        new Data.Builder().putString("error", "Input file is not accessible.").build()));
                return null;
            }

            try {
                doActualWork(input, output, speed, completer);
            } catch (Throwable t) {
                Log.e(TAG, "Exception starting transcoder.", t);
                completer.setException(t);
            }
            return null;
        });
    }

    private void doActualWork(
            @NonNull File input, @NonNull File output, float speed,
            @NonNull CallbackToFutureAdapter.Completer<Result> completer) {
        try {
            TranscoderOptions.Builder transcoder = Transcoder.into(output.getAbsolutePath());
            transcoder.addDataSource(input.getAbsolutePath());
            transcoder.setListener(new TranscoderListener() {
                private boolean completed = false;

                private synchronized void safeComplete(Runnable action) {
                    if (!completed) {
                        completed = true;
                        action.run();
                    }
                }

                @Override
                public void onTranscodeProgress(double progress) {
                    Log.d(TAG, "onTranscodeProgress: " +progress);
                }

                @Override
                public void onTranscodeCompleted(int code) {
                    Log.d(TAG, "Applying video speed has finished.");
                    safeComplete(() -> completer.set(Result.success()));
                    safeDelete(input, "input");
                }

                @Override
                public void onTranscodeCanceled() {
                    Log.d(TAG, "Applying video speed was cancelled.");
                    safeComplete(completer::setCancelled);
                    safeDelete(input, "input");
                    safeDelete(output, "output");
                }

                @Override
                public void onTranscodeFailed(@NonNull Throwable e) {
                    Log.e(TAG, "Applying video speed failed with error.", e);
                    safeComplete(() -> completer.setException(e));
                    safeDelete(input, "input");
                    safeDelete(output, "output");
                }
            });

            transcoder.setSpeed(speed);
            transcoder.setVideoTrackStrategy(DefaultVideoStrategies.for720x1280());
            transcoder.transcode();

        } catch (Throwable e) {
            Log.e(TAG, "Exception starting transcoding operation.", e);
            completer.setException(e);
            safeDelete(input, "input");
            safeDelete(output, "output");
        }
    }

    private void safeDelete(@NonNull File file, @NonNull String label) {
        try {
            if (file.exists() && !file.delete()) {
                Log.w(TAG, "Could not delete " + label + " file: " + file.getAbsolutePath());
            }
        } catch (Throwable t) {
            Log.w(TAG, "Exception deleting " + label + " file: " + file.getAbsolutePath(), t);
        }
    }
}

