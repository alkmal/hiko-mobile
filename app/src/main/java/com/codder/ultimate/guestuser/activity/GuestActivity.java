package com.codder.ultimate.guestuser.activity;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Picture;
import android.graphics.Typeface;
import android.graphics.drawable.PictureDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.caverock.androidsvg.SVG;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.chat.activity.ChatActivity;
import com.codder.ultimate.chat.activity.FakeChatActivity;
import com.codder.ultimate.databinding.ActivityGuestBinding;
import com.codder.ultimate.guestuser.adapter.GuestUserProfileViewPagerAdapter;
import com.codder.ultimate.guestuser.model.GuestViewModel;
import com.codder.ultimate.guestuser.utils.GuestViewModelFactory;
import com.codder.ultimate.guestuser.utils.UserRepository;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.UserApiCall;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.net.URL;

public class GuestActivity extends BaseActivity {
    public static final String TAG = "GuestActivity";

    private ActivityGuestBinding binding;
    private GuestViewModel viewModel;
    private String userId;
    private GuestProfileRoot.User user;
    private boolean previousBlockState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_guest);

        setupViewModel();
        initIntentData();
        observeViewModel();
        initListeners();
    }

    private void initTabLayout() {
        binding.viewPager.setAdapter(new GuestUserProfileViewPagerAdapter(getSupportFragmentManager(), user));
        binding.tabLayout.setupWithViewPager(binding.viewPager);
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                View v = tab.getCustomView();
                if (v != null) {
                    TextView tv = v.findViewById(R.id.tvTab);
                    ImageView indicator = v.findViewById(R.id.indicator);
                    tv.setTextColor(ContextCompat.getColor(GuestActivity.this, R.color.white));
                    Typeface typeface = ResourcesCompat.getFont(GuestActivity.this, R.font.airbnbcereal_w_bd);
                    tv.setTypeface(typeface);
                    indicator.setVisibility(VISIBLE);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                if (v != null) {
                    TextView tv = v.findViewById(R.id.tvTab);
                    ImageView indicator = v.findViewById(R.id.indicator);
                    tv.setTextColor(ContextCompat.getColor(GuestActivity.this, R.color.white_50));
                    Typeface typeface = ResourcesCompat.getFont(GuestActivity.this, R.font.airbnbcereal_w_bk);
                    tv.setTypeface(typeface);
                    indicator.setVisibility(INVISIBLE);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        setTab(new String[]{getString(R.string.posts), getString(R.string.relites)});
    }

    private void setTab(String[] tab) {
        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        binding.tabLayout.removeAllTabs();
        for (int i = 0; i < tab.length; i++) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setCustomView(createCustomView(i, tab[i])));
        }
    }

    private View createCustomView(int i, String s) {

        View v = LayoutInflater.from(GuestActivity.this).inflate(R.layout.layout_post_tab, null);
        TextView tv = v.findViewById(R.id.tvTab);
        ImageView indicator = v.findViewById(R.id.indicator);
        tv.setText(s);
        if (i == 0) {
            tv.setTextColor(ContextCompat.getColor(this, R.color.white));
            Typeface typeface = ResourcesCompat.getFont(this, R.font.airbnbcereal_w_bd);
            tv.setTypeface(typeface);
            indicator.setVisibility(VISIBLE);
        } else {
            tv.setTextColor(ContextCompat.getColor(this, R.color.white_50));
            Typeface typeface = ResourcesCompat.getFont(this, R.font.airbnbcereal_w_bk);
            tv.setTypeface(typeface);
            indicator.setVisibility(INVISIBLE);
        }
        return v;

    }

    private void setupViewModel() {
        UserRepository repository = new UserRepository(new UserApiCall(this));
        GuestViewModelFactory factory = new GuestViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(GuestViewModel.class);
    }

    private void initIntentData() {
        userId = getIntent().getStringExtra(Const.USERID);
        if (userId != null && !userId.isEmpty()) {
            binding.layMain.setVisibility(GONE);
            binding.shimmer.setVisibility(VISIBLE);
            binding.shimmer.startShimmer();

            viewModel.fetchGuestProfile(userId);
        } else {
            Toast.makeText(this, getString(R.string.invalid_user_id), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void observeViewModel() {
        viewModel.getUser().observe(this, user -> {
            if (user != null) {
                binding.shimmer.stopShimmer();
                binding.shimmer.setVisibility(GONE);
                binding.layMain.setVisibility(VISIBLE);

                boolean wasBlocked = previousBlockState;
                previousBlockState = user.isBlock(); // Update for next check

                this.user = user;
                populateUserDetails(user);
                initTabLayout();

                if (!wasBlocked && user.isBlock()) {
                    Toast.makeText(this, getString(R.string.user_has_been_blocked), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                binding.shimmer.stopShimmer();
                binding.shimmer.setVisibility(GONE);
                binding.layMain.setVisibility(VISIBLE);

                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getFollowLoading().observe(this, isLoading -> {
            binding.pdFollow.setVisibility(isLoading ? VISIBLE : GONE);
            binding.tvFollowStatus.setVisibility(isLoading ? GONE : VISIBLE);
        });

        viewModel.getBlockLoading().observe(this, isLoading -> {
            binding.pdBlock.setVisibility(isLoading ? VISIBLE : GONE);
            binding.tvBlock.setVisibility(isLoading ? GONE : VISIBLE);
        });
    }

    private void populateUserDetails(GuestProfileRoot.User user) {
        binding.tvTitle.setText(user.getName());
        binding.tvName.setText(user.getName());
        binding.tvAge.setText(String.valueOf(user.getAge()));
        if (user.getUniqueId() != null) {
            binding.tvUserName.setText(getString(R.string.id_) + user.getUniqueId());
            binding.layoutUsername.setVisibility(VISIBLE);
        } else {
            binding.layoutUsername.setVisibility(GONE);
        }
        binding.tvFollowers.setText(String.valueOf(user.getFollowers()));
        binding.tvFollowing.setText(String.valueOf(user.getFollowing()));
        binding.tvPost.setText(String.valueOf(user.getPost()));
        if (!user.getBio().isEmpty()) {
            binding.tvBio.setVisibility(VISIBLE);
            binding.tvBio.setText(user.getBio());
        } else {
            binding.tvBio.setText("");
            binding.tvBio.setVisibility(GONE);
        }
        binding.tvCountry.setText(user.getCountry());

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

                    runOnUiThread(() -> {
                        binding.svgWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        binding.svgWebView.setImageDrawable(drawable);
                        binding.svgWebView.invalidate();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }


        if (user.getLevel() != null && user.getLevel().getName() != null) {
            binding.tvLevel.setText(user.getLevel().getName());
        }
        if (user.getLevel() != null && user.getLevel().getImage() != null) {
            Glide.with(this).load(BuildConfig.BASE_URL + user.getLevel().getImage()).into(binding.ivLevel);
        }

        binding.tvType.setVisibility(user.isAgency() ? VISIBLE : GONE);
        binding.tvHostType.setVisibility(user.isHost() ? VISIBLE : GONE);
        binding.tvVIPType.setVisibility(user.isIsVIP() ? VISIBLE : GONE);
        binding.tvCoinseller.setVisibility(user.isCoinSeller() ? VISIBLE : GONE);

        binding.imgUser.setUserImage(user.getImage(), user.getAvatarFrameImage(), 40);

        binding.tvFollowStatus.setText(user.isFollow() ? getString(R.string.following) : getString(R.string.follow));


        if (user.isFollow()){
            Glide.with(this).load(R.drawable.icon_following).into(binding.ivFollow);
        }else{
            Glide.with(this).load(R.drawable.icon_follow).into(binding.ivFollow);
        }
        if (user.getGender().equals("Male")){
            Glide.with(this).load(R.drawable.ic_male).into(binding.ivGender);
        }else{
            Glide.with(this).load(R.drawable.ic_female).into(binding.ivGender);
        }

        binding.lytFollowUnfollow.setBackgroundResource(user.isFollow()
                ? R.drawable.bg_following
                : R.drawable.bg_follow);

        binding.tvBlock.setText(user.isBlock() ? getString(R.string.unblock) : getString(R.string.block));

    }

    private void initListeners() {

        binding.lytFollowUnfollow.setOnClickListener(v -> {
            if (user != null) {
                boolean shouldFollow = !user.isFollow();
                viewModel.followUnfollowUser(shouldFollow, user.getUserId());
            } else {
                Toast.makeText(this, getString(R.string.user_data_is_not_available), Toast.LENGTH_SHORT).show();
            }
        });

        binding.lytBlockUnblock.setOnClickListener(v -> {
            if (user != null) {
                viewModel.blockUnblockUser(user.getUserId());
            } else {
                Log.d(TAG, "initListeners: User data is not available");
            }
        });

        binding.tvMessages.setOnClickListener(v -> {
            if (user != null) {
                Intent chatIntent;
                if (user.isFake()) {
                    chatIntent = new Intent(this, FakeChatActivity.class);
                    chatIntent.putExtra(Const.CHATROOM, new Gson().toJson(user));
                    Log.d(TAG, "tvMessages clicked : ==> " + new Gson().toJson(user));
                }else {
                    chatIntent = new Intent(this, ChatActivity.class);
                    chatIntent.putExtra(Const.USER, new Gson().toJson(user));
                }
                startActivity(chatIntent);
            }
        });

        binding.copy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("User ID", user.getUniqueId());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, getString(R.string.copied_successfully), Toast.LENGTH_SHORT).show();
            }
        });

        binding.lytFollowing.setOnClickListener(v -> openFollowersList(1));
        binding.lytFollowers.setOnClickListener(v -> openFollowersList(2));
    }

    private void openFollowersList(int type) {
        Intent intent = new Intent(this, FollowersListActivity.class);
        intent.putExtra(Const.TYPE, type);
        intent.putExtra(Const.USERID, userId);
        startActivity(intent);
    }
}