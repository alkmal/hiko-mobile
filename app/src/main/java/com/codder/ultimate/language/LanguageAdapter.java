package com.codder.ultimate.language;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemLanguageBinding;
import com.codder.ultimate.launguagetranslation.modelclass.ActiveLanguageRoot;

import java.util.ArrayList;
import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.VH> {

    public interface OnLanguageClick {
        void onLanguageSelected(ActiveLanguageRoot.DocsItem item);
    }

    private List<ActiveLanguageRoot.DocsItem> data;
    private final OnLanguageClick listener;
    private String selectedKey;
    private final Context context;

    public LanguageAdapter(Context context, List<ActiveLanguageRoot.DocsItem> data,
                           OnLanguageClick listener, String selectedKey) {
        this.context = context;
        this.data = data;
        this.listener = listener;
        this.selectedKey = selectedKey;
    }

    public void setSelectedKey(String key) {
        this.selectedKey = key;
        notifyDataSetChanged();
    }

    public void updateData(List<ActiveLanguageRoot.DocsItem> newList, String selectedKey) {
        this.data.clear();
        this.data.addAll(newList);
        this.selectedKey = selectedKey;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLanguageBinding b = ItemLanguageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ActiveLanguageRoot.DocsItem item = data.get(position);

        h.b.tvName.setText(item.getLocalLanguageTitle());

        Glide.with(context)
                .load(BuildConfig.BASE_URL + item.getLanguageIcon())
                .circleCrop()
                .placeholder(R.drawable.flag_english)
                .error(R.drawable.flag_english)
                .into(h.b.ivFlag);

        boolean isSelected = item.getLanguageTitle().equalsIgnoreCase(selectedKey);
        h.b.ivCheck.setSelected(isSelected);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onLanguageSelected(item);
        });
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        ItemLanguageBinding b;

        VH(ItemLanguageBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}