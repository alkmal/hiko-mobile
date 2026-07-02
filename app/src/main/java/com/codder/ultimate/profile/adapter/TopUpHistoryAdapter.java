package com.codder.ultimate.profile.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.databinding.ItemSellerOfflineRechargeHistoryBinding;
import com.codder.ultimate.profile.modelclass.CoinSellerHistoryRoot;

public class TopUpHistoryAdapter extends ListAdapter<CoinSellerHistoryRoot.HistoryItem, TopUpHistoryAdapter.HistoryViewHolder> {

    public TopUpHistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<CoinSellerHistoryRoot.HistoryItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull CoinSellerHistoryRoot.HistoryItem oldItem,
                                               @NonNull CoinSellerHistoryRoot.HistoryItem newItem) {
                    return oldItem.getUniqueId().equals(newItem.getUniqueId()) &&
                            oldItem.getDate().equals(newItem.getDate());
                }

                @SuppressLint("DiffUtilEquals")
                @Override
                public boolean areContentsTheSame(@NonNull CoinSellerHistoryRoot.HistoryItem oldItem,
                                                  @NonNull CoinSellerHistoryRoot.HistoryItem newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSellerOfflineRechargeHistoryBinding itemBinding = ItemSellerOfflineRechargeHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HistoryViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemSellerOfflineRechargeHistoryBinding binding;

        public HistoryViewHolder(@NonNull ItemSellerOfflineRechargeHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        public void bind(CoinSellerHistoryRoot.HistoryItem item) {
            if (item == null) return;

            binding.tvAmount.setText("-" + RayziUtils.formatCoin(item.getCoin()));
            binding.tvTime.setText(item.getDate());

            String name = item.getName() != null ? item.getName() : "";
            String uid = item.getUniqueId() != null ? item.getUniqueId() : "";
            binding.tvText.setText(String.format("%s (%s)", name, uid));

            Glide.with(binding.getRoot())
                    .load(item.getImage())
                    .circleCrop()
                    .into(binding.image);
        }
    }
}
