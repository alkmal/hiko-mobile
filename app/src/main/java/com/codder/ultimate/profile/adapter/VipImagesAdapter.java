package com.codder.ultimate.profile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemVipSliderBinding;

public class VipImagesAdapter extends RecyclerView.Adapter<VipImagesAdapter.VipImagesViewHolder> {

    private final int[] images = {
            R.drawable.crown_royalty,
            R.drawable.extra_coin,
            R.drawable.all_level_access
    };

    private final int[] textList = {
            R.string.get_premium_crown,
            R.string.get_extra_coins,
            R.string.get_all_level_access
    };

    @NonNull
    @Override
    public VipImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemVipSliderBinding binding = ItemVipSliderBinding.inflate(inflater, parent, false);
        return new VipImagesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VipImagesViewHolder holder, int position) {
        holder.bind(images[position], textList[position]);
    }

    @Override
    public int getItemCount() {
        return Math.min(images.length, textList.length);
    }

    public static class VipImagesViewHolder extends RecyclerView.ViewHolder {
        private final ItemVipSliderBinding binding;
        private final Context context;

        public VipImagesViewHolder(@NonNull ItemVipSliderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = binding.getRoot().getContext();
        }

        public void bind(int imageResId, int textResId) {
            if (context != null && imageResId != 0) {
                Glide.with(context)
                        .load(imageResId)
                        .apply(MainApplication.requestOptions)
                        .into(binding.image);
            }

            binding.tvText.setText(context.getString(textResId));
        }
    }
}

