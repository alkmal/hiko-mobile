package com.codder.ultimate.profile.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.MyLoader;
import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.post.activity.FeedListActivity;
import com.codder.ultimate.adapter.FeedGridAdapter;
import com.codder.ultimate.databinding.ActivityMyPostBinding;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.post.model.PostRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPostsActivity extends BaseActivity {

    private static final String TAG = "MyPostsActivity";

    private ActivityMyPostBinding binding;
    private FeedGridAdapter feedGridAdapter;
    private final MyLoader myLoader = new MyLoader();

    private String userId;
    private int start = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_post);
        binding.setLoader(myLoader);

        setupAdapter();
        initViews();
        handleIntent();
        setupSwipeListeners();
    }

    private void setupAdapter() {
        feedGridAdapter = new FeedGridAdapter(this);
        binding.rvFeed.setAdapter(feedGridAdapter);

        feedGridAdapter.setOnFeedGridAdapterClickListener(new FeedGridAdapter.OnFeedGridAdapterClickListener() {
            @Override
            public void onFeedClick(int position) {
                List<PostRoot.PostItem> list = feedGridAdapter.getCurrentList();
                if (list != null && position < list.size()) {
                    startActivity(new Intent(MyPostsActivity.this, FeedListActivity.class)
                            .putExtra(Const.USERID, SessionManager.getUserId(MyPostsActivity.this))
                            .putExtra(Const.POSITION, position)
                            .putExtra(Const.DATA, new Gson().toJson(new ArrayList<>(list))));
                }
            }

            @Override
            public void onDeleteClick(PostRoot.PostItem postItem, int position) {
                if (postItem == null || postItem.getId() == null) return;

                new PopupBuilder(MyPostsActivity.this).deletePopup(getString(R.string.delete_post_confirmation), new PopupBuilder.OnMultiButtonPopupLister() {
                    @Override
                    public void onClickContinue() {
                        deletePost(postItem, position);
                    }

                    @Override
                    public void onClickCancel() {

                    }
                });
            }
        });
    }

    private void initViews() {

    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Const.DATA)) {
            userId = intent.getStringExtra(Const.DATA);
            if (userId != null && !userId.isEmpty()) {
                getData(false);
            } else {
                myLoader.noData.set(true);
            }
        }
    }

    private void setupSwipeListeners() {
        binding.swipeRefresh.setOnRefreshListener(refreshLayout -> getData(false));
        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> getData(true));
    }

    private void deletePost(PostRoot.PostItem postItem, int position) {
        if (postItem == null || postItem.getId() == null) {
            Toast.makeText(MyPostsActivity.this, R.string.invalid_post, Toast.LENGTH_SHORT).show();
            return;
        }

        if (customDialogClass != null && !customDialogClass.isShowing()) {
            customDialogClass.show();
        }

        RetrofitBuilder.create().deletePost(postItem.getId()).enqueue(new Callback<RestResponse>() {
            @Override
            public void onResponse(@NonNull Call<RestResponse> call, @NonNull Response<RestResponse> response) {
                if (customDialogClass != null) customDialogClass.dismiss();

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(MyPostsActivity.this, R.string.deleted_successfully, Toast.LENGTH_SHORT).show();
                    removePostAt(position);
                } else {
                    Log.w(TAG, "Delete failed: " + (response.body() != null ? response.body().getMessage() : "No message"));
                    Toast.makeText(MyPostsActivity.this, R.string.failed_to_delete, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RestResponse> call, @NonNull Throwable t) {
                if (customDialogClass != null) customDialogClass.dismiss();
                Log.e(TAG, "Delete failed due to network or server error", t);
                Toast.makeText(MyPostsActivity.this, R.string.error_try_again, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void removePostAt(int position) {
        List<PostRoot.PostItem> currentList = new ArrayList<>(feedGridAdapter.getCurrentList());
        if (position >= 0 && position < currentList.size()) {
            currentList.remove(position);
            feedGridAdapter.submitList(currentList);
        } else {
            Log.w(TAG, "Invalid position for removal: " + position);
        }
    }

    private void getData(boolean isLoadMore) {
        String userId = SessionManager.getUserId(this);

        if (userId == null || userId.trim().isEmpty()) {
            Log.e(TAG, "User ID is null or empty. Cannot fetch data.");
            myLoader.noData.set(true);
            return;
        }

        if (isLoadMore) {
            start += Const.LIMIT;
        } else {
            start = 0;
            feedGridAdapter.submitList(new ArrayList<>());
            feedGridAdapter.notifyDataSetChanged();

            myLoader.isFirstTimeLoading.set(true);
        }

        myLoader.noData.set(false);

        RetrofitBuilder.create().getUserPostList(userId, start, Const.LIMIT).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PostRoot> call, @NonNull Response<PostRoot> response) {
                myLoader.isFirstTimeLoading.set(false);
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<PostRoot.PostItem> posts = response.body().getPost();

                    if (posts != null && !posts.isEmpty()) {
                        Set<String> seenIds = new HashSet<>();
                        List<PostRoot.PostItem> updatedList;

                        if (isLoadMore) {
                            updatedList = new ArrayList<>(feedGridAdapter.getCurrentList());
                            for (PostRoot.PostItem item : updatedList) {
                                if (item.getId() != null) {
                                    seenIds.add(item.getId());
                                }
                            }
                        } else {
                            updatedList = new ArrayList<>();
                        }

                        for (PostRoot.PostItem newItem : posts) {
                            if (newItem.getId() != null && !seenIds.contains(newItem.getId())) {
                                updatedList.add(newItem);
                            }
                        }

                        feedGridAdapter.submitList(updatedList);
                    } else if (!isLoadMore) {
                        myLoader.noData.set(true);
                        feedGridAdapter.submitList(new ArrayList<>());
                    }
                } else {
                    Log.w(TAG, "Post load failed: HTTP " + response.code() + ", Message: " + response.message());
                    if (!isLoadMore) {
                        myLoader.noData.set(true);
                        feedGridAdapter.submitList(new ArrayList<>());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PostRoot> call, @NonNull Throwable t) {
                myLoader.isFirstTimeLoading.set(false);
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();

                Log.e(TAG, "Data fetch failed: " + t.getLocalizedMessage(), t);
                if (!isLoadMore) {
                    myLoader.noData.set(true);
                    feedGridAdapter.submitList(new ArrayList<>());
                }

                Toast.makeText(MyPostsActivity.this, R.string.error_try_again, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
