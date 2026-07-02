package com.codder.ultimate.popups;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.ItemExitLiveBinding;
import com.codder.ultimate.databinding.PopupDeleteBinding;
import com.codder.ultimate.databinding.PopupDeleteChatBinding;
import com.codder.ultimate.databinding.PopupDialogBinding;
import com.codder.ultimate.databinding.PopupDiscardBinding;
import com.codder.ultimate.databinding.PopupExitAppBinding;
import com.codder.ultimate.databinding.PopupLogoutBinding;
import com.codder.ultimate.databinding.PopupPkRequestBinding;
import com.codder.ultimate.databinding.PopupPlayMusicBinding;
import com.codder.ultimate.databinding.PopupPrivacyBinding;
import com.codder.ultimate.databinding.PopupRcoinConvertBinding;
import com.codder.ultimate.databinding.PopupRemoveBinding;
import com.codder.ultimate.databinding.PopupWithVectorBinding;
import com.codder.ultimate.musicfunction.AudioDetails;
import com.codder.ultimate.musicfunction.AudioMixingController;
import com.codder.ultimate.retrofit.Const;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import io.agora.rtc2.RtcEngine;

public class PopupBuilder {
    SessionManager sessionManager;
    private final Context mContext;
    Dialog mBuilder;

    public PopupBuilder(Context context) {
        this.mContext = context;
        if (mContext != null) {
            sessionManager = new SessionManager(context);
            mBuilder = new Dialog(mContext);

            mBuilder.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mBuilder.setCancelable(false);
            mBuilder.setCanceledOnTouchOutside(false);
            if (mBuilder != null && mBuilder.getWindow() != null) {
                mBuilder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    public void showSimplePopup(String Text, String btnContinue, OnPopupClickListener onPopupClickListener) {
        if (mContext == null)
            return;
        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(true);
        PopupDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.popup_dialog, null, false);
        mBuilder.setContentView(binding.getRoot());
        binding.tvText.setText(Text);
        binding.btnContinue.setText(btnContinue);
        binding.btnContinue.setOnClickListener(v -> {
            mBuilder.dismiss();
            if (onPopupClickListener != null) {
                onPopupClickListener.onClickContinue();
            }
        });
        mBuilder.show();

    }

    public void showSimplePopup(String Text,int image, String btnContinue, OnPopupClickListener onPopupClickListener) {
        if (mContext == null)
            return;
        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(true);
        PopupDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.popup_dialog, null, false);
        mBuilder.setContentView(binding.getRoot());
        binding.tvText.setText(Text);
        binding.btnContinue.setText(btnContinue);
        Glide.with(mContext).load(image).into(binding.ivVector);
        binding.btnContinue.setOnClickListener(v -> {
            mBuilder.dismiss();
            if (onPopupClickListener != null) {
                onPopupClickListener.onClickContinue();
            }
        });
        mBuilder.show();

    }

    public void showPopUpWithVector(int imageView, String Title, String Text, String btnContinue, OnPopupClickListener onPopupClickListener) {
        if (mContext == null)
            return;
        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(true);
        PopupWithVectorBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.popup_with_vector, null, false);
        mBuilder.setContentView(binding.getRoot());
        binding.ivVector.setImageResource(imageView);
        binding.tvTitle.setText(Title);
        binding.tvText.setText(Text);
        binding.btnContinue.setText(btnContinue);
        binding.btnContinue.setOnClickListener(v -> {
            mBuilder.dismiss();
            if (onPopupClickListener != null) {
                onPopupClickListener.onClickContinue();
            }

        });
        mBuilder.show();

    }

