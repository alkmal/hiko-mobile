package com.codder.ultimate.live.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivitySongPickerBinding;
import com.codder.ultimate.dialog.CustomDialogClass;
import com.codder.ultimate.live.adapter.SongsAdapter;
import com.codder.ultimate.live.model.SongRoot;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.utils.TempUtil;
import com.codder.ultimate.worker.FileDownloadWorker;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import nl.changer.audiowife.AudioWife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongPickerActivity extends BaseActivity {

    private static final String TAG = "SongPickerActivity";
    private ActivitySongPickerBinding binding;
    private int start = 0;
    private SongsAdapter songsAdapter = new SongsAdapter();

    private final ActivityResultLauncher<Intent> songFilePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        try {
                            closeWithSelection(null, copySongFile(uri));
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to copy song file: " + e.getMessage(), e);
                            Toast.makeText(this, getString(R.string.error_copying_file), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_song_picker);

        setupRecyclerView();
        setupBrowseButton();
        setupBottomSheet();
        fetchSongsData();
    }

    private void setupRecyclerView() {
        songsAdapter.setOnSongClickListener(song -> {
            if (song != null) {
                downloadSelectedSong(song);
            }
        });
        binding.rvSongs.setAdapter(songsAdapter);
    }

    private void setupBrowseButton() {
        View browse = binding.getRoot().findViewById(R.id.browse);
        if (browse != null) {
            browse.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                songFilePickerLauncher.launch(intent);

            });
        }
    }

    private void setupBottomSheet() {
        View sheet = binding.getRoot().findViewById(R.id.song_preview_sheet);
        if (sheet != null) {
            BottomSheetBehavior<View> bsb = BottomSheetBehavior.from(sheet);
            bsb.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View sheet, int state) {
                    Log.v(TAG, "Song preview sheet state is: " + state);
                    if (state == BottomSheetBehavior.STATE_COLLAPSED) {
                        AudioWife.getInstance().release();
                    }
                }

                @Override
                public void onSlide(@NonNull View sheet, float offset) {
                }
            });
        }
    }

    private void fetchSongsData() {
        showLoading();

        Call<SongRoot> call = RetrofitBuilder.create().getSongs();
        call.enqueue(new Callback<SongRoot>() {
            @Override
            public void onResponse(@NonNull Call<SongRoot> call, @NonNull Response<SongRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SongRoot songRoot = response.body();
                    if (songRoot.isStatus() && songRoot.getSong() != null && !songRoot.getSong().isEmpty()) {
                        songsAdapter.addData(songRoot.getSong());
                        showContent();
                    } else if (start == 0) {
                        showEmpty();
                    }
                } else {
                    showEmpty();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SongRoot> call, @NonNull Throwable t) {
                showError();
                Log.e(TAG, "Failed to fetch songs: " + t.getMessage(), t);
            }
        });
    }

    private void downloadSelectedSong(@NonNull final SongRoot.SongItem songDummy) {
        File songsDir = new File(getFilesDir(), "songs");
        if (!songsDir.exists() && !songsDir.mkdirs()) {
            Log.w(TAG, "Could not create directory at " + songsDir);
            Toast.makeText(this, getString(R.string.failed_to_create_directory), Toast.LENGTH_SHORT).show();
            return;
        }

        String songUrl = BuildConfig.BASE_URL + songDummy.getSong();
        String extension = getFileExtension(songDummy.getSong());
        File audioFile = new File(songsDir, songDummy.getId() + "." + extension);

        if (audioFile.exists()) {
            playSelection(songDummy, Uri.fromFile(audioFile));
            return;
        }

        CustomDialogClass progress = new CustomDialogClass(this, R.style.customStyle);
        progress.setCancelable(false);
        progress.show();

        Data input = new Data.Builder()
                .putString(FileDownloadWorker.KEY_INPUT, songUrl)
                .putString(FileDownloadWorker.KEY_OUTPUT, audioFile.getAbsolutePath())
                .build();
        WorkRequest request = new OneTimeWorkRequest.Builder(FileDownloadWorker.class)
                .setInputData(input)
                .build();

        WorkManager wm = WorkManager.getInstance(this);
        wm.enqueue(request);
        wm.getWorkInfoByIdLiveData(request.getId()).observe((LifecycleOwner) this, info -> {
            if (info == null) return;
            Log.d(TAG, "Download state: " + info.getState());
            boolean ended = info.getState() == WorkInfo.State.CANCELLED
                    || info.getState() == WorkInfo.State.FAILED
                    || info.getState() == WorkInfo.State.SUCCEEDED;
            if (ended) {
                progress.dismiss();
            }
            if (info.getState() == WorkInfo.State.SUCCEEDED && audioFile.exists()) {
                playSelection(songDummy, Uri.fromFile(audioFile));
            } else if (info.getState() == WorkInfo.State.FAILED) {
                Toast.makeText(this, "Failed to download song.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void closeWithSelection(@Nullable SongRoot.SongItem songDummy, @NonNull Uri file) {
        Intent data = new Intent();
        if (songDummy != null) {
            data.putExtra(RecorderActivity.EXTRA_SONG, songDummy);
        }
        data.putExtra(RecorderActivity.EXTRA_AUDIO, file);
        setResult(RESULT_OK, data);
        finish();
    }

    private Uri copySongFile(@NonNull Uri uri) throws Exception {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = getContentResolver().openInputStream(uri);
            if (is == null) throw new Exception("Input stream is null.");
            File target = TempUtil.createNewFile(this, "audio");
            os = new FileOutputStream(target);
            IOUtils.copy(is, os);
            return Uri.fromFile(target);
        } finally {
            if (is != null) try {
                is.close();
            } catch (Exception ignored) {
            }
            if (os != null) try {
                os.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void playSelection(@NonNull SongRoot.SongItem songDummy, @NonNull Uri file) {
        View sheet = binding.getRoot().findViewById(R.id.song_preview_sheet);

        if (sheet == null) {
            Log.e(TAG, "Preview sheet not found.");
            return;
        }
        sheet.setVisibility(View.VISIBLE);

        BottomSheetBehavior<View> bsb = BottomSheetBehavior.from(sheet);
        bsb.setState(BottomSheetBehavior.STATE_EXPANDED);  // This will make the bottom sheet visible

        AudioWife.getInstance().release();

        AudioWife.getInstance()
                .init(this, file)
                .setPlayView(sheet.findViewById(R.id.play))
                .setPauseView(sheet.findViewById(R.id.pause))
                .setSeekBar(sheet.findViewById(R.id.seekbar))
                .setRuntimeView(sheet.findViewById(R.id.start))
                .play();

        TextView song2 = sheet.findViewById(R.id.song);
        if (song2 != null) {
            song2.setText(songDummy.getTitle());
        }
        View useButton = sheet.findViewById(R.id.use);
        if (useButton != null) {
            useButton.setOnClickListener(v -> closeWithSelection(songDummy, file));
        }
    }

    private String getFileExtension(@NonNull String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot == -1 || lastDot == fileName.length() - 1) return "mp3";
        return fileName.substring(lastDot + 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding.shimmer.getVisibility() == View.VISIBLE) binding.shimmer.startShimmer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.shimmer.stopShimmer();
        try { AudioWife.getInstance().pause(); } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            AudioWife.getInstance().release();
        } catch (Exception e) {
            Log.w(TAG, "Audio release error: " + e.getMessage());
        }
    }


    private void showLoading() {
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.shimmer.startShimmer();
        binding.rvSongs.setVisibility(View.GONE);
        binding.layoutNoData.setVisibility(View.GONE);
    }

    private void showContent() {
        binding.shimmer.stopShimmer();
        binding.shimmer.setVisibility(View.GONE);
        binding.rvSongs.setVisibility(View.VISIBLE);
        binding.layoutNoData.setVisibility(View.GONE);
    }

    private void showEmpty() {
        binding.shimmer.stopShimmer();
        binding.shimmer.setVisibility(View.GONE);
        binding.rvSongs.setVisibility(View.GONE);
        binding.layoutNoData.setVisibility(View.VISIBLE);
    }

    private void showError() {  // if you want a separate state
        showEmpty();
    }

}
