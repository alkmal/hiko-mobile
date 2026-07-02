package com.codder.ultimate.live.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.ItemGiftUserListBinding;
import com.codder.ultimate.live.utils.UserSelectableClass;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends ListAdapter<UserSelectableClass, UserListAdapter.UserListViewHolder> {

    private Context context;
    private SessionManager sessionManager;
    private OnUserClickListener onUserClickListener;

    public UserListAdapter(Context context, OnUserClickListener listener) {
        super(DIFF_CALLBACK);
        this.context = context.getApplicationContext();
        this.sessionManager = new SessionManager(this.context);
        this.onUserClickListener = listener;
    }

    private static final DiffUtil.ItemCallback<UserSelectableClass> DIFF_CALLBACK = new DiffUtil.ItemCallback<UserSelectableClass>() {
        @Override
        public boolean areItemsTheSame(@NonNull UserSelectableClass oldItem, @NonNull UserSelectableClass newItem) {
            return oldItem.getSeatItem().getUserId().equals(newItem.getSeatItem().getUserId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull UserSelectableClass oldItem, @NonNull UserSelectableClass newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGiftUserListBinding binding = ItemGiftUserListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListViewHolder holder, int position) {
        UserSelectableClass user = getItem(position);
        if (user == null) return;

        holder.bind(user, position);
    }

    public List<UserSelectableClass> filterOutSessionUser(List<UserSelectableClass> originalList) {
        String sessionId = sessionManager.getUser() != null ? sessionManager.getUser().getId() : null;
        if (sessionId == null) return originalList;

        List<UserSelectableClass> filtered = new java.util.ArrayList<>();
        for (UserSelectableClass user : originalList) {
            if (user != null && user.getSeatItem() != null && user.getSeatItem().getUserId() != null
                    && !user.getSeatItem().getUserId().equals(sessionId)) {
                filtered.add(user);
            }
        }
        return filtered;
    }

    public List<UserSelectableClass> getVisibleItems() {
        return getCurrentList();
    }

    public void selectAll() {
        for (UserSelectableClass user : getCurrentList()) {
            user.setSelected(true);
        }
        notifyDataSetChanged();
    }


    public void addData(List<UserSelectableClass> users) {
        submitList(new ArrayList<>(users));
    }

    class UserListViewHolder extends RecyclerView.ViewHolder {
        private final ItemGiftUserListBinding binding;

        public UserListViewHolder(ItemGiftUserListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final UserSelectableClass user, int position) {

            if (user.getSeatItem() != null && user.getSeatItem().getImage() != null) {
                Glide.with(binding.getRoot().getContext())
                        .load(user.getSeatItem().getImage())
                        .circleCrop()
                        .placeholder(R.drawable.profile_placeholder)
                        .into(binding.imgview);
            } else {
                binding.imgview.setImageResource(R.drawable.img1);
            }

            binding.tvName.setText(String.valueOf(position + 1));

            if (user.isSelected()) {
                binding.layUserBg.setBackground(ContextCompat.getDrawable(context,R.drawable.selected_gift_user));
                binding.tvName.setBackground(ContextCompat.getDrawable(context,R.drawable.bg_selectgift_username));
            } else {
                binding.layUserBg.setBackground(null);
                binding.tvName.setBackground(ContextCompat.getDrawable(context,R.drawable.bg_unselectgift_username));
            }

            binding.getRoot().setOnClickListener(v -> {
                int currentPosition = getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    UserSelectableClass clickedUser = getItem(currentPosition);
                    if (clickedUser != null) {
                        clickedUser.setSelected(!clickedUser.isSelected());
                        if (onUserClickListener != null) {
                            onUserClickListener.onUserClick(clickedUser);
                        }
                        notifyItemChanged(currentPosition);
                    }
                }
            });
        }
    }

    public interface OnUserClickListener {
        void onUserClick(UserSelectableClass userSelectableClass);
    }
}

