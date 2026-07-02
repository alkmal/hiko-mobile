package com.codder.ultimate.profile.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.text.TextUtilsCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.ItemPurchaseCoinBinding;
import com.codder.ultimate.profile.modelclass.DiamondPlanRoot;
import com.codder.ultimate.retrofit.Const;

import java.util.Locale;

public class CoinPurchaseAdapter extends ListAdapter<DiamondPlanRoot.DiamondPlanItem, CoinPurchaseAdapter.CoinViewHolder> {

    private final OnBuyCoinClickListener onBuyCoinClickListener;

    private String selectedPlanId = null;

    public CoinPurchaseAdapter(@NonNull OnBuyCoinClickListener onBuyCoinClickListener) {
        super(DIFF_CALLBACK);
        this.onBuyCoinClickListener = onBuyCoinClickListener;
    }

    @NonNull
    @Override
    public CoinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPurchaseCoinBinding binding = ItemPurchaseCoinBinding.inflate(inflater, parent, false);
        return new CoinViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CoinViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static final DiffUtil.ItemCallback<DiamondPlanRoot.DiamondPlanItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull DiamondPlanRoot.DiamondPlanItem oldItem, @NonNull DiamondPlanRoot.DiamondPlanItem newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull DiamondPlanRoot.DiamondPlanItem oldItem, @NonNull DiamondPlanRoot.DiamondPlanItem newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public interface OnBuyCoinClickListener {
        void onBuyClick(DiamondPlanRoot.DiamondPlanItem plan);
    }

    public class CoinViewHolder extends RecyclerView.ViewHolder {
        private final ItemPurchaseCoinBinding binding;

        public CoinViewHolder(@NonNull ItemPurchaseCoinBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("ClickableViewAccessibility")
        public void bind(DiamondPlanRoot.DiamondPlanItem plan) {
            if (plan == null) return;

            Context context = binding.getRoot().getContext();
            SessionManager sessionManager = new SessionManager(context);

            String currencySymbol = sessionManager.getSetting().getCurrency().getSymbol() + " ";
            String price = String.valueOf(plan.getDollar());

            binding.tvCoin.setText(String.valueOf(plan.getDiamonds()));
            binding.tvAmount.setText(currencySymbol + price);

            if (plan.getTag() != null && !plan.getTag().trim().isEmpty()) {
                binding.layTag.setVisibility(View.VISIBLE);
                binding.tvTag.setText(plan.getTag());
            } else {
                binding.layTag.setVisibility(View.GONE);
            }


            binding.getRoot().setOnTouchListener((v, e) -> {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setForeground(ContextCompat.getDrawable(v.getContext(), R.drawable.coin_hover_overlay));
                } else if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.setForeground(null);
                }
                return false;
            });


            binding.getRoot().setOnClickListener(v -> {
                if (getBindingAdapterPosition() != RecyclerView.NO_POSITION) {

                    onBuyCoinClickListener.onBuyClick(plan);
                }
            });
        }
    }
}