    public void PrivacyPopup(OnSubmitClickListener onSubmitClickListener) {
        sessionManager = new SessionManager(mContext);
        mBuilder = new Dialog(mContext, R.style.customStyle);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        PopupPrivacyBinding popupPrivacyBinding = DataBindingUtil.inflate(inflater, R.layout.popup_privacy, null, false);
        mBuilder.setCancelable(false);
        mBuilder.setContentView(popupPrivacyBinding.getRoot());
        popupPrivacyBinding.textview.setText(sessionManager.getSetting().getPrivacyPolicyText());

        popupPrivacyBinding.tvContinue.setOnClickListener(v -> {
            if (popupPrivacyBinding.checkbox.isChecked()) {
                mBuilder.dismiss();
                onSubmitClickListener.onAccept();
            } else {
                Toast.makeText(mContext, R.string.please_accept_privacy_policy, Toast.LENGTH_SHORT).show();
            }


        });
        popupPrivacyBinding.tvCancel.setOnClickListener(v -> {
            mBuilder.dismiss();
            onSubmitClickListener.onDeny();

        });

        mBuilder.show();

    }

    public void showExitPopup(OnRemovePopupClickListener listener) {
        if (mContext == null) return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(true);
        PopupExitAppBinding popupExitAppBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.popup_exit_app, null, false);
        mBuilder.setContentView(popupExitAppBinding.getRoot());

        Window window = mBuilder.getWindow();
        if (window != null) {
            int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            int horizontalMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics());

            window.setLayout(screenWidth - (horizontalMargin * 2), ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }


