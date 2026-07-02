package com.codder.ultimate.live.bottomsheet;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.BottomSheetBannedListBinding;
import com.codder.ultimate.live.adapter.BannedListAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;

public class BottomSheetBannedList {

    private static final String TAG = "BottomSheetBannedList";
    private final BottomSheetDialog bottomSheetDialog;
    private final BannedListAdapter bannedListAdapter;

    public BottomSheetBannedList(@NonNull Context context,
                                 @Nullable JSONArray jsonArray,
                                 @NonNull OnclickListener onclickListener) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
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

        BottomSheetBannedListBinding bottomSheetBannedListBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.bottom_sheet_banned_list,
                null, false);

        bottomSheetDialog.setContentView(bottomSheetBannedListBinding.getRoot());

        bottomSheetBannedListBinding.rvBannedList.setLayoutManager(new LinearLayoutManager(context));
        bannedListAdapter = new BannedListAdapter(context, (id, position) -> {
            try {
                onclickListener.onUnblockClick(id);
                if (jsonArray != null && position >= 0 && position < jsonArray.length()) {
                    jsonArray.remove(position);
                }
                Toast.makeText(context, "Unblocked Successfully!!", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Error on unblock", e);
            }
        });

        Log.d(TAG, "BottomSheetBannedList: data = " + jsonArray);
        bottomSheetBannedListBinding.progressBar.setVisibility(android.view.View.VISIBLE);
        bottomSheetBannedListBinding.rvBannedList.setVisibility(android.view.View.GONE);
        bottomSheetBannedListBinding.lytNoData.lytNoData.setVisibility(android.view.View.GONE);

        if (jsonArray != null && jsonArray.length() > 0) {
            bannedListAdapter.addData(jsonArray);
            bottomSheetBannedListBinding.rvBannedList.setAdapter(bannedListAdapter);
            bottomSheetBannedListBinding.progressBar.setVisibility(android.view.View.GONE);
            bottomSheetBannedListBinding.rvBannedList.setVisibility(android.view.View.VISIBLE);
        } else {
            bottomSheetBannedListBinding.rvBannedList.setAdapter(null);
            bottomSheetBannedListBinding.progressBar.setVisibility(android.view.View.GONE);
            bottomSheetBannedListBinding.lytNoData.lytNoData.setVisibility(android.view.View.VISIBLE);
        }

        bottomSheetBannedListBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    public interface OnclickListener {
        void onUnblockClick(String id);
    }
}

