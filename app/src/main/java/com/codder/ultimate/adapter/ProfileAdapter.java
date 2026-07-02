package com.codder.ultimate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.databinding.ItemProfileFeatureBinding;
import com.codder.ultimate.databinding.ItemRvprofileBinding;
import com.codder.ultimate.modelclass.ProfileRoot;

import java.util.List;

public class ProfileAdapter extends ListAdapter<ProfileRoot, ProfileAdapter.MyViewHolder> {

    private OnItemClickListener onItemClickListener;
    private final boolean isMyFeature;

    public ProfileAdapter(@NonNull List<ProfileRoot> list,boolean isMyFeature, @NonNull OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        submitList(list);
        this.isMyFeature = isMyFeature;
        this.onItemClickListener = listener;
    }

    private static final DiffUtil.ItemCallback<ProfileRoot> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ProfileRoot oldItem, @NonNull ProfileRoot newItem) {
            return oldItem.getGetText().equals(newItem.getGetText())
                    && oldItem.getGetImages() == newItem.getGetImages();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProfileRoot oldItem, @NonNull ProfileRoot newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (isMyFeature) {
            ItemProfileFeatureBinding binding =
                    ItemProfileFeatureBinding.inflate(inflater, parent, false);
            return new MyViewHolder(binding);
        } else {
            ItemRvprofileBinding binding =
                    ItemRvprofileBinding.inflate(inflater, parent, false);
            return new MyViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ProfileRoot item = getItem(position);
        holder.bind(item);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ItemRvprofileBinding hostVipBinding;
        ItemProfileFeatureBinding myFeatureBinding;

        MyViewHolder(ItemRvprofileBinding binding) {
            super(binding.getRoot());
            this.hostVipBinding  = binding;
        }
        MyViewHolder(ItemProfileFeatureBinding binding) {
            super(binding.getRoot());
            myFeatureBinding = binding;
        }

        void bind(ProfileRoot item) {


            int position = getBindingAdapterPosition();
            boolean isLastItem = position == getItemCount() - 1;

            if (hostVipBinding != null) {
                hostVipBinding.ivAgency.setImageResource(item.getGetImages());
                hostVipBinding.tvAgency.setText(item.getGetText());
            } else {
                myFeatureBinding.ivAgency.setImageResource(item.getGetImages());
                myFeatureBinding.tvAgency.setText(item.getGetText());

                myFeatureBinding.view.setVisibility(
                        isLastItem ? View.GONE : View.VISIBLE
                );
            }


            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(item.getGetText());
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onClick(String type);
    }
}
