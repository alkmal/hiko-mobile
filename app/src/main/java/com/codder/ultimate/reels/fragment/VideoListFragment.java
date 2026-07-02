package com.codder.ultimate.reels.fragment;


import static com.google.android.exoplayer2.Player.STATE_BUFFERING;
import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static com.google.android.exoplayer2.Player.STATE_READY;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.bottomsheets.BottomSheetReport;
import com.codder.ultimate.bottomsheets.BottomSheetReportOption;
import com.codder.ultimate.databinding.FragmentVideoListBinding;
import com.codder.ultimate.databinding.ItemFeedBinding;
import com.codder.ultimate.databinding.ItemReelsBinding;
import com.codder.ultimate.fragments.BaseFragment;
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
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.gson.Gson;

import java.util.List;

public class VideoListFragment extends BaseFragment implements Player.Listener {

    private static final String TAG = "VideoListFragment";
    private static final String ARG_TYPE = "arg_type";

    private FragmentVideoListBinding binding;
    private ItemReelsBinding playerBinding;
    private ReelsViewModel viewModel;
    private CommentApiCalling commenApiCalling;

    protected UserApiCall userApiCall;

    private Animation animation;
    private int lastPosition = -1;
    private String type = "default";

    public static VideoListFragment newInstance(String type) {
        VideoListFragment fragment = new VideoListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE, "default");
        }

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_list, container, false);
        initWindowFlags();
        initViewModel();
        initView();
        initListeners();
        loadInitialReels();
        return binding.getRoot();
    }

    private void initWindowFlags() {
        if (getActivity() != null) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void initViewModel() {
        viewModel = new ViewModelFactory(new ReelsViewModel()).create(ReelsViewModel.class);
        viewModel.init(getActivity());
        binding.setViewModel(viewModel);
        commenApiCalling = new CommentApiCalling(requireActivity());
        userApiCall = new UserApiCall(requireActivity());
    }

    private void loadInitialReels() {
        if (sessionManager.getUser() != null) {
            viewModel.getReliteData(false, sessionManager.getUser().getId(), true, false, 0);
        }
    }

    private void initView() {
        animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slow_rotate);
        binding.rvReels.setAdapter(viewModel.reelsAdapter);

        LinearLayoutManager lm = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        binding.rvReels.setLayoutManager(lm);
        binding.rvReels.setHasFixedSize(true);
        binding.rvReels.setItemViewCacheSize(4);          // keep a few bound views
        binding.rvReels.setItemAnimator(null);            // avoid flicker

        // Remove blur bg entirely
        binding.backImageBlur.setVisibility(View.GONE); // no blur BG
        new PagerSnapHelper().attachToRecyclerView(binding.rvReels); // 1 item per page snap

    }

    private void initListeners() {
        setupRefreshListeners();
        setupScrollListener();
        setupAdapterCallbacks();
    }

    private void setupRefreshListeners() {
        binding.swipeRefresh.setOnRefreshListener(refreshLayout -> {

            MyExoPlayer.getInstance().stopAndReleasePlayer();

            LinearLayoutManager layoutManager = (LinearLayoutManager) binding.rvReels.getLayoutManager();
            if (layoutManager != null) {
                lastPosition = layoutManager.findFirstVisibleItemPosition();
            }

            loadInitialReels();
        });

        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> {
            if (sessionManager.getUser() != null) {
                viewModel.getReliteData(true, sessionManager.getUser().getId(), true, false, 0);
            }
        });

        // When a load (initial or load-more) completes: stop spinners and restore scroll/play
        viewModel.isLoadCompleted.observe(getViewLifecycleOwner(), completed -> {
            binding.swipeRefresh.finishRefresh();
            binding.swipeRefresh.finishLoadMore();
            viewModel.isFirstTimeLoading.set(false);
            viewModel.isLoadMoreLoading.set(false);

//            binding.shimmer.setVisibility(View.GONE);

            LinearLayoutManager layoutManager = (LinearLayoutManager) binding.rvReels.getLayoutManager();
            if (layoutManager != null) {
                binding.rvReels.post(() -> {
                    layoutManager.scrollToPositionWithOffset(lastPosition, 0);

                    RecyclerView.ViewHolder holder = binding.rvReels.findViewHolderForAdapterPosition(lastPosition);
                    if (holder instanceof ReelsAdapter.ReelsViewHolder) {
                        ItemReelsBinding binding1 = ((ReelsAdapter.ReelsViewHolder) holder).getBinding();
                        String videoUrl = viewModel.reelsAdapter.getCurrentList().get(lastPosition).getVideo();
                        playVideo(videoUrl, binding1);
                    }
                });
            }
        });

    }

    private void setupScrollListener() {
        binding.rvReels.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return;

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int position = 0;
                if (layoutManager != null) {
                    position = layoutManager.findFirstCompletelyVisibleItemPosition();
                }
                if (position == -1) {
                    position = layoutManager.findFirstVisibleItemPosition();
                }

                if (position >= 0 && position != lastPosition &&
                        position < viewModel.reelsAdapter.getCurrentList().size()) {

                    View itemView = null;
                    if (layoutManager != null) {
                        itemView = layoutManager.findViewByPosition(position);
                    }
                    if (itemView != null) {
                        ItemReelsBinding binding1 = DataBindingUtil.bind(itemView);
                        if (binding1 != null) {
                            lastPosition = position;

                            binding1.lytSound.startAnimation(animation);
                            String videoUrl = viewModel.reelsAdapter.getCurrentList().get(position).getVideo();

                            try {
                                playVideo(videoUrl, binding1);
                            } catch (Exception e) {
                                Log.e(TAG, "playVideo error ==== ", e);
                            }

                            preloadSurroundingVideos(position);
                        }
                    }
                }
            }
        });
    }

    private void preloadSurroundingVideos(int position) {
        try {
            int range = 2;
            List<ReliteRoot.VideoItem> list = viewModel.reelsAdapter.getCurrentList();
            for (int offset = 1; offset <= range; offset++) {
                int idx = position + offset;
                if (idx >= 0 && idx < list.size()) {
                    String preloadUrl = list.get(idx).getVideo();
                    MyExoPlayer.getInstance().preloadToCache(preloadUrl);

                    // Preload thumbnail too
                    String thumb = list.get(idx).getScreenshot();
                    Glide.with(this)
                            .load(thumb)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .preload();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Preloading error ==== ", e);
        }
    }


    private void setupAdapterCallbacks() {
        viewModel.reelsAdapter.setListener(new ReelsAdapter.OnReelsVideoAdapterListener() {
            @Override
            public void onItemClick(ItemReelsBinding reelsBinding, int pos, int type) {
                Log.d(TAG, "onItemClick fired. Pos: " + pos + " Type: " + type);
                if (pos >= viewModel.reelsAdapter.getCurrentList().size()) return;

                lastPosition = pos;

                if (type == 1) {
                    playVideo(viewModel.reelsAdapter.getCurrentList().get(pos).getVideo(), reelsBinding);
                } else if (type == 2) {
                    SimpleExoPlayer player = MyExoPlayer.getInstance().getPlayer(requireContext());

                    if (reelsBinding.playerView.getPlayer() == null) {
                        reelsBinding.playerView.setPlayer(player);
                    }

                    boolean currentlyPlaying = player.getPlayWhenReady();
                    player.setPlayWhenReady(!currentlyPlaying);
                    showMediaButton();
                }
            }

            @Override
            public void onClickCommentList(ReliteRoot.VideoItem relite, int pos) {
                lastPosition = pos;
                BottomSheetCommentLikeList.newInstance(
                        null,
                        new Gson().toJson(relite),
                        BottomSheetCommentLikeList.COMMENTS,
                        "Reels"
                ).show(requireActivity().getSupportFragmentManager(), "ReliteCommentBottomSheet");
            }

            @Override
            public void onClickLikeList(ReliteRoot.VideoItem relite) {
                BottomSheetCommentLikeList.newInstance(
                        null,
                        new Gson().toJson(relite),
                        BottomSheetCommentLikeList.LIKES,
                        "Reels"
                ).show(requireActivity().getSupportFragmentManager(), "ReliteLikeBottomSheet");
            }

            @Override
            public void onDoubleClick(ReliteRoot.VideoItem model, MotionEvent event, ItemReelsBinding binding) {
                showHeart(event, binding);
                if (!model.isLike()) binding.likebtn.performClick();
            }

            @Override
            public void onClickLike(ItemReelsBinding reelsBinding, int pos, ReliteRoot.VideoItem videoItem) {

                boolean isLiked1 = videoItem.isLike();
                int like1;
                if (!isLiked1) {
                    like1 = videoItem.getLike() + 1;
                } else {
                    like1 = videoItem.getLike() - 1;
                }
                videoItem.setLikeCount(like1);
                videoItem.setLike(!isLiked1);
                reelsBinding.tvLikeCount.setText(String.valueOf(like1));
//                viewModel.reelsAdapter.notifyItemChanged(pos, videoItem);

                commenApiCalling.toggleLikeRelite(viewModel.reelsAdapter.getCurrentList().get(pos).getId(), isLiked -> {
//                    viewModel.reelsAdapter.notifyItemChanged(pos, videoItem);
                });
            }

            @Override
            public void onClickShare(ReliteRoot.VideoItem reel) {
                shareRelite(reel);
            }

            @Override
            public void onClickUser(ReliteRoot.VideoItem reel) {
                startActivity(new Intent(getActivity(), GuestActivity.class).putExtra(Const.USERID, reel.getUserId()));
            }

            @Override
            public void onMentionClick(String userName) {
                startActivity(new Intent(getActivity(), GuestActivity.class).putExtra(Const.USERNAME, userName));
            }

            @Override
            public void onHashTagClick(String hashTag) {
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
                        Toast.makeText(requireActivity(), "Followed Successfully!!", Toast.LENGTH_SHORT).show();
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

    private void playVideo(String videoUrl, ItemReelsBinding itemReelsBinding) {

        Log.d(TAG, "Playing video: ==== " + videoUrl);

        if (playerBinding != null && playerBinding != itemReelsBinding) {
            playerBinding.playerView.setPlayer(null);
        }

        playerBinding = itemReelsBinding;
        itemReelsBinding.buffering.setVisibility(View.VISIBLE);

        itemReelsBinding.buffering.setVisibility(View.VISIBLE);
        itemReelsBinding.imgThumbnail.setVisibility(View.VISIBLE);

        MyExoPlayer.getInstance().playVideo(requireContext(), videoUrl);
        SimpleExoPlayer player = MyExoPlayer.getInstance().getPlayer(requireContext());

        itemReelsBinding.playerView.setPlayer(player);
        itemReelsBinding.playerView.setUseController(false);
        itemReelsBinding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        player.setPlayWhenReady(true);
        player.addListener(this);
    }

    private void showMediaButton() {
        SimpleExoPlayer player = MyExoPlayer.getInstance().getPlayer(requireContext());
        if (player == null) return;

        int icon = player.getPlayWhenReady() ? R.drawable.icon_pause : R.drawable.icon_play;
        binding.imgMedia.setImageDrawable(ContextCompat.getDrawable(requireContext(), icon));
        binding.imgMedia.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() ->
                binding.imgMedia.setVisibility(View.GONE), 1000);
    }

    private void openReportSheet(String userId) {
        new BottomSheetReportOption(requireContext(), new BottomSheetReportOption.OnReportedListener() {
            @Override
            public void onReported() {
                new BottomSheetReport(requireContext(), userId, () ->
                        Toast.makeText(requireActivity(), getString(R.string.we_will_take_immediate_action_thank_you), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onBlocked() {
                if (sessionManager.getUser() != null) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        viewModel.getReliteData(false, sessionManager.getUser().getId(), true, false, 0);
                    }, 300);

                }
            }
        });
    }

    private void shareRelite(ReliteRoot.VideoItem reel) {

        String deepLink = BuildConfig.BASE_URL + "open?type=RELITE&userId=" + reel.getUserId() + "&postId=" + reel.getId();
        Log.d(TAG, "onShareClick: ==" + deepLink);

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, deepLink);
            getActivity().startActivity(Intent.createChooser(shareIntent, "Share"));
        } catch (Exception e) {
            Log.e(TAG, "Share error: " + e.getMessage());
        }


    }

    /** Double-tap heart pop animation */
    public void showHeart(MotionEvent e, ItemReelsBinding binding) {

        binding.heartAnimation.setVisibility(View.VISIBLE);
        binding.heartAnimation.setAlpha(1f);
        binding.heartAnimation.setScaleX(0f);
        binding.heartAnimation.setScaleY(0f);

        binding.heartAnimation.animate()
                .alpha(1f)
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(300)
                .withEndAction(() -> binding.heartAnimation.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> binding.heartAnimation.setVisibility(View.GONE))
                        .start())
                .start();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint() || isVisible()) {
            resumeVideo();
        }
    }

    /** Legacy visibility hook for ViewPager setups */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            resumeVideo();
        }
    }

    /** Start playing the first item after view becomes active */
    public void resumeVideo() {
        if (!viewModel.reelsAdapter.getCurrentList().isEmpty()) {
            int pos = 0;
            viewModel.reelsAdapter.playVideoAt(pos);

            binding.rvReels.postDelayed(() -> {
                RecyclerView.ViewHolder holder = binding.rvReels.findViewHolderForAdapterPosition(pos);
                if (holder instanceof ReelsAdapter.ReelsViewHolder) {
                    ItemReelsBinding binding1 = ((ReelsAdapter.ReelsViewHolder) holder).getBinding();
                    playVideo(viewModel.reelsAdapter.getCurrentList().get(pos).getVideo(), binding1);
                }
            }, 200);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        SimpleExoPlayer player = MyExoPlayer.getInstance().getPlayer(requireContext());
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    public void onDestroy() {
        MyExoPlayer.getInstance().stopAndReleasePlayer();
        MyExoPlayer.getInstance().releasePreloader();
        super.onDestroy();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playerBinding == null) return;

        switch (playbackState) {
            case STATE_BUFFERING -> {
                // show slim progress bar; keep thumbnail visible to avoid flashes
                binding.buffering.setVisibility(View.VISIBLE);
                // No more fragment-level blur
                playerBinding.buffering.setVisibility(View.VISIBLE);
                playerBinding.imgThumbnail.setVisibility(View.VISIBLE);
            }
            case STATE_READY -> {
                binding.buffering.setVisibility(View.GONE);
                playerBinding.buffering.setVisibility(View.GONE);
                playerBinding.imgThumbnail.setVisibility(View.GONE); // video is visible now
            }
            case STATE_ENDED -> {
                Toast.makeText(getActivity(), getString(R.string.live_ended), Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> {
                    if (getActivity() != null) getActivity().finish();
                }, 2000);
            }
        }
    }

    /** Pause when hidden in a tab/pager; resume when shown again */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            onResume();
        } else {
            MyExoPlayer.getInstance().getPlayer(requireContext()).setPlayWhenReady(false);
        }
    }

}
