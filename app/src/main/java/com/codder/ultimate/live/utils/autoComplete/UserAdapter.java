package com.codder.ultimate.live.utils.autoComplete;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.MainApplication;
import com.codder.ultimate.databinding.ItemUserSlimBinding;
import com.codder.ultimate.modelclass.GuestProfileRoot;

import java.util.List;


class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final OnClickListener mListener;
    private List<GuestProfileRoot.User> mItems;

    protected UserAdapter(@NonNull OnClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        final GuestProfileRoot.User user = mItems.get(position);

        Glide.with(holder.itemView)
                .load(user.getImage())
                .apply(MainApplication.requestOptions)
                .circleCrop()
                .into(holder.binding.photo);

        holder.binding.name.setText(user.getName());
        holder.binding.username.setText("@" + user.getUsername());

        holder.binding.getRoot().setOnClickListener(v -> mListener.onUserClick(user));
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserSlimBinding binding = ItemUserSlimBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    public void submitData(List<GuestProfileRoot.User> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    interface OnClickListener {

        void onUserClick(GuestProfileRoot.User user);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        final ItemUserSlimBinding binding;

        UserViewHolder(@NonNull ItemUserSlimBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
