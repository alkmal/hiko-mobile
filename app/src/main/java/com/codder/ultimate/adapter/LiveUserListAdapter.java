package com.codder.ultimate.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.databinding.ItemLiveuserBinding;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot.UsersItem;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LiveUserListAdapter
        extends ListAdapter<PkAudioLiveUserRoot.UsersItem, LiveUserListAdapter.MyViewHolder> {

    private Context context;
    private OnHostClickLister onHostClickLister;

    public LiveUserListAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    public void setOnHostClickLister(OnHostClickLister onHostClickLister) {
        this.onHostClickLister = onHostClickLister;
    }

    // ================= DiffUtil =================
    public static final DiffUtil.ItemCallback<UsersItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<UsersItem>() {

                @Override
                public boolean areItemsTheSame(@NonNull UsersItem oldItem,
                                               @NonNull UsersItem newItem) {

                    // Static items
                    if (oldItem.getItemType() != UsersItem.TYPE_USER ||
                            newItem.getItemType() != UsersItem.TYPE_USER) {

                        return oldItem.getItemType() == newItem.getItemType();
                    }

                    // Real users
                    return oldItem.getId() != null
                            && oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull UsersItem oldItem,
                                                  @NonNull UsersItem newItem) {

                    // ⚠️ DO NOT call equals()

                    // Static items → never change
                    if (oldItem.getItemType() != UsersItem.TYPE_USER) {
                        return true;
                    }

                    // Compare only UI-impact fields
                    return oldItem.getView() == newItem.getView()
                            && Objects.equals(oldItem.getName(), newItem.getName())
                            && Objects.equals(oldItem.getImage(), newItem.getImage());
                }
            };



    // ================= Adapter =================
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new MyViewHolder(inflater.inflate(R.layout.item_liveuser, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PkAudioLiveUserRoot.UsersItem data = getItem(position);

        if (data.getItemType() == PkAudioLiveUserRoot.UsersItem.TYPE_MY_ROOM) {

            holder.binding.layProfile.setBackground(ContextCompat.getDrawable(context,R.drawable.ic_audioroom_bg));
            holder.binding.tvRoomName.setText("My Room");
            holder.binding.tvRoomName.setTextColor(ContextCompat.getColor(context,R.color.myroom_color));
            holder.binding.lottie.setVisibility(GONE);

            holder.itemView.setOnClickListener(v -> {
                // open my room
                onHostClickLister.onMyRoom();
            });

        } else if (data.getItemType() == PkAudioLiveUserRoot.UsersItem.TYPE_QUICK_JOIN) {

            holder.binding.layProfile.setBackground(ContextCompat.getDrawable(context,R.drawable.ic_quick_join));
            holder.binding.tvRoomName.setText("Quick Join");
            holder.binding.tvRoomName.setTextColor(ContextCompat.getColor(context,R.color.quick_join_color));
            holder.binding.lottie.setVisibility(GONE);


            holder.itemView.setOnClickListener(v -> {
                // quick join logic
                onHostClickLister.onQuickJoin(data);
            });

        } else {

            holder.binding.tvRoomName.setText(data.getRoomName());
            RayziUtils.marqueeText(holder.binding.tvRoomName);

            holder.binding.tvRoomName.setTextColor(ContextCompat.getColor(context,R.color.white));
            holder.binding.lottie.setVisibility(VISIBLE);
            Glide.with(context)
                    .load(data.getRoomImage())
                    .placeholder(R.drawable.profile_placeholder)
                    .circleCrop()
                    .into(holder.binding.ivProfile);

            startPulseAnimation(holder.binding.ivProfile);
            holder.binding.layProfile.post(() ->
                    startPulseAnimation(
                            holder.binding.ivProfile
                    )
            );


            holder.binding.getRoot().setOnClickListener(v -> {

                if (onHostClickLister != null) {
                    onHostClickLister.onHostItemClick(data);
                }
            });
        }
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void startPulseAnimation(View ivProfile) {

        // layProfile zoom IN (1f → 1.1f)
//        ObjectAnimator layScaleX = ObjectAnimator.ofFloat(layProfile, "scaleX", 1f, 1.1f);
//        ObjectAnimator layScaleY = ObjectAnimator.ofFloat(layProfile, "scaleY", 1f, 1.1f);

        // ivProfile zoom OUT (1f → 0.9f)
        ObjectAnimator imgScaleX = ObjectAnimator.ofFloat(ivProfile, "scaleX", 1f, 0.9f);
        ObjectAnimator imgScaleY = ObjectAnimator.ofFloat(ivProfile, "scaleY", 1f, 0.9f);

//        layScaleX.setRepeatCount(ValueAnimator.INFINITE);
//        layScaleY.setRepeatCount(ValueAnimator.INFINITE);
        imgScaleX.setRepeatCount(ValueAnimator.INFINITE);
        imgScaleY.setRepeatCount(ValueAnimator.INFINITE);

//        layScaleX.setRepeatMode(ValueAnimator.REVERSE);
//        layScaleY.setRepeatMode(ValueAnimator.REVERSE);
        imgScaleX.setRepeatMode(ValueAnimator.REVERSE);
        imgScaleY.setRepeatMode(ValueAnimator.REVERSE);

//        layScaleX.setDuration(800);
//        layScaleY.setDuration(800);
        imgScaleX.setDuration(800);
        imgScaleY.setDuration(800);

//        layScaleX.start();
//        layScaleY.start();
        imgScaleX.start();
        imgScaleY.start();
    }


    @Override
    public int getItemViewType(int position) {
        return getItem(position).getItemType();
    }

    // ================= Helpers =================
    public void submitData(List<PkAudioLiveUserRoot.UsersItem> list) {
        submitList(list == null ? new ArrayList<>() : new ArrayList<>(list));
    }

    public List<PkAudioLiveUserRoot.UsersItem> getCurrentItems() {
        return getCurrentList();
    }

    // ================= ViewHolder =================
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ItemLiveuserBinding binding;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemLiveuserBinding.bind(itemView);
        }
    }

    // ================= Click =================
    public interface OnHostClickLister {
        void onHostItemClick(PkAudioLiveUserRoot.UsersItem userDummy);
        void onQuickJoin(PkAudioLiveUserRoot.UsersItem userDummy);
        void onMyRoom();
    }
}

