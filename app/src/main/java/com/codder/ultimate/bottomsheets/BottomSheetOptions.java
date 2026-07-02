package com.codder.ultimate.bottomsheets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.BottomSheetAudioBgBinding;
import com.codder.ultimate.live.adapter.BackgroundAdapter;
import com.codder.ultimate.live.model.ThemeRoot;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetOptions {

    BottomSheetDialog bottomSheetDialog;
    BackgroundAdapter backgroundAdapter;
    Context context;


    public BottomSheetOptions(Context context, OnClickListener onClickListener) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        this.context = context;

        bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        BottomSheetAudioBgBinding sheetDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet_audio_bg, null, false);
        bottomSheetDialog.setContentView(sheetDialogBinding.getRoot());
        bottomSheetDialog.show();

        backgroundAdapter = new BackgroundAdapter();

        sheetDialogBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        getTheme(sheetDialogBinding);

        backgroundAdapter.setOnImageClickListener(themeItem -> {
            onClickListener.OnImage(themeItem);
            bottomSheetDialog.dismiss();
        });
    }

    private void getTheme(BottomSheetAudioBgBinding sheetBinding) {
        sheetBinding.shimmer.setVisibility(View.VISIBLE);
        sheetBinding.rvAudioBg.setVisibility(View.GONE);
        sheetBinding.layoutNoData.setVisibility(View.GONE);

        Call<ThemeRoot> call = RetrofitBuilder.create().getTheme();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ThemeRoot> call, Response<ThemeRoot> response) {
                sheetBinding.shimmer.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getTheme() != null) {
                    if (!response.body().getTheme().isEmpty()) {
                        backgroundAdapter.submitList(response.body().getTheme());
                        sheetBinding.rvAudioBg.setAdapter(backgroundAdapter);
                        sheetBinding.rvAudioBg.setVisibility(View.VISIBLE);
                        sheetBinding.layoutNoData.setVisibility(View.GONE);
                    } else {
                        sheetBinding.rvAudioBg.setVisibility(View.GONE);
                        sheetBinding.layoutNoData.setVisibility(View.VISIBLE);
                    }
                } else {
                    sheetBinding.rvAudioBg.setVisibility(View.GONE);
                    sheetBinding.layoutNoData.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ThemeRoot> call, Throwable t) {
                sheetBinding.shimmer.setVisibility(View.GONE);
                sheetBinding.rvAudioBg.setVisibility(View.GONE);
                sheetBinding.layoutNoData.setVisibility(View.VISIBLE);
            }
        });
    }


    public interface OnClickListener {
        void OnImage(String image);
    }
}
