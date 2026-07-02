package com.codder.ultimate.bottomsheets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.BottomSheetCallTypeBinding;
import com.codder.ultimate.databinding.BottomSheetReportBinding;
import com.codder.ultimate.dialog.CustomDialogClass;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class BottomsheetCallType {
    private BottomSheetDialog bottomSheetDialog;

    public BottomsheetCallType(@NonNull Context context,OnCallTypeListener onCallTypeListener) {

        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_call_type, null, false);


        BottomSheetCallTypeBinding binding = DataBindingUtil.bind(sheetView);
        if (binding == null) return;

        bottomSheetDialog.setContentView(binding.getRoot());
        bottomSheetDialog.show();


        binding.btnClose.setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
        });

        binding.layAudio.setOnClickListener(view -> {
            onCallTypeListener.onAudioCall();
            bottomSheetDialog.dismiss();
        });

        binding.layVideo.setOnClickListener(view -> {
            onCallTypeListener.onVideoCall();
            bottomSheetDialog.dismiss();
        });

    }

    public interface OnCallTypeListener {
        void onAudioCall();
        void onVideoCall();
    }

}
