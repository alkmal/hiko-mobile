package com.codder.ultimate.live.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.databinding.ItemRecieveGiftBinding;
import com.codder.ultimate.live.model.GiftRoot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class GiftReceiveAdapter extends RecyclerView.Adapter<GiftReceiveAdapter.GiftViewHolder> {

    private final Context context;
    private final List<GiftRoot.GiftItem> giftList = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public GiftReceiveAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecieveGiftBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_recieve_gift,
                parent,
                false);
        return new GiftViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
        GiftRoot.GiftItem giftItem = getGiftItemAt(position);
        if (giftItem == null) return;

        Animation animLtoR = AnimationUtils.loadAnimation(context, R.anim.anim_slide_left_to_right_gift);
        holder.binding.layoutGiftAni.startAnimation(animLtoR);
        holder.binding.layoutGiftAni.setVisibility(android.view.View.VISIBLE);

        holder.binding.name.setText(giftItem.getName() != null ? giftItem.getName() : "");

        String receiverNames = "";
        if (giftItem.getReceiverUserName() != null && !giftItem.getReceiverUserName().isEmpty()) {
            receiverNames = giftItem.getReceiverUserName().toString()
                    .replace("[", "")
                    .replace("]", "");
        }
        holder.binding.tvReceivername.setText(context.getString(R.string.send_a_gift_to) + " " + receiverNames);

        // Load gift image with Glide
        try {
            String imageUrl = giftItem.getImage() != null ? BuildConfig.BASE_URL + giftItem.getImage() : "";
            if (imageUrl.endsWith(".gif")) {
                Glide.with(context)
                        .asGif()
                        .load(imageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .circleCrop()
                        .into(holder.binding.gift);
            } else {
                Glide.with(context)
                        .load(imageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .circleCrop()
                        .into(holder.binding.gift);
            }
        } catch (Exception e) {
            holder.binding.gift.setImageDrawable(null);
        }

        // Load gift count image
        try {
            int countImageRes = RayziUtils.getImageFromNumber(giftItem.getCount());
            if (countImageRes != 0) {
                Glide.with(context)
                        .load(countImageRes)
                        .into(holder.binding.imgGiftCount);
            } else {
                holder.binding.imgGiftCount.setImageDrawable(null);
            }
        } catch (Exception e) {
            holder.binding.imgGiftCount.setImageDrawable(null);
        }

        handler.postDelayed(() -> {
            if (holder.binding != null) {
                holder.binding.layoutGiftAni.clearAnimation();
                holder.binding.layoutGiftAni.setVisibility(android.view.View.GONE);
            }
        }, 4000);

        if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL) {
            holder.binding.imageview.setScaleX(-1f);
        } else {
            holder.binding.imageview.setScaleX(1f);
        }
    }

    @Override
    public int getItemCount() {
        return giftList.size();
    }

    private GiftRoot.GiftItem getGiftItemAt(int position) {
        if (position < 0 || position >= giftList.size()) return null;
        return giftList.get(position);
    }

    /**
     * Remove a gift item safely by reference equality or id equality if available.
     */
    public void remove(@NonNull GiftRoot.GiftItem gift) {
        if (gift == null || giftList.isEmpty()) return;

        Iterator<GiftRoot.GiftItem> iterator = giftList.iterator();
        boolean removed = false;
        while (iterator.hasNext()) {
            GiftRoot.GiftItem item = iterator.next();
            if (item == gift || (item.getId() != null && item.getId().equals(gift.getId()))) {
                iterator.remove();
                removed = true;
                break;
            }
        }
        if (removed) {
            notifyDataSetChanged();
        }
    }

    /**
     * Add gift items safely and notify properly.
     */
    public void addData(@NonNull List<GiftRoot.GiftItem> gifts) {
        if (gifts == null || gifts.isEmpty()) return;
        int startPosition = giftList.size();
        giftList.addAll(gifts);
        notifyItemRangeInserted(startPosition, gifts.size());
    }

    public void clear() {
        giftList.clear();
        notifyDataSetChanged();
    }

    static class GiftViewHolder extends RecyclerView.ViewHolder {
        final ItemRecieveGiftBinding binding;

        GiftViewHolder(@NonNull ItemRecieveGiftBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
