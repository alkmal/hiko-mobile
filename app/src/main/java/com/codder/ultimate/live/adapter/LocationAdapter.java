package com.codder.ultimate.live.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.databinding.ItemLocationBinding;
import com.codder.ultimate.live.model.SearchLocationRoot;

public class LocationAdapter extends ListAdapter<SearchLocationRoot.DataItem, LocationAdapter.LocationViewHolder> {

    private OnLocationClickListener onLocationClickListener;
    private Context context;

    public LocationAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<SearchLocationRoot.DataItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<SearchLocationRoot.DataItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull SearchLocationRoot.DataItem oldItem, @NonNull SearchLocationRoot.DataItem newItem) {
            return oldItem.getLabel().equals(newItem.getLabel());
        }

        @Override
        public boolean areContentsTheSame(@NonNull SearchLocationRoot.DataItem oldItem, @NonNull SearchLocationRoot.DataItem newItem) {
            return oldItem.getLabel().equals(newItem.getLabel()) &&
                    oldItem.getContinent().equals(newItem.getContinent()) &&
                    oldItem.getCountry().equals(newItem.getCountry()) &&
                    oldItem.getCounty().equals(newItem.getCounty()) &&
                    oldItem.getLocality().equals(newItem.getLocality()) &&
                    oldItem.getType().equals(newItem.getType()) &&
                    oldItem.getCountryCode().equals(newItem.getCountryCode()) &&
                    oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getRegion().equals(newItem.getRegion()) &&
                    oldItem.getRegionCode().equals(newItem.getRegionCode());
        }
    };

    public void setOnLocationClickListener(OnLocationClickListener onLocationClickListener) {
        this.onLocationClickListener = onLocationClickListener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ItemLocationBinding binding = ItemLocationBinding.inflate(LayoutInflater.from(context), parent, false);
        return new LocationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        SearchLocationRoot.DataItem location = getItem(position);
        holder.setData(location);
    }

    public interface OnLocationClickListener {
        void onLocationClick(SearchLocationRoot.DataItem location);
    }

    public class LocationViewHolder extends RecyclerView.ViewHolder {
        private ItemLocationBinding binding;

        public LocationViewHolder(@NonNull ItemLocationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(SearchLocationRoot.DataItem location) {
            if (location != null) {
                binding.tvLocation.setText(location.getLabel());
                binding.getRoot().setOnClickListener(v -> {
                    if (onLocationClickListener != null) {
                        onLocationClickListener.onLocationClick(location);
                    }
                });
            }
        }
    }
}
