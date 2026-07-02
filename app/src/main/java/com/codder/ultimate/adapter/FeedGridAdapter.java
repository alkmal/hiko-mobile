package com.codder.ultimate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemFeedGridListBinding;
import com.codder.ultimate.post.model.PostRoot;

public class FeedGridAdapter extends ListAdapter<PostRoot.PostItem, FeedGridAdapter.FeedViewHolder> {

    private OnFeedGridAdapterClickListener onFeedGridAdapterClickListener;
    private final Context context;

    public FeedGridAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    public void setOnFeedGridAdapterClickListener(OnFeedGridAdapterClickListener listener) {
        this.onFeedGridAdapterClickListener = listener;
    }

    public static final DiffUtil.ItemCallback<PostRoot.PostItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull PostRoot.PostItem oldItem, @NonNull PostRoot.PostItem newItem) {
                    return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull PostRoot.PostItem oldItem, @NonNull PostRoot.PostItem newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFeedGridListBinding binding = ItemFeedGridListBinding.inflate(LayoutInflater.from(context), parent, false);
        return new FeedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        PostRoot.PostItem item = getItem(position);
        if (item != null) {
            holder.bind(item, position);
        }
    }

    public class FeedViewHolder extends RecyclerView.ViewHolder {
        private final ItemFeedGridListBinding binding;

        public FeedViewHolder(ItemFeedGridListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PostRoot.PostItem postItem, int position) {
            Glide.with(context)
                    .load(BuildConfig.BASE_URL + postItem.getPost())
                    .placeholder(R.drawable.bg_placeholder_feed)
                    .centerCrop()
                    .into(binding.imagePost);

            binding.imagePost.setAdjustViewBounds(true);

            int likeCount = postItem.getLikeCount();
            binding.tvLikes.setText(String.valueOf(likeCount));

            if (likeCount > 0) {
                binding.likeButton.setLiked(true);
                binding.likeButton.setLikeDrawableRes(R.drawable.ic_like);
            } else {
                binding.likeButton.setLiked(false);
                binding.likeButton.setLikeDrawableRes(R.drawable.ic_unlike);
            }

            binding.imagePost.setOnClickListener(v -> {
                if (onFeedGridAdapterClickListener != null) {
                    onFeedGridAdapterClickListener.onFeedClick(position);
                }
            });

            binding.imagePost.setOnLongClickListener(v -> {
                if (onFeedGridAdapterClickListener != null) {
                    onFeedGridAdapterClickListener.onDeleteClick(postItem, position);
                    return true;
                }
                return false;
            });
        }
    }

    public interface OnFeedGridAdapterClickListener {
        void onFeedClick(int position);
        void onDeleteClick(PostRoot.PostItem postItem, int position);
    }
}
