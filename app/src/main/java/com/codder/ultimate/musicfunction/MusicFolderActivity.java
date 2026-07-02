package com.codder.ultimate.musicfunction;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ActivityMusicFolderBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicFolderActivity extends AppCompatActivity {

    ActivityMusicFolderBinding binding;
    private FolderListAdapter adapter;
    private Map<String, List<String>> audioFoldersMap;
    private Map<String, List<String>> selectedSongsMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_music_folder);
        getWindow().setStatusBarColor(Color.parseColor("#170D1F"));

        binding.rvFolderList.setLayoutManager(new LinearLayoutManager(this));
        checkPermissions();


        binding.ivBack.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.txtConfirmAdd.setOnClickListener(v -> {
            ArrayList<AudioDetails> allSelectedSongs = getSelectedAudioDetailsList();
            int selectedCount = allSelectedSongs.size();

            for (int i = 0; i < allSelectedSongs.size(); i++) {
                Log.d("TAG", "allSelectedSongs: " + allSelectedSongs.get(i).getSongPath());
            }

            if (selectedCount == 0) {
                Toast.makeText(this, "Please select at least one song!", Toast.LENGTH_SHORT).show();
            } else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedCount", selectedCount);
                resultIntent.putParcelableArrayListExtra("selectedSongs", allSelectedSongs);

                Log.d("AudioListActivity", "Selected Songs Count: " + selectedCount);

                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                fetchAudioFiles();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                fetchAudioFiles();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchAudioFiles();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }

            });


    private final ActivityResultLauncher<Intent> audioListLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int selectedCount = result.getData().getIntExtra("selectedCount", 0);
                    String folderPath = result.getData().getStringExtra("folderPath");
                    ArrayList<String> selectedSongs = result.getData().getStringArrayListExtra("selectedSongs");

                    if (selectedCount > 0 && selectedSongs != null) {
                        selectedSongsMap.put(folderPath, selectedSongs);
                    } else {
                        selectedSongsMap.remove(folderPath);
                    }
                    adapter.updateSelectedCount(folderPath, selectedCount);

//                    if (selectedCount > 0) {
//                        adapter.updateSelectedCount(folderPath, selectedCount);
//                    } else {
//                        adapter.updateSelectedCount(folderPath, 0);
//                    }
                    updateConfirmAddText();
                }
            });

    private void fetchAudioFiles() {
        audioFoldersMap = new AudioFileFetcher().getAudioFilesByFolder(getContentResolver());
        adapter = new FolderListAdapter(audioFoldersMap,this::openAudioListActivity);
        binding.rvFolderList.setAdapter(adapter);
    }

    private void openAudioListActivity(String folderPath) {
        Intent intent = new Intent(this, AudioListActivity.class);
        intent.putExtra("folderPath", folderPath);
        intent.putStringArrayListExtra("audioFiles", new ArrayList<>(audioFoldersMap.get(folderPath)));
        audioListLauncher.launch(intent);
    }

//    private ArrayList<String> getAllSelectedSongsList() {
//        ArrayList<String> allSelectedSongsList = new ArrayList<>();
//
//        // Iterate through the map and add all selected songs to the list
//        for (List<String> selectedSongs : selectedSongsMap.values()) {
//            if (selectedSongs != null) {
//                allSelectedSongsList.addAll(selectedSongs);
//            }
//        }
//        return allSelectedSongsList;
//    }

    private ArrayList<AudioDetails> getSelectedAudioDetailsList() {
        ArrayList<AudioDetails> audioDetailsList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : selectedSongsMap.entrySet()) {
            for (String songPath : entry.getValue()) {
                AudioDetails audioDetails = new AudioDetails(
                        AudioData.getAudioName(songPath),  // Fetch name
                        AudioData.getAudioDuration(this,songPath),  // Fetch duration
                        AudioData.getAlbumArt(this,songPath),  // Fetch thumbnail
                        songPath  // Fetch thumbnail
                );
                audioDetailsList.add(audioDetails);
            }
        }
        return audioDetailsList;
    }

    private int getTotalSelectedSongsCount() {
        int totalSelectedCount = 0;

        for (List<String> selectedSongs : selectedSongsMap.values()) {
            if (selectedSongs != null) {
                totalSelectedCount += selectedSongs.size();
            }
        }

        return totalSelectedCount;
    }

    private void updateConfirmAddText() {
        int totalSelectedCount = getTotalSelectedSongsCount();
        if (totalSelectedCount > 0) {
            binding.txtConfirmAdd.setText("Confirm Add (" + totalSelectedCount + ")");
            binding.txtConfirmAdd.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.pink));
        } else {
            binding.txtConfirmAdd.setText("Confirm Add");
            binding.txtConfirmAdd.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.black50));
        }
    }

}