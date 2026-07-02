package com.codder.ultimate.live.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemSongBinding;
import com.codder.ultimate.live.model.SongRoot;

import java.util.ArrayList;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongsViewHolder> {

    private List<SongRoot.SongItem> songItems = new ArrayList<>();
    private OnSongClickListener onSongClickListener;

    public interface OnSongClickListener {
        void onSongClick(SongRoot.SongItem songDummy);
    }

    public void setOnSongClickListener(OnSongClickListener listener) {
        this.onSongClickListener = listener;
    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSongBinding binding = ItemSongBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SongsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsViewHolder holder, int position) {
        holder.bind(songItems.get(position));
    }

    @Override
    public int getItemCount() {
        return songItems != null ? songItems.size() : 0;
    }

    public void setData(List<SongRoot.SongItem> newItems) {
        songItems.clear();
        if (newItems != null) {
            songItems.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void addData(List<SongRoot.SongItem> moreItems) {
        if (moreItems == null || moreItems.isEmpty()) return;
        int oldSize = songItems.size();
        songItems.addAll(moreItems);
        notifyItemRangeInserted(oldSize, moreItems.size());
    }

    class SongsViewHolder extends RecyclerView.ViewHolder {
        private final ItemSongBinding binding;

        public SongsViewHolder(ItemSongBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SongRoot.SongItem songDummy) {
            if (songDummy == null) return;

            // fallback image for Glide
            RequestOptions options = MainApplication.requestOptions != null
                    ? MainApplication.requestOptions
                    : new RequestOptions().placeholder(R.drawable.song).error(R.drawable.song);

            String imageUrl = songDummy.getImage() != null
                    ? BuildConfig.BASE_URL + songDummy.getImage()
                    : "";

            // Glide for the song image
            Glide.with(binding.imgSong.getContext())
                    .load(imageUrl)
                    .apply(options)
                    .circleCrop()
                    .into(binding.imgSong);

            binding.title.setText(songDummy.getTitle() != null ? songDummy.getTitle() : itemView.getContext().getString(R.string.unknown_title));
            binding.info.setText(songDummy.getSinger() != null ? songDummy.getSinger() : itemView.getContext().getString(R.string.unknown_singer));

            // Defensive click listener
            binding.getRoot().setOnClickListener(v -> {
                if (onSongClickListener != null) {
                    onSongClickListener.onSongClick(songDummy);
                }
            });
        }
    }
}

