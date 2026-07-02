package com.codder.ultimate.live.bottomsheet;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.caverock.androidsvg.SVG;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.BottomSheetOnlineProfileBinding;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.net.URL;

public class BottomSheetViewersUserProfile {

    private final BottomSheetDialog bottomSheetDialog;
    private final Context context;

    public BottomSheetViewersUserProfile(@NonNull Context context,
                                         @Nullable PkAudioLiveUserRoot.UsersItem.SeatItem seatItem,
                                         @Nullable GuestProfileRoot.User userData,
                                         @NonNull OnClickListener onClickListener) {
        this.context = context;

        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        if (bottomSheetDialog.getWindow() != null) {
            bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        BottomSheetOnlineProfileBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.bottom_sheet_online_profile,
                null,
                false);

        bottomSheetDialog.setContentView(binding.getRoot());
        bottomSheetDialog.show();

        if (userData != null) {
            setupUserProfile(binding, userData);
        } else {
            binding.getRoot().setVisibility(View.GONE);
            return;
        }

        setupSeatAndMuteStatus(binding, seatItem);

        setupListeners(binding, seatItem, onClickListener);
    }

    private void setupUserProfile(@NonNull BottomSheetOnlineProfileBinding binding,
                                  @NonNull GuestProfileRoot.User userData) {

        // Load user image
        if (!TextUtils.isEmpty(userData.getImage()) || !TextUtils.isEmpty(userData.getAvatarFrameImage())) {
            binding.userImg.setUserImage(userData.getImage(), userData.getAvatarFrameImage(), 20);
        } else {
            binding.userImg.setUserImage(null, null, 20);
        }

        binding.userName.setText(safeString(userData.getName()));
        binding.userId.setText(safeString(context.getString(R.string.id_) + userData.getUniqueId()));
//        binding.gender.setText(safeString(userData.getGender()));
        binding.tvCountry.setText(safeString(userData.getCountry()));
        binding.tvLevel.setText(userData.getLevel() != null ? safeString(userData.getLevel().getName()) : "");



        if (userData.getLevel() != null && userData.getLevel().getImage() != null) {
            Glide.with(context).load(BuildConfig.BASE_URL + userData.getLevel().getImage()).into(binding.ivLevel);
        }

        String flagUrl = userData.getCountryFlagImage();
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
                        binding.svgWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        binding.svgWebView.setImageDrawable(drawable);
                        binding.svgWebView.invalidate();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        binding.tvType.setVisibility(userData.isAgency() ? VISIBLE : GONE);
        binding.tvHostType.setVisibility(userData.isHost() ? VISIBLE : GONE);
        binding.tvVIPType.setVisibility(userData.isIsVIP() ? VISIBLE : GONE);
        binding.tvCoinseller.setVisibility(userData.isCoinSeller() ? VISIBLE : GONE);


        binding.copy.setOnClickListener(v -> {
            if (userData != null && userData.getUniqueId() != null) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", userData.getUniqueId());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, R.string.copied_successfully, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSeatAndMuteStatus(@NonNull BottomSheetOnlineProfileBinding binding,
                                        @Nullable PkAudioLiveUserRoot.UsersItem.SeatItem seatItem) {
        if (seatItem == null) {
            // Hide seat and mute controls if no seat info
            binding.unMuteMic.setVisibility(View.GONE);
            binding.removeSit.setVisibility(View.GONE);
            binding.kickOut.setVisibility(View.GONE);
            return;
        }

        // Mute/unmute mic text & icon
        boolean isMuted = seatItem.isMute() == 1 || seatItem.isMute() == 2;
        binding.txtMic.setText(isMuted ? R.string.unmute_mic : R.string.mute_mic);
        Glide.with(context)
                .load(isMuted ? R.drawable.speaker_off : R.drawable.speaker)
                .into(binding.mute);

        // Seat action text & icon
        if (seatItem.isReserved()) {
            binding.txtSeat.setText(R.string.remove_mic);
            Glide.with(context).load(R.drawable.remove_sit).into(binding.seat);
        } else {
            binding.txtSeat.setText(R.string.invite_mic);
            Glide.with(context).load(R.drawable.take_sit).into(binding.seat);
        }
    }

    private void setupListeners(@NonNull BottomSheetOnlineProfileBinding binding,
                                @Nullable PkAudioLiveUserRoot.UsersItem.SeatItem seatItem,
                                @NonNull OnClickListener onClickListener) {

        binding.unMuteMic.setOnClickListener(v -> {
            try {
                onClickListener.onUnMute(binding);
            } catch (Exception e) {
                e.printStackTrace();
            }
            bottomSheetDialog.dismiss();
        });

        binding.removeSit.setOnClickListener(v -> {
            try {
                if (seatItem != null && seatItem.isReserved()) {
                    onClickListener.onRemoveSeat();
                } else {
                    onClickListener.inviteUser();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            bottomSheetDialog.dismiss();
        });

        binding.kickOut.setOnClickListener(v -> {
            try {
                onClickListener.onKickOut();
            } catch (Exception e) {
                e.printStackTrace();
            }
            bottomSheetDialog.dismiss();
        });
        

        binding.ivClose.setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
        });
        
    }
    

    private static String safeString(@Nullable String str) {
        return str == null ? "" : str;
    }

    public interface OnClickListener {
        void onUnMute(@NonNull BottomSheetOnlineProfileBinding sheetDialogBinding);

        void onRemoveSeat();

        void onKickOut();

        void inviteUser();
    }
}

