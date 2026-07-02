package com.codder.ultimate.chat.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.chat.adapter.GiftEventAdapter;
import com.codder.ultimate.chat.modelclass.ChatUserListRoot;
import com.codder.ultimate.chat.modelclass.GiftEvent;
import com.codder.ultimate.databinding.ActivityFakeVideoCallBinding;
import com.codder.ultimate.live.fragment.EmojiBottomSheetFragment;
import com.codder.ultimate.live.model.GiftRoot;
import com.codder.ultimate.live.viewModel.EmojiSheetViewModel;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.utils.SvgaCacheManager;
import com.codder.ultimate.viewModel.ViewModelFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FakeVideoCallActivity extends AppCompatActivity {

    private static final String TAG = "FakeVideoCallActivity";
    private GiftEventAdapter giftFeedAdapter;
    private final java.util.ArrayList<GiftEvent> giftFeed = new java.util.ArrayList<>();

    private ActivityFakeVideoCallBinding binding;
    private SessionManager sessionManager;
    private SimpleExoPlayer player;
    private String videoURL;
    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
    private Preview preview;
    private boolean isMuted = false;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private long startTime = 0;
    private EmojiSheetViewModel giftViewModel;
    private EmojiBottomSheetFragment emojiBottomsheetFragment;
    private LinearLayoutManager giftLm;
    private Uri defaultRawUri;
    private Uri currentSourceUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fake_video_call);

        applyInsets();
        sessionManager = new SessionManager(this);
        defaultRawUri = RawResourceDataSource.buildRawResourceUri(R.raw.fake_video1);

        handleIntentData();
        initializeCamera();
        fetchUpdatedCoins();
        initListeners();
        setupGiftFeed();
    }

    private void initListeners() {
        giftViewModel = new ViewModelProvider(this, new ViewModelFactory(new EmojiSheetViewModel()).createFor()).get(EmojiSheetViewModel.class);
        giftViewModel.initEmojiSheet(this);
        giftViewModel.getGiftCategory();
        emojiBottomsheetFragment = new EmojiBottomSheetFragment();
        binding.imggift2.setOnClickListener(v -> {
            if (!emojiBottomsheetFragment.isAdded())
                emojiBottomsheetFragment.show(getSupportFragmentManager(), "emojiSheet");
        });

        giftViewModel.finalGift.observe(this, giftItem -> {
            if (giftItem != null) {
                double totalCoin = giftItem.getCoin() * giftItem.getCount();
                if (sessionManager.getUser().getDiamond() < totalCoin) {
                    Toast.makeText(FakeVideoCallActivity.this, getString(R.string.you_not_have_enough_diamonds_to_send_gift), Toast.LENGTH_SHORT).show();
                    return;
                }

                getCoin(giftItem);

                String finalGiftLink = null;
                List<GiftRoot.GiftItem> giftItemList = sessionManager.getGiftsList(giftItem.getCategory());
                for (int i = 0; i < giftItemList.size(); i++) {
                    if (giftItem.getId().equals(giftItemList.get(i).getId())) {
                        finalGiftLink = BuildConfig.BASE_URL + giftItemList.get(i).getImage();
                    }
                }

                // Ensure we pass absolute URL to the feed
                addGiftToFeed(
                        sessionManager.getUser().getId(),
                        sessionManager.getUser().getName(),
                        sessionManager.getUser().getImage(),
                        finalGiftLink,               // absolute (works for .svga & .png/.webp)
                        giftItem.getCount()
                );

                if (giftItem.getType() == 2) {
                    // finalGiftLink must be the .svga URL (typically gift.getImage())
                    playSvga(finalGiftLink);
                } else {
                    Glide.with(FakeVideoCallActivity.this).load(finalGiftLink).into(binding.imgGift);
                    Glide.with(FakeVideoCallActivity.this).load(RayziUtils.getImageFromNumber(giftItem.getCount())).into(binding.imgGiftCount);
                    binding.tvGiftUserName.setText(sessionManager.getUser().getName() + getString(R.string.sent_a_gift));
                    binding.lytGift.setVisibility(VISIBLE);
                    binding.tvGiftUserName.setVisibility(VISIBLE);
                    new Handler().postDelayed(() -> {
                        if (binding != null) {
                            binding.lytGift.setVisibility(GONE);
                            binding.tvGiftUserName.setVisibility(GONE);
                            binding.tvGiftUserName.setText("");
                            binding.imgGift.setImageDrawable(null);
                            binding.imgGiftCount.setImageDrawable(null);
                        }
                    }, 4000);
                }

                emojiBottomsheetFragment.dismiss();
            }
        });

        binding.btnDecline.setOnClickListener(v -> finish());
        binding.btnSwitchCamera.setOnClickListener(this::switchCamera);
        binding.btnMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            if (isMuted) {
                binding.btnMute.setImageResource(R.drawable.btn_mute_pressed);
            } else {
                binding.btnMute.setImageResource(R.drawable.btn_unmute);
            }
        });

    }

    private void addGiftToFeed(String userId, String name, String avatar, String giftAbsUrl, int count) {
        GiftEvent ev =
                new GiftEvent(userId, name, avatar, giftAbsUrl, count, System.currentTimeMillis());
        giftFeed.add(ev);
        // trim to last N (avoid unbounded growth)
        if (giftFeed.size() > 50) giftFeed.remove(0);
        final boolean forceScroll = true;
        final boolean wasNearBottom = isGiftFeedNearBottom();

        giftFeedAdapter.submitList(new java.util.ArrayList<>(giftFeed), () -> {
            if (forceScroll || wasNearBottom) {
                binding.rvGiftFeed.post(() -> scrollGiftFeedToBottom(false));
            }
        });
    }

    private void scrollGiftFeedToBottom(boolean smooth) {
        int last = Math.max(giftFeedAdapter.getItemCount() - 1, 0);
        if (smooth) binding.rvGiftFeed.smoothScrollToPosition(last);
        else binding.rvGiftFeed.scrollToPosition(last);
    }

    private boolean isGiftFeedNearBottom() {
        int lastVisible = giftLm.findLastVisibleItemPosition();
        int total = giftFeedAdapter.getItemCount();
        return lastVisible >= total - 2; // “near” bottom = within last 2
    }

    private void setupGiftFeed() {
        giftFeedAdapter = new GiftEventAdapter();
        giftLm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        giftLm.setStackFromEnd(true); // viewport hugs the bottom like a chat
        binding.rvGiftFeed.setLayoutManager(giftLm);
        binding.rvGiftFeed.setAdapter(giftFeedAdapter);

        binding.rvGiftFeed.setItemAnimator(null);
    }

    private void getCoin(GiftRoot.GiftItem selectedGift) {
        Call<UserRoot> call = RetrofitBuilder.create().sendGiftFakeHost(sessionManager.getUser().getId(), selectedGift.getCoin(), "", Const.CALL_GIFT);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && response.body().getUser() != null) {
                        sessionManager.saveUser(response.body().getUser());
                        Log.d(TAG, "onResponse: getCoin == sessionManager.getUser().getDiamond() ==  " + sessionManager.getUser().getDiamond());
                    }
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void handleIntentData() {
        videoURL = getIntent().getStringExtra(Const.VIDEO_LINK);

        Uri sourceUri = (videoURL == null || videoURL.isEmpty())
                ? defaultRawUri
                : Uri.parse(videoURL);

        if (videoURL == null || videoURL.isEmpty()) {
            Toast.makeText(this, R.string.please_try_after_some_time, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userJson = getIntent().getStringExtra(Const.USER);
        boolean isFromRandom = getIntent().getBooleanExtra(Const.IS_FROM_RANDOM, false);

        if (userJson != null) {
            if (isFromRandom) {
                GuestProfileRoot.User guestUser = new Gson().fromJson(userJson, GuestProfileRoot.User.class);
                bindUserDetails(guestUser.getUniqueId(), guestUser.getName(), guestUser.getImage(), guestUser.getAvatarFrameImage());
            } else {
                int randomUniqueId = new Random().nextInt(900000) + 100000;

                ChatUserListRoot.ChatUserItem chatUser = new Gson().fromJson(userJson, ChatUserListRoot.ChatUserItem.class);
                bindUserDetails(String.valueOf(randomUniqueId), chatUser.getName(), chatUser.getImage(), chatUser.getAvatarFrameImage());
            }
        }

        // Start timer
        startTime = System.currentTimeMillis();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                binding.tvTimer.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);

        initializePlayer(sourceUri);
    }

    private void bindUserDetails(String userId, String name, String imageUrl, String frameImage) {
        binding.tvUniqueId.setText(getString(R.string.id_) + userId);
        binding.tvName.setText(name);
        binding.imgProfile.setUserImage(imageUrl, frameImage, 30);
    }

    private void initializePlayer(@NonNull Uri sourceUri) {
        player = new SimpleExoPlayer.Builder(this).build();
        binding.playerView.setPlayer(player);

        currentSourceUri = sourceUri;

        MediaSource mediaSource = new ProgressiveMediaSource
                .Factory(new DefaultDataSourceFactory(this, "exoplayer-rayzi"))
                .createMediaSource(MediaItem.fromUri(sourceUri));

        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.setPlayWhenReady(true);
        player.setMediaSource(mediaSource);
        player.prepare();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_BUFFERING:
                        Log.d(TAG, "Buffering...");
                        break;
                    case Player.STATE_READY:
                        Log.d(TAG, "Playback Ready");
                        break;
                    case Player.STATE_ENDED:
                        Log.d(TAG, "Playback Ended");
                        Toast.makeText(FakeVideoCallActivity.this, R.string.call_ended, Toast.LENGTH_SHORT).show();
                        binding.playerView.postDelayed(() -> finish(), 1500);
                        break;
                    case Player.STATE_IDLE:
                        Log.d(TAG, "Player Idle");
                        break;
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Log.w(TAG, "Playback error: " + error.getMessage());
                // If not already using the default, fall back to raw
                if (!defaultRawUri.equals(currentSourceUri)) {
                    currentSourceUri = defaultRawUri;
                    MediaSource fallback = new ProgressiveMediaSource
                            .Factory(new DefaultDataSourceFactory(FakeVideoCallActivity.this, "exoplayer-rayzi"))
                            .createMediaSource(MediaItem.fromUri(defaultRawUri));
                    player.setMediaSource(fallback);
                    player.prepare();
                    player.setPlayWhenReady(true);
                } else {
                    Log.e(TAG, "Fallback also failed.", error);
                    Toast.makeText(FakeVideoCallActivity.this, R.string.please_try_after_some_time, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initializeCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
            return;
        }

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera initialization error", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void switchCamera(View view) {
        if (cameraProvider == null || preview == null) return;

        cameraSelector = (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
                ? CameraSelector.DEFAULT_BACK_CAMERA
                : CameraSelector.DEFAULT_FRONT_CAMERA;

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview);
        } catch (Exception e) {
            Log.e(TAG, "Failed to switch camera", e);
        }
    }

    private void fetchUpdatedCoins() {
        String userId = sessionManager.getUser().getId();
        double callCharge = sessionManager.getSetting().getCallCharge();

        RetrofitBuilder.create().sendGiftFakeHost(userId, callCharge, "", Const.CAll).enqueue(new Callback<UserRoot>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserRoot result = response.body();

                    if (result.isStatus() && result.getUser() != null) {
                        sessionManager.saveUser(result.getUser());
                        Log.d(TAG, "Coin updated. Current balance: " + result.getUser().getRCoin());
                    } else {
                        Log.w(TAG, "Coin update failed: " + (result.getMessage() != null ? result.getMessage() : "Unknown error"));
                    }
                } else {
                    Log.e(TAG, "Coin API response error. Code: " + response.code() + ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                Log.e(TAG, "Coin API request failed: " + t.getLocalizedMessage(), t);
            }
        });
    }



    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) player.setPlayWhenReady(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) player.setPlayWhenReady(true);
    }

    @Override
    protected void onDestroy() {
        if (player != null) player.release();
        if (cameraProvider != null) cameraProvider.unbindAll();
        super.onDestroy();

        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }

    }

    private void startSvga(@NonNull SVGAVideoEntity video) {
        binding.svgaImage.setImageDrawable(new SVGADrawable(video));
        binding.svgaImage.startAnimation();

        long durMs = (long) (video.getFrames() * 1000f / video.getFPS());
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.svgaImage.stopAnimation();
            binding.svgaImage.clear();
            binding.svgaImage.setVisibility(GONE);
        }, Math.max(durMs, 1500)); // ensure it’s visible briefly even for short files
    }

    private void playSvga(@NonNull String svgaAbsUrl) {
        if (svgaAbsUrl.trim().isEmpty()) return;

        binding.svgaImage.clear();
        binding.svgaImage.setLoops(1);
        binding.svgaImage.setVisibility(View.VISIBLE);
        binding.svgaImage.bringToFront();
        binding.svgaImage.setElevation(20f);

        // 1) Try local cache
        SvgaCacheManager.decodeSvgaFromCache(this, svgaAbsUrl, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(@NonNull SVGAVideoEntity video) {
                Log.d(TAG, "SVGA loaded from cache: " + svgaAbsUrl);
                startSvga(video);
            }

            @Override
            public void onError() {
                // 2) Fallback to network
                SVGAParser parser = new SVGAParser(FakeVideoCallActivity.this);
                try {
                    parser.decodeFromURL(new URL(svgaAbsUrl), new SVGAParser.ParseCompletion() {
                        @Override
                        public void onComplete(@NonNull SVGAVideoEntity video) {
                            Log.d(TAG, "SVGA loaded from network: " + svgaAbsUrl);
                            startSvga(video);
                        }

                        @Override
                        public void onError() {
                            Log.e(TAG, "SVGA decode error (network): " + svgaAbsUrl);
                            binding.svgaImage.setVisibility(GONE);
                        }
                    }, null);
                } catch (Exception e) {
                    Log.e(TAG, "SVGA URL error", e);
                    binding.svgaImage.setVisibility(GONE);
                }
            }
        });
    }

    private void applyInsets() {
        // small self preview bubble uses margins (keeps its size)
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutPreview, (v, insets) -> {
            Insets sys = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.topMargin = sys.top + dp(20);   // preserve your original spacing
            lp.rightMargin = sys.right + dp(30);
            v.setLayoutParams(lp);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.lytHost, (v, insets) -> {
            Insets sys = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.topMargin = sys.top + dp(20);   // preserve your original spacing
            lp.rightMargin = sys.right + dp(30);
            v.setLayoutParams(lp);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutBottom, (v, insets) -> {
            int b = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            lp.height = getResources().getDimensionPixelSize(R.dimen.bottom_offset_dp) + b;
            v.setLayoutParams(lp);
            // keep your own internal padding if needed
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(binding.lytMute, (v, insets) -> {
            int b = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            lp.height = getResources().getDimensionPixelSize(R.dimen.bottom_insets) + b;
            v.setLayoutParams(lp);
            // keep your own internal padding if needed
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.lytSwitchCamera, (v, insets) -> {
            int b = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            lp.height = getResources().getDimensionPixelSize(R.dimen.bottom_insets) + b;
            v.setLayoutParams(lp);
            // keep your own internal padding if needed
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.lytGift2, (v, insets) -> {
            int b = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            lp.height = getResources().getDimensionPixelSize(R.dimen.bottom_insets) + b;
            v.setLayoutParams(lp);
            // keep your own internal padding if needed
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.lytDecline, (v, insets) -> {
            int b = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            lp.height = getResources().getDimensionPixelSize(R.dimen.bottom_insets) + b;
            v.setLayoutParams(lp);
            // keep your own internal padding if needed
            return insets;
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.navSpacer), (v, insets) -> {
            int b = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            lp.height = b;
            v.setLayoutParams(lp);
            return insets;
        });
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
