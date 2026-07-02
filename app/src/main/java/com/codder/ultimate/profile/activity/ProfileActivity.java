package com.codder.ultimate.profile.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.activity.BlockedUserListActivity;
import com.codder.ultimate.activity.EditProfileActivity;
import com.codder.ultimate.activity.HostRequestActivity;
import com.codder.ultimate.adapter.ProfileAdapter;
import com.codder.ultimate.databinding.ActivityProfileBinding;
import com.codder.ultimate.guestuser.activity.FollowersListActivity;
import com.codder.ultimate.modelclass.BlockedUserListRoot;
import com.codder.ultimate.modelclass.ProfileRoot;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.profile.fragment.MyWalletActivity;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.retrofit.UserApiCall;
import com.codder.ultimate.viewModel.ViewModelFactory;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {

    public static final String TAG = "ProfileActivity";
    ActivityProfileBinding binding;
    private SessionManager sessionManager;
    private UserApiCall userApiCall;
    private UserRoot.User user;
    private ProfileViewModel viewModel;
    private boolean isFirstLoadDone = false;
    private List<ProfileRoot> hostVipList = new ArrayList<>();
    private List<ProfileRoot> myFeatureList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        getWindow().setStatusBarColor(Color.parseColor("#360D46"));
        viewModel = new ViewModelProvider(this, new ViewModelFactory(new ProfileViewModel())).get(ProfileViewModel.class);
        binding.setViewModel(viewModel);

        sessionManager = new SessionManager(this);
        userApiCall = new UserApiCall(this);

        initListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirstLoadDone) {
            viewModel.isLoading.set(true);
            loadUserData(true);
            isFirstLoadDone = true;
        } else {
            loadUserData(false);
        }
    }

    private void loadUserData(boolean showShimmer) {
        if (showShimmer) {
            viewModel.isLoading.set(true);
        }

        String userId = sessionManager.getUser() != null ? sessionManager.getUser().getId() : null;
        RetrofitBuilder.create().getBlockUser(userId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<BlockedUserListRoot> call, Response<BlockedUserListRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int totalBlocked = response.body().getTotal();
                    binding.tvBlock.setText(String.valueOf(totalBlocked));
                    Log.d(TAG, "Blocked users loaded: " + totalBlocked);
                } else {
                    Log.w(TAG, "Blocked users response failed: Code " + response.code() +
                            ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<BlockedUserListRoot> call, Throwable throwable) {
                Log.e(TAG, "Failed to load blocked users", throwable);
            }
        });

        userApiCall.getUser(new UserApiCall.OnUserApiListener() {
            @Override
            public void onUserGot(UserRoot.User userData) {
                user = userData;
                sessionManager.saveUser(user);
                updateUI();
                if (showShimmer) {
                    viewModel.isLoading.set(false);
                }
            }

            @Override
            public void onUserStatusFailed(String message) {
                Log.e(TAG, "Failed to get user data: " + message);
                if (showShimmer) {
                    viewModel.isLoading.set(false);
                }
            }
        });
    }

    private void updateUI() {
        if (user == null) return;

        setupRecyclerView();

        if (user.getAvatarFrameImage().isEmpty()) {
            binding.imgUser.setVisibility(View.GONE);
            binding.layImg.setVisibility(View.VISIBLE);
            binding.profileBorder.setVisibility(View.VISIBLE);
        } else {
            binding.imgUser.setVisibility(View.VISIBLE);
            binding.layImg.setVisibility(View.GONE);
            binding.profileBorder.setVisibility(View.GONE);
        }

        if (!isFinishing() && !isDestroyed()) {
            Glide.with(this).load(user.getImage()).placeholder(R.drawable.profile_placeholder).into(binding.imgUser2);
        }

        binding.imgUser.setUserImage(user.getImage(), user.getAvatarFrameImage(), 40);

        binding.tvName.setText(user.getName());
        binding.tvAge.setText(String.valueOf(user.getAge()));
        binding.tvCoin.setText(RayziUtils.formatCoin(user.getDiamond()));
        binding.tvFollowrs.setText(String.valueOf(user.getFollowers()));
        if (user.getLevel() != null){
            binding.tvLevel.setText(user.getLevel().getName());
        }
        binding.tvFollowing.setText(String.valueOf(user.getFollowing()));
        binding.tvUserId.setText(user.getUniqueId() != null ? getString(R.string.id) + user.getUniqueId() : "");

        binding.tvUserId.setVisibility(user.getUniqueId() != null ? View.VISIBLE : View.GONE);
        binding.copy.setVisibility(user.getUniqueId() != null ? View.VISIBLE : View.GONE);

        Drawable genderIcon = ContextCompat.getDrawable(this,
                user.getGender().equalsIgnoreCase(Const.MALE) ? R.drawable.ic_male : R.drawable.ic_female);

        binding.tvAge.setCompoundDrawablesRelativeWithIntrinsicBounds(genderIcon, null, null, null);


        setupRoleIndicators();
    }

    private void setupRecyclerView() {
        binding.rvHostVip.setLayoutManager(new GridLayoutManager(this, 4));
        binding.rvMyFeatures.setLayoutManager(new LinearLayoutManager(this));

        hostVipList = getHostVipFeatures();
        myFeatureList = getMyFeatureList();

        binding.rvHostVip.setAdapter(new ProfileAdapter(hostVipList,false, this::handleFeatureClick));
        binding.rvMyFeatures.setAdapter(new ProfileAdapter(myFeatureList, true,this::handleFeatureClick));
    }

    private List<ProfileRoot> getHostVipFeatures() {
        List<ProfileRoot> list = new ArrayList<>();
        if (user == null) return list;

        if (isDemoVersion) {
            list.add(new ProfileRoot(R.drawable.ic_agency_center, getString(R.string.demo_agency_center)));
            list.add(new ProfileRoot(R.drawable.ic_host_center, getString(R.string.demo_host_center_text)));
            list.add(new ProfileRoot(R.drawable.ic_host_request, getString(R.string.host_request)));
        } else {
            if (user.isAgency()) {
                list.add(new ProfileRoot(R.drawable.ic_agency_center, getString(R.string.agency_center_text)));
            }

            if (user.isHost()) {
                list.add(new ProfileRoot(R.drawable.ic_host_center, getString(R.string.host_center)));
            }

            if (!user.isAgency() && !user.isHost()) {
                list.add(new ProfileRoot(R.drawable.ic_host_request, getString(R.string.host_request)));
            }
        }

        list.add(new ProfileRoot(R.drawable.ic_become_vip, getString(R.string.become_vip)));
        return list;
    }


    private List<ProfileRoot> getMyFeatureList() {
        List<ProfileRoot> list = new ArrayList<>();
        if (user == null) return list;

        if (isDemoVersion) {
            list.add(new ProfileRoot(R.drawable.ic_coin_seller, getString(R.string.offline_recharge)));
        } else {
            if (user.isCoinSeller()) {
                list.add(new ProfileRoot(R.drawable.ic_coin_seller, getString(R.string.offline_recharge)));
            }
        }
        list.add(new ProfileRoot(R.drawable.ic_my_post1, getString(R.string.my_posts)));
        list.add(new ProfileRoot(R.drawable.ic_my_relites, getString(R.string.my_relites)));
        list.add(new ProfileRoot(R.drawable.ic_store, getString(R.string.store)));
        list.add(new ProfileRoot(R.drawable.ic_free_coin, getString(R.string.free_coins)));
        list.add(new ProfileRoot(R.drawable.ic_user_level, getString(R.string.user_level)));


        return list;
    }

    private void handleFeatureClick(String type) {
        if (type.equals(getString(R.string.demo_agency_center))) {
            WebActivity.open(this, getString(R.string.demo_agency_center), Const.DEMO_AGENCY_CENTER_URL, false);
        } else if (type.equals(getString(R.string.demo_host_center))) {
            WebActivity.open(this, getString(R.string.demo_host_center_text), Const.DEMO_HOST_CENTER_URL, false);
        } else if (type.equals(getString(R.string.agency_center))) {
            openWebWithLanguage(
                    ProfileActivity.this,
                    getString(R.string.agency_center_text),
                    user.getAgencyLoginString()
            );

        } else if (type.equals(getString(R.string.host_center))) {
            openWebWithLanguage(
                    ProfileActivity.this,
                    getString(R.string.host_center),
                    user.getHostLoginString()
            );

        } else if (type.equals(getString(R.string.offline_recharge))) {
            startActivity(new Intent(this, SellerOfflineRechargeActivity.class));
        } else if (type.equals(getString(R.string.my_posts))) {
            startActivity(new Intent(this, MyPostsActivity.class).putExtra(Const.DATA, new Gson().toJson(user)));
        } else if (type.equals(getString(R.string.my_relites))) {
            startActivity(new Intent(this, MyRelitesActivity.class).putExtra(Const.DATA, new Gson().toJson(user)));
        } else if (type.equals(getString(R.string.host_request))) {
            startActivity(new Intent(this, HostRequestActivity.class));
        } else if (type.equals(getString(R.string.store))) {
            startActivity(new Intent(this, StoreActivity.class));
        } else if (type.equals(getString(R.string.free_coins))) {
            startActivity(new Intent(this, FreeDiamondsActivity.class));
        } else if (type.equals(getString(R.string.become_vip))) {
            startActivity(new Intent(this, VipPlanActivity.class));
        } else if (type.equals(getString(R.string.user_level))) {
            startActivity(new Intent(this, MyLevelListActivity.class));
        }
    }

    private void setupRoleIndicators() {
        binding.tvType.setVisibility(sessionManager.getUser().isAgency() ? View.VISIBLE : View.GONE);
        binding.tvHostType.setVisibility(sessionManager.getUser().isHost() ? View.VISIBLE : View.GONE);
        binding.tvVIPType.setVisibility(sessionManager.getUser().isIsVIP() ? View.VISIBLE : View.GONE);
    }


    private void initListeners() {
        binding.ivBack.setOnClickListener(v -> onBackPressed());
        binding.btnSetting.setOnClickListener(v -> startActivity(new Intent(this, SettingActivity.class)));
        binding.btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        binding.tvLevel.setOnClickListener(v -> startActivity(new Intent(this, MyLevelListActivity.class)));
        binding.layWallet.setOnClickListener(v -> startActivity(new Intent(this, MyWalletActivity.class)));
        binding.lytBlock.setOnClickListener(v -> startActivity(new Intent(this, BlockedUserListActivity.class)));

        binding.lytFollowers.setOnClickListener(v -> {
            if (user != null) {
                startActivity(new Intent(this, FollowersListActivity.class)
                        .putExtra(Const.TYPE, 2)
                        .putExtra(Const.USERID, user.getId()));
            }
        });

        binding.lytFollowing.setOnClickListener(v -> {
            if (user != null) {
                startActivity(new Intent(this, FollowersListActivity.class)
                        .putExtra(Const.TYPE, 1)
                        .putExtra(Const.USERID, user.getId()));
            }
        });

        binding.copy.setOnClickListener(v -> {
            if (user != null && user.getUniqueId() != null) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", user.getUniqueId());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, R.string.copied_successfully, Toast.LENGTH_SHORT).show();
            }
        });
    }


    public static class ProfileViewModel extends ViewModel {
        public final ObservableBoolean isLoading = new ObservableBoolean(true);
    }

    private void openWebWithLanguage(Context ctx, String title, String baseUrl) {
        String code = sessionManager.getStringValue(Const.SELECTED_LANGUAGE);
        if (code == null || code.trim().isEmpty()) {
            code = "en"; // default
        }
        code = code.trim().toLowerCase(Locale.ROOT);

        Uri uri = Uri.parse(baseUrl)
                .buildUpon()
                .appendQueryParameter("language", code)
                .build();
        Log.d(TAG, "openWebWithLanguage: ==> " +code);
        WebActivity.open(ctx, title, uri.toString(), false);
    }
}