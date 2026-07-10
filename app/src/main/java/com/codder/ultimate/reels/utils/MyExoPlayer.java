package com.codder.ultimate.reels.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.MainApplication;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheWriter;

import java.util.concurrent.atomic.AtomicBoolean;

public class MyExoPlayer {

    private static final String TAG = "MyExoPlayer";

    // Single player instance only (avoid a second decoder instance).
    private SimpleExoPlayer player;

    // Data source + cache
    private final CacheDataSource.Factory cacheDataSourceFactory;

    // Background thread for cache prefetch (no Player involved)
    private final HandlerThread preloadThread = new HandlerThread("PreloadThread");
    private final Handler preloadHandler;

    private static final MyExoPlayer INSTANCE = new MyExoPlayer();

    private MyExoPlayer() {
        cacheDataSourceFactory = new CacheDataSource.Factory()
                .setCache(MainApplication.simpleCache)
                .setUpstreamDataSourceFactory(new DefaultHttpDataSource.Factory().setUserAgent("TejTok"))
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);

        preloadThread.start();
        preloadHandler = new Handler(preloadThread.getLooper());
    }

    public static MyExoPlayer getInstance() {
        return INSTANCE;
    }

    public SimpleExoPlayer getPlayer(Context context) {
        if (player == null) {
            Context appContext = context.getApplicationContext();

            // TrackSelector with capped video size and bitrate
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(appContext);
            trackSelector.setParameters(
                    trackSelector.buildUponParameters()
                            .setMaxVideoSize(1280, 720)      // cap at 720p
                            .setMaxVideoBitrate(2_000_000)   // ~2 Mbps
            );

            // LoadControl to reduce memory usage
            DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                    .setBufferDurationsMs(5_000, 10_000, 1_000, 2_000)
                    .setBackBuffer(0, false)
                    .build();

            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(appContext)
                    .setEnableDecoderFallback(true);

            player = new SimpleExoPlayer.Builder(appContext, renderersFactory) // pass here
                    .setTrackSelector(trackSelector)
                    .setLoadControl(loadControl)
                    .build();


            // Repeat mode
            player.setRepeatMode(Player.REPEAT_MODE_ONE);


            // Add error listener
            player.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(PlaybackException error) {
                    Log.e(TAG, "ExoPlayer playback error", error);

                    // Optional: try to recover gracefully
                    // For example, reload media or switch to lower-resolution stream
                }
            });
        }
        return player;
    }

    public void playVideo(Context context, String videoUrl) {
        if (player == null) {
            getPlayer(context);
        }

        String normalizedUrl = normalizeVideoUrl(videoUrl);
        Log.d(TAG, "Playing video: " + normalizedUrl);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(normalizedUrl));
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(mediaItem);

        try {
            player.setMediaSource(mediaSource, true);
            player.prepare();
            player.setPlayWhenReady(true);
        } catch (Exception e) {
            Log.e(TAG, "Error starting playback", e);
            // Optional: attempt fallback or notify user
        }
    }


    public void stopAndReleasePlayer() {
        if (player != null) {
            try {
                Log.d(TAG, "Stopping and releasing player");
                player.setPlayWhenReady(false);
                player.stop();
                player.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing player", e);
            } finally {
                player = null;
            }
        } else {
            Log.d(TAG, "stopAndReleasePlayer called but player was null");
        }
    }

    /**
     * Pre-cache data into SimpleCache WITHOUT creating a decoder.
     * Safe to call repeatedly; it runs on a background thread.
     */
    public void preloadToCache(String videoUrl) {
        preloadHandler.post(() -> {
            byte[] tempBuffer = new byte[8 * 1024]; // small scratch buffer
            AtomicBoolean isCanceled = new AtomicBoolean(false);

            try {
                String normalizedUrl = normalizeVideoUrl(videoUrl);
                DataSpec dataSpec = new DataSpec(Uri.parse(normalizedUrl));
                CacheWriter cacheWriter = new CacheWriter(
                        cacheDataSourceFactory.createDataSource(),
                        dataSpec,
                        tempBuffer,
                        (requestLength, bytesCached, newBytesCached) -> {
                            // Optional: progress callback
                            // Log.d(TAG, "Prefetch progress: " + bytesCached + "/" + requestLength);
                        });

                cacheWriter.cache();
                Log.d(TAG, "Pre-cache complete for: " + normalizedUrl);
            } catch (Exception e) {
                Log.e(TAG, "Pre-cache error for " + videoUrl, e);
            }
        });
    }

    private String normalizeVideoUrl(String videoUrl) {
        if (videoUrl == null) return "";
        String trimmed = videoUrl.trim();
        if (trimmed.startsWith("http://")
                || trimmed.startsWith("https://")
                || trimmed.startsWith("content://")
                || trimmed.startsWith("file://")) {
            return trimmed;
        }
        String baseUrl = BuildConfig.BASE_URL.replaceAll("/+$", "");
        return trimmed.startsWith("/") ? baseUrl + trimmed : baseUrl + "/" + trimmed;
    }

    public void releasePreloader() {
        try {
            preloadThread.quitSafely();
        } catch (Exception ignored) {
        }
    }
}
