package com.codder.ultimate.live.bottomsheet;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.BottomsheetAudioroomNameBinding;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.socket.MySocketManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class BottomSheetAudioRoomName {

    private final BottomSheetDialog bottomSheetDialog;

    public BottomSheetAudioRoomName(Context context, PkAudioLiveUserRoot.UsersItem liveUser, OnRoomNameSubmittedListener listener) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet == null) return;

        });

        BottomsheetAudioroomNameBinding audioRoomNameBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottomsheet_audioroom_name, null, false);
        bottomSheetDialog.setContentView(audioRoomNameBinding.getRoot());
        bottomSheetDialog.show();

        audioRoomNameBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        audioRoomNameBinding.etName.setText(liveUser.getRoomName());

        audioRoomNameBinding.tvSubmit.setOnClickListener(v -> {
            String roomName = audioRoomNameBinding.etName.getText().toString();
            listener.onRoomNameSubmitted(roomName);

            if (roomName.isEmpty()) {
                Toast.makeText(context, R.string.please_enter_room_name, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                    jsonObject.put("roomName", roomName);
                    MySocketManager.getInstance().getSocket().emit(Const.EVENT_ROOM_NAME, jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            bottomSheetDialog.dismiss();
        });
    }

    public interface OnRoomNameSubmittedListener {
        void onRoomNameSubmitted(String roomName);
    }

}
