package com.codder.ultimate.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.codder.ultimate.activity.BaseActivity.isDemoVersion;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.caverock.androidsvg.SVG;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.activity.BlockedUserListActivity;
import com.codder.ultimate.activity.EditProfileActivity;
import com.codder.ultimate.activity.HostRequestActivity;
import com.codder.ultimate.adapter.ProfileAdapter;
import com.codder.ultimate.databinding.FragmentProfileBinding;
import com.codder.ultimate.guestuser.activity.FollowersListActivity;
import com.codder.ultimate.modelclass.BlockedUserListRoot;
import com.codder.ultimate.modelclass.ProfileRoot;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.profile.activity.FreeDiamondsActivity;
import com.codder.ultimate.profile.activity.MyLevelListActivity;
import com.codder.ultimate.profile.activity.MyPostsActivity;
import com.codder.ultimate.profile.activity.MyRelitesActivity;
import com.codder.ultimate.profile.activity.SellerOfflineRechargeActivity;
import com.codder.ultimate.profile.activity.SettingActivity;
import com.codder.ultimate.profile.activity.StoreActivity;
import com.codder.ultimate.profile.activity.VipPlanActivity;
import com.codder.ultimate.profile.activity.WebActivity;
import com.codder.ultimate.profile.fragment.MyWalletActivity;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.retrofit.UserApiCall;
import com.codder.ultimate.viewModel.ViewModelFactory;
import com.google.gson.Gson;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends BaseFragment {

    public static final String TAG = "ProfileActivity";
    FragmentProfileBinding binding;
    private SessionManager sessionManager;
    private UserApiCall userApiCall;
    private UserRoot.User user;
    private ProfileViewModel viewModel;
    private boolean isFirstLoadDone = false;
    private List<ProfileRoot> hostVipList = new ArrayList<>();
    private List<ProfileRoot> myFeatureList = new ArrayList<>();

    @Override
    public @Nullable View onCreateView(@NonNull LayoutInflater inflater,
                                       @Nullable ViewGroup container,
                                       @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this, new ViewModelFactory(new ProfileViewModel())).get(ProfileViewModel.class);
        binding.setViewModel(viewModel);

        sessionManager = new SessionManager(requireActivity());
        userApiCall = new UserApiCall(requireActivity());

        initListeners();
    }

    @Override
    public void onResume() {
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
        binding.imgUser.setUserImage(user.getImage(), user.getAvatarFrameImage(), 40);

        binding.tvName.setText(user.getName());
        binding.tvAge.setText(String.valueOf(user.getAge()));
        binding.tvCountry.setText(String.valueOf(user.getCountry()));
        binding.tvCoin.setText(String.valueOf(RayziUtils.formatCoin(user.getDiamond())));
        binding.tvFollowers.setText(String.valueOf(user.getFollowers()));
        if (user.getLevel() != null) {
            binding.tvLevel.setText(user.getLevel().getName());
        }

        if (user.getLevel().getImage() != null) {
            Glide.with(requireActivity()).load(BuildConfig.BASE_URL + user.getLevel().getImage()).into(binding.ivLevel);
        }

        String flagUrl = user.getCountryFlagImage();
        if (flagUrl != null && !flagUrl.isEmpty()) {
            AsyncTask.execute(() -> {
                try {
                    URL url = new URL(flagUrl);
                    SVG svg = SVG.getFromInputStream(url.openStream());

                    Picture picture;
                    float width = svg.getDocumentWidth();
                    float height = svg.getDocumentHeight();

                    if (width > 0 && height > 0) {
                        picture = svg.renderToPicture();
                    } else {
                        picture = svg.renderToPicture(100, 60); // fallback dimensions
                    }

                    PictureDrawable drawable = new PictureDrawable(picture);

                    requireActivity().runOnUiThread(() -> {
                        binding.svgWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        binding.svgWebView.setImageDrawable(drawable);
                        binding.svgWebView.invalidate();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        binding.tvFollowing.setText(String.valueOf(user.getFollowing()));
        binding.tvUserId.setText(user.getUniqueId() != null ? getString(R.string.id) + user.getUniqueId() : "");

        binding.tvUserId.setVisibility(user.getUniqueId() != null ? VISIBLE : GONE);
        binding.copy.setVisibility(user.getUniqueId() != null ? VISIBLE : GONE);

        if (user.getGender().equals("Male")) {
            Glide.with(requireActivity()).load(R.drawable.ic_male).into(binding.ivGender);
        } else {
            Glide.with(requireActivity()).load(R.drawable.ic_female).into(binding.ivGender);
        }

        setupRoleIndicators();
    }

    private void setupRecyclerView() {
        binding.rvHostVip.setLayoutManager(new GridLayoutManager(requireActivity(), 4));
        binding.rvMyFeatures.setLayoutManager(new LinearLayoutManager(requireActivity()));

        hostVipList = getHostVipFeatures();
        myFeatureList = getMyFeatureList();

        binding.rvHostVip.setAdapter(new ProfileAdapter(hostVipList, false, this::handleFeatureClick));
        binding.rvMyFeatures.setAdapter(new ProfileAdapter(myFeatureList, true, this::handleFeatureClick));
    }

    private List<ProfileRoot> getHostVipFeatures() {
        List<ProfileRoot> list = new ArrayList<>();
        if (user == null) return list;

        if (isDemoVersion) {
            list.add(new ProfileRoot(R.drawable.ic_new_agency, getString(R.string.agency_center_text)));
            list.add(new ProfileRoot(R.drawable.ic_new_bd, getString(R.string.bd_center)));
            list.add(new ProfileRoot(R.drawable.ic_new_hostcenter, getString(R.string.host_center)));
        } else {
            if (user.isAgency()) {
                list.add(new ProfileRoot(R.drawable.ic_new_agency, getString(R.string.agency_center_text)));
            }


            if (user.isBd()) {
                list.add(new ProfileRoot(R.drawable.ic_new_bd, getString(R.string.bd_center)));
            }


            if (user.isHost()) {
                list.add(new ProfileRoot(R.drawable.ic_new_hostcenter, getString(R.string.host_center)));
            }


            if (!user.isAgency() && !user.isHost()) {
                binding.layBecomeHost.setVisibility(VISIBLE);
            } else {
                binding.layBecomeHost.setVisibility(GONE);
            }

        }


        list.add(new ProfileRoot(R.drawable.ic_new_becomevip, getString(R.string.become_vip)));
        return list;
    }


    private List<ProfileRoot> getMyFeatureList() {
        List<ProfileRoot> list = new ArrayList<>();
        if (user == null) return list;

        if (isDemoVersion) {
            list.add(new ProfileRoot(R.drawable.ic_new_coinseller, getString(R.string.offline_recharge)));
        } else {
            if (user.isCoinSeller()) {
                list.add(new ProfileRoot(R.drawable.ic_new_coinseller, getString(R.string.offline_recharge)));
            }
        }
        list.add(new ProfileRoot(R.drawable.ic_new_post, getString(R.string.my_posts)));
        list.add(new ProfileRoot(R.drawable.ic_new_reel, getString(R.string.my_relites)));
        list.add(new ProfileRoot(R.drawable.ic_new_store, getString(R.string.store)));
        list.add(new ProfileRoot(R.drawable.ic_new_freecoin, getString(R.string.free_coins)));
        list.add(new ProfileRoot(R.drawable.ic_new_userlevel, getString(R.string.user_level)));


        return list;
    }

    private void handleFeatureClick(String type) {
        if (isDemoVersion) {
            if (type.equals(getString(R.string.agency_center_text))) {
                WebActivity.open(requireActivity(), getString(R.string.agency_center_text), Const.DEMO_AGENCY_CENTER_URL + "&language=" + sessionManager.getStringValue(Const.SELECTED_LANGUAGE), false);
            } else if (type.equals(getString(R.string.host_center))) {
                WebActivity.open(requireActivity(), getString(R.string.host_center), Const.DEMO_HOST_CENTER_URL+ "&language=" + sessionManager.getStringValue(Const.SELECTED_LANGUAGE), false);
            } else if (type.equals(getString(R.string.bd_center))) {
                WebActivity.open(requireActivity(), getString(R.string.bd_center), Const.DEMO_BD_CENTER_URL+ "&language=" + sessionManager.getStringValue(Const.SELECTED_LANGUAGE), false);
            }
        } else {
            if (type.equals(getString(R.string.agency_center))) {
                openWebWithLanguage(
                        requireActivity(),
                        getString(R.string.agency_center_text),
                        user.getAgencyLoginString()+ "&language=" + sessionManager.getStringValue(Const.SELECTED_LANGUAGE)
                );

            } else if (type.equals(getString(R.string.host_center))) {
                openWebWithLanguage(
                        requireActivity(),
                        getString(R.string.host_center),
                        user.getHostLoginString()+ "&language=" + sessionManager.getStringValue(Const.SELECTED_LANGUAGE)
                );

            } else if (type.equals(getString(R.string.bd_center))) {
                openWebWithLanguage(
                        requireActivity(),
                        getString(R.string.bd_center),
                        user.getBdLoginString()+ "&language=" + sessionManager.getStringValue(Const.SELECTED_LANGUAGE)
                );
            }
        }

        if (type.equals(getString(R.string.offline_recharge))) {
            startActivity(new Intent(requireActivity(), SellerOfflineRechargeActivity.class));
        } else if (type.equals(getString(R.string.my_posts))) {
            startActivity(new Intent(requireActivity(), MyPostsActivity.class).putExtra(Const.DATA, new Gson().toJson(user)));
        } else if (type.equals(getString(R.string.my_relites))) {
            startActivity(new Intent(requireActivity(), MyRelitesActivity.class).putExtra(Const.DATA, new Gson().toJson(user)));
        } else if (type.equals(getString(R.string.store))) {
            startActivity(new Intent(requireActivity(), StoreActivity.class));
        } else if (type.equals(getString(R.string.free_coins))) {
            startActivity(new Intent(requireActivity(), FreeDiamondsActivity.class));
        } else if (type.equals(getString(R.string.become_vip))) {
            startActivity(new Intent(requireActivity(), VipPlanActivity.class));
        } else if (type.equals(getString(R.string.user_level))) {
            startActivity(new Intent(requireActivity(), MyLevelListActivity.class));
        }
    }

    private void setupRoleIndicators() {
        binding.tvType.setVisibility(sessionManager.getUser().isAgency() ? VISIBLE : GONE);
        binding.tvHostType.setVisibility(sessionManager.getUser().isHost() ? VISIBLE : GONE);
        binding.tvBdType.setVisibility(sessionManager.getUser().isBd() ? VISIBLE : GONE);
        binding.tvVIPType.setVisibility(sessionManager.getUser().isIsVIP() ? VISIBLE : GONE);
        binding.tvCoinseller.setVisibility(sessionManager.getUser().isCoinSeller() ? VISIBLE : GONE);

    }


    private void initListeners() {
        binding.btnSetting.setOnClickListener(v -> startActivity(new Intent(requireActivity(), SettingActivity.class)));
        binding.btnEditProfile.setOnClickListener(v -> startActivity(new Intent(requireActivity(), EditProfileActivity.class)));
        binding.tvLevel.setOnClickListener(v -> startActivity(new Intent(requireActivity(), MyLevelListActivity.class)));
        binding.layWallet.setOnClickListener(v -> startActivity(new Intent(requireActivity(), MyWalletActivity.class)));
        binding.lytBlock.setOnClickListener(v -> startActivity(new Intent(requireActivity(), BlockedUserListActivity.class)));
        binding.layBecomeHost.setOnClickListener(view -> startActivity(new Intent(requireActivity(), HostRequestActivity.class)));


        binding.lytFollowers.setOnClickListener(v -> {
            if (user != null) {
                startActivity(new Intent(requireActivity(), FollowersListActivity.class)
                        .putExtra(Const.TYPE, 2)
                        .putExtra(Const.USERID, user.getId()));
            }
        });

        binding.lytFollowing.setOnClickListener(v -> {
            if (user != null) {
                startActivity(new Intent(requireActivity(), FollowersListActivity.class)
                        .putExtra(Const.TYPE, 1)
                        .putExtra(Const.USERID, user.getId()));
            }
        });

        binding.copy.setOnClickListener(v -> {
            if (user != null && user.getUniqueId() != null) {
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", user.getUniqueId());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireActivity(), R.string.copied_successfully, Toast.LENGTH_SHORT).show();
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
        Log.d(TAG, "openWebWithLanguage: ==> " + code);
        WebActivity.open(ctx, title, uri.toString(), false);
    }
}