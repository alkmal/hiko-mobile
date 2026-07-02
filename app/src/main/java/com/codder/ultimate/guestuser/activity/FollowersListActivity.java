package com.codder.ultimate.guestuser.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.MyLoader;
import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityFollowersListBinding;
import com.codder.ultimate.guestuser.adapter.FollowersUsersAdapter;
import com.codder.ultimate.modelclass.GuestUsersListRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowersListActivity extends BaseActivity {

    public static final String TAG = "FollowersListActivity";
    private ActivityFollowersListBinding binding;
    private final FollowersUsersAdapter adapter = new FollowersUsersAdapter();
    private int listType;
    private String userId;
    private int start = 0;
    private final MyLoader loader = new MyLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_followers_list);
        binding.setMyLoder(loader);

        retrieveIntentData();
        setTitleBasedOnType();
        setupRecyclerView();
        setupSwipeListeners();

        if (userId != null && !userId.isEmpty()) {
            fetchUserList(false);
        }
    }

    private void retrieveIntentData() {
        Intent intent = getIntent();
        userId = intent.getStringExtra(Const.USERID);
        listType = intent.getIntExtra(Const.TYPE, 0);
    }

    private void setupRecyclerView() {
        binding.rvFeed.setAdapter(adapter);
    }

    private void setupSwipeListeners() {
        binding.swipeRefresh.setOnRefreshListener(refreshLayout -> fetchUserList(false));
        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> fetchUserList(true));
    }

    private void fetchUserList(boolean isLoadMore) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "fetchUserList: Invalid userId");
            return;
        }

        loader.noData.set(false);

        if (isLoadMore) {
            start += Const.LIMIT;
        } else {
            start = 0;
            loader.isFirstTimeLoading.set(true);
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("userId", userId);
        payload.addProperty("start", start);
        payload.addProperty("limit", Const.LIMIT);

        Call<GuestUsersListRoot> apiCall = (listType == 1)
                ? RetrofitBuilder.create().getFollowingList(payload)
                : RetrofitBuilder.create().getFollowersList(payload);

        apiCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GuestUsersListRoot> call, Response<GuestUsersListRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GuestUsersListRoot result = response.body();

                    if (result.isStatus() && result.getUser() != null && !result.getUser().isEmpty()) {
                        adapter.submitList(result.getUser());
                        Log.d(TAG, "Fetched " + result.getUser().size() + " users.");
                    } else if (start == 0) {
                        loader.noData.set(true);
                        adapter.submitList(new ArrayList<>());
                        Log.d(TAG, "No users found for the given criteria.");
                    }
                } else {
                    Log.e(TAG, "fetchUserList: Error response - Code: " + response.code() + ", Message: " + response.message());
                    if (start == 0) {
                        loader.noData.set(true);
                        adapter.submitList(new ArrayList<>());
                    }
                }
                finalizeLoading();
            }

            @Override
            public void onFailure(Call<GuestUsersListRoot> call, Throwable t) {
                Log.e(TAG, "fetchUserList: Network failure - " + t.getLocalizedMessage(), t);
                finalizeLoading();
            }
        });
    }

    private void setTitleBasedOnType() {
        if (listType == 2) {
            binding.tvTitle.setText(getString(R.string.my_followers));
        } else if (listType == 1) {
            binding.tvTitle.setText(getString(R.string.my_following));
        }
    }


    private void finalizeLoading() {
        loader.isFirstTimeLoading.set(false);
        loader.noData.set(loader.noData.get() || adapter.getItemCount() == 0);
        binding.swipeRefresh.finishRefresh();
        binding.swipeRefresh.finishLoadMore();
    }
}
