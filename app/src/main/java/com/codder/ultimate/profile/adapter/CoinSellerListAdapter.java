package com.codder.ultimate.profile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemCoinSellerBinding;
import com.codder.ultimate.profile.modelclass.CoinSellerRoot;

public class CoinSellerListAdapter extends ListAdapter<CoinSellerRoot.CoinSellerItem, CoinSellerListAdapter.CoinSellerViewHolder> {

    private OnCoinSellerClickListener onCoinSellerClickListener;

    public CoinSellerListAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnCoinPlanClickListener(OnCoinSellerClickListener listener) {
        this.onCoinSellerClickListener = listener;
    }

    @NonNull
    @Override
    public CoinSellerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCoinSellerBinding binding = ItemCoinSellerBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CoinSellerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CoinSellerViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public static final DiffUtil.ItemCallback<CoinSellerRoot.CoinSellerItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull CoinSellerRoot.CoinSellerItem oldItem, @NonNull CoinSellerRoot.CoinSellerItem newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull CoinSellerRoot.CoinSellerItem oldItem, @NonNull CoinSellerRoot.CoinSellerItem newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public interface OnCoinSellerClickListener {
        void onCoinSellerClick(@NonNull CoinSellerRoot.CoinSellerItem item);
    }

    class CoinSellerViewHolder extends RecyclerView.ViewHolder {
        private final ItemCoinSellerBinding binding;

        CoinSellerViewHolder(ItemCoinSellerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CoinSellerRoot.CoinSellerItem item) {
            Context context = binding.getRoot().getContext();

            binding.nameCoinSeller.setText(item.getName() != null ? item.getName() : context.getString(R.string.unknown));

            Glide.with(context)
                    .load(item.getImage())
                    .placeholder(R.drawable.placeholder_live)
                    .apply(new RequestOptions()
                            .transform(new CenterCrop(), new RoundedCorners(16)))
                    .into(binding.profileCoinSeller);

            binding.getRoot().setOnClickListener(v -> {
                if (onCoinSellerClickListener != null && item != null) {
                    onCoinSellerClickListener.onCoinSellerClick(item);
                }
            });
        }
    }
}
