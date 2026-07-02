package com.codder.ultimate.live.bottomsheet;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.BottomsheetAudioroomWheatmodeBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class BottomSheetAudioRoomWheatMode {

    private final BottomSheetDialog bottomSheetDialog;

    public BottomSheetAudioRoomWheatMode(Context context, int size, OnSeatClickListener onSeatClickListener) {

        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);

        BottomsheetAudioroomWheatmodeBinding audioRoomSettingsBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottomsheet_audioroom_wheatmode, null, false);
        bottomSheetDialog.setContentView(audioRoomSettingsBinding.getRoot());

        View bottomSheet = (View) audioRoomSettingsBinding.getRoot().getParent();
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);

        // Let content define height; or set a max height if you prefer a cap
        behavior.setFitToContents(false);
        behavior.setExpandedOffset((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, context.getResources().getDisplayMetrics()));
        behavior.setHalfExpandedRatio(0.6f);  // optional, if you want a stable mid state
        behavior.setGestureInsetBottomIgnored(true); // avoids gesture-inset jumps on Q+
        bottomSheetDialog.show();

        if (size == 8) {
            audioRoomSettingsBinding.laySelect8.setBackgroundColor(Color.parseColor("#BA3A7E"));
            audioRoomSettingsBinding.laySelect12.setBackgroundColor(Color.parseColor("#80000000"));
            audioRoomSettingsBinding.laySelect16.setBackgroundColor(Color.parseColor("#80000000"));
            audioRoomSettingsBinding.tvSeatSelect8.setText(R.string.using);
        } else if (size == 12) {
            audioRoomSettingsBinding.laySelect8.setBackgroundColor(Color.parseColor("#80000000"));
            audioRoomSettingsBinding.laySelect12.setBackgroundColor(Color.parseColor("#BA3A7E"));
            audioRoomSettingsBinding.laySelect16.setBackgroundColor(Color.parseColor("#80000000"));
            audioRoomSettingsBinding.tvSeatSelect12.setText(R.string.using);
        } else if (size == 15) {
            audioRoomSettingsBinding.laySelect16.setBackgroundColor(Color.parseColor("#BA3A7E"));
            audioRoomSettingsBinding.laySelect8.setBackgroundColor(Color.parseColor("#80000000"));
            audioRoomSettingsBinding.laySelect12.setBackgroundColor(Color.parseColor("#80000000"));
            audioRoomSettingsBinding.tvSeatSelect16.setText(R.string.using);
        }

        audioRoomSettingsBinding.lay8Seat.setOnClickListener(v -> {
            onSeatClickListener.onSeatClick(8);
            bottomSheetDialog.dismiss();
        });

        audioRoomSettingsBinding.lay12Seat.setOnClickListener(v -> {
            onSeatClickListener.onSeatClick(12);
            bottomSheetDialog.dismiss();
        });

        audioRoomSettingsBinding.lay16Seat.setOnClickListener(v -> {
            onSeatClickListener.onSeatClick(15);
            bottomSheetDialog.dismiss();
        });

        audioRoomSettingsBinding.btnClose.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });
    }

    public interface OnSeatClickListener {
        void onSeatClick(int seatCount);
    }
}