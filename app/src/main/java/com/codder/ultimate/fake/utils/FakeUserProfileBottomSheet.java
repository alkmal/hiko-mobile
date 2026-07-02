package com.codder.ultimate.fake.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.chat.activity.ChatActivity;
import com.codder.ultimate.chat.activity.FakeChatActivity;
import com.codder.ultimate.databinding.BottomSheetFakeUserProfileBinding;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.UserApiCall;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

public class FakeUserProfileBottomSheet {
    private static final String TAG = "UserProfileBottomSheet";
    private final BottomSheetDialog bottomSheetDialog;
    private final BottomSheetFakeUserProfileBinding sheetDialogBinding;
    private OnUserTapListener onUserTapListener;
    private final Context context;
    private final SessionManager sessionManager;

    public FakeUserProfileBottomSheet(Context context) {
        this.context = context;
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        sessionManager = new SessionManager(context);

        if (bottomSheetDialog.getWindow() != null) {
            bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        bottomSheetDialog.setOnShowListener(dialog -> {
            try {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error expanding bottom sheet", e);
            }
        });

        sheetDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet_fake_user_profile, null, false);
        bottomSheetDialog.setContentView(sheetDialogBinding.getRoot());
//        bottomSheetDialog.show();
    }

    public OnUserTapListener getOnUserTapListener() {
        return onUserTapListener;
    }

    public void setOnUserTapListener(OnUserTapListener onUserTapListener) {
        this.onUserTapListener = onUserTapListener;
    }

    public void show(boolean isHost, GuestProfileRoot.User user, String liveStreamingId, boolean isWatchAudioActivity) {
        if (user == null || sessionManager == null || context == null) {
            Log.e(TAG, "User or sessionManager or context is null. Aborting show().");
            return;
        }

        Log.d(TAG, "show: " +user.getName());
        Log.d(TAG, "show: ========" + user.getImage());

        try {
            String currentUserId = sessionManager.getUser() != null ? sessionManager.getUser().getId() : "";
            boolean isSelf = currentUserId.equalsIgnoreCase(user.getUserId());

            sheetDialogBinding.ivClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

            if (isHost) {
                sheetDialogBinding.btnMessage.setVisibility(View.GONE);
                sheetDialogBinding.lytFollowUnfollow.setVisibility(View.GONE);
            } else {
                sheetDialogBinding.lytFollowUnfollow.setVisibility(View.VISIBLE);
                sheetDialogBinding.btnMessage.setVisibility(View.VISIBLE);
            }

            if (isSelf) {
                sheetDialogBinding.btnMessage.setVisibility(View.GONE);
                sheetDialogBinding.lytFollowUnfollow.setVisibility(View.GONE);
            } else {
                sheetDialogBinding.btnMessage.setVisibility(View.VISIBLE);
                sheetDialogBinding.lytFollowUnfollow.setVisibility(View.VISIBLE);
            }

            try {

                sheetDialogBinding.imgUser.setUserImage(user.getImage(), user.getAvatarFrameImage(), 30);
            } catch (Exception e) {
                Log.w(TAG, "Error loading user image", e);
            }

            sheetDialogBinding.tvName.setText(user.getName());
            sheetDialogBinding.tvAge.setText(String.valueOf(user.getAge()));
            sheetDialogBinding.tvCountry.setText(user.getCountry());
            if (user.getLevel() != null) {
                sheetDialogBinding.tvLevel.setText(user.getLevel().getName());
            }
            sheetDialogBinding.tvPosts.setText(String.valueOf(user.getPost()));
            sheetDialogBinding.tvFollowrs.setText(String.valueOf(user.getFollowers()));
            sheetDialogBinding.tvVideos.setText(String.valueOf(user.getVideo()));

            sheetDialogBinding.tvFollowStatus.setText(user.isFollow() ? context.getString(R.string.following) : context.getString(R.string.follow));

            sheetDialogBinding.userId.setText(safeString(context.getString(R.string.id_) + user.getUniqueId()));



//            sheetDialogBinding.tvFollowStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
//                    user.isFollow() ? R.drawable.ic_following_fill : R.drawable.icon_follow_fill,
//                    0,
//                    0,
//                    0
//            );
//
//            sheetDialogBinding.lytFollowUnfollow.setBackgroundResource(user.isFollow()
//                    ? R.drawable.bg_following
//                    : R.drawable.bg_follow);
            sheetDialogBinding.pdFollow.setVisibility(View.GONE);
            sheetDialogBinding.copy.setOnClickListener(view -> {

                if (user != null && user.getUniqueId() != null) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("", user.getUniqueId());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, R.string.copied_successfully, Toast.LENGTH_SHORT).show();
                }
            });

