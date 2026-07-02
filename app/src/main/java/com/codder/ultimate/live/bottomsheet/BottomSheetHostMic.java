package com.codder.ultimate.live.bottomsheet;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.BottomSheetHostMicBinding;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class BottomSheetHostMic {

    BottomSheetDialog bottomSheetDialog;
    Context context;


    public BottomSheetHostMic(Context context, PkAudioLiveUserRoot.UsersItem.SeatItem seatItem, OnClickListener onClickListener) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        this.context = context;

        bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        BottomSheetHostMicBinding sheetDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet_host_mic, null, false);
        bottomSheetDialog.setContentView(sheetDialogBinding.getRoot());
        bottomSheetDialog.show();

        if (seatItem.isMute() == 1 || seatItem.isMute() == 2) {
            sheetDialogBinding.tvMute.setText(R.string.unmute);
        }
        if (seatItem.isLock()) {
            sheetDialogBinding.tvLock.setText(R.string.unlock);
        }
        if (seatItem.isReserved()) {
            sheetDialogBinding.tvGiveRemove.setText(R.string.remove);
        }
        sheetDialogBinding.takeMic.setOnClickListener(v -> {
            onClickListener.onTakeMic();
            bottomSheetDialog.dismiss();
        });
        sheetDialogBinding.tvGiveRemove.setOnClickListener(v -> {
            if (seatItem.isReserved()) {
                onClickListener.onClickRemove();
            } else {
                onClickListener.onGiveMic();
            }
            bottomSheetDialog.dismiss();

        });
        sheetDialogBinding.tvLock.setOnClickListener(v -> {
            onClickListener.onLockMic();
            bottomSheetDialog.dismiss();
        });
        sheetDialogBinding.tvMute.setOnClickListener(v -> {
            onClickListener.onMuteMic();
            bottomSheetDialog.dismiss();
        });

        sheetDialogBinding.cancel.setOnClickListener(v -> {
            onClickListener.onCancelClick();
            bottomSheetDialog.dismiss();
        });


    }


    public interface OnClickListener {
        void onTakeMic();

        void onGiveMic();

        void onLockMic();

        void onMuteMic();

        void onCancelClick();

        void onClickRemove();
    }


}
