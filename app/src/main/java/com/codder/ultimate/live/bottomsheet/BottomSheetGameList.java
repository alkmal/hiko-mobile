package com.codder.ultimate.live.bottomsheet;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.BottomSheetGameListBinding;
import com.codder.ultimate.live.adapter.GameListAdapter;
import com.codder.ultimate.profile.modelclass.SettingRoot;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class BottomSheetGameList {

    private static final String TAG = "BottomSheetGameList";
    private final SessionManager sessionManager;
    private BottomSheetDialog bottomSheetDialog;

    public BottomSheetGameList(@NonNull Context context, @NonNull OnGameListLister gameListLister) {
        sessionManager = new SessionManager(context);

        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        if (bottomSheetDialog.getWindow() != null) {
            bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        BottomSheetGameListBinding sheetDialogBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.bottom_sheet_game_list,
                null,
                false
        );
        bottomSheetDialog.setContentView(sheetDialogBinding.getRoot());

        GameListAdapter gameListAdapter = new GameListAdapter();
        sheetDialogBinding.rvGameList.setAdapter(gameListAdapter);

        List<SettingRoot.Game> games = null;
        try {
            if (sessionManager.getSetting() != null) {
                games = sessionManager.getSetting().getGame();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting game list from SessionManager", e);
        }

        int size = (games != null) ? games.size() : 0;
        Log.d(TAG, "BottomSheetGameList: getGame == " + size);

        if (games != null && !games.isEmpty()) {
            gameListAdapter.submitList(games);
            gameListAdapter.setClickGameList((gameItem, position) -> {
                if (gameListLister != null && gameItem != null) {
                    gameListLister.onClickGame(gameItem, position);
                }
                dismiss();
            });
        } else {
        }

        sheetDialogBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void dismiss() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
    }

    public interface OnGameListLister {
        void onClickGame(SettingRoot.Game gameItem, int position);
    }
}
