package com.codder.ultimate.live.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.databinding.ItemEmojiGridBinding;
import com.codder.ultimate.live.model.GiftRoot;
import com.codder.ultimate.live.utils.OnEmojiSelectLister;

import java.util.ArrayList;
import java.util.List;

public class EmojiGridAdapter extends RecyclerView.Adapter<EmojiGridAdapter.EmojiViewHolder> {

    private static final String TAG = "EmojiGridAdapter";
    private Context context;
    private OnEmojiSelectLister onEmojiSelectLister;
    public final List<GiftRoot.GiftItem> giftRootDummies = new ArrayList<>();

    public OnEmojiSelectLister getOnEmojiSelectLister() {
        return onEmojiSelectLister;
    }

    public void setOnEmojiSelectLister(OnEmojiSelectLister onEmojiSelectLister) {
        this.onEmojiSelectLister = onEmojiSelectLister;
    }

    @NonNull
    @Override
    public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemEmojiGridBinding binding = ItemEmojiGridBinding.inflate(inflater, parent, false);
        return new EmojiViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return giftRootDummies.size();
    }

    /**
     * Safely adds data, clearing any previous data.
     */
    public void addData(List<?> newGifts) {
        giftRootDummies.clear();
        if (newGifts != null) {
            for (Object obj : newGifts) {
                if (obj instanceof GiftRoot.GiftItem) {
                    giftRootDummies.add((GiftRoot.GiftItem) obj);
                }
            }
        }
        notifyDataSetChanged();
    }

    class EmojiViewHolder extends RecyclerView.ViewHolder {
        private final ItemEmojiGridBinding binding;

        EmojiViewHolder(ItemEmojiGridBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(int position) {
            if (position < 0 || position >= giftRootDummies.size()) return;

            GiftRoot.GiftItem gift = giftRootDummies.get(position);
            if (gift == null) return;

            binding.tvCoin.setText(RayziUtils.formatCoin(gift.getCoin()));

            try {
                String imageUrl = (gift.getType() == 2) ? gift.getSvgaImage() : gift.getImage();
                if (!TextUtils.isEmpty(imageUrl)) {
                    String url = BuildConfig.BASE_URL + imageUrl;

                    Glide.with(context)
                            .load(url)
                            .placeholder(R.drawable.loadergif)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Log.d(TAG, "❌ Load failed: " + url, e);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    Log.d(TAG, "✅ Loaded from: " + dataSource.name());
                                    return false;
                                }
                            })
                            .into(binding.imgEmoji);
                }

            } catch (Exception e) {
                Log.e(TAG, "Glide image load failed", e);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (onEmojiSelectLister != null) {
                    onEmojiSelectLister.onEmojiSelect(binding, gift);
                } else {
                    Log.w(TAG, "onEmojiSelectLister is null. Emoji not handled.");
                }
            });
        }
    }
}
