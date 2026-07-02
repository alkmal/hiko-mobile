package com.codder.ultimate.live.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.bumptech.glide.Glide;
import com.codder.ultimate.databinding.ItemGameListBinding;
import com.codder.ultimate.profile.modelclass.SettingRoot;

public class GameListAdapter extends ListAdapter<SettingRoot.Game, GameListAdapter.GameListViewHolder> {

    private Context context;
    private onClickGameList clickGameList;

    public GameListAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<SettingRoot.Game> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull SettingRoot.Game oldItem, @NonNull SettingRoot.Game newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull SettingRoot.Game oldItem, @NonNull SettingRoot.Game newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public GameListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ItemGameListBinding binding = ItemGameListBinding.inflate(LayoutInflater.from(context), parent, false);
        return new GameListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GameListViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public void setClickGameList(onClickGameList clickGameList) {
        this.clickGameList = clickGameList;
    }

    class GameListViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        private final ItemGameListBinding binding;

        public GameListViewHolder(@NonNull ItemGameListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SettingRoot.Game gameItem, int position) {
            if (gameItem == null) return;
            Glide.with(context)
                    .load(gameItem.getImage())
                    .error(android.R.drawable.stat_notify_error)
                    .into(binding.image);

            binding.gameName.setText(gameItem.getName() != null ? gameItem.getName() : "-");

            binding.getRoot().setOnClickListener(v -> {
                if (clickGameList != null && gameItem != null) {
                    clickGameList.onClickGame(gameItem, getAdapterPosition());
                }
            });
        }
    }

    public interface onClickGameList {
        void onClickGame(SettingRoot.Game gameItem, int position);
    }
}
