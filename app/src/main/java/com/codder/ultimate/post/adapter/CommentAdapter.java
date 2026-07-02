package com.codder.ultimate.post.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.databinding.ItemCommentBinding;
import com.codder.ultimate.post.model.PostCommentRoot;

import java.util.Objects;

public class CommentAdapter extends ListAdapter<PostCommentRoot.CommentsItem, CommentAdapter.CommentViewHolder> {

    public interface OnCommentClickListener {
        void onLongPress(PostCommentRoot.CommentsItem item, int position);
    }

    private OnCommentClickListener listener;

    public CommentAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnCommentClickListener(OnCommentClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCommentBinding binding = ItemCommentBinding.inflate(inflater, parent, false);
        return new CommentViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public void clear() {
        submitList(new java.util.ArrayList<>());
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommentBinding binding;
        private final OnCommentClickListener listener;

        public CommentViewHolder(ItemCommentBinding binding, OnCommentClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        public void bind(PostCommentRoot.CommentsItem item) {
            if (item == null) return;

            binding.imgUser.setUserImage(item.getImage(), item.getAvatarFrameImage(), 3);
            binding.tvUserName.setText(item.getName());
            binding.tvDate.setText(item.getTime());
            binding.tvComment.setText(item.getComment().isEmpty() ? item.getName() : item.getComment());

            binding.getRoot().setOnLongClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onLongPress(item, position);
                }
                return true;
            });
        }
    }

    private static final DiffUtil.ItemCallback<PostCommentRoot.CommentsItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {

                @Override
                public boolean areItemsTheSame(@NonNull PostCommentRoot.CommentsItem oldItem,
                                               @NonNull PostCommentRoot.CommentsItem newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }


                @Override
                public boolean areContentsTheSame(@NonNull PostCommentRoot.CommentsItem oldItem,
                                                  @NonNull PostCommentRoot.CommentsItem newItem) {
                    return oldItem.equals(newItem);
                }
            };
}
