package com.codder.ultimate.live.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ActivityRecorderBinding;
import com.codder.ultimate.dialog.CustomDialogClass;
import com.codder.ultimate.live.adapter.FilterAdapter;
import com.codder.ultimate.live.filters.ExposureFilter;
import com.codder.ultimate.live.filters.HazeFilter;
import com.codder.ultimate.live.filters.MonochromeFilter;
import com.codder.ultimate.live.filters.PixelatedFilter;
import com.codder.ultimate.live.filters.SolarizeFilter;
import com.codder.ultimate.live.model.SongRoot;
import com.codder.ultimate.live.model.StickerRoot;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.utils.AnimationUtil;
import com.codder.ultimate.utils.BitmapUtil;
import com.codder.ultimate.utils.IntentUtil;
import com.codder.ultimate.utils.SharedConstants;
import com.codder.ultimate.utils.TempUtil;
import com.codder.ultimate.utils.TextFormatUtil;
import com.codder.ultimate.utils.VideoFilter;
import com.codder.ultimate.utils.VideoUtil;
import com.codder.ultimate.worker.FileDownloadWorker;
import com.codder.ultimate.worker.MergeVideosWorker2;
import com.codder.ultimate.worker.VideoSpeedWorker2;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.segmentedprogressbar.SegmentedProgressBar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.slider.Slider;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.filter.Filters;
import com.otaliastudios.cameraview.filters.BrightnessFilter;
import com.otaliastudios.cameraview.filters.GammaFilter;
import com.otaliastudios.cameraview.filters.SharpnessFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import info.hoang8f.android.segmented.SegmentedGroup;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class RecorderActivity extends AppCompatActivity {

    public static final String EXTRA_AUDIO = "audio";
    public static final String EXTRA_SONG = "song";
    private static final String TAG = "RecorderActivity";
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    int timeInSeconds = 0;
    ActivityRecorderBinding binding;
    private CameraView mCamera;
    private final Runnable mStopper = this::stopRecording;
    private MediaPlayer mMediaPlayer;
    private RecorderActivityViewModel mModel;
    private YoYo.YoYoString mPulse;
    private CustomDialogClass mProgress;
    private boolean isFinishingActivity = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            if (requestCode == SharedConstants.REQUEST_CODE_PICK_VIDEO && resultCode == RESULT_OK && data != null) {
                double fileSizeMB = getFileSizeInMB(data.getData());
                if (fileSizeMB <= Const.UPLOADING_LIMIT) {
                    submitUpload(data.getData());
                } else {
                    Toast.makeText(this, R.string.you_cannot_upload_video_above_30_mb, Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (requestCode == SharedConstants.REQUEST_CODE_PICK_SONG && resultCode == RESULT_OK && data != null) {
                SongRoot.SongItem songDummy = data.getParcelableExtra(EXTRA_SONG);
                Uri audio = data.getParcelableExtra(EXTRA_AUDIO);
                setupSong(songDummy, audio);
            } else if (requestCode == SharedConstants.REQUEST_CODE_PICK_STICKER && resultCode == RESULT_OK && data != null) {
                StickerRoot.StickerItem stickerDummy = data.getParcelableExtra(StickerPickerActivity.EXTRA_STICKER);
                downloadSticker(stickerDummy);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onActivityResult", e);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recorder);
        mModel = new ViewModelProvider(this).get(RecorderActivityViewModel.class);

        try {
            SongRoot.SongItem songDummy = getIntent().getParcelableExtra(EXTRA_SONG);
            Uri audio = getIntent().getParcelableExtra(EXTRA_AUDIO);
            if (audio != null) {
                setupSong(songDummy, audio);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting song/audio from intent", e);
        }

        mCamera = findViewById(R.id.camera);
        if (mCamera == null) {
            Toast.makeText(this, getString(R.string.camera_is_not_available), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mCamera.setLifecycleOwner(this);
        mCamera.setMode(Mode.VIDEO);
        binding.close.setOnClickListener(view -> confirmClose());
        binding.done.setOnClickListener(view -> {
            if (mCamera.isTakingVideo()) {
                Toast.makeText(this, R.string.recorder_error_in_progress, Toast.LENGTH_SHORT)
                        .show();
            } else if (mModel.segments.isEmpty()) {
                Toast.makeText(this, R.string.recorder_error_no_clips, Toast.LENGTH_SHORT)
                        .show();
            } else {
                commitRecordings(mModel.segments, mModel.audio);
            }
        });
        binding.flip.setOnClickListener(view -> {
            if (mCamera.isTakingVideo()) {
                Toast.makeText(this, R.string.recorder_error_in_progress, Toast.LENGTH_SHORT)
                        .show();
            } else {
                mCamera.toggleFacing();
            }
        });
        binding.flash.setOnClickListener(view -> {
            if (mCamera.isTakingVideo()) {
                Toast.makeText(this, R.string.recorder_error_in_progress, Toast.LENGTH_SHORT)
                        .show();
            } else {
                mCamera.setFlash(mCamera.getFlash() == Flash.OFF ? Flash.TORCH : Flash.OFF);
            }
        });
        SegmentedGroup speeds = findViewById(R.id.speeds);
        View speed = findViewById(R.id.speed);
        speed.setOnClickListener(view -> {
            if (mCamera.isTakingVideo()) {
                Toast.makeText(this, R.string.recorder_error_in_progress, Toast.LENGTH_SHORT)
                        .show();
            } else {
                speeds.setVisibility(
                        speeds.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });
        speed.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? View.VISIBLE : View.GONE);
        RadioButton speed05x = findViewById(R.id.speed05x);
        RadioButton speed075x = findViewById(R.id.speed075x);
        RadioButton speed1x = findViewById(R.id.speed1x);
        RadioButton speed15x = findViewById(R.id.speed15x);
        RadioButton speed2x = findViewById(R.id.speed2x);
        speed05x.setChecked(mModel.speed == .5f);
        speed075x.setChecked(mModel.speed == .75f);
        speed1x.setChecked(mModel.speed == 1);
        speed15x.setChecked(mModel.speed == 1.5f);
        speed2x.setChecked(mModel.speed == 2);
        speeds.setOnCheckedChangeListener((group, checked) -> {
            float factor = 1;
            if (checked != R.id.speed05x) {
                speed05x.setChecked(false);
            } else {
                factor = .5f;
            }

            if (checked != R.id.speed075x) {
                speed075x.setChecked(false);
            } else {
                factor = .75f;
            }

            if (checked != R.id.speed1x) {
                speed1x.setChecked(false);
            }

            if (checked != R.id.speed15x) {
                speed15x.setChecked(false);
            } else {
                factor = 1.5f;
            }

            if (checked != R.id.speed2x) {
                speed2x.setChecked(false);
            } else {
                factor = 2;
            }

            mModel.speed = factor;
        });
        RecyclerView filters = findViewById(R.id.filters);
        findViewById(R.id.filter).setOnClickListener(view -> {
            if (mCamera.isTakingVideo()) {
                Toast.makeText(this, R.string.recorder_error_in_progress, Toast.LENGTH_SHORT)
                        .show();
            } else if (filters.getVisibility() == View.VISIBLE) {
                filters.setAdapter(null);
                filters.setVisibility(View.GONE);
            } else {
                mProgress = new CustomDialogClass(this, R.style.customStyle);
                mProgress.setCancelable(false);
                mProgress.show();
                mCamera.takePictureSnapshot();
            }
        });

        findViewById(R.id.sticker).setOnClickListener(v -> {
            Intent intent = new Intent(this, StickerPickerActivity.class);
            startActivityForResult(intent, SharedConstants.REQUEST_CODE_PICK_STICKER);
        });
        View sticker = findViewById(R.id.sticker);
        sticker.setVisibility(getResources().getBoolean(R.bool.stickers_enabled) ? View.VISIBLE : View.GONE);


        View sheet = findViewById(R.id.timer_sheet);
        BottomSheetBehavior<View> bsb = BottomSheetBehavior.from(sheet);
        ImageView close = sheet.findViewById(R.id.btnClose);
        close.setOnClickListener(view -> bsb.setState(BottomSheetBehavior.STATE_COLLAPSED));


        ImageView start = sheet.findViewById(R.id.btnDone);
        start.setOnClickListener(view -> {
            bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
            startTimer();
        });
        findViewById(R.id.timer).setOnClickListener(view -> {
            if (mCamera.isTakingVideo()) {
                Toast.makeText(this, R.string.recorder_error_in_progress, Toast.LENGTH_SHORT)
                        .show();
            } else {
                bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        TextView maximum = findViewById(R.id.maximum);
        View sound = findViewById(R.id.sound);
        sound.setOnClickListener(view -> {
            if (mModel.segments.isEmpty()) {
                Intent intent = new Intent(this, SongPickerActivity.class);
                startActivityForResult(intent, SharedConstants.REQUEST_CODE_PICK_SONG);
            } else if (mModel.audio == null) {
                Toast.makeText(this, R.string.message_song_select, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.message_song_change, Toast.LENGTH_SHORT).show();
            }
        });
        Slider selection = findViewById(R.id.selection);
        selection.setLabelFormatter(value -> TextFormatUtil.toMMSS((long) value));


        View upload = findViewById(R.id.upload);
        upload.setOnClickListener(view -> {
            String[] permissions = new String[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                permissions = new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO,
                };
            } else {
                permissions = new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                };
            }
            if (EasyPermissions.hasPermissions(RecorderActivity.this, permissions)) {
                chooseVideoForUpload();
            } else {
                Toast.makeText(this, R.string.you_need_storage_permission, Toast.LENGTH_SHORT).show();
                EasyPermissions.requestPermissions(
                        this,
                        getString(R.string.permission_rationale_upload),
                        SharedConstants.REQUEST_CODE_PERMISSIONS_UPLOAD,
                        permissions);
            }
        });
        bsb.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onSlide(@NonNull View v, float offset) {
            }

            @Override
            public void onStateChanged(@NonNull View v, int state) {
                if (state == BottomSheetBehavior.STATE_EXPANDED) {
                    long max;
                    max = SharedConstants.MAX_DURATION - mModel.recorded();
                    max = TimeUnit.MILLISECONDS.toSeconds(max);
                    max = TimeUnit.SECONDS.toMillis(max);
                    selection.setValue(0);
                    selection.setValueTo(max);
                    selection.setValue(max);
                    maximum.setText(TextFormatUtil.toMMSS(max));
                }
            }
        });
        SegmentedProgressBar segments = findViewById(R.id.segments);
        segments.enableAutoProgressView(SharedConstants.MAX_DURATION);
        segments.setDividerColor(Color.BLACK);
        segments.setDividerEnabled(true);
        segments.setDividerWidth(2f);
        segments.setListener(l -> { /* eaten */ });
        segments.setShader(new int[]{
                ContextCompat.getColor(this, R.color.purple),
                ContextCompat.getColor(this, R.color.pink),
        });
        mCamera.addCameraListener(new CameraListener() {

            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                super.onPictureTaken(result);
                result.toBitmap(bitmap -> {
                    if (bitmap != null) {
                        Bitmap square = BitmapUtil.getSquareThumbnail(bitmap, 250);
                        bitmap.recycle();
                        Bitmap rounded = BitmapUtil.addRoundCorners(square, 10);
                        square.recycle();
                        FilterAdapter adapter =
                                new FilterAdapter(RecorderActivity.this, rounded);
                        adapter.setListener(RecorderActivity.this::applyPreviewFilter);
                        RecyclerView filters = findViewById(R.id.filters);
                        filters.setAdapter(adapter);
                        filters.setVisibility(View.VISIBLE);
                    }

                    mProgress.dismiss();
                });
            }

            @Override
            public void onVideoRecordingEnd() {
                Log.v(TAG, "Video recording has ended.");
                segments.pause();
                segments.addDivider();
                mHandler.removeCallbacks(mStopper);
                mHandler.postDelayed(() -> processCurrentRecording(), 500);
                if (mMediaPlayer != null) {
                    mMediaPlayer.pause();
                }//

                mPulse.stop();
                binding.record.setSelected(false);
                toggleVisibility(true);
            }

            @Override
            public void onVideoRecordingStart() {
                Log.v(TAG, "Video recording has started.");
                segments.resume();
                if (mMediaPlayer != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        float speed = 1f;
                        if (mModel.speed == .5f) {
                            speed = 2f;
                        } else if (mModel.speed == .75f) {
                            speed = 1.5f;
                        } else if (mModel.speed == 1.5f) {
                            speed = .75f;
                        } else if (mModel.speed == 2f) {
                            speed = .5f;
                        }

                        PlaybackParams params = new PlaybackParams();
                        params.setSpeed(speed);
                        mMediaPlayer.setPlaybackParams(params);
                    }

                    mMediaPlayer.start();
                }

                mPulse = YoYo.with(Techniques.Pulse)
                        .repeat(YoYo.INFINITE)
                        .playOn(binding.record);
                binding.record.setSelected(true);
                toggleVisibility(false);
            }
        });

        binding.record.setOnClickListener(view -> {
            if (mCamera.isTakingVideo()) {
                stopRecording();
            } else {
                filters.setVisibility(View.GONE);
                speeds.setVisibility(View.GONE);
                startRecording();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isFinishingActivity = true;
        try {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            if (mModel != null && mModel.segments != null) {
                for (RecordSegment segment : mModel.segments) {
                    if (segment != null && segment.file != null) {
                        File file = new File(segment.file);
                        if (file.exists() && !file.delete()) {
                            Log.v(TAG, "Could not delete record segment file: " + file);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onDestroy error: ", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void addSticker(File file) {
        View remove = findViewById(R.id.remove);
        if (remove != null) remove.setVisibility(View.VISIBLE);
    }

    private void applyPreviewFilter(VideoFilter filter) {
        if (mCamera == null) return;
        switch (filter) {
            case BRIGHTNESS: {
                BrightnessFilter glf = (BrightnessFilter) Filters.BRIGHTNESS.newInstance();
                glf.setBrightness(1.2f);
                mCamera.setFilter(glf);
                break;
            }
            case EXPOSURE:
                mCamera.setFilter(new ExposureFilter());
                break;
            case GAMMA: {
                GammaFilter glf = (GammaFilter) Filters.GAMMA.newInstance();
                glf.setGamma(2);
                mCamera.setFilter(glf);
                break;
            }
            case GRAYSCALE:
                mCamera.setFilter(Filters.GRAYSCALE.newInstance());
                break;
            case HAZE: {
                HazeFilter glf = new HazeFilter();
                glf.setSlope(-0.5f);
                mCamera.setFilter(glf);
                break;
            }
            case INVERT:
                mCamera.setFilter(Filters.INVERT_COLORS.newInstance());
                break;
            case MONOCHROME:
                mCamera.setFilter(new MonochromeFilter());
                break;
            case PIXELATED: {
                PixelatedFilter glf = new PixelatedFilter();
                glf.setPixel(5);
                mCamera.setFilter(glf);
                break;
            }
            case POSTERIZE:
                mCamera.setFilter(Filters.POSTERIZE.newInstance());
                break;
            case SEPIA:
                mCamera.setFilter(Filters.SEPIA.newInstance());
                break;
            case SHARP: {
                SharpnessFilter glf = (SharpnessFilter) Filters.SHARPNESS.newInstance();
                glf.setSharpness(0.25f);
                mCamera.setFilter(glf);
                break;
            }
            case SOLARIZE:
                mCamera.setFilter(new SolarizeFilter());
                break;
            case VIGNETTE:
                mCamera.setFilter(Filters.VIGNETTE.newInstance());
                break;
            default:
                mCamera.setFilter(Filters.NONE.newInstance());
                break;
        }
    }

    private void applyVideoSpeed(File file, float speed, long duration) {
        if (isFinishingActivity) return;
        File output = TempUtil.createNewFile(this, ".mp4");
        mProgress = new CustomDialogClass(this, R.style.customStyle);
        mProgress.setCancelable(false);
        mProgress.show();

        Data data = new Data.Builder()
                .putString(VideoSpeedWorker2.KEY_INPUT, file.getAbsolutePath())
                .putString(VideoSpeedWorker2.KEY_OUTPUT, output.getAbsolutePath())
                .putFloat(VideoSpeedWorker2.KEY_SPEED, speed)
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(VideoSpeedWorker2.class)
                .setInputData(data)
                .build();
        WorkManager wm = WorkManager.getInstance(this);
        wm.enqueue(request);
        wm.getWorkInfoByIdLiveData(request.getId())
                .observe(this, info -> {
                    if (isFinishingActivity || info == null) return;
                    boolean ended = info.getState() == WorkInfo.State.CANCELLED
                            || info.getState() == WorkInfo.State.FAILED
                            || info.getState() == WorkInfo.State.SUCCEEDED;
                    if (ended && mProgress != null) {
                        mProgress.dismiss();
                    }
                    if (info.getState() == WorkInfo.State.SUCCEEDED) {
                        RecordSegment segment = new RecordSegment();
                        segment.file = output.getAbsolutePath();
                        segment.duration = duration;
                        if (mModel != null) mModel.segments.add(segment);
                    }
                });

    }

    @AfterPermissionGranted(SharedConstants.REQUEST_CODE_PERMISSIONS_UPLOAD)
    private void chooseVideoForUpload() {
        try {
            IntentUtil.startChooser(
                    this,
                    SharedConstants.REQUEST_CODE_PICK_VIDEO,
                    "video/mp4");
        } catch (Exception e) {
            Log.e(TAG, "chooseVideoForUpload error: ", e);
            Toast.makeText(this, getString(R.string.unable_to_choose_video_for_upload), Toast.LENGTH_SHORT).show();
        }
    }

    private void commitRecordings(@NonNull List<RecordSegment> segments, @Nullable Uri audio) {
        timeInSeconds = 0;
        mProgress = new CustomDialogClass(this, R.style.customStyle);
        mProgress.setCancelable(false);
        mProgress.show();

        List<String> videos = new ArrayList<>();
        if (segments != null) {
            for (RecordSegment segment : segments) {
                if (segment != null && segment.file != null) videos.add(segment.file);
            }
        }

        File merged = TempUtil.createNewFile(this, ".mp4");
        Data data = new Data.Builder()
                .putStringArray(MergeVideosWorker2.KEY_INPUTS, videos.toArray(new String[0]))
                .putString(MergeVideosWorker2.KEY_OUTPUT, merged.getAbsolutePath())
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(MergeVideosWorker2.class)
                .setInputData(data)
                .build();
        WorkManager wm = WorkManager.getInstance(this);
        wm.enqueue(request);
        wm.getWorkInfoByIdLiveData(request.getId())
                .observe(this, info -> {
                    if (isFinishingActivity || info == null) return;
                    boolean ended = info.getState() == WorkInfo.State.CANCELLED
                            || info.getState() == WorkInfo.State.FAILED
                            || info.getState() == WorkInfo.State.SUCCEEDED;
                    if (ended && mProgress != null) mProgress.dismiss();

                    if (info.getState() == WorkInfo.State.SUCCEEDED) {
                        if (audio != null) {
                            proceedToVolume(merged, new File(audio.getPath()));
                        } else {
                            proceedToFilter(merged);
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        confirmClose();
    }

    private void confirmClose() {
        new PopupBuilder(this).showReliteDiscardPopup(
                R.drawable.vector_delete,
                getString(R.string.discard_entire_video),
                getString(R.string.if_you_go_back_now_you_will_lose_all_the_clips_added_to_your_video),
                getString(R.string.discard_video),
                getString(R.string.cancel),
                () -> finish()
        );
    }

    private void downloadSticker(StickerRoot.StickerItem stickerDummy) {
        if (stickerDummy == null) {
            Toast.makeText(this, getString(R.string.sticker_item_is_missing), Toast.LENGTH_SHORT).show();
            return;
        }
        File stickers = new File(getFilesDir(), "stickers");
        if (!stickers.exists() && !stickers.mkdirs()) {
            Log.w(TAG, "Could not create directory at " + stickers);
        }
        String extension = ".png";
        try {
            extension = stickerDummy.getSticker().substring(stickerDummy.getSticker().lastIndexOf("."));
        } catch (Exception ignored) {
        }
        File image = new File(stickers, stickerDummy.getId() + extension);
        if (image.exists()) {
            addSticker(image);
            return;
        }
        CustomDialogClass progress = new CustomDialogClass(this, R.style.customStyle);
        progress.setCancelable(false);
        progress.show();
        Data input = new Data.Builder()
                .putString(FileDownloadWorker.KEY_INPUT, stickerDummy.getSticker())
                .putString(FileDownloadWorker.KEY_OUTPUT, image.getAbsolutePath())
                .build();
        WorkRequest request = new OneTimeWorkRequest.Builder(FileDownloadWorker.class)
                .setInputData(input)
                .build();
        WorkManager wm = WorkManager.getInstance(this);
        wm.enqueue(request);
        wm.getWorkInfoByIdLiveData(request.getId())
                .observe(this, info -> {
                    if (isFinishingActivity || info == null) return;
                    boolean ended = info.getState() == WorkInfo.State.CANCELLED
                            || info.getState() == WorkInfo.State.FAILED
                            || info.getState() == WorkInfo.State.SUCCEEDED;
                    if (ended && progress != null) progress.dismiss();

                    if (info.getState() == WorkInfo.State.SUCCEEDED) {
                        addSticker(image);
                    }
                });
    }

    private void proceedToFilter(File video) {
        if (isFinishingActivity || video == null) return;
        Intent intent = new Intent(this, FilterActivity.class);
        intent.putExtra(FilterActivity.EXTRA_SONG, mModel != null ? mModel.song : "");
        intent.putExtra(FilterActivity.EXTRA_VIDEO, video.getAbsolutePath());
        startActivity(intent);
        finish();
    }

    private void proceedToVolume(File video, File audio) {
        if (isFinishingActivity || video == null || audio == null) return;
        Intent intent = new Intent(this, VolumeActivity.class);
        intent.putExtra(VolumeActivity.EXTRA_SONG, mModel != null ? mModel.song : "");
        intent.putExtra(VolumeActivity.EXTRA_VIDEO, video.getAbsolutePath());
        intent.putExtra(VolumeActivity.EXTRA_AUDIO, audio.getAbsolutePath());
        startActivity(intent);
        finish();
    }

    private void processCurrentRecording() {
        if (mModel != null && mModel.video != null) {
            long duration = 0;
            try {
                duration = VideoUtil.getDuration(this, Uri.fromFile(mModel.video));
            } catch (IOException e) {
                Log.e(TAG, "Error getting video duration", e);
            }
            if (mModel.speed != 1) {
                applyVideoSpeed(mModel.video, mModel.speed, duration);
            } else {
                RecordSegment segment = new RecordSegment();
                segment.file = mModel.video.getAbsolutePath();
                segment.duration = duration;
                mModel.segments.add(segment);
            }
        }
        if (mModel != null) mModel.video = null;
    }

    private void setupSong(@Nullable SongRoot.SongItem songDummy, Uri file) {
        if (file == null) {
            Toast.makeText(this, getString(R.string.audio_file_is_missing), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.release();
            }
            mMediaPlayer = MediaPlayer.create(this, file);
            if (mMediaPlayer == null) {
                Toast.makeText(this, getString(R.string.could_not_load_audio), Toast.LENGTH_SHORT).show();
                return;
            }
            mMediaPlayer.setOnCompletionListener(mp -> mMediaPlayer = null);
            TextView sound = findViewById(R.id.sound);
            if (sound != null) {
                if (songDummy != null) {
                    sound.setText(songDummy.getTitle());
                    if (mModel != null) mModel.song = songDummy.getId();
                } else {
                    sound.setText(getString(R.string.audio_from_clip));
                }
            }
            if (mModel != null) mModel.audio = file;
        } catch (Exception e) {
            Log.e(TAG, "Error setting up song", e);
        }
    }

    private void startRecording() {
        if (mModel == null || mCamera == null) return;
        long recorded = mModel.recorded();
        if (recorded >= SharedConstants.MAX_DURATION) {
            Toast.makeText(RecorderActivity.this, R.string.recorder_error_maxed_out, Toast.LENGTH_SHORT).show();
        } else {
            mModel.video = TempUtil.createNewFile(this, ".mp4");
            mCamera.takeVideoSnapshot(mModel.video, (int) (SharedConstants.MAX_DURATION - recorded));
        }
    }

    @SuppressLint("SetTextI18n")
    private void startTimer() {
        View countdown = findViewById(R.id.countdown);
        TextView count = findViewById(R.id.count);
        if (count != null) count.setText(null);
        Slider selection = findViewById(R.id.selection);
        if (selection == null) return;
        long duration = (long) selection.getValue();
        CountDownTimer timer = new CountDownTimer(3000, 1000) {

            @Override
            public void onTick(long remaining) {
                if (count != null) {
                    mHandler.post(() -> count.setText(TimeUnit.MILLISECONDS.toSeconds(remaining) + 1 + ""));
                }
            }

            @Override
            public void onFinish() {
                if (countdown != null) mHandler.post(() -> countdown.setVisibility(View.GONE));
                startRecording();
                mHandler.postDelayed(mStopper, duration);
            }
        };
        if (countdown != null) {
            countdown.setOnClickListener(v -> {
                timer.cancel();
                countdown.setVisibility(View.GONE);
            });
            countdown.setVisibility(View.VISIBLE);
        }
        timer.start();
    }

    private void stopRecording() {
        if (mCamera == null) return;
        mCamera.stopVideo();
    }

    private double getFileSizeInMB(Uri fileUri) {
        if (fileUri == null) return 0.0;
        Cursor returnCursor = null;
        try {
            returnCursor = getContentResolver().query(fileUri, null, null, null, null);
            if (returnCursor == null) return 0.0;
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            if (!returnCursor.moveToFirst()) return 0.0;
            long size = returnCursor.getLong(sizeIndex);
            return (double) size / (1024 * 1024);
        } catch (Exception e) {
            Log.e(TAG, "Error getting file size", e);
            return 0.0;
        } finally {
            if (returnCursor != null) returnCursor.close();
        }
    }

    private void submitUpload(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, getString(R.string.selected_video_uri_is_missing), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File copy = TempUtil.createCopy(this, uri, ".mp4");
            // Handle the video upload/trim workflow here
        } catch (Exception e) {
            Log.e(TAG, "Error in submitUpload", e);
            Toast.makeText(this, getString(R.string.failed_to_prepare_video_for_upload), Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleVisibility(boolean show) {
        if (!getResources().getBoolean(R.bool.clutter_free_recording_enabled)) {
            return;
        }
        View top = findViewById(R.id.top);
        AnimationUtil.toggleVisibilityToTop(top, show);
        View right = findViewById(R.id.right);
        AnimationUtil.toggleVisibilityToLeft(right, show);
        View upload = findViewById(R.id.upload);
        AnimationUtil.toggleVisibilityToBottom(upload, show);
        View done = findViewById(R.id.done);
        AnimationUtil.toggleVisibilityToBottom(done, show);
    }

    public static class RecorderActivityViewModel extends ViewModel {
        public Uri audio;
        public List<RecordSegment> segments = new ArrayList<>();
        public String song = "";
        public float speed = 1;
        public File video;

        public long recorded() {
            long recorded = 0;
            if (segments != null) {
                for (RecordSegment segment : segments) {
                    if (segment != null) recorded += segment.duration;
                }
            }
            return recorded;
        }
    }

    public static class RecordSegment {
        public String file;
        public long duration;
    }
}
