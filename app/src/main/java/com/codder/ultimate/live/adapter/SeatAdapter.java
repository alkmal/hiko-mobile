package com.codder.ultimate.live.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.ItemSeatBinding;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc2.IRtcEngineEventHandler;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatHolder> {

    private static final String TAG = "SeatAdapter";
    private Context context;
    private List<PkAudioLiveUserRoot.UsersItem.SeatItem> seatList = new ArrayList<>();
    private SessionManager sessionManager;
    private String hostId;
    private onSeatClick onSeatClick;

    public SeatAdapter(Context context, SessionManager sessionManager , String hostId) {
        this.context = context;
        this.sessionManager = sessionManager;
        this.hostId = hostId;
    }

    public void setOnSeatClick(onSeatClick onSeatClick) {
        this.onSeatClick = onSeatClick;
    }

    @NonNull
    @Override
    public SeatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSeatBinding binding = ItemSeatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SeatHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatHolder holder, int position) {
        PkAudioLiveUserRoot.UsersItem.SeatItem seatItem = seatList.get(position);

        Log.d(TAG, "onBindViewHolder: seatItem.isMute() " + seatItem.isMute());

        setImage(seatItem, holder.binding);
        holder.binding.ivMute.setVisibility(seatItem.isMute() == 1 || seatItem.isMute() == 2 ? VISIBLE : GONE);
        holder.binding.nameCount.setText(getNameText(seatItem));
        RayziUtils.marqueeText(holder.binding.nameCount);
        handleSpeakingAnimation(seatItem, holder.binding);
        holder.binding.image.setOnClickListener(view -> {
            onSeatClick.OnClickSeat(seatItem, position);
        });

        if (seatItem.isAnimate()) {
            startAnimation(holder.binding);
        } else {
            stopAnimation(holder.binding);
        }
    }

    @Override
    public int getItemCount() {
        return seatList.size();
    }


    public void onAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers) {
        for (IRtcEngineEventHandler.AudioVolumeInfo info : speakers) {
            for (int i = 0; i < seatList.size(); i++) {
                if (info.uid == seatList.get(i).getAgoraUid()) {
                    Log.d(TAG, "onAudioVolumeIndication: INADAPTER " + info.uid + "  user:pos " + i + "agoraUID " + seatList.get(i).getAgoraUid());
                    seatList.get(i).setAnimate(true);
                    notifyItemChanged(i);
                }
            }
        }
    }

    public void onAudioVolumeIndicationSingle(IRtcEngineEventHandler.AudioVolumeInfo info) {

        for (int i = 0; i < seatList.size(); i++) {
            if (info.uid == seatList.get(i).getAgoraUid()) {
                Log.d(TAG, "onAudioVolumeIndication: INADAPTER " + info.uid + "  user:pos " + i + "agoraUID " + seatList.get(i).getAgoraUid());
                seatList.get(i).setAnimate(true);
                notifyItemChanged(i);
            }
        }

    }

    public void setReaction(int uid, String reactionUrl) {
        for (int i = 0; i < seatList.size(); i++) {
            PkAudioLiveUserRoot.UsersItem.SeatItem seatItem = seatList.get(i);
            if (uid == seatItem.getAgoraUid()) {

                // Always show the latest reaction image immediately
                seatItem.setReactionImage(reactionUrl);
                notifyItemChanged(i);

                if (!seatItem.isReactionRunning()) {
                    // Start a single display window
                    seatItem.setReactionRunning(true);
                    int finalI = i;

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Hide reaction after 7s, no dequeueing/looping
                        seatItem.setReactionRunning(false);
                        seatItem.setReactionImage(null);
                        notifyItemChanged(finalI);
                    }, 7000);
                }

                // NOTE: no queue logic at all — newest reaction just replaces the image
                break;
            }
        }
    }

    private void setImage(PkAudioLiveUserRoot.UsersItem.SeatItem seatItem, ItemSeatBinding binding) {
        Log.d(TAG, "setImage: " + seatItem.toString());


        if (seatItem.getUserId() != null) {
            if (seatItem.getUserId().equals(hostId)) {
                binding.ivHost.setVisibility(VISIBLE);
            } else {
                binding.ivHost.setVisibility(GONE);
            }
        }


        binding.avatarFrameImage.setVisibility(VISIBLE);

        String avatarFrame = seatItem.getAvatarFrame() != null ? seatItem.getAvatarFrame() : "";

        if (!avatarFrame.isEmpty()) {
            binding.userImage.setPadding(20, 20, 20, 20);
        } else {
            binding.userImage.setPadding(0, 0, 0, 0);
        }

        if (seatItem.isReserved() && avatarFrame.isEmpty()) {
            binding.ivSeatBg.setVisibility(VISIBLE);
        } else {
            binding.ivSeatBg.setVisibility(GONE);
        }

        if (seatItem.isReserved()) {
            Glide.with(context).load(seatItem.getImage()).circleCrop().placeholder(R.drawable.profile_placeholder).into(binding.userImage);
            Glide.with(context).load(!avatarFrame.isEmpty() ? BuildConfig.BASE_URL + avatarFrame : "").into(binding.avatarFrameImage);
            binding.ivMute.setVisibility(GONE);
        } else if (!seatItem.isReserved() && !seatItem.isLock()) {
            Glide.with(context).load(R.drawable.audio_seat).into(binding.userImage);
            binding.avatarFrameImage.setVisibility(GONE);
            binding.ivHost.setVisibility(GONE);
            binding.ivSeatBg.setVisibility(GONE);
            binding.ivMute.setVisibility(GONE);
        } else if (seatItem.isLock()) {
            Glide.with(context).load(R.drawable.audio_lock).into(binding.userImage);
            binding.avatarFrameImage.setVisibility(GONE);
            binding.ivHost.setVisibility(GONE);
            binding.ivSeatBg.setVisibility(GONE);
            binding.ivMute.setVisibility(GONE);
        }

        if (!seatItem.isReserved() && (seatItem.isMute() == 1 || seatItem.isMute() == 2)) {
            binding.ivMute.setVisibility(VISIBLE);
            binding.muteMicSeat.setVisibility(VISIBLE);
        }


        if (seatItem.isReactionRunning()) {
            Glide.with(context).load(seatItem.getReactionImage()).into(binding.imgHostReaction);
        } else {
            binding.imgHostReaction.setImageDrawable(null);
        }
    }

    private void handleSpeakingAnimation(PkAudioLiveUserRoot.UsersItem.SeatItem seatItem, ItemSeatBinding binding) {
        if (seatItem.isIsSpeaking() && seatItem.getUserId() != null && seatItem.getUserId().equalsIgnoreCase(sessionManager.getUser().getId())) {
            binding.animationView1.setVisibility(VISIBLE);
            new Handler().postDelayed(() -> binding.animationView1.setVisibility(GONE), 3000);
        }
    }

    private void startAnimation(ItemSeatBinding binding) {
        binding.animationView1.setVisibility(VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.animationView1.setVisibility(GONE);
        }, 1500);
    }

    private void stopAnimation(ItemSeatBinding binding) {
        binding.animationView1.setVisibility(GONE);
    }

    public String getNameText(PkAudioLiveUserRoot.UsersItem.SeatItem seatItem) {
        return seatItem.isReserved() ? seatItem.getName() : String.valueOf(seatItem.getPosition() + 1);
    }

    public void addData(List<PkAudioLiveUserRoot.UsersItem.SeatItem> seat) {
        int startPosition = seatList.size();
        seatList.addAll(seat);
        notifyItemRangeInserted(startPosition, seat.size());
    }

    public void updateData(List<PkAudioLiveUserRoot.UsersItem.SeatItem> seat) {
        seatList.clear();
        seatList.addAll(seat);
        checkMuteValues();
        notifyDataSetChanged();
    }

    public void clear() {
        seatList.clear();
        notifyDataSetChanged();
    }

    public List<PkAudioLiveUserRoot.UsersItem.SeatItem> getList() {
        return seatList;
    }

    public class SeatHolder extends RecyclerView.ViewHolder {
        ItemSeatBinding binding;

        public SeatHolder(ItemSeatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void checkMuteValues() {
        for (int i = 0; i < seatList.size(); i++) {
            PkAudioLiveUserRoot.UsersItem.SeatItem seatItem = seatList.get(i);
            Log.d(TAG, "Position: " + i + " | isMute: " + seatItem.isMute());
        }
    }


    public interface onSeatClick {
        void OnClickSeat(PkAudioLiveUserRoot.UsersItem.SeatItem seatItem, int position);


    }
}
