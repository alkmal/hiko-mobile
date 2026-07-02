package com.codder.ultimate.post.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityFeedListBinding;
import com.codder.ultimate.databinding.ItemFeedBinding;
import com.codder.ultimate.guestuser.activity.GuestActivity;
import com.codder.ultimate.post.BottomSheetCommentLikeList;
import com.codder.ultimate.post.adapter.FeedAdapter;
import com.codder.ultimate.post.model.PostRoot;
import com.codder.ultimate.retrofit.CommentApiCalling;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.viewModel.FeedListViewModel2;
import com.codder.ultimate.viewModel.ViewModelFactory;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class FeedListActivity extends BaseActivity implements FeedAdapter.OnPostClickListener {

    public static final String TAG = "FeedListActivity";
    private ActivityFeedListBinding binding;
    private FeedListViewModel2 viewModel;
    private CommentApiCalling commentApiCalling;
    private boolean isSharing = false;
    private String userId = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_feed_list);
        getWindow().setStatusBarColor(Color.parseColor("#360D46"));
        viewModel = new ViewModelProvider(this, new ViewModelFactory(new FeedListViewModel2())).get(FeedListViewModel2.class);
        viewModel.init(this, Const.USER);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        commentApiCalling = new CommentApiCalling(this);

        initializeIntentData();
        setupRecyclerView();
        setupSwipeToRefresh();
        observeLiveData();
    }

    private void initializeIntentData() {
        Intent intent = getIntent();
        if (intent == null) return;

        int position = intent.getIntExtra(Const.POSITION, 0);
        String data = intent.getStringExtra(Const.DATA);
        userId = intent.getStringExtra(Const.USERID);

        if (data != null && !data.isEmpty()) {
            List<PostRoot.PostItem> postItems = new Gson().fromJson(data, new TypeToken<ArrayList<PostRoot.PostItem>>() {
            }.getType());
            if (postItems != null && !postItems.isEmpty()) {
                viewModel.start = Math.max(0, postItems.size() - Const.LIMIT);
                viewModel.feedAdapter.submitList(new ArrayList<>(postItems));
                binding.rvFeed.scrollToPosition(position);

//                if (userId == null || userId.isEmpty()) {
//                    userId = postItems.get(0).getUserId();
//                }
//
//                String name = postItems.get(0).getName();
//                if (name != null && !name.isEmpty()) {
//                    binding.tvTitle.setText(name);
//                } else {
//                    binding.tvTitle.setText("");
//                }
            }
            return;

        }

        String postId = intent.getStringExtra("postId");
        String userId = intent.getStringExtra("userId");

        if (postId != null && !postId.isEmpty() && userId != null && !userId.isEmpty()) {
            loadFeedAndSelectPost(postId, userId);
        }

    }


    private void loadFeedAndSelectPost(String postId, String userId) {
        // First load feed for that user
        viewModel.getPostData(false, userId);

        // Observe until feed loads
        viewModel.isLoadCompleted.observe(this, isDone -> {
            if (Boolean.TRUE.equals(isDone)) {
                List<PostRoot.PostItem> list = viewModel.feedAdapter.getCurrentList();
                if (list == null || list.isEmpty()) return;

                int idx = -1;
                for (int i = 0; i < list.size(); i++) {
                    if (postId.equals(list.get(i).getId())) {
                        idx = i;
                        break;
                    }
                }

                if (idx >= 0) {
                    binding.rvFeed.scrollToPosition(idx);
                    binding.tvTitle.setText(list.get(idx).getName());
                } else {
                    // If not found in first page → load more
                    loadMoreUntilFound(postId, userId);
                }
            }
        });
    }

    private void loadMoreUntilFound(String postId, String userId) {
        binding.rvFeed.post(() -> {
            viewModel.getPostData(true, userId);

            viewModel.isLoadCompleted.observe(this, done -> {
                if (!Boolean.TRUE.equals(done)) return;

                List<PostRoot.PostItem> list = viewModel.feedAdapter.getCurrentList();

                for (int i = 0; i < list.size(); i++) {
                    if (postId.equals(list.get(i).getId())) {
                        binding.rvFeed.scrollToPosition(i);
                        binding.tvTitle.setText(list.get(i).getName());
                        return;
                    }
                }

                // If still not found - stop search if no more data
                if (viewModel.noData.get() || viewModel.isLoadMoreLoading.get()) {
                    Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show();
                } else {
                    loadMoreUntilFound(postId, userId);
                }
            });
        });
    }


    private void setupRecyclerView() {
        viewModel.feedAdapter.setOnPostClickListener(this);
        binding.rvFeed.setAdapter(viewModel.feedAdapter);
    }

    private void setupSwipeToRefresh() {
        binding.swipeRefresh.setOnRefreshListener(refreshLayout -> viewModel.getPostData(false, userId));
        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> viewModel.getPostData(true, userId));
    }

    private void observeLiveData() {
        viewModel.isLoadCompleted.observe(this, isCompleted -> {
            if (isCompleted != null && isCompleted) {
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();
                viewModel.isFirstTimeLoading.set(false);
                viewModel.isLoadMoreLoading.set(false);
            }
        });
    }

    @Override
    public void onLikeClick(PostRoot.PostItem postDummy, int position, ItemFeedBinding binding) {
        if (postDummy.isLikeInProgress()) return;

        boolean wasLiked = postDummy.isUserLiked();
        int currentLikes = postDummy.getLikeCount();

        PostRoot.PostItem updatedPost = new PostRoot.PostItem(postDummy); // clone!
        updatedPost.setLikeInProgress(true);
        updatedPost.setUserLiked(!wasLiked);
        updatedPost.setLikeCount(wasLiked ? currentLikes - 1 : currentLikes + 1);

        viewModel.feedAdapter.updateItem(position, updatedPost);

        commentApiCalling.toggleLikePost(postDummy.getId(), isSuccess -> {
            updatedPost.setLikeInProgress(false);
            binding.likeButton.setEnabled(true);

            if (!isSuccess) {
                // Revert to original state
                updatedPost.setUserLiked(wasLiked);
                updatedPost.setLikeCount(currentLikes);
            }

            viewModel.feedAdapter.updateItem(position, new PostRoot.PostItem(updatedPost));
        });
    }


    @Override
    public void onCommentListClick(PostRoot.PostItem postItem) {
        if (postItem == null) return;

        BottomSheetCommentLikeList bottomSheet = BottomSheetCommentLikeList.newInstance(
                new Gson().toJson(postItem),
                null,
                BottomSheetCommentLikeList.COMMENTS,
                "Feed"
        );
        bottomSheet.show(getSupportFragmentManager(), "CommentLikeListBottomSheet");

        
    }

    @Override
    public void onLikeListClick(PostRoot.PostItem postItem) {
        if (postItem == null) return;

        BottomSheetCommentLikeList bottomSheet = BottomSheetCommentLikeList.newInstance(
                new Gson().toJson(postItem),
                null,
                BottomSheetCommentLikeList.LIKES,
                "Feed"
        );

        bottomSheet.show(getSupportFragmentManager(), "CommentLikeListBottomSheet");
    }

    @Override
    public void onShareClick(PostRoot.PostItem postItem) {
//        if (postItem == null || isSharing) return;
//
//        isSharing = true;
//
//        BranchUniversalObject buo = new BranchUniversalObject()
//                .setCanonicalIdentifier("content/12345")
//                .setTitle(postItem.getCaption())
//                .setContentDescription("By : " + postItem.getName())
//                .setContentImageUrl(BuildConfig.BASE_URL + postItem.getPost())
//                .setContentMetadata(new ContentMetadata()
//                        .addCustomMetadata("type", "POST")
//                        .addCustomMetadata(Const.DATA, new Gson().toJson(postItem)));
//
//        LinkProperties lp = new LinkProperties()
//                .setChannel("facebook")
//                .setFeature("sharing")
//                .setCampaign("content 123 launch")
//                .setStage("new user")
//                .addControlParameter("timestamp", Long.toString(Calendar.getInstance().getTimeInMillis()));
//
//        buo.generateShortUrl(this, lp, (url, error) -> {
//            isSharing = false;
//
//            if (url != null && error == null) {
//                try {
//                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                    shareIntent.setType("text/plain");
//                    shareIntent.putExtra(Intent.EXTRA_TEXT, url);
//                    startActivity(Intent.createChooser(shareIntent, "Choose one"));
//                } catch (Exception e) {
//                    Log.e(TAG, "Share failed: " + e.getMessage());
//                    Toast.makeText(this, getString(R.string.unable_to_share_at_this_moment), Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Log.e(TAG, "Branch link error: " + (error != null ? error.getMessage() : "Unknown"));
//            }
//        });


        String deepLink = BuildConfig.BASE_URL + "open?type=POST&userId=" + postItem.getUserId() + "&postId=" + postItem.getId();

        Log.d(TAG, "onShareClick: ==" + deepLink);

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, deepLink);
           startActivity(Intent.createChooser(shareIntent, "Share"));
        } catch (Exception e) {
            Log.e(TAG, "Share error: " + e.getMessage());
        }


    }


    @Override
    public void onMentionClick(String userName) {
        if (userName != null && !userName.isEmpty()) {
            startActivity(new Intent(this, GuestActivity.class).putExtra(Const.USERNAME, userName));
        }
    }
}