            // User type badges
            if (user.isHost()) {
                sheetDialogBinding.tvType.setVisibility(View.VISIBLE);
                sheetDialogBinding.tvType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_user_place, 0, 0, 0);
                sheetDialogBinding.tvType.setText(R.string.creator);
            } else if (user.isAgency()) {
                sheetDialogBinding.tvType.setVisibility(View.VISIBLE);
                sheetDialogBinding.tvType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_agency, 0, 0, 0);
                sheetDialogBinding.tvType.setText(R.string.agency);
            } else {
                sheetDialogBinding.tvType.setVisibility(View.GONE);
            }

            if (user.isVIP()) {
                sheetDialogBinding.tvVIPType.setVisibility(View.VISIBLE);
                sheetDialogBinding.tvVIPType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_crown, 0, 0, 0);
            } else {
                sheetDialogBinding.tvVIPType.setVisibility(View.GONE);
            }

            if (user.isCoinSeller()) {
                sheetDialogBinding.tvCoinType.setVisibility(View.VISIBLE);
                sheetDialogBinding.tvCoinType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_agency, 0, 0, 0);
                sheetDialogBinding.tvCoinType.setText(R.string.coin_seller);
            } else {
                sheetDialogBinding.tvCoinType.setVisibility(View.GONE);
            }

            // Follow/unfollow button logic
            // Hold a local state flag
//            final boolean[] isFollowing = {false};
            final boolean[] isFollowing = {user.isFollow()};

            sheetDialogBinding.lytFollowUnfollow.setOnClickListener(v -> {
                // Disable while processing
                sheetDialogBinding.lytFollowUnfollow.setEnabled(false);
                sheetDialogBinding.pdFollow.setVisibility(View.VISIBLE);
                sheetDialogBinding.tvFollowStatus.setVisibility(View.INVISIBLE);

                // Simulate API delay
                v.postDelayed(() -> {
                    // Toggle state
                    isFollowing[0] = !isFollowing[0];

                    if (isFollowing[0]) {
                        // User just followed
                        sheetDialogBinding.tvFollowStatus.setText(R.string.following);
                        sheetDialogBinding.lytFollowUnfollow.setBackgroundResource(R.drawable.bg_following);
                        Glide.with(context).load(R.drawable.icon_following).into(sheetDialogBinding.ivFollow);
                    } else {
                        // User just unfollowed
                        sheetDialogBinding.tvFollowStatus.setText(R.string.follow);
                        sheetDialogBinding.lytFollowUnfollow.setBackgroundResource(R.drawable.bg_follow);
                        Glide.with(context).load(R.drawable.icon_follow).into(sheetDialogBinding.ivFollow);
                    }
                    sheetDialogBinding.lytFollowUnfollow.setEnabled(true);
                    sheetDialogBinding.pdFollow.setVisibility(View.GONE);
                    sheetDialogBinding.tvFollowStatus.setVisibility(View.VISIBLE);

                }, 500); // half-second delay to mimic network
            });

            // Message button logic
            sheetDialogBinding.btnMessage.setOnClickListener(view -> {
                try {
                    Intent intent = new Intent(context, FakeChatActivity.class)
                            .putExtra(Const.CHATROOM, new Gson().toJson(user))
                            .putExtra("fromUserProfileSheet", true);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting ChatActivity", e);
                }
            });

            // Block button logic
            sheetDialogBinding.btnBlock.setOnClickListener(v -> {
                if (onUserTapListener != null) {
                    onUserTapListener.onBlockClick(user);
                }
                bottomSheetDialog.dismiss();
            });

//            if (user.getGender().equals("Male")){
//                Glide.with(context).load(R.drawable.ic_male).into(sheetDialogBinding.ivGender);
//            }else {
//                Glide.with(context).load(R.drawable.ic_female).into(sheetDialogBinding.ivGender);
//            }

            bottomSheetDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Exception in UserProfileBottomSheet.show()", e);
        }
    }

    private static String safeString(@Nullable String str) {
        return str == null ? "" : str;
    }

    public interface OnUserTapListener {
        void onBlockClick(GuestProfileRoot.User userDummy);
    }
}

