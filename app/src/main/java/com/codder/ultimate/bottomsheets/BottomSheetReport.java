package com.codder.ultimate.bottomsheets;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.BottomSheetReportBinding;
import com.codder.ultimate.dialog.CustomDialogClass;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetReport {

    private static final String TAG = "BottomSheetReport";
    private BottomSheetDialog bottomSheetDialog;
    private CustomDialogClass customDialogClass;
    private boolean submitButtonEnabled = false;

    public BottomSheetReport(@NonNull Context context, @NonNull String otherUserId, @NonNull OnReportedListener onReportedListener) {
        if (context == null || otherUserId == null || otherUserId.isEmpty() || onReportedListener == null) return;

        customDialogClass = new CustomDialogClass(context, R.style.customStyle);
        customDialogClass.setCancelable(false);
        customDialogClass.setCanceledOnTouchOutside(false);

        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_report, null, false);

        if (sheetView == null) return;

        BottomSheetReportBinding binding = DataBindingUtil.bind(sheetView);
        if (binding == null) return;

        bottomSheetDialog.setContentView(binding.getRoot());
        setupDialogBehavior();
        setupListeners(binding, context, otherUserId, onReportedListener);
        bottomSheetDialog.show();
    }

    private void setupDialogBehavior() {
        bottomSheetDialog.setOnShowListener(dialog -> {
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    private void setupListeners(BottomSheetReportBinding binding, Context context, String otherUserId, OnReportedListener listener) {

        binding.btnClose.setOnClickListener(v -> dismissBottomSheet());

        binding.etDes.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                submitButtonEnabled = !s.toString().trim().isEmpty();
                binding.btnSubmit.setBackgroundTintList(ContextCompat.getColorStateList(context,
                        submitButtonEnabled ? R.color.pink : R.color.lightGray1));
                binding.btnSubmit.setTextColor(ContextCompat.getColor(context,
                        submitButtonEnabled ? R.color.white : R.color.black));
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        binding.btnSubmit.setOnClickListener(v -> {
            if (!submitButtonEnabled) return;

            customDialogClass.show();
            JsonObject reportData = new JsonObject();
            String userId = new SessionManager(context).getUser() != null ?
                    new SessionManager(context).getUser().getId() : null;

            if (userId == null || userId.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.user_session_not_found), Toast.LENGTH_SHORT).show();
                customDialogClass.dismiss();
                return;
            }

            reportData.addProperty("toUserId", userId);
            reportData.addProperty("fromUserId", otherUserId);
            reportData.addProperty("description", binding.etDes.getText().toString().trim());

            RetrofitBuilder.create().reportThisUser(reportData).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                    customDialogClass.dismiss();
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        listener.onReported();
                        dismissBottomSheet();
                        Toast.makeText(context, R.string.report_successful, Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMsg = response.body() != null && response.body().getMessage() != null
                                ? response.body().getMessage()
                                : context.getString(R.string.unable_to_report);
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Report failed: " + errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<RestResponse> call, Throwable t) {
                    customDialogClass.dismiss();
                    Log.e("ReportUser", "Network error while reporting user", t);
                    Toast.makeText(context, R.string.something_went_wrong_text, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void dismissBottomSheet() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
    }

    public interface OnReportedListener {
        void onReported();
    }
}
