package com.codder.ultimate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemEffectBinding;

import java.util.List;

public class EffectAdapter extends RecyclerView.Adapter<EffectAdapter.VH> {

    public interface OnEffectClick {
        void onClick(int position);
    }

    private final List<String> names;
    private final List<Integer> images;
    private final OnEffectClick listener;
    private final Context mContext;
    private int selected = 0;

    public EffectAdapter(Context mContext, List<String> names, List<Integer> images, OnEffectClick listener) {
        this.mContext = mContext;
        this.names = names;
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemEffectBinding binding = ItemEffectBinding.inflate(inflater, parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        h.binding.tvEffectName.setText(names.get(pos));

        Integer res = images.get(pos);
        int resId = (res == null) ? 0 : res;
        if (resId != 0) {
            h.binding.imgEffect.setImageResource(resId);
        } else {
            h.binding.imgEffect.setImageDrawable(null);
        }


        h.binding.layBg.setBackground(pos == selected ? ContextCompat.getDrawable(mContext,R.drawable.ic_selected_filter) : null );

        h.binding.getRoot().setOnClickListener(v -> {
            int old = selected;
            selected = h.getBindingAdapterPosition();
            if (old != RecyclerView.NO_POSITION) notifyItemChanged(old);
            if (selected != RecyclerView.NO_POSITION) notifyItemChanged(selected);
            if (listener != null && selected != RecyclerView.NO_POSITION) listener.onClick(selected);
        });
    }


    @Override
    public int getItemCount() {
        return names.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemEffectBinding binding;

        VH(@NonNull ItemEffectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

