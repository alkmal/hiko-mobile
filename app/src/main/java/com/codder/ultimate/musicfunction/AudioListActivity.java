package com.codder.ultimate.musicfunction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ActivityAudioListBinding;

import java.util.ArrayList;

public class AudioListActivity extends AppCompatActivity {

    ActivityAudioListBinding binding;
    private ArrayList<String> audioFiles;
    private AudioListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_audio_list);
        getWindow().setStatusBarColor(Color.parseColor("#170D1F"));

        String folderPath = getIntent().getStringExtra("folderPath");
        audioFiles = getIntent().getStringArrayListExtra("audioFiles");

        binding.txtSongCount.setOnClickListener(v -> {
            ArrayList<String> selectedSongs = new ArrayList<>(adapter.getSelectedFiles());
            int selectedCount = selectedSongs.size();

            // Create an Intent to pass data back with the selected song list
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedCount", selectedCount);
            resultIntent.putExtra("folderPath", folderPath);
            resultIntent.putStringArrayListExtra("selectedSongs", selectedSongs);

            Log.d("AudioListActivity", "Selected Songs Count: " + selectedCount);

            setResult(RESULT_OK, resultIntent);
            finish();
        });

        binding.ivBack.setOnClickListener(view -> onBackPressed());

        setupRecyclerView();
        updateSelectedSongCount(0);
    }

    private void setupRecyclerView() {
        binding.rvAudioList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AudioListAdapter(audioFiles, this::onSelectionChanged);
        binding.rvAudioList.setAdapter(adapter);

        binding.lytSelectAll.setOnClickListener(view -> toggleSelectAll());
    }

    private void toggleSelectAll() {
        if (adapter.getSelectedFiles().size() == audioFiles.size()) {
            adapter.clearSelectedFiles();
            updateSelectAllState(false);
        } else {
            adapter.selectAllFiles();
            updateSelectAllState(true);
        }
    }

    private void updateSelectAllState(boolean isSelected) {
        if (isSelected) {
            binding.ivSelectAll.setImageResource(R.drawable.music_selected);  // Change to selected icon
            binding.txtSelectAll.setText("Deselect all");
        } else {
            binding.ivSelectAll.setImageResource(R.drawable.music_unselected);  // Change to unselected icon
            binding.txtSelectAll.setText("Select all");
        }
    }
    public void onSelectionChanged(int selectedCount) {
        updateSelectedSongCount(selectedCount);
    }

    private void updateSelectedSongCount(int count) {
        if (count == 0) {
            binding.txtSongCount.setText("Add");
            binding.txtSongCount.setTextColor(ContextCompat.getColorStateList(this,R.color.white_60));
        } else {
            binding.txtSongCount.setText("Add(" + count + ")");
            binding.txtSongCount.setTextColor(ContextCompat.getColorStateList(this,R.color.white));
        }
    }
}