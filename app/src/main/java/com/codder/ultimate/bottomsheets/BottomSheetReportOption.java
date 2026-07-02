package com.codder.ultimate.bottomsheets;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.BottomSheetReportUserBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class BottomSheetReportOption {

    private BottomSheetDialog bottomSheetDialog;
    private BottomSheetReportUserBinding binding;

    public BottomSheetReportOption(@NonNull Context context, @NonNull OnReportedListener onReportedListener) {
        if (context == null || onReportedListener == null) return;

        try {
            initDialog(context, onReportedListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initDialog(Context context, OnReportedListener listener) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_report_user, null, false);

        if (sheetView == null) return;

        binding = DataBindingUtil.bind(sheetView);
        if (binding == null) return;

        bottomSheetDialog.setContentView(binding.getRoot());

        bottomSheetDialog.setOnShowListener(dialog -> {
            Dialog d = (Dialog) dialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        setupListeners(context, listener);
        bottomSheetDialog.show();
    }

    private void setupListeners(Context context, OnReportedListener listener) {
        if (binding.tvTime != null) {
            binding.tvTime.setOnClickListener(v -> {
                dismissDialog();
                listener.onReported();
            });
        }

        View.OnClickListener blockAction = v -> {
            dismissDialog();
            listener.onBlocked();
            if (context != null) {
                Toast.makeText(context, R.string.blocked_successfully, Toast.LENGTH_SHORT).show();
            }
        };

        if (binding.tvCopy != null) {
            binding.tvCopy.setOnClickListener(blockAction);
        }

        if (binding.layBlock != null) {
            binding.layBlock.setOnClickListener(blockAction);
        }

        binding.btnClose.setOnClickListener(v -> dismissDialog());
    }

    private void dismissDialog() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
    }

    public interface OnReportedListener {
        void onReported();

        void onBlocked();
    }
}
