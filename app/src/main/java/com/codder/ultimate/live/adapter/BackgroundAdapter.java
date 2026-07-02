package com.codder.ultimate.live.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemAudioBgBinding;
import com.codder.ultimate.live.model.ThemeRoot;

import java.util.Objects;

public class BackgroundAdapter extends ListAdapter<ThemeRoot.ThemeItem, BackgroundAdapter.BgHolder> {

    private Context context;
    private OnImageClickListener onImageClickListener;

    public BackgroundAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.onImageClickListener = listener;
    }

    @NonNull
    @Override
    public BgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemAudioBgBinding binding = ItemAudioBgBinding.inflate(inflater, parent, false);
        return new BgHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BgHolder holder, int position) {
        ThemeRoot.ThemeItem item = getItem(position);
        if (item == null || item.getTheme() == null) {
            holder.binding.image.setImageDrawable(null);
            holder.binding.getRoot().setOnClickListener(null);
            return;
        }

        try {
            Glide.with(context)
                    .load(BuildConfig.BASE_URL + item.getTheme())
                    .placeholder(R.drawable.placeholder)
                    .into(holder.binding.image);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            if (onImageClickListener != null) {
                onImageClickListener.onClick(item.getTheme());
            }
        });
    }

    public interface OnImageClickListener {
        void onClick(@NonNull String image);
    }

    static class BgHolder extends RecyclerView.ViewHolder {
        final ItemAudioBgBinding binding;

        BgHolder(@NonNull ItemAudioBgBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<ThemeRoot.ThemeItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull ThemeRoot.ThemeItem oldItem, @NonNull ThemeRoot.ThemeItem newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull ThemeRoot.ThemeItem oldItem, @NonNull ThemeRoot.ThemeItem newItem) {
                    return oldItem.equals(newItem);
                }
            };
}
