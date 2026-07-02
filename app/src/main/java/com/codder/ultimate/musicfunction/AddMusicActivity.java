package com.codder.ultimate.musicfunction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ActivityAddMusicBinding;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class AddMusicActivity extends AppCompatActivity {

    private static final String TAG = "AddMusicActivity";
    ActivityAddMusicBinding binding;
    private ArrayList<AudioDetails> confirmedSongs = new ArrayList<>();
    private SongListAdapter songListAdapter;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS_NAME = "MyPrefs";
    private static final String KEY_CONFIRMED_SONGS = "confirmedSongs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_music);
        getWindow().setStatusBarColor(Color.parseColor("#170D1F"));

        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        confirmedSongs = loadConfirmedSongs();

        for (int i = 0; i < confirmedSongs.size(); i++) {
            Log.d("TAG", "onCreate: " + confirmedSongs.get(i).getSongPath());
        }

        setupRecyclerView();

        binding.ivBack.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.ivaddMusic.setOnClickListener(v -> {
            musicFolderLauncher.launch(new Intent(AddMusicActivity.this, MusicFolderActivity.class));
        });


        binding.txtplay.setOnClickListener(v -> {
            if (songListAdapter != null && songListAdapter.getSelectedItems() != null) {
                Intent resultIntent = new Intent();
                resultIntent.putParcelableArrayListExtra("selectedSong", songListAdapter.getSelectedItems());
                resultIntent.putParcelableArrayListExtra("savedSong", confirmedSongs);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please Select Songs For Play", Toast.LENGTH_SHORT).show();
            }
        });
        binding.ivDelete.setOnClickListener(v -> {
            deleteSelectedSongs();
        });

    }

    private final ActivityResultLauncher<Intent> musicFolderLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<AudioDetails> selectedSongs = result.getData().getParcelableArrayListExtra("selectedSongs");
                    if (selectedSongs != null) {
                        for (AudioDetails newSong : selectedSongs) {
                            boolean exists = false;
                            for (AudioDetails existingSong : confirmedSongs) {
                                if (existingSong.getSongPath().equals(newSong.getSongPath())) {
                                    exists = true;
                                    break;
                                }else {
                                    Log.d(TAG, ": This song is already added!" );
                                }
                            }
                            if (!exists) {
                                confirmedSongs.add(newSong);
                            }
                        }
                        saveConfirmedSongs();
                        songListAdapter.notifyDataSetChanged();

                        if (confirmedSongs.isEmpty()) {
                            binding.noMusic.setVisibility(View.VISIBLE);
                            binding.rvMusic.setVisibility(View.GONE);
                            binding.txtplay.setVisibility(View.GONE);
                        } else {
                            binding.noMusic.setVisibility(View.GONE);
                            binding.rvMusic.setVisibility(View.VISIBLE);
                            binding.txtplay.setVisibility(View.VISIBLE);
                        }

                        for (int i = 0; i < confirmedSongs.size(); i++) {
                            Log.d("TAG", "activityresult: " + confirmedSongs.get(i).getSongPath());
                        }

                    }
                }
            });

    private void setupRecyclerView() {
        binding.ivDelete.setVisibility(View.GONE);
        binding.rvMusic.setLayoutManager(new LinearLayoutManager(this));
        songListAdapter = new SongListAdapter(confirmedSongs, new SongListAdapter.SongClickListener() {
            @Override
            public void onSongClick(AudioDetails song, boolean isSelected) {
                binding.ivDelete.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            }
        });
        binding.rvMusic.setAdapter(songListAdapter);

        if (confirmedSongs.isEmpty()) {
            binding.noMusic.setVisibility(View.VISIBLE);
            binding.rvMusic.setVisibility(View.GONE);
            binding.txtplay.setVisibility(View.GONE);
        } else {
            binding.noMusic.setVisibility(View.GONE);
            binding.rvMusic.setVisibility(View.VISIBLE);
            binding.txtplay.setVisibility(View.VISIBLE);
        }
    }

    private void saveConfirmedSongs() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(confirmedSongs);
        editor.putString(KEY_CONFIRMED_SONGS, json);
        Log.d("TAG", "saveConfirmedSongs: " + json);
        editor.apply();
    }

    private ArrayList<AudioDetails> loadConfirmedSongs() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(KEY_CONFIRMED_SONGS, null);
        Log.d("TAG", "saveConfirmedSongs: load " + json);
        Type type = new TypeToken<ArrayList<AudioDetails>>() {
        }.getType();
        ArrayList<AudioDetails> songs = gson.fromJson(json, type);

        if (songs == null) {
            songs = new ArrayList<>();
        }
        return songs;
    }

    private void deleteSelectedSongs() {
        ArrayList<AudioDetails> selectedItems = songListAdapter.getSelectedItems();
        if (!selectedItems.isEmpty()) {
            confirmedSongs.removeAll(selectedItems);
            saveConfirmedSongs();
            songListAdapter.notifyDataSetChanged();

            if (confirmedSongs.isEmpty()) {
                binding.noMusic.setVisibility(View.VISIBLE);
                binding.rvMusic.setVisibility(View.GONE);
                binding.txtplay.setVisibility(View.GONE);
            }

            Toast.makeText(this, "Selected songs deleted", Toast.LENGTH_SHORT).show();
            binding.ivDelete.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "No songs selected to delete", Toast.LENGTH_SHORT).show();
        }
    }


}