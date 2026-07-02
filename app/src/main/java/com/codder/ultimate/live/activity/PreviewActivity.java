package com.codder.ultimate.live.activity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.File;

public class PreviewActivity extends BaseActivity {
    private static final String TAG = "PreviewActivity";

    public static final String EXTRA_VIDEO = "video";

    private PreviewActivityViewModel mModel;
    private SimpleExoPlayer mPlayer;
    private String mVideo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preview);
        getWindow().setStatusBarColor(Color.parseColor("#310A44"));

        mModel = new ViewModelProvider(this).get(PreviewActivityViewModel.class);

        mVideo = getIntent().getStringExtra(EXTRA_VIDEO);
        if (mVideo == null || mVideo.trim().isEmpty()) {
            Log.e(TAG, "Video path is null or empty, cannot play video.");
            finish();
            return;
        }

        mPlayer = new SimpleExoPlayer.Builder(this).build();

        PlayerView playerView = findViewById(R.id.player);
        playerView.setPlayer(mPlayer);

        startPlayer();
    }

    private void startPlayer() {
        if (mPlayer == null || mVideo == null) {
            Log.w(TAG, "Player or video path is null, cannot start playback.");
            return;
        }

        try {
            Uri videoUri = Uri.fromFile(new File(mVideo));
            if (videoUri == null) {
                Log.e(TAG, "Invalid video URI");
                return;
            }

            // Use MediaItem instead of deprecated LoopingMediaSource
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(videoUri)
                    .build();

            mPlayer.setMediaItem(mediaItem);
            mPlayer.setRepeatMode(Player.REPEAT_MODE_ONE); // loop playback
            mPlayer.seekTo(mModel.window, mModel.position);
            mPlayer.setPlayWhenReady(true);
            mPlayer.prepare();

        } catch (Exception e) {
            Log.e(TAG, "Error starting player", e);
        }
    }

    private void stopPlayer() {
        if (mPlayer == null) {
            return;
        }

        try {
            mModel.position = mPlayer.getCurrentPosition();
            mModel.window = mPlayer.getCurrentWindowIndex();
            mPlayer.setPlayWhenReady(false);
            mPlayer.stop();
        } catch (Exception e) {
            Log.e(TAG, "Error stopping player", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPlayer != null && mPlayer.getPlaybackState() == Player.STATE_IDLE) {
            startPlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            try {
                stopPlayer();
                mPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing player", e);
            } finally {
                mPlayer = null;
            }
        }
    }

    public static class PreviewActivityViewModel extends ViewModel {
        public long position = 0L;
        public int window = 0;
    }
}