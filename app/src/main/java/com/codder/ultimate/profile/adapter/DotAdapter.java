package com.codder.ultimate.profile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemDotsBinding;

public class DotAdapter extends RecyclerView.Adapter<DotAdapter.DotViewHolder> {

    private final int slideCount;
    private final int activeColorResId;
    private int selectedPosition = 0;

    public DotAdapter(int slideCount, @ColorRes int activeColorResId) {
        this.slideCount = slideCount;
        this.activeColorResId = activeColorResId;
    }

    @NonNull
    @Override
    public DotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemDotsBinding binding = ItemDotsBinding.inflate(inflater, parent, false);
        return new DotViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DotViewHolder holder, int position) {
        Context context = holder.itemView.getContext();

        boolean isSelected = position == selectedPosition;

        ViewGroup.LayoutParams params = holder.binding.dot.getLayoutParams();

        if (isSelected) {
            params.width = dpToPx(holder.itemView.getContext(), 22);
            params.height = dpToPx(holder.itemView.getContext(), 8);
            holder.binding.dot.setBackgroundResource(R.drawable.dot_selected);
        } else {
            params.width = dpToPx(holder.itemView.getContext(), 8);
            params.height = dpToPx(holder.itemView.getContext(), 8);
            holder.binding.dot.setBackgroundResource(R.drawable.dot_unselected);
        }

        holder.binding.dot.setLayoutParams(params);


    }
    private int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }


    @Override
    public int getItemCount() {
        return slideCount;
    }

    public void changeDot(int scrollPosition) {
        selectedPosition = scrollPosition;
        notifyDataSetChanged();
    }

    public static class DotViewHolder extends RecyclerView.ViewHolder {
        final ItemDotsBinding binding;

        public DotViewHolder(@NonNull ItemDotsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}