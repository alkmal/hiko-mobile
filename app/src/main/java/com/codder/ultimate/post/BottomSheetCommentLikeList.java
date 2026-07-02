package com.codder.ultimate.post;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.bottomsheets.BottomSheetReport;
import com.codder.ultimate.bottomsheets.BottomSheetReportOption;
import com.codder.ultimate.databinding.BottomSheetCommentLikeListBinding;
import com.codder.ultimate.databinding.ItemFeedBinding;
import com.codder.ultimate.dialog.CustomDialogClass;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.post.adapter.CommentAdapter;
import com.codder.ultimate.post.model.PostCommentRoot;
import com.codder.ultimate.post.model.PostRoot;
import com.codder.ultimate.post.viewmodel.CommentLikeListViewModel;
import com.codder.ultimate.reels.model.ReliteRoot;
import com.codder.ultimate.retrofit.CommentApiCalling;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.retrofit.UserApiCall;

import com.codder.ultimate.viewModel.ViewModelFactory;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetCommentLikeList extends BottomSheetDialogFragment {

    public static final int COMMENTS = 1;
    public static final int LIKES = 2;

    private BottomSheetCommentLikeListBinding binding;
    private CommentLikeListViewModel viewModel;
    private PostRoot.PostItem postItem;
    private ReliteRoot.VideoItem reliteItem;
    private int viewType;
    String isFeed;
    private int contentType;
    private boolean isBottomSheetOpen = false;

    private SessionManager sessionManager;
    private CustomDialogClass customDialogClass;
    private CommentApiCalling commentApiCalling;
    private UserApiCall userApiCall;

    public static BottomSheetCommentLikeList newInstance(
            @Nullable String postDataJson,
            @Nullable String reliteDataJson,
            int viewType,
            String Feed
    ) {
        BottomSheetCommentLikeList fragment = new BottomSheetCommentLikeList();
        Bundle args = new Bundle();
        if (!TextUtils.isEmpty(postDataJson)) args.putString(Const.POST_DATA, postDataJson);
        if (!TextUtils.isEmpty(reliteDataJson)) args.putString(Const.RELITE_DATA, reliteDataJson);
        args.putInt(Const.TYPE, viewType);
        args.putString("feed",Feed);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onStart() {
        super.onStart();

        View view = getView();
        if (view != null) {
            view.post(() -> {
                View parent = (View) view.getParent();
                BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(parent);

                if (viewType == COMMENTS && isFeed.equals("Feed")) {
                    // fixed 500dp height
                    int height = (int) (700 * getResources().getDisplayMetrics().density);
                    parent.getLayoutParams().height = height;
                    behavior.setPeekHeight(height);
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {

                    int screenHeight = getResources().getDisplayMetrics().heightPixels;
                    int halfHeight = screenHeight / 2;
                    parent.getLayoutParams().height = halfHeight;
                    behavior.setPeekHeight(halfHeight);
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                }

                parent.requestLayout();
            });
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackground(null); // Removes the default white background
            }

            if (dialog.getWindow() != null) {
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        });

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = BottomSheetCommentLikeListBinding.inflate(inflater, container, false);
        commentApiCalling = new CommentApiCalling(requireActivity());
        userApiCall = new UserApiCall(requireActivity());
        initArgs();
        initViewModel();
        setupUI();
        setupObservers();
        loadCommentsOrLikes();

        return binding.getRoot();
    }

    private void initArgs() {
        if (getArguments() != null) {
            viewType = getArguments().getInt(Const.TYPE, COMMENTS);

            String postData = getArguments().getString(Const.POST_DATA);
            if (postData != null) {
                postItem = new Gson().fromJson(postData, PostRoot.PostItem.class);
                contentType = CommentLikeListViewModel.POST;
            }

            String reliteData = getArguments().getString(Const.RELITE_DATA);
            if (reliteData != null) {
                reliteItem = new Gson().fromJson(reliteData, ReliteRoot.VideoItem.class);
                contentType = CommentLikeListViewModel.RELITE;
            }
           isFeed = getArguments().getString("feed");


            if (isFeed.equals("Feed") && viewType == COMMENTS){
                binding.layPost.setVisibility(VISIBLE);
            }else {
                binding.layPost.setVisibility(GONE);
            }

        }
    }

    private void initViewModel() {
        sessionManager = new SessionManager(requireContext());
        customDialogClass = new CustomDialogClass(requireContext(), R.style.customStyle);

        viewModel = new ViewModelProvider(this,
                new ViewModelFactory(new CommentLikeListViewModel(requireContext())).createFor())
                .get(CommentLikeListViewModel.class);

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
    }

    private void setupUI() {
        binding.tvDesc.setText(viewType == COMMENTS ? getString(R.string.no_comments_yet_be_the_first_to_say_something) : getString(R.string.no_likes_yet_tap_to_spread_some_love));
        binding.tvViewType.setText(viewType == COMMENTS ? R.string.comments : R.string.likes);
        binding.lytBottom.setVisibility(viewType == COMMENTS ? VISIBLE : GONE);
        binding.btnClose.setOnClickListener(v -> dismiss());


        postData();


        binding.btnSend.setOnClickListener(v -> {
            String comment = binding.etComment.getText().toString().trim();
            if (!comment.isEmpty()) {
                binding.etComment.setText("");
                submitComment(comment);
            }
        });

        viewModel.getCommentAdapter().setOnCommentClickListener((item, position) -> {
            if (item.getId().equals(sessionManager.getUser().getId())) {
                showDeleteDialog(item, position);
            }
        });
    }

    public void postData(){

        if (postItem == null){
            return;
        }

        binding.imgUser.setUserImage(postItem.getUserImage(), postItem.getAvatarFrameImage(), 16);
        Glide.with(this).load(BuildConfig.BASE_URL + postItem.getPost()).into(binding.imagePost);

        binding.tvLocation.setText(postItem.getTime());
        binding.tvUserName.setText(postItem.getName());

        binding.tvCaption1.setText(postItem.getCaption());
        binding.tvCaption1.setVisibility(postItem.getCaption().isEmpty() ? View.GONE : View.VISIBLE);

        binding.tvCaption1.setHashtagEnabled(true);
        binding.tvCaption1.setMentionEnabled(true);
        binding.tvCaption1.setHashtagColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
        binding.tvCaption1.setMentionColors(ContextCompat.getColorStateList(requireContext(), R.color.tintColor));
        binding.likeButton.setLiked(postItem.isUserLiked());
        binding.tvLikes.setText(postItem.getLikeCount() + " ");
        binding.tvComments.setText(postItem.getComment() + " ");

        binding.likeButton.setOnClickListener(null);
        binding.btnReport.setOnClickListener(v -> onClickReport(postItem));

        if (postItem.isFollow()) {
            binding.tvFollow.setText(R.string.following);
            binding.tvFollow.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.gradient_bg_radius_50));
            binding.tvFollow.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.icon_following, 0, 0, 0);
        } else {
            binding.tvFollow.setText(R.string.follow);
            binding.tvFollow.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.btn_bg));
            binding.tvFollow.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.icon_follow, 0, 0, 0);
        }

        binding.ivShare.setOnClickListener(view -> {
            String deepLink = BuildConfig.BASE_URL + "open?type=POST&userId=" + postItem.getUserId() + "&postId=" + postItem.getId();


            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, deepLink);
                getActivity().startActivity(Intent.createChooser(shareIntent, "Share"));
            } catch (Exception e) {

            }
        });

        binding.tvFollow.setOnClickListener(view -> {
            if (postItem.getUserId() == null) return;

            binding.pd.setVisibility(View.VISIBLE);
            binding.tvFollow.setVisibility(View.GONE);

            userApiCall.followUnfollowUser(!postItem.isFollow(), postItem.getUserId(), "", new UserApiCall.OnFollowUnfollowListener() {
                @Override
                public void onFollowSuccess() {
                    postItem.setFollow(true);
                    binding.pd.setVisibility(GONE);
                    binding.tvFollow.setText(R.string.following);
                    binding.tvFollow.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.gradient_bg_radius_50));
                    binding.tvFollow.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.icon_following, 0, 0, 0);
                    binding.tvFollow.setVisibility(View.VISIBLE);
                }

                @Override
                public void onUnfollowSuccess() {
                    postItem.setFollow(false);
                    binding.pd.setVisibility(GONE);
                    binding.tvFollow.setText(R.string.follow);
                    binding.tvFollow.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.btn_bg));
                    binding.tvFollow.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.icon_follow, 0, 0, 0);
                    binding.tvFollow.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFail() {
                    toggleFollowUI();
                }
            });
        });



    }

    private void toggleFollowUI() {
        binding.tvFollow.setVisibility(View.VISIBLE);
        binding.pd.setVisibility(View.GONE);
    }

    private void onClickReport(PostRoot.PostItem post) {
        if (post == null) return;
        new BottomSheetReportOption(requireContext(), new BottomSheetReportOption.OnReportedListener() {
            @Override
            public void onReported() {
                new BottomSheetReport(requireContext(), post.getUserId(), () ->
                        Toast.makeText(requireContext(), R.string.we_will_take_immediately_action_for_this_user_thank_you, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onBlocked() {
            }
        });
    }

    private void setupObservers() {
        viewModel.getListCountFinal().observe(getViewLifecycleOwner(), count -> {
            String formatted = String.format("(%d)", count);
            Log.d("CommentCount", "Formatted count: " + formatted);
            binding.tvCommentCount.setText(formatted);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                binding.shimmer.setVisibility(VISIBLE);
                binding.shimmer.startShimmer();
            } else {
                binding.shimmer.stopShimmer();
                binding.shimmer.setVisibility(GONE);
            }
        });

        viewModel.getNoData().observe(getViewLifecycleOwner(), isEmpty -> {
            if (isEmpty != null) {
                binding.layoutNoData.setVisibility(isEmpty ? VISIBLE : GONE);
            }
        });
    }

    private void loadCommentsOrLikes() {
        String id = postItem != null ? postItem.getId() : (reliteItem != null ? reliteItem.getId() : null);
        if (id != null) {
            viewModel.fetchCommentsOrLikes(id, contentType, viewType == COMMENTS, false);
        }
    }

    private void submitComment(String text) {
        if (text == null || text.trim().isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.comment_text_cannot_be_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("userId", sessionManager.getUser().getId());

        if (contentType == CommentLikeListViewModel.POST && postItem != null) {
            payload.addProperty("postId", postItem.getId());
        } else if (contentType == CommentLikeListViewModel.RELITE && reliteItem != null) {
            payload.addProperty("videoId", reliteItem.getId());
        }

        payload.addProperty("comment", text);
        binding.progressbar.setVisibility(VISIBLE);
        binding.btnSend.setVisibility(GONE);

        RetrofitBuilder.create().addComment(payload).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                binding.progressbar.setVisibility(GONE);
                binding.btnSend.setVisibility(VISIBLE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    PostCommentRoot.CommentsItem comment = new PostCommentRoot.CommentsItem();
                    comment.setUserId(sessionManager.getUser().getId());
                    comment.setComment(text);
                    comment.setImage(sessionManager.getUser().getImage());
                    comment.setName(sessionManager.getUser().getName());
                    comment.setUsername(sessionManager.getUser().getUsername());
                    comment.setTime("Just now");

                    viewModel.addCommentLocally(comment);
                    binding.rvComments.scrollToPosition(0);
                    viewModel.setNoData(false);
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_submit_comment_please_try_again), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {
                binding.progressbar.setVisibility(GONE);
                binding.btnSend.setVisibility(VISIBLE);
                Toast.makeText(requireContext(), getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteDialog(PostCommentRoot.CommentsItem commentDummy, int position) {

        new PopupBuilder(requireContext()).deletePopup(getString(R.string.delete_confirmation_text), new PopupBuilder.OnMultiButtonPopupLister() {
            @Override
            public void onClickContinue() {
                viewModel.deleteComment(commentDummy, position, customDialogClass);
            }

            @Override
            public void onClickCancel() {

            }
        });
    }
}
