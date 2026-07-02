package com.codder.ultimate.profile.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.databinding.ItemRedeemHistoryBinding;
import com.codder.ultimate.profile.modelclass.ReedemListRoot;
import com.codder.ultimate.retrofit.Const;

public class RedeemHistoryAdapter extends ListAdapter<ReedemListRoot.RedeemItem, RedeemHistoryAdapter.ReedemHistoryViewHolder> {

    public RedeemHistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ReedemListRoot.RedeemItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull ReedemListRoot.RedeemItem oldItem, @NonNull ReedemListRoot.RedeemItem newItem) {
                    return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull ReedemListRoot.RedeemItem oldItem, @NonNull ReedemListRoot.RedeemItem newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public ReedemHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemRedeemHistoryBinding binding = ItemRedeemHistoryBinding.inflate(inflater, parent, false);
        return new ReedemHistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReedemHistoryViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public static class ReedemHistoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemRedeemHistoryBinding binding;

        public ReedemHistoryViewHolder(@NonNull ItemRedeemHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ReedemListRoot.RedeemItem item) {
            if (item == null) return;

            binding.tvCoin.setText(String.format("%s%s", RayziUtils.formatCoin(item.getRCoin()), Const.CoinName));
            binding.tvdate.setText(item.getDate() != null ? item.getDate() : "N/A");
            binding.tvPaymentGateway.setText(item.getPaymentGateway() != null ? item.getPaymentGateway() : "Unknown");

            // label stays as before
            String label = parseStatus(item.getStatus());
            binding.tvStatus.setText(label);

            // color only
            int colorRes = colorResForStatus(item.getStatus());
            binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(binding.tvStatus.getContext(), colorRes)));
        }

        private int colorResForStatus(String status) {
            if (status == null) return R.color.status_unknown;
            switch (status.trim().toLowerCase()) {
                case "0":
                case "pending":
                    return R.color.status_pending;
                case "1":
                case "accepted":
                    return R.color.status_accepted;
                case "2":
                case "decline":
                case "declined":
                    return R.color.status_declined;
                default:
                    return R.color.status_unknown;
            }
        }

        private String parseStatus(String status) {
            if (status == null) return "Unknown";
            switch (status.toLowerCase()) {
                case "0":
                case "pending":
                    return "Pending";
                case "1":
                case "accepted":
                    return "Accepted";
                case "2":
                case "decline":
                case "declined":
                    return "Declined";
                default:
                    return status;
            }
        }
    }
}

