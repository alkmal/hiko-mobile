package com.codder.ultimate.live.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemEmojiGridBinding;
import com.codder.ultimate.databinding.ItemReactionBinding;
import com.codder.ultimate.live.model.ReactionRoot;

import java.util.Objects;

public class ReactionGridAdapter extends ListAdapter<ReactionRoot.DataItem, ReactionGridAdapter.EmojiViewHolder> {

    private OnReactionClickListener onReactionClickListener;
    private Context context;

    public ReactionGridAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ReactionRoot.DataItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull ReactionRoot.DataItem oldItem, @NonNull ReactionRoot.DataItem newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull ReactionRoot.DataItem oldItem, @NonNull ReactionRoot.DataItem newItem) {
                    return Objects.equals(oldItem, newItem);
                }
            };

    public void setOnReactionClickListener(OnReactionClickListener listener) {
        this.onReactionClickListener = listener;
    }

    @NonNull
    @Override
    public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemReactionBinding binding = ItemReactionBinding.inflate(inflater, parent, false);
        return new EmojiViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
        ReactionRoot.DataItem item = getItem(position);
        holder.bind(item);
    }

    class EmojiViewHolder extends RecyclerView.ViewHolder {
        private final ItemReactionBinding binding;

        public EmojiViewHolder(@NonNull ItemReactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(@NonNull ReactionRoot.DataItem reaction) {

            if (reaction.getImage() != null && !reaction.getImage().isEmpty()) {
                Glide.with(binding.getRoot())
                        .load(reaction.getImage())
                        .apply(MainApplication.requestOptions)
                        .thumbnail(Glide.with(context).load(R.drawable.loadergif))
                        .into(binding.imgEmoji);
            } else {
            }

            binding.tvGiftName.setText(reaction.getName() != null ? reaction.getName() : "");

            binding.getRoot().setOnClickListener(v -> {
                if (onReactionClickListener != null) {
                    onReactionClickListener.onReactionClick(reaction);
                }
            });
        }
    }

    public interface OnReactionClickListener {
        void onReactionClick(@NonNull ReactionRoot.DataItem reaction);
    }
}
