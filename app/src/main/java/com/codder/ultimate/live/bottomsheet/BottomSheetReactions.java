package com.codder.ultimate.live.bottomsheet;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.BottomSheetReactionsBinding;
import com.codder.ultimate.live.adapter.ReactionGridAdapter;
import com.codder.ultimate.live.model.ReactionRoot;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class BottomSheetReactions {

    private static final String TAG = "BottomSheetReactions";

    private final BottomSheetDialog bottomSheetDialog;
    private final BottomSheetReactionsBinding binding;
    private final ReactionGridAdapter reactionGridAdapter;

    private OnReactionClickListener onReactionClickListener;
    private final Context appContext;

    public BottomSheetReactions(@NonNull Context context) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        appContext = context.getApplicationContext();

        if (bottomSheetDialog.getWindow() != null) {
            bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet_reactions, null, false);
        bottomSheetDialog.setContentView(binding.getRoot());

        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                Log.w(TAG, "Bottom sheet FrameLayout is null!");
            }
        });

        reactionGridAdapter = new ReactionGridAdapter();
        binding.rvEmoji.setAdapter(reactionGridAdapter);

        binding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

        reactionGridAdapter.setOnReactionClickListener(reaction -> {
            if (onReactionClickListener != null && reaction != null) {
                onReactionClickListener.onReactionClick(reaction);
            }
            if (bottomSheetDialog.isShowing()) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    public void setOnReactionClickListener(OnReactionClickListener listener) {
        this.onReactionClickListener = listener;
    }

    public OnReactionClickListener getOnReactionClickListener() {
        return onReactionClickListener;
    }

    public void loadData(List<ReactionRoot.DataItem> data) {
        if (data == null) {
            Log.w(TAG, "loadData called with null data list.");
            return;
        }
        preloadImages(data);
        reactionGridAdapter.submitList(data);

    }

    public void show() {
        if (!bottomSheetDialog.isShowing()) {
            bottomSheetDialog.show();
        } else {
            Log.d(TAG, "BottomSheetDialog already showing.");
        }
    }

    public interface OnReactionClickListener {
        void onReactionClick(@NonNull ReactionRoot.DataItem reaction);
    }

    private void preloadImages(List<ReactionRoot.DataItem> data) {
        for (ReactionRoot.DataItem item : data) {
            String imageUrl = item.getImage();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Preload each image into the cache
                Glide.with(appContext)
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)  // Cache both original and transformed images
                        .preload();  // Preload the image into the cache
            }
        }
    }
}

