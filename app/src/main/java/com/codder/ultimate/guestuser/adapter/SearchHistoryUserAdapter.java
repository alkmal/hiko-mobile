package com.codder.ultimate.guestuser.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.codder.ultimate.databinding.ItemSearchUsersHistoryBinding;
import com.codder.ultimate.modelclass.GuestProfileRoot;

public class SearchHistoryUserAdapter extends ListAdapter<GuestProfileRoot.User, SearchHistoryUserAdapter.SearchHistoryUserViewHolder> {

    private OnUserClickListener onUserClickListener;

    public SearchHistoryUserAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.onUserClickListener = listener;
    }

    @NonNull
    @Override
    public SearchHistoryUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSearchUsersHistoryBinding binding = ItemSearchUsersHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SearchHistoryUserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHistoryUserViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public interface OnUserClickListener {
        void onDeleteClick(@NonNull GuestProfileRoot.User user, @NonNull ItemSearchUsersHistoryBinding binding, int position);
        void onUserClick(@NonNull GuestProfileRoot.User user, @NonNull ItemSearchUsersHistoryBinding binding, int position);
    }

    public class SearchHistoryUserViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        private final ItemSearchUsersHistoryBinding binding;

        public SearchHistoryUserViewHolder(@NonNull ItemSearchUsersHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(@NonNull GuestProfileRoot.User user, int position) {
            Context context = binding.getRoot().getContext();

            binding.imageUser.setUserImage(user.getImage(), user.getAvatarFrameImage(), 5);
            binding.tvUserName.setText(user.getName());

            if (user.getBio() != null && !user.getBio().isEmpty()) {
                binding.tvBio.setText(user.getBio());
            } else {
                binding.tvBio.setText(user.getUsername());
            }

            binding.btnRemove.setOnClickListener(v -> {
                if (onUserClickListener != null) {
                    onUserClickListener.onDeleteClick(user, binding, position);
                }
            });

            binding.getRoot().setOnClickListener(v -> {
                if (onUserClickListener != null) {
                    onUserClickListener.onUserClick(user, binding, position);
                }
            });
        }
    }

    private static final DiffUtil.ItemCallback<GuestProfileRoot.User> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull GuestProfileRoot.User oldItem, @NonNull GuestProfileRoot.User newItem) {
                    return oldItem.getUserId() != null && oldItem.getUserId().equals(newItem.getUserId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull GuestProfileRoot.User oldItem, @NonNull GuestProfileRoot.User newItem) {
                    return oldItem.equals(newItem);
                }
            };
}
