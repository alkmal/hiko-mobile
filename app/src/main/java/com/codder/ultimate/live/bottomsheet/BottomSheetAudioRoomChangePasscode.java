package com.codder.ultimate.live.bottomsheet;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.BottomSheetAudioroomChangepasscodeBinding;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetAudioRoomChangePasscode {

    private static final String TAG = "BottomSheetAudioRoomChangePasscode";
    private final BottomSheetDialog bottomSheetDialog;
    OnRoomPasscodeSubmittedListener listener;
    SessionManager sessionManager;


    public BottomSheetAudioRoomChangePasscode(Context context, PkAudioLiveUserRoot.UsersItem liveUser, OnRoomPasscodeSubmittedListener listener) {
        this.listener = listener;
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet == null) return;

        });

        BottomSheetAudioroomChangepasscodeBinding audioRoomPasscodeBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet_audioroom_changepasscode, null, false);
        bottomSheetDialog.setContentView(audioRoomPasscodeBinding.getRoot());
        sessionManager = new SessionManager(context);
        bottomSheetDialog.show();


        audioRoomPasscodeBinding.tvSubmit.setOnClickListener(v -> {

            String roomPasscode = audioRoomPasscodeBinding.etPasscode.getText().toString();

            if (roomPasscode.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.passcode_empty_error), Toast.LENGTH_SHORT).show();
                return;
            }

            listener.OnRoomPasscodeSubmitted(roomPasscode);

            Call<RestResponse> call = RetrofitBuilder.create().updatePasscode(roomPasscode, liveUser.getLiveUserId());
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RestResponse> call, @NonNull Response<RestResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().isStatus()) {
                            Toast.makeText(context, context.getString(R.string.room_passcode_changed), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Passcode updated successfully.");
                        } else {
                            Log.w(TAG, "Failed to update passcode: " + response.body().getMessage());
                            Toast.makeText(context, context.getString(R.string.failed_to_update_passcode), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Unsuccessful response or no body returned. Code: " + response.code());
                        Toast.makeText(context, context.getString(R.string.failed_to_update_passcode), Toast.LENGTH_SHORT).show();
                    }
                    bottomSheetDialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<RestResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "Error updating passcode", t);
                    Toast.makeText(context, context.getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                }
            });
            bottomSheetDialog.dismiss();

        });

        audioRoomPasscodeBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

    }

    public interface OnRoomPasscodeSubmittedListener {
        void OnRoomPasscodeSubmitted(String RoomPassCode);
    }
}
