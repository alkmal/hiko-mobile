package com.codder.ultimate.profile.adapter;

import static com.codder.ultimate.RayziUtils.formatCoin;
import static com.codder.ultimate.activity.BaseActivity.isRTL;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.ItemVipPlanBinding;
import com.codder.ultimate.profile.modelclass.VipPlanRoot;

import java.util.Locale;

public class VipPlanAdapter extends ListAdapter<VipPlanRoot.VipPlanItem, VipPlanAdapter.VipPlanViewHolder> {

    private OnPlanClickListener onPlanClickListener;
    private int selectedPosition = 0;

    public VipPlanAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnPlanClickListener(OnPlanClickListener listener) {
        this.onPlanClickListener = listener;
    }

    @NonNull
    @Override
    public VipPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemVipPlanBinding binding = ItemVipPlanBinding.inflate(inflater, parent, false);
        return new VipPlanViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VipPlanViewHolder holder, int position) {
        VipPlanRoot.VipPlanItem plan = getItem(position);
        holder.bind(plan, position == selectedPosition);

        Log.d("VipPlanAdapter", "Binding item at position: " + position + ", Plan ID: " + getItem(position).getId());

        holder.binding.getRoot().setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();

            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            if (onPlanClickListener != null) {
                onPlanClickListener.onPlanClick(plan);
            }
        });
    }

    public interface OnPlanClickListener {
        void onPlanClick(VipPlanRoot.VipPlanItem plan);
    }

    static class VipPlanViewHolder extends RecyclerView.ViewHolder {
        final ItemVipPlanBinding binding;

        VipPlanViewHolder(ItemVipPlanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(VipPlanRoot.VipPlanItem plan, boolean isSelected) {
            if (plan == null) return;

            Context context = binding.getRoot().getContext();
            SessionManager sessionManager = new SessionManager(context);

            String currencySymbol = sessionManager.getSetting().getCurrency().getSymbol() + " ";
            double amount = plan.getDollar();

            if (isSelected) {
                binding.mainRelativeLayout.setBackgroundResource(R.drawable.vip_plan_selected);
                binding.layAmount.setBackgroundResource(R.drawable.vip_plan_amount_selected);
            } else {
                binding.mainRelativeLayout.setBackgroundResource(R.drawable.plan_bg);
                binding.layAmount.setBackgroundResource(R.drawable.vip_plan_amount_unselected);
            }

            binding.tvDays.setText(String.valueOf(plan.getValidity()));
            binding.planTime.setText(plan.getValidityType());

            String formattedPrice = formatCoin(amount);
            binding.tvAmount.setText(currencySymbol + formattedPrice);

            Log.d("Calculation Debug", "setData: " + plan.getValidity());
            Log.d("Calculation Debug", "setData: " + plan.getDollar());
            Log.d("Calculation Debug", "setData: " + plan.getValidityType());

            double perDay = 0;

            if (plan.getValidity() > 0) {
                int totalDays;

                switch (plan.getValidityType().toLowerCase()) {
                    case "month":
                        totalDays = plan.getValidity() * 30; // Approximate 1 month = 30 days
                        break;
                    case "year":
                        totalDays = plan.getValidity() * 365; // Approximate 1 year = 365 days
                        break;
                    default:
                        totalDays = plan.getValidity(); // For "day" or unrecognized types
                        break;
                }

                perDay = amount / totalDays;


                String perDayText = (perDay % 1 == 0)
                        ? String.format(Locale.US, "%s %.0f/D", currencySymbol, perDay)
                        : String.format(Locale.US, "%s %.2f/D", currencySymbol, perDay);

                binding.tvAmountPerMonth.setText(perDayText);


            } else {
                binding.tvAmountPerMonth.setText(R.string.invalid_validity_period);
            }


            if (plan.isTop()) {
                binding.tvOfferTag1.setVisibility(View.VISIBLE);
                binding.tvOfferTag1.setText(R.string.hot);
            } else {
                binding.tvOfferTag1.setVisibility(View.GONE);
            }

            if (isRTL(context)) {
                binding.tvOfferTag1.setScaleX(-1);
            }
        }
    }

    private static final DiffUtil.ItemCallback<VipPlanRoot.VipPlanItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull VipPlanRoot.VipPlanItem oldItem, @NonNull VipPlanRoot.VipPlanItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull VipPlanRoot.VipPlanItem oldItem, @NonNull VipPlanRoot.VipPlanItem newItem) {
            return oldItem.getId().equals(newItem.getId()) &&
                    oldItem.getDollar() == newItem.getDollar() &&
                    oldItem.getValidity() == newItem.getValidity();
        }

    };
}
