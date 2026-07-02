package com.codder.ultimate.musicfunction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemFolderBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderListAdapter extends RecyclerView.Adapter<FolderListAdapter.MyViewHolder> {

    private final List<String> folderList;
    private final Map<String, List<String>> audioFoldersMap;
    private final OnFolderClickListener listener;
    private final Map<String, Integer> selectedCountMap = new HashMap<>();


    public interface OnFolderClickListener {
        void onFolderClick(String folderPath);
    }

    public FolderListAdapter(Map<String, List<String>> audioFoldersMap, OnFolderClickListener listener) {
        this.folderList = new ArrayList<>(audioFoldersMap.keySet());
        this.audioFoldersMap = audioFoldersMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String folderPath = folderList.get(position);
        List<String> audioFiles = audioFoldersMap.get(folderPath);

        String folderName = new File(folderPath).getName();
        int songCount = (audioFiles != null) ? audioFiles.size() : 0;

        holder.binding.folderName.setText(folderName);
        holder.binding.folderInfo.setText(songCount + " Songs" + "," + folderPath);

        int selectedCount = selectedCountMap.getOrDefault(folderPath, 0);
        if (selectedCount > 0) {
            holder.binding.txtselectedCount.setText(selectedCount + " song");
            Log.d("TAG", "onBindViewHolder: songcount......" + selectedCount);
            holder.binding.txtselectedCount.setVisibility(View.VISIBLE);
        } else {
            holder.binding.txtselectedCount.setText("");
            holder.binding.txtselectedCount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onFolderClick(folderPath));
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ItemFolderBinding binding;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemFolderBinding.bind(itemView);
        }
    }

    public void updateSelectedCount(String folderPath, int selectedCount) {
        selectedCountMap.put(folderPath, selectedCount);
        int position = folderList.indexOf(folderPath);
        if (position != -1) {
            notifyItemChanged(position);
        }
    }
}
