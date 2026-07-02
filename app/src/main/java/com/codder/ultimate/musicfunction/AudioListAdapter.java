package com.codder.ultimate.musicfunction;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemAudiolistBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.MyViewHolder> {

    private final List<String> audioFiles;
    private final List<String> selectedFiles = new ArrayList<>();
    private final SelectionChangedListener selectionChangedListener;

    public interface SelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    public AudioListAdapter(List<String> audioFiles, SelectionChangedListener listener) {
        this.audioFiles = audioFiles;
        this.selectionChangedListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audiolist, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String filePath = audioFiles.get(position);
        File file = new File(filePath);
        String fileName = file.getName();

        Bitmap albumArt = null;
        try {
            albumArt = getAlbumArt(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (albumArt != null) {
            holder.binding.albumArt.setImageBitmap(albumArt);
        } else {
            holder.binding.albumArt.setImageResource(R.drawable.ic_music);
        }
        // Set audio title and file info
        holder.binding.audioTitle.setText(fileName);
        try {
            holder.binding.audioInfo.setText(getAudioInfo(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (selectedFiles.contains(filePath)) {
            holder.binding.ivSelectUnselect.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.music_selected));
        } else {
            holder.binding.ivSelectUnselect.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.music_unselected));

        }


        holder.itemView.setOnClickListener(v -> {
            if (selectedFiles.contains(filePath)) {
                // Deselect the item
                selectedFiles.remove(filePath);
            } else {
                // Select the item
                selectedFiles.add(filePath);
            }
            // Notify the adapter to update the UI
            notifyItemChanged(position);
            if (selectionChangedListener != null) {
                selectionChangedListener.onSelectionChanged(selectedFiles.size());
            }

        });
    }

    public void selectAllFiles() {
        selectedFiles.clear();
        selectedFiles.addAll(audioFiles);
        notifyDataSetChanged();
    }

    public void clearSelectedFiles() {
        selectedFiles.clear();
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return audioFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ItemAudiolistBinding binding;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemAudiolistBinding.bind(itemView);
        }
    }

    private String getAudioInfo(String filePath) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);


            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long durationMs = durationStr != null ? Long.parseLong(durationStr) : 0;

            String mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
//            String format = getFormatFromMimeType(mimeType);

            String durationFormatted = formatDuration(durationMs);

            return durationFormatted ; //+ " " + format;
        } catch (Exception e) {
            e.printStackTrace();
            return "00:00 Unknown";
        } finally {
            retriever.release();
        }
    }

    public List<String> getSelectedFiles() {
        return new ArrayList<>(selectedFiles);
    }

    private String formatDuration(long durationMs) {
        int totalSeconds = (int) (durationMs / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private String getFormatFromMimeType(String mimeType) {
        if (mimeType == null) {
            return "Unknown";
        }

        if (mimeType.contains("mp3")) {
            return "MP3";
        } else if (mimeType.contains("aac")) {
            return "AAC";
        } else if (mimeType.contains("wav")) {
            return "WAV";
        } else if (mimeType.contains("m4a")) {
            return "M4A";
        } else if (mimeType.contains("ogg")) {
            return "OGG";
        } else if (mimeType.contains("flac")) {
            return "FLAC";
        } else {
            return "Unknown";
        }
    }

    private Bitmap getAlbumArt(String filePath) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);

            byte[] artBytes = retriever.getEmbeddedPicture();
            if (artBytes != null) {
                // Decode the byte array to a Bitmap
                return BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return null;
    }
}
