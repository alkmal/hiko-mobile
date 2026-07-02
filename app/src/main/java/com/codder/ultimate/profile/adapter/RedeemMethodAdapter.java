package com.codder.ultimate.profile.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemRedeemMethodBinding;

public class RedeemMethodAdapter extends ListAdapter<String, RedeemMethodAdapter.ReedemMethodViewHolder> {

    private final OnRedeemMethodClickListener onRedeemMethodClickListener;
    private int selectedPos = 0;
    private Context context;

    public RedeemMethodAdapter(@NonNull OnRedeemMethodClickListener onRedeemMethodClickListener) {
        super(DIFF_CALLBACK);
        this.onRedeemMethodClickListener = onRedeemMethodClickListener;
    }

    @NonNull
    @Override
    public ReedemMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemRedeemMethodBinding binding = ItemRedeemMethodBinding.inflate(inflater, parent, false);
        return new ReedemMethodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReedemMethodViewHolder holder, int position) {
        String method = getItem(position);
        holder.bind(method, position);
    }

    public interface OnRedeemMethodClickListener {
        void onMethodChange(String method);
    }

    public class ReedemMethodViewHolder extends RecyclerView.ViewHolder {
        private final ItemRedeemMethodBinding binding;

        public ReedemMethodViewHolder(@NonNull ItemRedeemMethodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String method, int position) {
            binding.tvText.setText(method);

            if (selectedPos == position) {
                binding.tvText.setTextColor(ContextCompat.getColor(context, R.color.white));
                binding.lytMain.setBackground(ContextCompat.getDrawable(context, R.drawable.home_tab_selectedbg));
            } else {
                binding.tvText.setTextColor(ContextCompat.getColor(context, R.color.white_76));
                binding.lytMain.setBackground(ContextCompat.getDrawable(context, R.drawable.home_tab_unselectedbg));
            }

            binding.getRoot().setOnClickListener(v -> {
                if (selectedPos != position) {
                    selectedPos = position;
                    onRedeemMethodClickListener.onMethodChange(method);
                    notifyDataSetChanged();
                }
            });
        }
    }

    private static final DiffUtil.ItemCallback<String> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equalsIgnoreCase(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equalsIgnoreCase(newItem);
        }
    };
}

