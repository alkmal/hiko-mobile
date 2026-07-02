package com.codder.ultimate.musicfunction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemAudiolistBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.MyViewHolder> {

    private final List<AudioDetails> songList;
    private final SongClickListener listener;
    private final Set<Integer> selectedPositions = new HashSet<>(); // Store selected positions

    public SongListAdapter(List<AudioDetails> songList, SongClickListener listener) {
        this.songList = songList;
        this.listener = listener;
        selectedPositions.add(0);
    }

    public interface SongClickListener {
        void onSongClick(AudioDetails song, boolean isSelected);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audiolist, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AudioDetails song = songList.get(position);

        holder.binding.audioTitle.setText(song.getName());

        holder.binding.audioInfo.setText(song.getDuration());

        Glide.with(holder.itemView.getContext())
                .load(song.getThumbnailPath())
                .placeholder(R.drawable.ic_music)
                .into(holder.binding.albumArt);

        if (selectedPositions.contains(position)) {
            holder.binding.ivSelectUnselect.setImageResource(R.drawable.music_selected);
        } else {
            holder.binding.ivSelectUnselect.setImageResource(R.drawable.music_unselected);
        }

        holder.itemView.setOnClickListener(v -> {
            if (selectedPositions.contains(position)) {
                selectedPositions.remove(position);
                listener.onSongClick(song, false);
            } else {
                selectedPositions.add(position);
                listener.onSongClick(song, true);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    /**
     * Get a list of selected items.
     */
    public ArrayList<AudioDetails> getSelectedItems() {
        ArrayList<AudioDetails> selectedItems = new ArrayList<>();

        if (songList != null && !songList.isEmpty()) {
            for (Integer position : selectedPositions) {
                if (position >= 0 && position < songList.size()) {
                    selectedItems.add(songList.get(position));
                    Log.d("TAG", "getSelectedItems: " + songList.get(position).getSongPath());
                } else {
                    Log.e("TAG", "Invalid position: " + position);
                }
            }
        } else {
            Log.e("TAG", "songList is empty or null");
        }
        return selectedItems;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        ItemAudiolistBinding binding;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemAudiolistBinding.bind(itemView);
        }
    }
}
