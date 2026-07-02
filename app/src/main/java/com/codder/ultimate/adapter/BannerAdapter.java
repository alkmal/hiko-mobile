package com.codder.ultimate.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemBannerBinding;
import com.codder.ultimate.live.model.BannerRoot;
import com.codder.ultimate.profile.activity.WebActivity;

public class BannerAdapter extends ListAdapter<BannerRoot.BannerItem, BannerAdapter.BannerViewHolder> {

    private static final String TAG = "BannerAdapter";

    public BannerAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<BannerRoot.BannerItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<BannerRoot.BannerItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull BannerRoot.BannerItem oldItem, @NonNull BannerRoot.BannerItem newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull BannerRoot.BannerItem oldItem, @NonNull BannerRoot.BannerItem newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBannerBinding binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BannerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        BannerRoot.BannerItem bannerItem = getItem(position);
        if (bannerItem == null) return;

        Context context = holder.binding.getRoot().getContext();

        String imageUrl = bannerItem.getImage();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            String fullUrl = BuildConfig.BASE_URL + imageUrl;
            Log.d(TAG, "Loading image: " + fullUrl);
            Glide.with(context)
                    .load(fullUrl)
                    .centerCrop()
                    .placeholder(R.drawable.home_banner)
                    .error(R.drawable.home_banner)
                    .into(holder.binding.imageview);
        } else {
            Log.d(TAG, "Empty image URL, loading placeholder drawable");
            Glide.with(context)
                    .load(R.drawable.home_banner)
                    .into(holder.binding.imageview);
        }

        String url = bannerItem.getURL();
        if (url != null && !url.trim().isEmpty()) {
            holder.binding.imageview.setOnClickListener(v -> WebActivity.open(context, "", url, true));
        } else {
            holder.binding.imageview.setOnClickListener(null);
        }
    }


    static class BannerViewHolder extends RecyclerView.ViewHolder {
        final ItemBannerBinding binding;

        public BannerViewHolder(@NonNull ItemBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

