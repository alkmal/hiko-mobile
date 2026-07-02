package com.codder.ultimate.leaderboard;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemRanklistBinding;
import com.codder.ultimate.guestuser.activity.GuestActivity;
import com.codder.ultimate.retrofit.Const;

public class LeaderboardAdapter extends ListAdapter<LeaderboardDataRoot.DataItem, LeaderboardAdapter.MyViewHolder> {

    private final Context context;
    private String type = "spending"; // "user" | "host" | "agency"

    public interface OnItemClickListener {
        void onItemClick(LeaderboardDataRoot.DataItem item, int position);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<LeaderboardDataRoot.DataItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull LeaderboardDataRoot.DataItem oldItem,
                                               @NonNull LeaderboardDataRoot.DataItem newItem) {
                    // Use a stable unique id from your model
                    return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull LeaderboardDataRoot.DataItem oldItem,
                                                  @NonNull LeaderboardDataRoot.DataItem newItem) {
                    // Compare fields that affect the row UI
                    if (oldItem.getName() != null ? !oldItem.getName().equals(newItem.getName()) : newItem.getName() != null)
                        return false;
                    if (oldItem.getImage() != null ? !oldItem.getImage().equals(newItem.getImage()) : newItem.getImage() != null)
                        return false;
                    if (oldItem.getTotalEarnrCoin() != newItem.getTotalEarnrCoin()) return false;
                    if (oldItem.getTotalSpentDiamond() != newItem.getTotalSpentDiamond()) return false;
                    if (oldItem.getFinalTotalAmount() != newItem.getFinalTotalAmount()) return false;
                    // Agency block (null-safe)
                    if (oldItem.getAgency() != null && newItem.getAgency() != null) {
                        if (oldItem.getAgency().getName() != null ? !oldItem.getAgency().getName().equals(newItem.getAgency().getName()) : newItem.getAgency().getName() != null)
                            return false;
                        if (oldItem.getAgency().getImage() != null ? !oldItem.getAgency().getImage().equals(newItem.getAgency().getImage()) : newItem.getAgency().getImage() != null)
                            return false;
                    } else if (oldItem.getAgency() != newItem.getAgency()) {
                        return false;
                    }
                    return true;
                }
            };

    public LeaderboardAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    public void setType(String type) {
        this.type = type;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranklist, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        final ItemRanklistBinding binding;
        private final RequestOptions glideOpts = new RequestOptions()
                .placeholder(R.drawable.profile_placeholder)   // ← replace with your drawable
                .error(R.drawable.profile_placeholder);              // ← replace with your drawable

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView.getRootView());
        }

        void bind(LeaderboardDataRoot.DataItem dataItem, int position) {
            if (dataItem == null || binding == null) return;

            // List ranks start from 4 since top 1–3 are shown above
            binding.tvCount.setText(String.valueOf(position + 4));

            if (position == 0){
                binding.layMain.setBackground(ContextCompat.getDrawable(context,R.drawable.leaderboard_bg1));
            } else if (position == 1) {
                binding.layMain.setBackground(ContextCompat.getDrawable(context,R.drawable.leaderboard_bg2));
            }else {
                binding.layMain.setBackground(ContextCompat.getDrawable(context,R.drawable.bg_12dp));
            }

            if ("agency".equals(type)) {
                if (dataItem.getAgency() != null) {
                    itemView.setOnClickListener(null);
                    Glide.with(context)
                            .load(resolveImage(dataItem.getAgency().getImage()))
                            .apply(glideOpts)
                            .circleCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(false)
                            .dontAnimate()
                            .into(binding.ivUser);
                    binding.tvName.setText(dataItem.getAgency().getName());
                    binding.tvDiamond.setText(formatNumber(dataItem.getFinalTotalAmount()));
                }
            } else {
                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onItemClick(dataItem, getBindingAdapterPosition());
                });

                Glide.with(context)
                        .load(resolveImage(dataItem.getImage()))
                        .apply(glideOpts)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .skipMemoryCache(false)
                        .dontAnimate()
                        .into(binding.ivUser);

                binding.tvName.setText(dataItem.getName());
                binding.tvDiamond.setText("user".equals(type)
                        ? formatNumber(dataItem.getTotalSpentDiamond())
                        : formatNumber(dataItem.getTotalEarnrCoin()));
            }
        }
    }

    public String formatNumber(double number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000d);
        } else if (number >= 10_000) {
            return String.format("%.1fK", number / 1_000d);
        } else {
            return String.valueOf((int) number);
        }
    }

    private Object resolveImage(Object source) {
        if (source == null) return null;
        if (source instanceof Integer) return source;
        if (source instanceof String s) {
            try {
                return Integer.valueOf(Integer.parseInt(s));
            } catch (NumberFormatException ignored) {
                return s;
            }
        }
        return source;
    }

}