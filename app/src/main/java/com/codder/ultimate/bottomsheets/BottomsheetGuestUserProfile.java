package com.codder.ultimate.bottomsheets;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.bumptech.glide.Glide;
import com.caverock.androidsvg.SVG;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.chat.activity.ChatActivity;
import com.codder.ultimate.chat.activity.FakeChatActivity;
import com.codder.ultimate.databinding.BottomSheetGuestuserProfileBinding;
import com.codder.ultimate.databinding.BottomSheetReportBinding;
import com.codder.ultimate.dialog.CustomDialogClass;
import com.codder.ultimate.guestuser.activity.FollowersListActivity;
import com.codder.ultimate.guestuser.activity.GuestActivity;
import com.codder.ultimate.guestuser.adapter.GuestUserProfileViewPagerAdapter;
import com.codder.ultimate.guestuser.model.GuestViewModel;
import com.codder.ultimate.guestuser.utils.GuestViewModelFactory;
import com.codder.ultimate.guestuser.utils.UserRepository;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.UserApiCall;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.net.URL;

public class BottomsheetGuestUserProfile extends BottomSheetDialogFragment {

    BottomSheetGuestuserProfileBinding binding;

    private GuestViewModel viewModel;
    private String userId;
    private boolean isHost;
    private GuestProfileRoot.User user;
    private boolean previousBlockState = false;

    Context ctx;


    @Override
    public void onStart() {
        super.onStart();

        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

        if (bottomSheet != null) {
            int height = (int) (600 * getResources().getDisplayMetrics().density);
            bottomSheet.getLayoutParams().height = height;
            bottomSheet.requestLayout();

            bottomSheet.setBackgroundColor(Color.TRANSPARENT);

            BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(true);
            behavior.setHideable(true);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    public BottomsheetGuestUserProfile(String userId, boolean isHost) {
        this.userId = userId;
        this.isHost = isHost;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_guestuser_profile, container, false);
        ctx = requireContext();
        setupViewModel();
        initIntentData();
        observeViewModel();
        initListeners();
        return binding.getRoot();
    }


    private void initTabLayout() {
        binding.viewPager.setAdapter(new GuestUserProfileViewPagerAdapter(getChildFragmentManager(), user));
        binding.tabLayout.setupWithViewPager(binding.viewPager);
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                View v = tab.getCustomView();
                if (v != null) {
                    TextView tv = v.findViewById(R.id.tvTab);
                    tv.setTextColor(ContextCompat.getColor(ctx, R.color.white));
                    tv.setBackground(ContextCompat.getDrawable(ctx, R.drawable.home_tab_selectedbg));
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                if (v != null) {
                    TextView tv = v.findViewById(R.id.tvTab);
                    tv.setTextColor(ContextCompat.getColor(ctx, R.color.white_76));
                    tv.setBackground(ContextCompat.getDrawable(ctx, R.drawable.home_tab_unselectedbg));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        setTab(new String[]{ctx.getString(R.string.posts), ctx.getString(R.string.relites)});
    }

    private void setTab(String[] tab) {
        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        binding.tabLayout.removeAllTabs();
        for (int i = 0; i < tab.length; i++) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setCustomView(createCustomView(i, tab[i])));
        }

        ViewGroup tabStrip = (ViewGroup) binding.tabLayout.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            View tabView = tabStrip.getChildAt(i);
            tabView.setPadding(0, 0, 15, 0);
        }

    }

    private View createCustomView(int i, String s) {

        View v = LayoutInflater.from(ctx).inflate(R.layout.layout_custom_tab, null);
        TextView tv = v.findViewById(R.id.tvTab);
        tv.setText(s);
        if (i == 0) {
            tv.setTextColor(ContextCompat.getColor(ctx, R.color.white));
            tv.setBackground(ContextCompat.getDrawable(ctx, R.drawable.home_tab_selectedbg));
        } else {
            tv.setTextColor(ContextCompat.getColor(ctx, R.color.white_76));
            tv.setBackground(ContextCompat.getDrawable(ctx, R.drawable.home_tab_unselectedbg));
        }
        return v;

    }

    private void setupViewModel() {
        UserRepository repository = new UserRepository(new UserApiCall(ctx));
        GuestViewModelFactory factory = new GuestViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(GuestViewModel.class);
    }

    private void initIntentData() {

        if (userId != null && !userId.isEmpty()) {
            binding.layMain.setVisibility(GONE);
            binding.layOption.setVisibility(GONE);
            binding.shimmer.setVisibility(VISIBLE);
            binding.shimmer.startShimmer();

            viewModel.fetchGuestProfile(userId);
        } else {
            Toast.makeText(ctx, ctx.getString(R.string.invalid_user_id), Toast.LENGTH_SHORT).show();
            if (isAdded()) dismiss();
        }
    }

