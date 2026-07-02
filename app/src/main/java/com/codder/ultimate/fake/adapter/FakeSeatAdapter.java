package com.codder.ultimate.fake.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemSeatBinding;
import com.codder.ultimate.modelclass.SeatModalClass;

public class FakeSeatAdapter extends ListAdapter<SeatModalClass, FakeSeatAdapter.SeatViewHolder> {

    private final java.util.Set<Integer> framedSeatPositions = new java.util.HashSet<>();

    public interface OnTakeSeatListener {
        void onClickSeat(SeatModalClass seatModalClass, int position, ItemSeatBinding binding);
    }

    public interface OnSeatClick {
        void onSeatClick(SeatModalClass seatModalClass, int position, ItemSeatBinding binding);
    }

    private OnTakeSeatListener onTakeSeatListener;
    private OnSeatClick onSeatClickListener;

    public void setOnTakeSeatListener(OnTakeSeatListener listener) {
        this.onTakeSeatListener = listener;
    }

    public void setOnSeatClickListener(OnSeatClick listener) {
        this.onSeatClickListener = listener;
    }

    public FakeSeatAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<SeatModalClass> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull SeatModalClass oldItem, @NonNull SeatModalClass newItem) {
            return oldItem.getSeat_id().equals(newItem.getSeat_id());
        }

        @Override
        public boolean areContentsTheSame(@NonNull SeatModalClass oldItem, @NonNull SeatModalClass newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSeatBinding binding = ItemSeatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SeatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        SeatModalClass seat = getItem(position);
        if (seat == null) return;
        holder.bind(seat, position);
    }

    public class SeatViewHolder extends RecyclerView.ViewHolder {
        public final ItemSeatBinding binding;

        public SeatViewHolder(ItemSeatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SeatModalClass seat, int position) {
            Glide.with(binding.userImage.getContext())
                    .load(seat.getImage())
                    .placeholder(R.drawable.audio_seat)
                    .circleCrop()
                    .into(binding.userImage);


            binding.nameCount.setText(seat.getName() != null && !seat.getName().isEmpty() ?
                    seat.getName() : String.valueOf(Integer.parseInt(seat.getSeat_id()) + 1));

            binding.getRoot().setOnClickListener(v -> {
                if (onTakeSeatListener != null) {
                    onTakeSeatListener.onClickSeat(seat, position, binding);
                }
                if (onSeatClickListener != null) {
                    onSeatClickListener.onSeatClick(seat,position,binding);
                }
            });

            if (seat.isReserved()) {
                binding.ivSeatBg.setVisibility(View.VISIBLE);

                if (framedSeatPositions.contains(position)) {
                    binding.avatarFrameImage.setImageResource(R.drawable.ic_avatar_frame);
                    binding.ivSeatBg.setImageResource(0);
                } else {
                    binding.avatarFrameImage.setImageResource(0);
                    binding.ivSeatBg.setImageResource(R.drawable.profile_round_bg);
                }
            } else {
                binding.ivSeatBg.setVisibility(View.GONE);
            }
        }
    }

    public void assignFramedSeats() {
        framedSeatPositions.clear();
        // Randomly pick 1 or 2 reserved seats from the current list
        java.util.List<Integer> reservedPositions = new java.util.ArrayList<>();
        for (int i = 0; i < getCurrentList().size(); i++) {
            if (getCurrentList().get(i).isReserved()) {
                reservedPositions.add(i);
            }
        }
        java.util.Collections.shuffle(reservedPositions);
        int frameCount = reservedPositions.isEmpty() ? 0 : new java.util.Random().nextInt(2) + 1;
        for (int i = 0; i < frameCount && i < reservedPositions.size(); i++) {
            framedSeatPositions.add(reservedPositions.get(i));
        }
    }
}

