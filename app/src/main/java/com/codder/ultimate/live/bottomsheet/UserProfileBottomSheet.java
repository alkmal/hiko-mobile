package com.codder.ultimate.live.bottomsheet;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.caverock.androidsvg.SVG;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.chat.activity.ChatActivity;
import com.codder.ultimate.databinding.BottomSheetUserProfileBinding;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.UserApiCall;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

import java.net.URL;

public class UserProfileBottomSheet {
    private static final String TAG = "UserProfileBottomSheet";

    private final BottomSheetDialog bottomSheetDialog;
    private final BottomSheetUserProfileBinding sheetDialogBinding;
    private final UserApiCall userApiCall;
    private OnUserTapListener onUserTapListener;
    private final Context context;
    private final SessionManager sessionManager;

    public UserProfileBottomSheet(Context context) {
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

        userApiCall = new UserApiCall(context);
        sheetDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet_user_profile, null, false);
        bottomSheetDialog.setContentView(sheetDialogBinding.getRoot());
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

        try {
            String currentUserId = sessionManager.getUser() != null ? sessionManager.getUser().getId() : "";
            boolean isSelf = currentUserId.equalsIgnoreCase(user.getUserId());

            // Block button visibility
            sheetDialogBinding.btnBlock.setVisibility((isWatchAudioActivity || isSelf) ? View.GONE : View.VISIBLE);


            if (isHost || isSelf) {
                sheetDialogBinding.btnMessage.setVisibility(View.GONE);
                sheetDialogBinding.lytFollowUnfollow.setVisibility(View.GONE);
                sheetDialogBinding.layOption.setVisibility(View.GONE);
            } else {
                sheetDialogBinding.lytFollowUnfollow.setVisibility(View.VISIBLE);
                sheetDialogBinding.btnMessage.setVisibility(View.VISIBLE);
                sheetDialogBinding.layOption.setVisibility(View.VISIBLE);
            }

            try {

                sheetDialogBinding.imgUser.setUserImage(user.getImage(), user.getAvatarFrameImage(), 30);
            } catch (Exception e) {
                Log.w(TAG, "Error loading user image", e);
            }


            sheetDialogBinding.tvName.setText(user.getName());
            sheetDialogBinding.tvAge.setText(String.valueOf(user.getAge()));
            sheetDialogBinding.tvCountry.setText(user.getCountry());

            if (user.getGender().equals("Male")){
                Glide.with(context).load(R.drawable.ic_male).into(sheetDialogBinding.ivGender);
            }else {
                Glide.with(context).load(R.drawable.ic_female).into(sheetDialogBinding.ivGender);
            }

            if (user.getLevel() != null) {
                sheetDialogBinding.tvLevel.setText(user.getLevel().getName());
            }
            sheetDialogBinding.tvPosts.setText(String.valueOf(user.getPost()));
            sheetDialogBinding.tvFollowrs.setText(String.valueOf(user.getFollowers()));
            sheetDialogBinding.tvVideos.setText(String.valueOf(user.getVideo()));

            sheetDialogBinding.tvFollowStatus.setText(user.isFollow() ? context.getString(R.string.following) : context.getString(R.string.follow));

            sheetDialogBinding.userId.setText(safeString(context.getString(R.string.id_) + user.getUniqueId()));

            sheetDialogBinding.ivClose.setOnClickListener(view -> {
                bottomSheetDialog.dismiss();
            });


           if (user.isFollow()){
               Glide.with(context).load(R.drawable.icon_following).into(sheetDialogBinding.ivFollow);
           }else {
               Glide.with(context).load(R.drawable.icon_follow).into(sheetDialogBinding.ivFollow);
           }

            sheetDialogBinding.lytFollowUnfollow.setBackgroundResource(user.isFollow()
                    ? R.drawable.bg_following
                    : R.drawable.bg_follow);
            sheetDialogBinding.pdFollow.setVisibility(View.GONE);

            sheetDialogBinding.copy.setOnClickListener(v -> {
                if (user != null && user.getUniqueId() != null) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("", user.getUniqueId());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, R.string.copied_successfully, Toast.LENGTH_SHORT).show();
                }
            });

            if (user.getLevel() != null && user.getLevel().getImage() != null) {
                Glide.with(context).load(BuildConfig.BASE_URL + user.getLevel().getImage()).into(sheetDialogBinding.ivLevel);
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
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            sheetDialogBinding.svgWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                            sheetDialogBinding.svgWebView.setImageDrawable(drawable);
                            sheetDialogBinding.svgWebView.invalidate();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            sheetDialogBinding.tvType.setVisibility(user.isAgency() ? VISIBLE : GONE);
            sheetDialogBinding.tvHostType.setVisibility(user.isHost() ? VISIBLE : GONE);
            sheetDialogBinding.tvVIPType.setVisibility(user.isIsVIP() ? VISIBLE : GONE);
            sheetDialogBinding.tvCoinseller.setVisibility(user.isCoinSeller() ? VISIBLE : GONE);

            // Follow/unfollow button logic
            sheetDialogBinding.lytFollowUnfollow.setOnClickListener(v -> {
                if (isSelf) return;
                sheetDialogBinding.lytFollowUnfollow.setEnabled(false);
                sheetDialogBinding.pdFollow.setVisibility(View.VISIBLE);
                sheetDialogBinding.tvFollowStatus.setVisibility(View.INVISIBLE);

                userApiCall.followUnfollowUser(!user.isFollow(), user.getUserId(), liveStreamingId, new UserApiCall.OnFollowUnfollowListener() {
                    @Override
                    public void onFollowSuccess() {
                        sheetDialogBinding.pdFollow.setVisibility(View.GONE);
                        user.setFollow(true);
                        sheetDialogBinding.tvFollowStatus.setText(R.string.following);
                        sheetDialogBinding.tvFollowStatus.setVisibility(View.VISIBLE);
                        sheetDialogBinding.lytFollowUnfollow.setBackgroundResource(R.drawable.bg_following);
                        Glide.with(context).load(R.drawable.icon_following).into(sheetDialogBinding.ivFollow);
                        sheetDialogBinding.lytFollowUnfollow.setEnabled(true);
                    }

                    @Override
                    public void onUnfollowSuccess() {
                        sheetDialogBinding.pdFollow.setVisibility(View.GONE);
                        user.setFollow(false);
                        sheetDialogBinding.tvFollowStatus.setText(R.string.follow);
                        sheetDialogBinding.tvFollowStatus.setVisibility(View.VISIBLE);
                        sheetDialogBinding.lytFollowUnfollow.setBackgroundResource(R.drawable.bg_follow);
                        Glide.with(context).load(R.drawable.icon_follow).into(sheetDialogBinding.ivFollow);
                        sheetDialogBinding.lytFollowUnfollow.setEnabled(true);
                    }

                    @Override
                    public void onFail() {
                        sheetDialogBinding.pdFollow.setVisibility(View.GONE);
                        sheetDialogBinding.tvFollowStatus.setVisibility(View.VISIBLE);
                        sheetDialogBinding.lytFollowUnfollow.setEnabled(true);
                        Log.w(TAG, "Follow/unfollow failed");
                    }
                });
            });

            // Message button logic
            sheetDialogBinding.btnMessage.setOnClickListener(view -> {
                try {
                    Intent intent = new Intent(context, ChatActivity.class)
                            .putExtra(Const.USER, new Gson().toJson(user))
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