    private void observeViewModel() {
        viewModel.getUser().observe(this, user -> {
            if (user != null) {
                binding.shimmer.stopShimmer();
                binding.shimmer.setVisibility(GONE);
                binding.layMain.setVisibility(VISIBLE);
                binding.layOption.setVisibility(VISIBLE);

                boolean wasBlocked = previousBlockState;
                previousBlockState = user.isBlock(); // Update for next check

                this.user = user;
                populateUserDetails(user);
                initTabLayout();

                if (!wasBlocked && user.isBlock()) {
                    Toast.makeText(ctx, ctx.getString(R.string.user_has_been_blocked), Toast.LENGTH_SHORT).show();
                    if (isAdded()) dismiss();
                }
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                binding.shimmer.stopShimmer();
                binding.shimmer.setVisibility(GONE);
                binding.layMain.setVisibility(VISIBLE);
                binding.layOption.setVisibility(VISIBLE);

                Toast.makeText(ctx, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getFollowLoading().observe(requireActivity(), isLoading -> {
            binding.pdFollow.setVisibility(isLoading ? VISIBLE : GONE);
            binding.tvFollowStatus.setVisibility(isLoading ? GONE : VISIBLE);
        });

        viewModel.getBlockLoading().observe(requireActivity(), isLoading -> {
            binding.pdBlock.setVisibility(isLoading ? VISIBLE : GONE);
            binding.tvBlock.setVisibility(isLoading ? GONE : VISIBLE);
        });
    }

    private void populateUserDetails(GuestProfileRoot.User user) {
        binding.tvName.setText(user.getName());
        binding.tvAge.setText(String.valueOf(user.getAge()));
        if (user.getUniqueId() != null) {
            binding.tvUserName.setText(ctx.getString(R.string.id_) + user.getUniqueId());
            binding.layoutUsername.setVisibility(VISIBLE);
        } else {
            binding.layoutUsername.setVisibility(GONE);
        }
        binding.tvFollowers.setText(String.valueOf(user.getFollowers()));
        binding.tvFollowing.setText(String.valueOf(user.getFollowing()));
        binding.tvCountry.setText(user.getCountry());
        if (user.getLevel() != null && user.getLevel().getName() != null) {
            binding.tvLevel.setText(user.getLevel().getName());
        }

        if (user.getLevel() != null && user.getLevel().getImage() != null) {
            Glide.with(this).load(BuildConfig.BASE_URL + user.getLevel().getImage()).into(binding.ivLevel);
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


        binding.tvType.setVisibility(user.isAgency() ? VISIBLE : GONE);
        binding.tvHostType.setVisibility(user.isHost() ? VISIBLE : GONE);
        binding.tvVIPType.setVisibility(user.isIsVIP() ? VISIBLE : GONE);
        binding.tvCoinseller.setVisibility(user.isCoinSeller() ? VISIBLE : GONE);


        binding.imgUser.setUserImage(user.getImage(), user.getAvatarFrameImage(), 40);
        boolean isSelf = isSelfUser(user);
        binding.lytFollowUnfollow.setVisibility(isSelf ? GONE : VISIBLE);
        binding.lytBlockUnblock.setVisibility(isSelf ? GONE : VISIBLE);
        binding.tvMessages.setVisibility(isSelf ? GONE : VISIBLE);

        binding.tvFollowStatus.setText(user.isFollow() ? ctx.getString(R.string.following) : ctx.getString(R.string.follow));


        if (user.isFollow()) {
            Glide.with(ctx).load(R.drawable.icon_following).into(binding.ivFollow);
        } else {
            Glide.with(ctx).load(R.drawable.icon_follow).into(binding.ivFollow);
        }
        if (user.getGender().equals("Male")) {
            Glide.with(ctx).load(R.drawable.ic_male).into(binding.ivGender);
        } else {
            Glide.with(ctx).load(R.drawable.ic_female).into(binding.ivGender);
        }

        binding.lytFollowUnfollow.setBackgroundResource(user.isFollow()
                ? R.drawable.bg_following
                : R.drawable.bg_follow);

        binding.tvBlock.setText(user.isBlock() ? ctx.getString(R.string.unblock) : ctx.getString(R.string.block));

    }

    private void initListeners() {

        if (isHost || isSelfUser(user)) {
            binding.lytBlockUnblock.setVisibility(GONE);
        } else {
            binding.lytBlockUnblock.setVisibility(VISIBLE);
        }

        binding.ivClose.setOnClickListener(v -> {
            dismiss();
        });

        binding.lytFollowUnfollow.setOnClickListener(v -> {
            if (user != null && !isSelfUser(user)) {
                boolean shouldFollow = !user.isFollow();
                viewModel.followUnfollowUser(shouldFollow, user.getUserId());
            } else {
                Toast.makeText(ctx, ctx.getString(R.string.user_data_is_not_available), Toast.LENGTH_SHORT).show();
            }
        });

        binding.lytBlockUnblock.setOnClickListener(v -> {
            if (user != null && !isSelfUser(user)) {
                viewModel.blockUnblockUser(user.getUserId());
            } else {
                Log.d("TAG", "initListeners: User data is not available");
            }
        });

        binding.tvMessages.setOnClickListener(v -> {
            if (user != null && !isSelfUser(user)) {
                Intent chatIntent;
                if (user.isFake()) {
                    chatIntent = new Intent(ctx, FakeChatActivity.class);
                    chatIntent.putExtra(Const.CHATROOM, new Gson().toJson(user));
                    Log.d("TAG", "tvMessages clicked : ==> " + new Gson().toJson(user));
                } else {
                    chatIntent = new Intent(ctx, ChatActivity.class);
                    chatIntent.putExtra(Const.USER, new Gson().toJson(user));
                }
                ctx.startActivity(chatIntent);
            }
        });

        binding.copy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("User ID", user.getUniqueId());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ctx, ctx.getString(R.string.copied_successfully), Toast.LENGTH_SHORT).show();
            }
        });

        binding.lytFollowing.setOnClickListener(v -> openFollowersList(1));
        binding.lytFollowers.setOnClickListener(v -> openFollowersList(2));
    }

    private boolean isSelfUser(GuestProfileRoot.User candidate) {
        if (candidate == null || candidate.getUserId() == null || ctx == null) return false;
        SessionManager sessionManager = new SessionManager(ctx);
        return sessionManager.getUser() != null && candidate.getUserId().equals(sessionManager.getUser().getId());
    }

    private void openFollowersList(int type) {
        Intent intent = new Intent(ctx, FollowersListActivity.class);
        intent.putExtra(Const.TYPE, type);
        intent.putExtra(Const.USERID, userId);
        ctx.startActivity(intent);
    }


}
