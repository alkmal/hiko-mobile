package com.codder.ultimate.live.bottomsheet;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.BottomsheetAudiorromPasscodeUpdateBinding;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class BottomSheetAudioRoomPasscode {

    private final BottomSheetDialog bottomSheetDialog;
    SessionManager sessionManager;

    public BottomSheetAudioRoomPasscode(Context context, PkAudioLiveUserRoot.UsersItem liveUser, OnWelcomeMessageSubmittedListener listener) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        BottomsheetAudiorromPasscodeUpdateBinding audioRoomWelcomeMsgBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottomsheet_audiorrom_passcode_update, null, false);
        bottomSheetDialog.setContentView(audioRoomWelcomeMsgBinding.getRoot());
        sessionManager = new SessionManager(context);
        bottomSheetDialog.show();

        audioRoomWelcomeMsgBinding.tvRoomName.setText(liveUser.getRoomName());

        if (liveUser.isIsFake()) {
            audioRoomWelcomeMsgBinding.etPasscode.setText(R.string._123456);
        }

        audioRoomWelcomeMsgBinding.tvSubmit.setOnClickListener(v -> {
            if (!audioRoomWelcomeMsgBinding.etPasscode.getText().toString().isEmpty()) {
                if (liveUser.getPrivateCode() == Integer.parseInt(audioRoomWelcomeMsgBinding.etPasscode.getText().toString())) {
                    listener.OnWelcomeMessageSubmitted(Integer.parseInt(audioRoomWelcomeMsgBinding.etPasscode.getText().toString()));
                    bottomSheetDialog.dismiss();
                } else {
                    audioRoomWelcomeMsgBinding.etPasscode.setError(context.getString(R.string.enter_valid_passcode));
                }
            } else {
                audioRoomWelcomeMsgBinding.etPasscode.setError(context.getString(R.string.enter_passcode));
            }
        });

        audioRoomWelcomeMsgBinding.btnClose.setOnClickListener(view -> {
            listener.OnWelcomeMessageSubmitted(0);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setOnCancelListener(dialogInterface -> {
            listener.OnWelcomeMessageSubmitted(0);
            bottomSheetDialog.dismiss();
        });

        audioRoomWelcomeMsgBinding.tvSubmit.setOnClickListener(v -> {
            if (!audioRoomWelcomeMsgBinding.etPasscode.getText().toString().isEmpty()) {
                if (liveUser.getPrivateCode() == Integer.parseInt(audioRoomWelcomeMsgBinding.etPasscode.getText().toString())) {
                    listener.OnWelcomeMessageSubmitted(Integer.parseInt(audioRoomWelcomeMsgBinding.etPasscode.getText().toString()));
                    bottomSheetDialog.dismiss();
                } else {
                    audioRoomWelcomeMsgBinding.etPasscode.setError(context.getString(R.string.enter_valid_passcode));
                }
            } else {
                audioRoomWelcomeMsgBinding.etPasscode.setError(context.getString(R.string.enter_passcode));
            }
        });

    }

    public interface OnWelcomeMessageSubmittedListener {
        void OnWelcomeMessageSubmitted(int privateCode);
    }

}
