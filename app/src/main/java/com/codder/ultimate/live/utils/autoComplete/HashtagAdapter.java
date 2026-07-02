package com.codder.ultimate.live.utils.autoComplete;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemHashtagSlimBinding;
import com.codder.ultimate.live.model.HashtagsRoot;
import com.codder.ultimate.utils.TextFormatUtil;

import java.util.List;

public class HashtagAdapter extends RecyclerView.Adapter<HashtagAdapter.HashtagViewHolder> {

    private final Context mContext;
    private final OnClickListener mListener;
    private List<HashtagsRoot.HashtagItem> mItems;

    protected HashtagAdapter(@NonNull Context context, @NonNull OnClickListener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    @Override
    public void onBindViewHolder(@NonNull HashtagViewHolder holder, int position) {
        final HashtagsRoot.HashtagItem hashtag = mItems.get(position);
        holder.binding.name.setText("#" + hashtag.getHashtag());
        holder.binding.clips.setText(
                mContext.getString(R.string.count_clips, TextFormatUtil.toShortNumber(hashtag.getCount())));
        holder.itemView.setOnClickListener(v -> mListener.onHashtagClick(hashtag));
    }

    @NonNull
    @Override
    public HashtagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHashtagSlimBinding binding = ItemHashtagSlimBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HashtagViewHolder(binding);
    }

    public void submitData(List<HashtagsRoot.HashtagItem> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    public interface OnClickListener {
        void onHashtagClick(HashtagsRoot.HashtagItem hashtag);
    }

    static class HashtagViewHolder extends RecyclerView.ViewHolder {
        final ItemHashtagSlimBinding binding;

        HashtagViewHolder(@NonNull ItemHashtagSlimBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
