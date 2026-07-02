package com.codder.ultimate.worker.uploadingprogress;

import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

public class CountingRequestBody extends RequestBody {
    private static final String TAG = "CountingRequestBody";

    private final RequestBody delegate;
    private final Listener listener;

    private CountingSink countingSink;

    public CountingRequestBody(RequestBody delegate, Listener listener) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate RequestBody must not be null");
        }
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return delegate.contentLength();
        } catch (IOException e) {
            Log.e(TAG, "Failed to get content length", e);
            return -1;
        }
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (sink == null) {
            throw new IllegalArgumentException("sink must not be null");
        }

        countingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);

        try {
            delegate.writeTo(bufferedSink);
            bufferedSink.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error writing request body", e);
            throw e;
        }
    }

    protected final class CountingSink extends ForwardingSink {
        private long bytesWritten = 0L;
        private final long contentLength;

        CountingSink(Sink delegate) {
            super(delegate);
            long len = -1;
            try {
                len = contentLength();
            } catch (Exception e) {
                Log.w(TAG, "Could not get content length for CountingSink", e);
            }
            this.contentLength = len;
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            bytesWritten += byteCount;

            if (listener != null) {
                try {
                    listener.onRequestProgress(bytesWritten, contentLength);
                } catch (Exception e) {
                    Log.w(TAG, "Listener threw exception in onRequestProgress", e);
                }
            }
        }
    }

    public interface Listener {
        void onRequestProgress(long bytesWritten, long contentLength);
    }
}

