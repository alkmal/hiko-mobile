package com.codder.ultimate.live.adapter;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.databinding.ItemStickerBinding;
import com.codder.ultimate.live.model.StickerRoot;

public class StickerAdapter extends ListAdapter<StickerRoot.StickerItem, StickerAdapter.StickerViewHolder> {

    private OnStickerClickListener onStickerClickListener;

    public StickerAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<StickerRoot.StickerItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<StickerRoot.StickerItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull StickerRoot.StickerItem oldItem, @NonNull StickerRoot.StickerItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull StickerRoot.StickerItem oldItem, @NonNull StickerRoot.StickerItem newItem) {
            return oldItem.equals(newItem);
        }
    };

    public void setOnStickerClickListener(OnStickerClickListener listener) {
        this.onStickerClickListener = listener;
    }

    @NonNull
    @Override
    public StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStickerBinding binding = ItemStickerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new StickerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StickerViewHolder holder, int position) {
        StickerRoot.StickerItem stickerItem = getItem(position);

        if (stickerItem == null) {
            holder.binding.image.setImageURI((Uri) null);
            holder.binding.image.setOnClickListener(null);
            return;
        }

        holder.binding.image.setImageURI(stickerItem.getSticker());

        holder.binding.image.setOnClickListener(v -> {
            if (onStickerClickListener != null) {
                Log.d("StickerAdapter", "Sticker clicked: " + stickerItem.getSticker());
                onStickerClickListener.onStickerClick(stickerItem);
            }
        });
    }

    public interface OnStickerClickListener {
        void onStickerClick(@NonNull StickerRoot.StickerItem stickerItem);
    }

    static class StickerViewHolder extends RecyclerView.ViewHolder {
        final ItemStickerBinding binding;
        StickerViewHolder(ItemStickerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}