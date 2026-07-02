package com.codder.ultimate.profile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.databinding.ItemLevelBinding;
import com.codder.ultimate.profile.modelclass.LevelRoot;

import java.util.List;

public class LevelsAdapter extends RecyclerView.Adapter<LevelsAdapter.LevelsViewHolder> {

    private final List<LevelRoot.LevelItem> levelList;

    public LevelsAdapter(@NonNull List<LevelRoot.LevelItem> levelList) {
        this.levelList = levelList != null ? levelList : List.of();
    }

    @NonNull
    @Override
    public LevelsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLevelBinding binding = ItemLevelBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LevelsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelsViewHolder holder, int position) {
        LevelRoot.LevelItem levelItem = levelList.get(position);
        holder.bind(levelItem);
    }

    @Override
    public int getItemCount() {
        return levelList.size();
    }

    public static class LevelsViewHolder extends RecyclerView.ViewHolder {

        private final ItemLevelBinding binding;

        public LevelsViewHolder(ItemLevelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(LevelRoot.LevelItem item) {
            if (item == null) return;

            Context context = binding.getRoot().getContext();

            String imageUrl = item.getImage() != null ? BuildConfig.BASE_URL + item.getImage() : null;
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.logo);

            binding.tvCoins.setText(RayziUtils.formatCoin(item.getCoin()));
            binding.tvLevel.setText(item.getName() != null ? item.getName() : context.getString(R.string.unknown));
        }
    }
}

