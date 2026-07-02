package com.codder.ultimate.worker.uploadingprogress;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class ProgressRequestBody extends RequestBody {
    private static final String TAG = "ProgressRequestBody";
    private static final int DEFAULT_BUFFER_SIZE = 2048;

    private final File mFile;
    private final String mContentType;
    private final UploadCallbacks mListener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage);
        void onError();
        void onFinish();
    }

    public ProgressRequestBody(File file, String contentType, UploadCallbacks listener) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        this.mFile = file;
        this.mContentType = contentType != null ? contentType : "application/octet-stream";
        this.mListener = listener;
    }

    @Override
    public MediaType contentType() {
        try {
            return MediaType.parse(mContentType);
        } catch (Exception e) {
            Log.w(TAG, "Invalid content type, falling back to application/octet-stream", e);
            return MediaType.parse("application/octet-stream");
        }
    }

    @Override
    public long contentLength() throws IOException {
        return mFile.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileSize = contentLength();
        if (fileSize <= 0) {
            Log.w(TAG, "File size is zero or negative, skipping upload.");
            notifyFinish();
            return;
        }

        try (Source source = Okio.source(mFile)) {
            long totalBytesRead = 0;
            long read;

            while ((read = source.read(sink.buffer(), DEFAULT_BUFFER_SIZE)) != -1) {
                totalBytesRead += read;
                sink.flush();

                final int progress = (int) ((100 * totalBytesRead) / fileSize);

                // Post progress update to main thread safely
                notifyProgress(progress);
            }

            notifyFinish();

        } catch (Exception e) {
            Log.e(TAG, "Exception during file upload", e);
            notifyError();
            throw e;
        }
    }

    private void notifyProgress(final int progress) {
        if (mListener == null) return;

        final int safeProgress = Math.min(100, Math.max(0, progress));

        mainHandler.post(() -> {
            try {
                mListener.onProgressUpdate(safeProgress);
            } catch (Exception e) {
                Log.e(TAG, "Error in onProgressUpdate callback", e);
            }
        });
    }

    private void notifyError() {
        if (mListener == null) return;

        mainHandler.post(() -> {
            try {
                mListener.onError();
            } catch (Exception e) {
                Log.e(TAG, "Error in onError callback", e);
            }
        });
    }

    private void notifyFinish() {
        if (mListener == null) return;

        mainHandler.post(() -> {
            try {
                mListener.onFinish();
            } catch (Exception e) {
                Log.e(TAG, "Error in onFinish callback", e);
            }
        });
    }
}

