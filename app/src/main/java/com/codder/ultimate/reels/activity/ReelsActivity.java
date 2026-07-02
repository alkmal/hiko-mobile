package com.codder.ultimate.reels.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.bottomsheets.BottomSheetReport;
import com.codder.ultimate.bottomsheets.BottomSheetReportOption;
import com.codder.ultimate.databinding.ActivityReelsBinding;
import com.codder.ultimate.databinding.ItemReelsBinding;
import com.codder.ultimate.guestuser.activity.GuestActivity;
import com.codder.ultimate.post.BottomSheetCommentLikeList;
import com.codder.ultimate.reels.adapter.ReelsAdapter;
import com.codder.ultimate.reels.model.ReliteRoot;
import com.codder.ultimate.reels.utils.MyExoPlayer;
import com.codder.ultimate.reels.viewmodel.ReelsViewModel;
import com.codder.ultimate.retrofit.CommentApiCalling;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.UserApiCall;
import com.codder.ultimate.viewModel.ViewModelFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ReelsActivity extends BaseActivity implements Player.Listener {

    private static final String TAG = "ReelsActivity";
    ActivityReelsBinding binding;
    private SimpleExoPlayer player;
    private ItemReelsBinding playerBinding;
    private int lastPosition = -1;
    private Animation animation;
    private ReelsViewModel viewModel;
    private int position;
    private List<ReliteRoot.VideoItem> reels = new ArrayList<>();
    private CommentApiCalling commentApiCalling;
    private String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reels);
        animation = AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.slow_rotate);
        new PagerSnapHelper().attachToRecyclerView(binding.rvReels);

        commentApiCalling = new CommentApiCalling(this);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new ReelsViewModel()).createFor()).get(ReelsViewModel.class);
        viewModel.init(this);
        binding.setViewModel(viewModel);

        Intent intent = Objects.requireNonNull(getIntent());
        position = intent.getIntExtra(Const.POSITION, 0);
        String data = intent.getStringExtra(Const.DATA);
        userId = intent.getStringExtra(Const.USERID);

        if (data != null && !data.isEmpty()) {
            try {
                reels = new Gson().fromJson(data, new TypeToken<ArrayList<ReliteRoot.VideoItem>>() {
                }.getType());
                viewModel.start = reels.size() - Const.LIMIT;
                viewModel.reelsAdapter.submitList(reels); // updated line
                binding.rvReels.scrollToPosition(position);
                viewModel.reelsAdapter.playVideoAt(position); // updated line
                if (userId == null || userId.isEmpty()) {
                    userId = reels.get(0).getUserId();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing reel data", e);
            }
        }else if (data == null || data.isEmpty()) {
            // Special case: data null, but we have userId and reelId
            String reelId = intent.getStringExtra("postId"); // get the reelId
            String userId = intent.getStringExtra("userId"); // get the reelId
            if (userId != null && !userId.isEmpty() && reelId != null && !reelId.isEmpty()) {
                // Fetch user's reels
                viewModel.getReliteData(false, userId, false, true, 0);

                // Observe the adapter to scroll to the reelId when data is loaded
                viewModel.reelsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        List<ReliteRoot.VideoItem> list = viewModel.reelsAdapter.getCurrentList();
                        for (int i = 0; i < list.size(); i++) {
                            if (reelId.equals(list.get(i).getId())) {
                                binding.rvReels.scrollToPosition(i);
                                viewModel.reelsAdapter.playVideoAt(i);
                                break;
                            }
                        }
                        viewModel.reelsAdapter.unregisterAdapterDataObserver(this);
                    }
                });
            }
        }



        initListener();
    }

    private void initListener() {
        Log.d(TAG, "initView: ll " + lastPosition);
        binding.swipeRefresh.autoLoadMore();

        binding.swipeRefresh.setOnRefreshListener(refreshLayout -> {
            viewModel.getReliteData(false, userId, false, true, 0);
        });

        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> {
            viewModel.getReliteData(true, userId, false, true, 0);
        });

        viewModel.isLoadCompleted.observe(this, aBoolean -> {
            binding.swipeRefresh.finishRefresh();
            binding.swipeRefresh.finishLoadMore();
            viewModel.isFirstTimeLoading.set(false);
            viewModel.isLoadMoreLoading.set(false);
        });

        binding.rvReels.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int position = ((LinearLayoutManager) Objects.requireNonNull(binding.rvReels.getLayoutManager())).findFirstCompletelyVisibleItemPosition();
                    if (position != -1 && lastPosition != position) {
                        View view = Objects.requireNonNull(binding.rvReels.getLayoutManager()).findViewByPosition(position);
                        if (view != null) {
                            lastPosition = position;
                            ItemReelsBinding binding1 = DataBindingUtil.bind(view);
                            if (binding1 != null) {
                                binding1.lytSound.startAnimation(animation);
                                playVideo(viewModel.reelsAdapter.getCurrentList().get(position).getVideo(), binding1);
                            }
                        }
                    }
                }
            }
        });

        viewModel.reelsAdapter.setListener(new ReelsAdapter.OnReelsVideoAdapterListener() {
            @Override
            public void onItemClick(ItemReelsBinding reelsBinding, int pos, int type) {
                Log.d(TAG, "onItemClick fired. Pos: " + pos + " Type: " + type);
                if (pos >= viewModel.reelsAdapter.getCurrentList().size()) return;

                lastPosition = pos;

                if (type == 1) {
                    playVideo(viewModel.reelsAdapter.getCurrentList().get(pos).getVideo(), reelsBinding);
                } else if (type == 2) {
                    SimpleExoPlayer currentPlayer = (SimpleExoPlayer) reelsBinding.playerView.getPlayer();

                    if (currentPlayer != null) {
                        boolean isPlaying = currentPlayer.getPlayWhenReady();
                        currentPlayer.setPlayWhenReady(!isPlaying);

                        showMediaButton();
                    } else {
                        Log.w(TAG, "Player is null or not attached to view.");
                    }
                }

            }

            @Override
            public void onClickUser(ReliteRoot.VideoItem reel) {

            }

            @Override
            public void onClickShare(ReliteRoot.VideoItem reel) {

            }

            @Override
            public void onClickCommentList(ReliteRoot.VideoItem reel, int pos) {
                BottomSheetCommentLikeList.newInstance(
                        null,
                        new Gson().toJson(reel),
                        BottomSheetCommentLikeList.COMMENTS,
                        "Reels"
                ).show(getSupportFragmentManager(), "BottomSheetCommentList");
            }

            @Override
            public void onClickLikeList(ReliteRoot.VideoItem reel) {
                BottomSheetCommentLikeList.newInstance(
                        null,
                        new Gson().toJson(reel),
                        BottomSheetCommentLikeList.LIKES,
                        "Reels"
                ).show(getSupportFragmentManager(), "BottomSheetLikeList");
            }


            @Override
            public void onHashTagClick(String hashTag) {

            }

            @Override
            public void onMentionClick(String userName) {
                startActivity(new Intent(ReelsActivity.this, GuestActivity.class).putExtra(Const.USERNAME, userName));
            }

            @Override
            public void onDoubleClick(ReliteRoot.VideoItem reel, MotionEvent e, ItemReelsBinding binding) {
                showHeart(e, binding);
                binding.likebtn.performClick();
            }

            @Override
            public void onClickLike(ItemReelsBinding reelsBinding, int pos, ReliteRoot.VideoItem videoItem) {
                ReliteRoot.VideoItem model = viewModel.reelsAdapter.getCurrentList().get(pos);

                boolean willBeLiked = !model.isLike();
                int currentCount = model.getLikeCount();
                int updatedCount = willBeLiked ? currentCount + 1 : Math.max(0, currentCount - 1); // prevent negative

                viewModel.cacheLikeState(videoItem.getId(), willBeLiked, updatedCount);

                model.setLike(willBeLiked);
                model.setLikeCount(updatedCount);

                reelsBinding.likebtn.setLiked(willBeLiked);
                reelsBinding.tvLikeCount.setText(String.valueOf(updatedCount));
                viewModel.reelsAdapter.notifyItemChanged(pos, model); // update UI immediately

                // Sync with backend
                commentApiCalling.toggleLikeRelite(videoItem.getId(), isLiked -> {
                    model.setLike(isLiked);
                    model.setLikeCount(isLiked ? updatedCount : Math.max(0, updatedCount - 1));
                    reelsBinding.likebtn.setLiked(isLiked);
                    reelsBinding.tvLikeCount.setText(String.valueOf(model.getLikeCount()));
                    viewModel.reelsAdapter.notifyItemChanged(pos, model);
                    viewModel.cacheLikeState(videoItem.getId(), isLiked, model.getLikeCount());

                });
            }


            @Override
            public void onClickReport(ReliteRoot.VideoItem reel) {
                openReportSheet(reel.getUserId());
            }

            @Override
            public void onFollowClick(@NonNull ReliteRoot.VideoItem reel, @NonNull ItemReelsBinding binding, int position) {
                if (reel.getUserId() == null) return;

                binding.pd.setVisibility(View.VISIBLE);
                binding.tvFollow.setVisibility(View.GONE);

                userApiCall.followUnfollowUser(!reel.isFollow(), reel.getUserId(), "", new UserApiCall.OnFollowUnfollowListener() {
                    @Override
                    public void onFollowSuccess() {
                        reel.setFollow(true);
                        viewModel.reelsAdapter.notifyItemChanged(position, reel);
                        binding.tvFollow.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onUnfollowSuccess() {
                        reel.setFollow(false);
                        viewModel.reelsAdapter.notifyItemChanged(position, reel);
                        binding.tvFollow.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFail() {
                        toggleFollowUI(binding);
                    }
                });
            }
        });

    }

    private void toggleFollowUI(ItemReelsBinding binding) {
        binding.tvFollow.setVisibility(View.VISIBLE);
        binding.pd.setVisibility(View.GONE);
    }

    private void showMediaButton() {
        SimpleExoPlayer player = MyExoPlayer.getInstance().getPlayer(ReelsActivity.this);
        if (player == null) return;

        int icon = player.getPlayWhenReady() ? R.drawable.icon_pause : R.drawable.icon_play;
        binding.imgMedia.setImageDrawable(ContextCompat.getDrawable(ReelsActivity.this, icon));
        binding.imgMedia.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() ->
                binding.imgMedia.setVisibility(View.GONE), 1000);
    }

    private void openReportSheet(String userId) {
        new BottomSheetReportOption(ReelsActivity.this, new BottomSheetReportOption.OnReportedListener() {
            @Override
            public void onReported() {
                new BottomSheetReport(ReelsActivity.this, userId, () -> {
                    View layout = LayoutInflater.from(ReelsActivity.this).inflate(R.layout.toast_layout, findViewById(R.id.layout_custom_toast));
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                });
            }

            @Override
            public void onBlocked() {
                finish();
            }
        });
    }

    private void playVideo(String videoUrl, ItemReelsBinding itemReelsBinding) {

        Log.d(TAG, "Playing video: ==== " + videoUrl);

        if (playerBinding != null && playerBinding != itemReelsBinding) {
            playerBinding.playerView.setPlayer(null);
        }

        playerBinding = itemReelsBinding;
        itemReelsBinding.buffering.setVisibility(View.VISIBLE);

        itemReelsBinding.buffering.setVisibility(View.VISIBLE);
        itemReelsBinding.imgThumbnail.setVisibility(View.VISIBLE);

        MyExoPlayer.getInstance().playVideo(ReelsActivity.this, videoUrl);
        SimpleExoPlayer player = MyExoPlayer.getInstance().getPlayer(ReelsActivity.this);

        itemReelsBinding.playerView.setPlayer(player);
        itemReelsBinding.playerView.setUseController(false);
        itemReelsBinding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        player.setPlayWhenReady(true);
        player.addListener(this);
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, "exoplayer-codelab");
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_BUFFERING) {
            binding.pd.setVisibility(View.VISIBLE);
            playerBinding.imgThumbnail.setVisibility(View.VISIBLE);
        } else if (playbackState == Player.STATE_READY) {
            binding.pd.setVisibility(View.GONE);
            playerBinding.buffering.setVisibility(View.GONE);
            playerBinding.imgThumbnail.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        SimpleExoPlayer player = MyExoPlayer.getInstance().getPlayer(this);
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        SimpleExoPlayer player = MyExoPlayer.getInstance().getPlayer(this);
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyExoPlayer.getInstance().stopAndReleasePlayer();
        MyExoPlayer.getInstance().releasePreloader();
    }

    public void showHeart(MotionEvent e, ItemReelsBinding binding) {
        int x = (int) e.getX() - 200;
        int y = (int) e.getY() - 200;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        final ImageView iv = new ImageView(this);
        lp.setMargins(x, y, 0, 0);
        iv.setLayoutParams(lp);
        iv.setRotation(new Random().nextInt(61) - 30);
        iv.setImageResource(R.drawable.ic_heart_gradient);
        if (binding.rtl.getChildCount() > 0) {
            binding.rtl.removeAllViews();
        }
        binding.rtl.addView(iv);
        Animation fadeoutani = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        fadeoutani.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.rtl.removeView(iv);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        iv.startAnimation(fadeoutani);
    }
}
