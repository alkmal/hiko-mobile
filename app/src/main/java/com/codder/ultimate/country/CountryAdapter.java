package com.codder.ultimate.country;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemCountryBinding;
import com.codder.ultimate.retrofit.Const;

public class CountryAdapter extends ListAdapter<CountryModel, CountryAdapter.MyViewHolder> {

    private int selectedPos = 0;
    Context context;

    public interface OnCountryClickListener {
        void onCountryClick(CountryModel model, int position);
    }

    private OnCountryClickListener listener;

    public void setOnCountryClickListener(OnCountryClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        int old = selectedPos;
        selectedPos = position;
        notifyItemChanged(old);
        notifyItemChanged(position);
    }

    public CountryAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    private static final DiffUtil.ItemCallback<CountryModel> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CountryModel>() {

                @Override
                public boolean areItemsTheSame(
                        @NonNull CountryModel oldItem,
                        @NonNull CountryModel newItem) {
                    return oldItem.getName().equals(newItem.getName());
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull CountryModel oldItem,
                        @NonNull CountryModel newItem) {
                    return oldItem.getName().equals(newItem.getName()) &&
                            oldItem.getPhoneCode().equals(newItem.getPhoneCode());
                }
            };

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_country, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        CountryModel model = getItem(position);
        if (model.getName().equals("All")){
            holder.binding.tvCountry.setText(model.getName());
        }else {
            holder.binding.tvCountry.setText(model.getFlag() + "  " + model.getName());
        }

        if (selectedPos == position) {
            holder.binding.mainbg.setBackground(ContextCompat.getDrawable(context,R.drawable.home_tab_selectedbg));
        } else {
            holder.binding.mainbg.setBackground(ContextCompat.getDrawable(context,R.drawable.home_tab_unselectedbg));
        }

        holder.itemView.setOnClickListener(v -> {

            setSelectedPosition(position);

            if (listener != null) {
                listener.onCountryClick(model, selectedPos);
            }
        });
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ItemCountryBinding binding;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemCountryBinding.bind(itemView);

        }
    }
}
