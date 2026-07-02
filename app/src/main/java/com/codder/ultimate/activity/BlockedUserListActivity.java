package com.codder.ultimate.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.codder.ultimate.R;
import com.codder.ultimate.adapter.BlockedUserListAdapter;
import com.codder.ultimate.databinding.ActivityBlockedUserListBinding;
import com.codder.ultimate.retrofit.UserApiCall;
import com.codder.ultimate.viewModel.BlockedUserViewModel;
import com.codder.ultimate.viewModel.ViewModelFactory;

public class BlockedUserListActivity extends BaseActivity {

    public static final String TAG = "BlockedUserListActivity";
    ActivityBlockedUserListBinding binding;
    private BlockedUserListAdapter blockedUserListAdapter;
    private UserApiCall userApiCall;
    private BlockedUserViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_blocked_user_list);
        viewModel = new ViewModelProvider(this, new ViewModelFactory(new BlockedUserViewModel())).get(BlockedUserViewModel.class);

        binding.setViewModel(viewModel);

        userApiCall = new UserApiCall(this);

        initListener();
        setupRecyclerView();
        setupObservers();
        fetchBlockedUsers();

    }

    private void initListener() {
        binding.ivBack.setOnClickListener(v -> onBackPressed());

        binding.swipeRefresh.setOnRefreshListener(refreshLayout -> {
            fetchBlockedUsers();
            refreshLayout.finishRefresh(1500);
        });

    }

    private void setupRecyclerView() {
        binding.rvBannedList.setLayoutManager(new LinearLayoutManager(this));
        blockedUserListAdapter = new BlockedUserListAdapter(this, (id, position) -> onUnblockUser(id, position));
        binding.rvBannedList.setAdapter(blockedUserListAdapter);
    }

    private void setupObservers() {
        viewModel.getBlockedUsers().observe(this, blockedUsers -> {
            if (blockedUsers != null) {
                blockedUserListAdapter.submitList(blockedUsers);
                binding.shimmer.setVisibility(View.GONE);
                binding.layoutNoData.setVisibility(blockedUsers.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getError().observe(this, errorMessage -> {
            binding.shimmer.setVisibility(View.GONE);
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.isLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                binding.shimmer.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                binding.rvBannedList.setVisibility(isLoading ? View.GONE : View.VISIBLE);
                if (isLoading) {
                    binding.shimmer.startShimmer();
                } else {
                    binding.shimmer.stopShimmer();
                }
            }
        });
    }

    private void fetchBlockedUsers() {
        viewModel.fetchBlockedUsers(sessionManager.getUser().getId());
    }

    private void onUnblockUser(String id, int position) {
        userApiCall.blockUnblock(id, new UserApiCall.OnBlockUnblockListener() {
            @Override
            public void onBlockSuccess() {
                Log.d(TAG, "onBlockSuccess: ");
            }

            @Override
            public void onUnblockSuccess() {
                blockedUserListAdapter.removeItem(position);
                Toast.makeText(BlockedUserListActivity.this, getString(R.string.unblocked_successfully), Toast.LENGTH_SHORT).show();

                binding.rvBannedList.postDelayed(() -> {
                    if (blockedUserListAdapter.getItemCount() == 0) {
                        binding.layoutNoData.setVisibility(View.VISIBLE);
                        binding.rvBannedList.setVisibility(View.GONE);
                    }
                }, 100);
            }
        });
    }
}