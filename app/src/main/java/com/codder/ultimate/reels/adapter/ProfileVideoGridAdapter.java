package com.codder.ultimate.reels.adapter;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemVidProfileListBinding;
import com.codder.ultimate.reels.model.ReliteRoot;

import java.util.ArrayList;
import java.util.List;

public class ProfileVideoGridAdapter extends ListAdapter<ReliteRoot.VideoItem, ProfileVideoGridAdapter.FeedViewHolder> {

    private OnVideoGridClickListener onVideoGridClickListener;

    public ProfileVideoGridAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnVideoGridClickListener(OnVideoGridClickListener listener) {
        this.onVideoGridClickListener = listener;
    }

    public interface OnVideoGridClickListener {
        void onVideoClick(int position);

        void onDeleteClick(@NonNull ReliteRoot.VideoItem postItem, int position);
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemVidProfileListBinding binding = ItemVidProfileListBinding.inflate(inflater, parent, false);
        return new FeedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public class FeedViewHolder extends RecyclerView.ViewHolder {
        private final ItemVidProfileListBinding binding;

        public FeedViewHolder(@NonNull ItemVidProfileListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(@NonNull ReliteRoot.VideoItem videoItem, int position) {
            Glide.with(binding.getRoot().getContext())
                    .load(videoItem.getScreenshot())
                    .placeholder(R.drawable.bg_placeholder_feed)
                    .centerCrop()
                    .into(binding.imgThumb);

            int likeCount = videoItem.getLikeCount();
            binding.tvViewCount.setText(String.valueOf(likeCount));


            binding.getRoot().setOnClickListener(v -> {
                if (onVideoGridClickListener != null) {
                    onVideoGridClickListener.onVideoClick(position);
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (onVideoGridClickListener != null) {
                    onVideoGridClickListener.onDeleteClick(videoItem, position);
                    return true;
                }
                return false;
            });
        }
    }

    public List<ReliteRoot.VideoItem> getCurrentItems() {
        return new ArrayList<>(getCurrentList());
    }

    private static final DiffUtil.ItemCallback<ReliteRoot.VideoItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ReliteRoot.VideoItem oldItem, @NonNull ReliteRoot.VideoItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ReliteRoot.VideoItem oldItem, @NonNull ReliteRoot.VideoItem newItem) {
            return oldItem.equals(newItem);
        }
    };
}
