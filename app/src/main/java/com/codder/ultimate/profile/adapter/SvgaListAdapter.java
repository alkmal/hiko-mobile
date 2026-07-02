package com.codder.ultimate.profile.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.ItemSvgaListBinding;
import com.codder.ultimate.profile.modelclass.SvgaListRoot;

public class SvgaListAdapter extends ListAdapter<SvgaListRoot.DataItem, SvgaListAdapter.SvgaViewHolder> {
    private static final String TAG = "SvgaListAdapter";

    private Context context;
    private SessionManager sessionManager;
    private onSvgaClickListener onSvgaClickListener;

    public SvgaListAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnSvgaClickListener(SvgaListAdapter.onSvgaClickListener onSvgaClickListener) {
        this.onSvgaClickListener = onSvgaClickListener;
    }

    @NonNull
    @Override
    public SvgaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        sessionManager = new SessionManager(context);
        View view = LayoutInflater.from(context).inflate(R.layout.item_svga_list, parent, false);
        return new SvgaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SvgaViewHolder holder, int position) {
        holder.setData(getItem(position));
    }

    public class SvgaViewHolder extends RecyclerView.ViewHolder {
        ItemSvgaListBinding binding;

        public SvgaViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSvgaListBinding.bind(itemView);
        }

        public void setData(SvgaListRoot.DataItem svgaItem) {
            binding.svgaName.setText(svgaItem.getName());
            binding.priceDiamonds.setText(RayziUtils.formatCoin(svgaItem.getDiamond()));
            binding.validationTag.setText(" /" + svgaItem.getValidationTag());

            Glide.with(context).load(BuildConfig.BASE_URL + svgaItem.getThumbnail()).into(binding.svgImage);

            Log.d(TAG, "setData: isPurchase=" + svgaItem.isIsPurchase() + ", isSelected=" + svgaItem.isIsSelected());

            if (svgaItem.isIsPurchase()) {
                binding.btnPurchase.setText(R.string.select);
                binding.btnPurchase.setBackground(ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.btn_bg));
                binding.btnPurchase.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }

            if (svgaItem.isIsSelected()) {
                binding.btnPurchase.setText(R.string.selected);
                binding.btnPurchase.setBackground(ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.selected_btn_bg));
            }

            binding.btnPurchase.setOnClickListener(v -> {
                if (svgaItem.isIsPurchase() && !svgaItem.isIsSelected()) {
                    for (SvgaListRoot.DataItem item : getCurrentList()) {
                        item.setSelected(false);
                    }
                }
                if (onSvgaClickListener != null)
                    onSvgaClickListener.onPurchaseClick(svgaItem, binding);
            });

            binding.svgImage.setOnClickListener(v -> {
                if (onSvgaClickListener != null)
                    onSvgaClickListener.onSvgaClick(svgaItem);
            });
        }
    }

    public interface onSvgaClickListener {
        void onPurchaseClick(SvgaListRoot.DataItem svgaItem, ItemSvgaListBinding binding);
        void onSvgaClick(SvgaListRoot.DataItem svgaItem);
    }

    private static final DiffUtil.ItemCallback<SvgaListRoot.DataItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull SvgaListRoot.DataItem oldItem, @NonNull SvgaListRoot.DataItem newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull SvgaListRoot.DataItem oldItem, @NonNull SvgaListRoot.DataItem newItem) {
                    return oldItem.equals(newItem);
                }
            };
}
