package com.codder.ultimate.profile.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.ItemAvatarListBinding;
import com.codder.ultimate.profile.modelclass.SvgaListRoot;

public class AvatarListAdapter extends ListAdapter<SvgaListRoot.DataItem, AvatarListAdapter.SvgaViewHolder> {

    private final SessionManager sessionManager;
    private OnAvatarClickListener onAvatarClickListener;

    public AvatarListAdapter(SessionManager sessionManager, OnAvatarClickListener listener) {
        super(DIFF_CALLBACK);
        this.sessionManager = sessionManager;
        this.onAvatarClickListener = listener;
    }

    public void setOnAvatarClickListener(OnAvatarClickListener listener) {
        this.onAvatarClickListener = listener;
    }

    @NonNull
    @Override
    public SvgaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAvatarListBinding binding = ItemAvatarListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SvgaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SvgaViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class SvgaViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        private final ItemAvatarListBinding binding;

        public SvgaViewHolder(@NonNull ItemAvatarListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SvgaListRoot.DataItem svgaItem) {
            if (svgaItem == null) return;

            binding.svgaName.setText(svgaItem.getName());
            binding.priceDiamonds.setText(RayziUtils.formatCoin(svgaItem.getDiamond()));
            binding.validationTag.setText(" /" + svgaItem.getValidationTag());

            if (sessionManager != null && sessionManager.getUser() != null) {
                Glide.with(binding.getRoot().getContext())
                        .load(sessionManager.getUser().getImage())
                        .circleCrop()
                        .into(binding.userImage);
            }

            Glide.with(binding.getRoot().getContext())
                    .load(BuildConfig.BASE_URL + svgaItem.getImage())
                    .into(binding.avatarFrame);

            if (svgaItem.isIsPurchase()) {
                binding.btnPurchase.setText(R.string.select);
                binding.btnPurchase.setBackground(ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.btn_bg));
                binding.btnPurchase.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }

            if (svgaItem.isIsSelected()) {
                binding.btnPurchase.setText(R.string.selected);
                binding.btnPurchase.setBackground(ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.selected_btn_bg));
                Drawable endDrawable = ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.icon_selected);
                binding.btnPurchase.setCompoundDrawablesWithIntrinsicBounds(null, null, endDrawable, null);
            }

            binding.btnPurchase.setOnClickListener(v -> {
                if (svgaItem.isIsPurchase() && !svgaItem.isIsSelected()) {
                    for (int i = 0; i < getCurrentList().size(); i++) {
                        SvgaListRoot.DataItem item = getCurrentList().get(i);
                        if (item.isIsSelected()) {
                            item.setSelected(false);
                        }
                    }
                }
                onAvatarClickListener.onAvatarClick(svgaItem, binding);
            });
        }
    }

    public interface OnAvatarClickListener {
        void onAvatarClick(SvgaListRoot.DataItem svgaItem, ItemAvatarListBinding binding);
    }

    private static final DiffUtil.ItemCallback<SvgaListRoot.DataItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SvgaListRoot.DataItem>() {
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
