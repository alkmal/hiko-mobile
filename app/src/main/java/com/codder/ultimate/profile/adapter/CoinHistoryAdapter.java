package com.codder.ultimate.profile.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.databinding.ItemCoinHistoryBinding;
import com.codder.ultimate.guestuser.activity.GuestActivity;
import com.codder.ultimate.profile.modelclass.HistoryListRoot;
import com.codder.ultimate.retrofit.Const;

public class CoinHistoryAdapter extends ListAdapter<HistoryListRoot.HistoryItem, CoinHistoryAdapter.CoinHistoryViewHolder> {

    private String coinType = "";

    public CoinHistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setCoinType(String coinType) {
        this.coinType = coinType;
    }

    @NonNull
    @Override
    public CoinHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCoinHistoryBinding binding = ItemCoinHistoryBinding.inflate(inflater, parent, false);
        return new CoinHistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CoinHistoryViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static final DiffUtil.ItemCallback<HistoryListRoot.HistoryItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull HistoryListRoot.HistoryItem oldItem, @NonNull HistoryListRoot.HistoryItem newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull HistoryListRoot.HistoryItem oldItem, @NonNull HistoryListRoot.HistoryItem newItem) {
                    return oldItem.equals(newItem);
                }
            };

    class CoinHistoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCoinHistoryBinding binding;

        public CoinHistoryViewHolder(@NonNull ItemCoinHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(HistoryListRoot.HistoryItem item) {
            Context context = binding.getRoot().getContext();

            if (item == null) return;

            // Set amount with sign and color
            String amountPrefix = item.isIsAdd() ? "+" : "-";
            String signedDiamondValue = item.getDiamond() != 0 ? amountPrefix : " ";
            String signedRCoinValue = item.getRCoin() != 0 ? amountPrefix : " ";

            int amountColor = item.isIsAdd() ? R.color.green : R.color.red;

            String amount = coinType.equals(Const.DIAMOND)
                    ? signedDiamondValue + RayziUtils.formatCoin(item.getDiamond())
                    : signedRCoinValue + RayziUtils.formatCoin(item.getRCoin());

            binding.tvAmount.setText(amount);
            binding.tvAmount.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, amountColor)));

            // Set date
            binding.tvTime.setText(item.getDate());

            // Set username if available
            if (item.getUserName() != null && !item.getUserName().isEmpty()) {
                binding.tvUserName.setVisibility(View.VISIBLE);
                binding.tvUserName.setText(Html.fromHtml("<font color='#FFFFFF'>by </font>@" + item.getUserName()));
                binding.tvUserName.setOnClickListener(v -> {
                    Intent intent = new Intent(context, GuestActivity.class);
                    intent.putExtra(Const.USERID, item.getUserId());
                    context.startActivity(intent);
                });
            } else {
                binding.tvUserName.setVisibility(View.GONE);
            }

            // Set title and icon
            setTitleFromType(context, item);
        }

        private void setTitleFromType(Context context, HistoryListRoot.HistoryItem item) {
            if (item == null) return;

            int type = item.getType();

            switch (type) {
                case 0:
                    handleGiftTransaction(context, item);
                    binding.image.setImageResource(R.drawable.ic_gift);
                    break;
                case 1:
                    binding.tvText.setText(Const.CoinName + context.getString(R.string.converted_to_diamonds));
                    binding.image.setImageResource(R.drawable.rcoin);
                    break;
                case 2:
                    binding.tvText.setText(R.string.diamond_purchase);
                    binding.image.setImageResource(R.drawable.diamond);
                    break;
                case 3:
                    binding.tvText.setText(R.string.you_have_called);
                    binding.image.setImageResource(R.drawable.videocall);
                    break;
                case 4:
                    setPositiveItem(context, R.string.watching_ads, R.drawable.ads);
                    break;
                case 5:
                    setPositiveItem(context, R.string.login_bonus, R.drawable.moneybag);
                    break;
                case 6:
                    setPositiveItem(context, R.string.referral_bonus, R.drawable.moneybag);
                    break;
                case 7:
                    setNegativeItem(context, R.string.cash_out, R.drawable.withdraw);
                    break;
                case 8:
                    handleAdminTransaction(context, item);
                    break;
                case 9:
                    setNegativeItem(context, R.string.svga_animation_purchased, R.drawable.diamond);
                    break;
                case 10:
                    setNegativeItem(context, R.string.add_in_teenpatti_game, R.drawable.diamond);
                    break;
                case 11:
                    setNegativeItem(context, R.string.purchased_avatar_frame, R.drawable.diamond);
                    break;
                case 12:
                    setPositiveItem(context, R.string.coin_add_by_offline_recharge, R.drawable.diamond);
                    break;
                case 13:
                    binding.tvText.setText(R.string.call_coin_received);
                    binding.image.setImageResource(R.drawable.diamond);
                    break;
                case 14:
                    setPositiveItem(context, R.string.lucy_gift_bonus, R.drawable.diamond);
                    break;
                case 15:
                    setNegativeItem(context, R.string.add_in_roulette_game, R.drawable.diamond);
                    break;
                case 16:
                    setNegativeItem(context, R.string.add_in_ferry_wheel_game, R.drawable.diamond);
                    break;
                case 17:
                    setNegativeItem(context, R.string.convert_in_coin_seller_coin, R.drawable.diamond);
                    break;
                case 18:
                    binding.tvText.setText(R.string.chat_gift);
                    binding.image.setImageResource(R.drawable.diamond);
                    break;
                case 19:
                    binding.tvText.setText(R.string.video_call_gift);
                    binding.image.setImageResource(R.drawable.diamond);
                    break;
                case 20:
                    binding.tvText.setText("Audio call");
                    binding.image.setImageResource(R.drawable.videocall);
                    break;
                default:
                    binding.tvText.setText(R.string.unknown_transaction);
                    binding.image.setImageResource(R.drawable.diamond);
                    break;
            }
        }

        private void handleGiftTransaction(Context context, HistoryListRoot.HistoryItem item) {
            if (item.getUserId() == null || item.getUserId().isEmpty()) {
                if (!item.isIsAdd()) {
                    binding.tvText.setText(R.string.gift_broadcast_during_livestream_by_you);
                }
            } else if (item.isIsAdd()) {
                binding.tvText.setText(R.string.gift_received);
            } else {
                binding.tvText.setText(R.string.gift_send_to);
            }
        }

        private void handleAdminTransaction(Context context, HistoryListRoot.HistoryItem item) {
            if (item.isIsAdd()) {
                binding.tvText.setText(R.string.added_by_admin);
                binding.tvAmount.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green)));
            } else {
                binding.tvText.setText(R.string.reduce_by_admin);
                binding.tvAmount.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
            }

            if (item.getRCoin() == 0) {
                binding.image.setImageResource(R.drawable.diamond);
            } else {
                binding.image.setImageResource(R.drawable.rcoin);
            }
        }

        private void setPositiveItem(Context context, int stringRes, int drawableRes) {
            binding.tvText.setText(stringRes);
            binding.image.setImageResource(drawableRes);
            binding.tvAmount.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green)));
        }

        private void setNegativeItem(Context context, int stringRes, int drawableRes) {
            binding.tvText.setText(stringRes);
            binding.image.setImageResource(drawableRes);
            binding.tvAmount.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
        }
    }
}