        popupExitAppBinding.btnContinue.setOnClickListener(view -> {
            mBuilder.dismiss();
            listener.onClickSure();
        });
        popupExitAppBinding.btnCancel.setOnClickListener(view -> {
            mBuilder.dismiss();
        });
        mBuilder.show();

    }

    public void deletePopup(String text, OnMultiButtonPopupLister onPopupClickListener) {
        if (mContext == null) return;

        mBuilder.setCancelable(false);
        PopupDeleteBinding popupDeletePostBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.popup_delete, null, false);
        mBuilder.setContentView(popupDeletePostBinding.getRoot());

        popupDeletePostBinding.tvText.setText(text);

        popupDeletePostBinding.yes.setOnClickListener(v -> {
            mBuilder.dismiss();
            onPopupClickListener.onClickContinue();
        });
        popupDeletePostBinding.no.setOnClickListener(view -> {
            mBuilder.dismiss();
            onPopupClickListener.onClickCancel();
        });
        mBuilder.show();
    }

    public void showDeleteChatPopup(int imageView, String s1, String s2, String btn1, String btn2, OnPopupClickListener onPopupClickListener) {
        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(true);
        PopupDeleteChatBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.popup_delete_chat, null, false);
        mBuilder.setContentView(binding.getRoot());
        binding.tvText.setText(s1);
        binding.tvText2.setText(s2);
        binding.ivVector.setImageResource(imageView);
        binding.btnContinue.setText(btn1);
        binding.btnCancel.setText(btn2);
        binding.btnCancel.setOnClickListener(v -> mBuilder.dismiss());
        binding.btnContinue.setOnClickListener(v -> {
            mBuilder.dismiss();
            onPopupClickListener.onClickContinue();
        });
        if (s1.isEmpty()) {
            binding.tvText.setVisibility(GONE);
        }
        if (s2.isEmpty()) {
            binding.tvText2.setVisibility(GONE);
        }
        if (btn1.isEmpty()) {
            binding.btnContinue.setVisibility(GONE);
        }
        if (btn2.isEmpty()) {
            binding.btnCancel.setVisibility(GONE);
        }
        mBuilder.show();

    }

    public void showReliteDiscardPopup(int imageView, String s1, String s2, String btn1, String btn2, OnPopupClickListener onPopupClickListener) {
        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(true);
        PopupDiscardBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.popup_discard, null, false);
        mBuilder.setContentView(binding.getRoot());
        binding.tvText.setText(s1);
        binding.tvText2.setText(s2);
        binding.ivVector.setImageResource(imageView);
        binding.btnContinue.setText(btn1);
        binding.btnCancel.setText(btn2);
        binding.btnCancel.setOnClickListener(v -> mBuilder.dismiss());
        binding.btnContinue.setOnClickListener(v -> {
            mBuilder.dismiss();
            onPopupClickListener.onClickContinue();
        });
        if (s1.isEmpty()) {
            binding.tvText.setVisibility(GONE);
        }
        if (s2.isEmpty()) {
            binding.tvText2.setVisibility(GONE);
        }
        if (btn1.isEmpty()) {
            binding.btnContinue.setVisibility(GONE);
        }
        if (btn2.isEmpty()) {
            binding.btnCancel.setVisibility(GONE);
        }
        mBuilder.show();

    }

    public void showLogoutPopup(int imageView, String s1, String s2, String btn1, String btn2, OnPopupClickListener onPopupClickListener) {
        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(true);
        PopupLogoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.popup_logout, null, false);
        mBuilder.setContentView(binding.getRoot());
        binding.tvText.setText(s1);
        binding.tvText2.setText(s2);
        binding.ivVector.setImageResource(imageView);
        binding.btnContinue.setText(btn1);
        binding.btnCancel.setText(btn2);
        binding.btnCancel.setOnClickListener(v -> mBuilder.dismiss());
        binding.btnContinue.setOnClickListener(v -> {
            mBuilder.dismiss();
            onPopupClickListener.onClickContinue();
        });
        if (s1.isEmpty()) {
            binding.tvText.setVisibility(GONE);
        }
        if (s2.isEmpty()) {
            binding.tvText2.setVisibility(GONE);
        }
        if (btn1.isEmpty()) {
            binding.btnContinue.setVisibility(GONE);
        }
        if (btn2.isEmpty()) {
            binding.btnCancel.setVisibility(GONE);
        }
        mBuilder.show();

    }

    public void showRemovePopup(OnRemovePopupClickListener onRemovePopupClickListener) {
        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(true);
        PopupRemoveBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.popup_remove, null, false);
        mBuilder.setContentView(binding.getRoot());

        binding.btnSure.setOnClickListener(v -> {
            mBuilder.dismiss();
            onRemovePopupClickListener.onClickSure();
        });

        binding.btnCancel.setOnClickListener(v -> mBuilder.dismiss());

        mBuilder.show();

    }

    public void showPkRequestPopup(String title, String userImage, String avatarFrame, String btnPositive, String btnNegative, OnMultiButtonPopupLister onPopupClickListener) {
        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(true);
        PopupPkRequestBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.popup_pk_request, null, false);
        mBuilder.setContentView(binding.getRoot());

        binding.imgUser.setUserImage(userImage, avatarFrame, 30);

        binding.tvText.setText(title);
        binding.btnContinue.setText(btnPositive);
        binding.btnContinue.setOnClickListener(v -> {
            mBuilder.dismiss();
            onPopupClickListener.onClickContinue();
        });

        binding.btnCancel.setText(btnNegative);
        binding.btnCancel.setOnClickListener(v -> {
            mBuilder.dismiss();

            onPopupClickListener.onClickCancel();
        });
        mBuilder.show();

    }

    public void showLiveEndPopup(OnMultiButtonPopupLister listener) {
        if (mContext == null) return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(true);
        ItemExitLiveBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_exit_live, null, false);
        mBuilder.setContentView(binding.getRoot());
        mBuilder.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        binding.ivKeep.setOnClickListener(v -> {
            mBuilder.dismiss();
            listener.onClickCancel();
        });
        binding.ivExit.setOnClickListener(v -> {
            mBuilder.dismiss();
            listener.onClickContinue();
        });
        mBuilder.show();
    }

    public void showLiveEndPopup(String s, String btnText, OnPopupClickListener onPopupClickListener) {
        if (mContext == null)
            return;

        mBuilder.setCancelable(false);
        mBuilder.setCanceledOnTouchOutside(false);
        PopupDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.popup_dialog, null, false);
        mBuilder.setContentView(binding.getRoot());
        binding.tvText.setText(s);
        binding.btnContinue.setText(btnText);
        binding.btnContinue.setOnClickListener(v -> {
            mBuilder.dismiss();
            onPopupClickListener.onClickContinue();
        });
        mBuilder.show();

    }

    public interface OnPopupClickListener {
        void onClickContinue();

    }

    public interface OnSubmitClickListener {
        void onAccept();

        void onDeny();

    }

    public interface OnRemovePopupClickListener {
        void onClickSure();

    }

    public interface OnMultiButtonPopupLister {
        void onClickContinue();

        void onClickCancel();
    }

    public void showRcoinConvertPopup(boolean isCashOut, double maxCoin, OnRcoinConvertPopupClickListener onRcoinConvertPopupClickListener) {
        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(true);
        PopupRcoinConvertBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.popup_rcoin_convert, null, false);
        mBuilder.setContentView(binding.getRoot());
        final int[] coin = new int[1];


        if (isCashOut) {
            binding.tvText.setText(R.string.how_much_rcoins_convert_cash);
            binding.btnContinue.setText(R.string.cash_out);

        } else {
            binding.tvText.setText(R.string.how_much_rcoins_convert_into_diamonds);
            binding.btnContinue.setText(R.string.convert_to_diamond);
        }


        binding.tvDiamondsValue.setVisibility(GONE);
        binding.etRcoin.addTextChangedListener(new TextWatcher() {


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (sessionManager.getSetting().getRCoinForDiamond() == 0) {
                    Toast.makeText(mContext, R.string.setting_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                double rCoinForDiamond = sessionManager.getSetting().getRCoinForDiamond();

                if (binding.tvDiamondsValue.getText().toString().isEmpty()){

                }else {

                }
                if (!s.toString().isEmpty()) {
                    binding.tvDiamondsValue.setVisibility(VISIBLE);
                    try {
                        coin[0] = Integer.parseInt(s.toString());
                    } catch (NumberFormatException ex) { // handle your exception
                    }
                    if (coin[0] < rCoinForDiamond) {
                        binding.tvDiamondsValue.setText(mContext.getString(R.string.minimum_amount_is) + RayziUtils.formatCoin(rCoinForDiamond) + Const.CoinName);
                        binding.tvDiamondsValue.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                        new Handler(Looper.getMainLooper()).postDelayed(() -> binding.tvDiamondsValue.setTextColor(ContextCompat.getColor(mContext, R.color.yellow)), 1000);

                    } else if (coin[0] > maxCoin) {
                        binding.tvDiamondsValue.setText(mContext.getString(R.string.you_not_have_enough) + Const.CoinName);
                        binding.tvDiamondsValue.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                        new Handler(Looper.getMainLooper()).postDelayed(() -> binding.tvDiamondsValue.setTextColor(ContextCompat.getColor(mContext, R.color.yellow)), 1000);
                    } else {
                        double diamond = coin[0] / rCoinForDiamond;
                        String formatted = String.format(Locale.US, "%,.2f", diamond);

                        String resultText = isCashOut
                                ? mContext.getString(R.string.you_will_receive) + formatted + " " + Const.getCurrency()
                                : mContext.getString(R.string.you_will_receive) + formatted + mContext.getString(R.string._diamonds);

                        binding.tvDiamondsValue.setText(resultText);

                    }
                }else {
                    binding.tvDiamondsValue.setVisibility(GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.btnContinue.setOnClickListener(v -> {
            mBuilder.dismiss();
            onRcoinConvertPopupClickListener.onClickConvert(coin[0]);
        });
        binding.btnCancel.setOnClickListener(v -> mBuilder.dismiss());
        mBuilder.show();

        Window window = mBuilder.getWindow();
        if (window != null) {
            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }


    }

    public interface OnRcoinConvertPopupClickListener {
        void onClickConvert(int rCoin);
    }

    public interface OnAddMusicListener {
        void onAddMusicClick();
    }

    public void playMusicPopup(ArrayList<AudioDetails> selectedSongs, RtcEngine rtcEngine, OnAddMusicListener listener) {
        PopupPlayMusicBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.popup_play_music, null, false);
        mBuilder.setContentView(binding.getRoot());
        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(true);

      /*  int currentIndex = AudioMixingController.getInstance().getCurrentIndex();
        if (currentIndex >= 0 && currentIndex < selectedSongs.size()) {
            Log.d("====>TAG", "playMusicPopup: " + selectedSongs.get(currentIndex).getName());
        } else {
            Log.w("====>TAG", "Invalid index: " + currentIndex + ", list size: " + selectedSongs.size());
        }*/

        Window window = mBuilder.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.BOTTOM;
            params.y = 20;
            window.setAttributes(params);
            window.setWindowAnimations(R.style.BottomDialogAnimation);
        }

        binding.ivAdd.setOnClickListener(v -> {
            if (AudioMixingController.getInstance().isPlaying()) {
                AudioMixingController.getInstance().stop();
            }
            listener.onAddMusicClick();
            mBuilder.dismiss();
//            mContext.startActivity(new Intent(mContext, AddMusicActivity.class));
        });

        binding.ivplay.setOnClickListener(v -> {
            if (!selectedSongs.isEmpty()) {
                AudioMixingController.getInstance().togglePlayPause();
            } else {
                Toast.makeText(mContext, "Empty list", Toast.LENGTH_SHORT).show();
            }
        });

//        binding.ivSound.setOnClickListener(v -> {
//            if (binding.soundSeekbar.getVisibility() == View.VISIBLE) {
//                binding.soundSeekbar.setVisibility(View.GONE);
//                binding.ivSound.setImageDrawable(AppCompatResources.getDrawable(mContext, R.drawable.ic_volume_off));
//                AudioMixingController.getInstance().setVolume(0);
//            } else {
//                binding.soundSeekbar.setVisibility(View.VISIBLE);
//                binding.ivSound.setImageDrawable(AppCompatResources.getDrawable(mContext, R.drawable.ic_volume_up));
//                AudioMixingController.getInstance().setVolume(100);
//            }
//        });

        binding.soundSeekbar.setMax(100);
        binding.soundSeekbar.setProgress(AudioMixingController.getInstance().getVolume());
        AudioMixingController.getInstance().setVolume(AudioMixingController.getInstance().getVolume());

        binding.soundSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    AudioMixingController.getInstance().setVolume(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.ivNext.setOnClickListener(v -> {
            AudioMixingController.getInstance().next();
        });
        binding.ivPrevious.setOnClickListener(v -> {
            AudioMixingController.getInstance().previous();
        });

        binding.seekbar.setMax((int) (rtcEngine.getAudioMixingDuration()));
        binding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    AudioMixingController.getInstance().seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        AudioMixingController.getInstance().setAudioMixingListner(new AudioMixingController.AudioMixingListner() {
            @Override
            public void isPlaying(boolean isPlaying) {
                Log.d("TAG", "isPlaying: ");
                binding.ivplay.setImageResource(isPlaying ? R.drawable.ic_music_puase : R.drawable.ic_music_play);
            }

            @Override
            public void currentProgress(long progress, long totalDuration) {
                Log.d("TAG", "currentProgress: " + progress);

                binding.txtstarttime.setText(formatTime(progress));
                binding.txttotaltime.setText(formatTime(totalDuration));
                binding.seekbar.setMax((int) totalDuration);
                binding.seekbar.setProgress((int) progress);
            }

            @Override
            public void onAudioTitleUpdated(String title) {
                binding.tvTitle.setText(title);
            }
        });

        if (!AudioMixingController.getInstance().isLoaded()) {
            AudioMixingController.getInstance().load(selectedSongs);
        }


        for (int i = 0; i < selectedSongs.size(); i++) {
            Log.d("TAG========> ", "Song[" + i + "]: " + selectedSongs.get(i).getSongPath());
        }


        if (AudioMixingController.getInstance().isPlaying()) {
            binding.ivplay.setImageResource(R.drawable.ic_music_puase);
        } else {
            binding.ivplay.setImageResource(R.drawable.ic_music_play);
        }

        int currentIndex = AudioMixingController.getInstance().getCurrentIndex();
        if (AudioMixingController.getInstance().isLoaded() && currentIndex >= 0 && currentIndex < selectedSongs.size()) {
            String currentTitle = selectedSongs.get(currentIndex).getName();
            binding.tvTitle.setText(currentTitle);
        }


        binding.ivClose.setOnClickListener(v -> {
            AudioMixingController.getInstance().stop();
            mBuilder.dismiss();
        });

        mBuilder.show();
    }

    private String formatTime(long milliseconds) {
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

}
