package com.codder.ultimate.live.bottomsheet;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.BottomsheetAudiorromWelcomemsgBinding;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.socket.MySocketManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class BottomSheetAudioRoomWelcomeMsg {

    private static final String TAG = "BottomSheetAudioRoomWelcomeMsg";
    private final BottomSheetDialog bottomSheetDialog;
    private final OnWelcomeMessageSubmittedListener listener;
    SessionManager sessionManager;

    public BottomSheetAudioRoomWelcomeMsg(Context context, PkAudioLiveUserRoot.UsersItem liveUser, OnWelcomeMessageSubmittedListener listener) {
        this.listener = listener;
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet == null) return;
        });

        BottomsheetAudiorromWelcomemsgBinding audioRoomWelcomeMsgBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottomsheet_audiorrom_welcomemsg, null, false);
        bottomSheetDialog.setContentView(audioRoomWelcomeMsgBinding.getRoot());
        sessionManager = new SessionManager(context);
        bottomSheetDialog.show();

        audioRoomWelcomeMsgBinding.etWelcomeMessage.setText(liveUser.getRoomWelcome());
        audioRoomWelcomeMsgBinding.tvSubmit.setOnClickListener(v -> {
            String welcomeMessage = audioRoomWelcomeMsgBinding.etWelcomeMessage.getText().toString();
            listener.OnWelcomeMessageSubmitted(welcomeMessage);

            if (welcomeMessage.isEmpty()) {
                Toast.makeText(context, R.string.please_enter_room_welcome_message, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                    jsonObject.put("roomWelcome", welcomeMessage);
                    MySocketManager.getInstance().getSocket().emit(Const.EVENT_ROOM_WELCOME, jsonObject);
                    Log.d(TAG, "BottomSheetAudioRoomWelcomeMsg: ==> " +jsonObject.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            bottomSheetDialog.dismiss();

        });

        audioRoomWelcomeMsgBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

    }

    public interface OnWelcomeMessageSubmittedListener {
        void OnWelcomeMessageSubmitted(String WlcMessage);
    }

}
