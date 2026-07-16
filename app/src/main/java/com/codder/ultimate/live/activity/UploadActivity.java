package com.codder.ultimate.live.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.beak.gifmakerlib.GifMaker;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityUploadBinding;
import com.codder.ultimate.databinding.BottomSheetPrivacyBinding;
import com.codder.ultimate.live.model.SearchLocationRoot;
import com.codder.ultimate.live.utils.SocialSpanUtil;
import com.codder.ultimate.live.utils.autoComplete.AutocompleteUtil;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.utils.Draft;
import com.codder.ultimate.utils.TempUtil;
import com.codder.ultimate.utils.VideoUtil;
import com.codder.ultimate.worker.uploadingprogress.StoryUploadWorker;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.jakewharton.rxbinding4.widget.RxTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.disposables.Disposable;

public class UploadActivity extends BaseActivity {

    private static final String TAG = "UploadActivity";

    public static final String EXTRA_DRAFT = "draft";
    public static final String EXTRA_SONG = "song";
    public static final String EXTRA_VIDEO = "video";



    ActivityUploadBinding binding;
    private UploadActivityViewModel mModel;
    private Draft mDraft;
    private String mVideo;
    private String mSong;
    private SearchLocationRoot.DataItem selectedLocation;
    private RayziUtils.Privacy privacy = RayziUtils.Privacy.PUBLIC;

    private final List<Disposable> disposables = new ArrayList<>();


    private final ActivityResultLauncher<Intent> locationPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String locationData = result.getData().getStringExtra(Const.DATA);
                    if (locationData != null) {
                        SearchLocationRoot.DataItem location = new Gson().fromJson(locationData, SearchLocationRoot.DataItem.class);
                        if (location != null) {
                            selectedLocation = location;
                            binding.tvLocation.setText(location.getLabel());
                            if (mModel != null) {
                                mModel.location = location.getLabel();
                            }
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload);
        initViewModel();
        initUI();
        setupDescriptionTextWatcher();
        populateDataFromDraftOrIntent();

        loadVideoThumbnail();
        setupPrivacyUI();
        handleRTL();
    }

    private void initViewModel() {
        mModel = new ViewModelProvider(this).get(UploadActivityViewModel.class);
    }

    private void initUI() {
        binding.ivBack.setOnClickListener(v -> finish());
        binding.lytLocation.setOnClickListener(v -> openLocationPicker());
        binding.lytPrivacy.setOnClickListener(v -> showPrivacyBottomSheet());
        binding.switchComments.setOnCheckedChangeListener((buttonView, isChecked) -> mModel.hasComments = isChecked);
        binding.btnPost.setOnClickListener(v -> onClickPost());
    }

    private void setupDescriptionTextWatcher() {
        Disposable disposable = RxTextView.afterTextChangeEvents(binding.descriptionView)
                .skipInitialValue()
                .subscribe(e -> {
                    Editable editable = e.getEditable();
                    mModel.description = (editable != null) ? editable.toString() : null;
                });
        disposables.add(disposable);

        SocialSpanUtil.apply(binding.descriptionView, mModel.description, null);
        AutocompleteUtil.setupForHashtags(this, binding.descriptionView);
        AutocompleteUtil.setupForUsers(this, binding.descriptionView);
    }

    private void populateDataFromDraftOrIntent() {
        mDraft = getIntent().getParcelableExtra(EXTRA_DRAFT);
        if (mDraft != null) {
            // Use draft data
            mSong = mDraft.songId != null && !mDraft.songId.isEmpty() ? mDraft.songId : "";
            mVideo = mDraft.video;
            mModel.preview = mDraft.preview;
            mModel.screenshot = mDraft.screenshot;
            mModel.description = mDraft.description;
            mModel.privacy = mDraft.privacy;
            mModel.hasComments = mDraft.hasComments;
            mModel.location = mDraft.location;

            binding.descriptionView.setText(mDraft.description);
            binding.tvLocation.setText(mDraft.location);
            binding.switchComments.setChecked(mDraft.hasComments);
        } else {
            mSong = getIntent().getStringExtra(EXTRA_SONG);
            mVideo = getIntent().getStringExtra(EXTRA_VIDEO);
            Log.d(TAG, "Received songId: " + mSong);
        }
    }

    private void loadVideoThumbnail() {
        if (mVideo == null || mVideo.isEmpty()) {
            Log.w(TAG, "Video path is null or empty, skipping thumbnail load");
            return;
        }
        Bitmap image = VideoUtil.getFrameAtTime(mVideo, TimeUnit.SECONDS.toMicros(3));
        ImageView thumbnail = findViewById(R.id.imageview);
        thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumbnail.setImageBitmap(image);

        thumbnail.setOnClickListener(v -> {
            Intent intent = new Intent(this, PreviewActivity.class);
            intent.putExtra(PreviewActivity.EXTRA_VIDEO, mVideo);
            startActivity(intent);
        });
    }

    private void openLocationPicker() {
        Intent intent = new Intent(this, LocationChooseActivity.class);
        intent.putExtra(Const.DATA, binding.tvLocation.getText().toString());
        locationPickerLauncher.launch(intent);
    }

    private void showPrivacyBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.customStyle);

        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet == null) return;

        });

        BottomSheetPrivacyBinding sheetPrivacyBinding = DataBindingUtil.inflate(
                LayoutInflater.from(this), R.layout.bottom_sheet_privacy, null, false);
        bottomSheetDialog.setContentView(sheetPrivacyBinding.getRoot());
        bottomSheetDialog.show();

        if (privacy == RayziUtils.Privacy.PUBLIC) {
            sheetPrivacyBinding.lytPublic.setBackground(ContextCompat.getDrawable(this, R.drawable.store_tab_selected_background));
            sheetPrivacyBinding.lytOnlyFollower.setBackground(ContextCompat.getDrawable(this, R.drawable.bottom_sheet_btn_bg));
        } else {
            sheetPrivacyBinding.lytOnlyFollower.setBackground(ContextCompat.getDrawable(this, R.drawable.store_tab_selected_background));
            sheetPrivacyBinding.lytPublic.setBackground(ContextCompat.getDrawable(this, R.drawable.bottom_sheet_btn_bg));
        }

        sheetPrivacyBinding.lytPublic.setOnClickListener(v -> {
            setPrivacy(RayziUtils.Privacy.PUBLIC);
            bottomSheetDialog.dismiss();
        });
        sheetPrivacyBinding.lytOnlyFollower.setOnClickListener(v -> {
            setPrivacy(RayziUtils.Privacy.FOLLOWERS);
            bottomSheetDialog.dismiss();
        });
    }

    private void setupPrivacyUI() {
        setPrivacy(privacy);
    }

    private void handleRTL() {
        int gravity = isRTL(this) ? Gravity.END : Gravity.START;
        binding.showToLyt.setGravity(gravity);
    }

    public void onClickPost() {
        customDialogClass.show();
        if (mModel == null) {
            Log.e(TAG, "ViewModel is null, cannot proceed with upload");
            customDialogClass.dismiss();
            return;
        }

        binding.btnPost.setEnabled(false);

        String description = binding.descriptionView.getText().toString();
        List<String> mentions = binding.descriptionView.getMentions();
        List<String> hashtags = binding.descriptionView.getHashtags();

        Log.d(TAG, "Post Description: " + description);
        Log.d(TAG, "Hashtags: " + hashtags);
        Log.d(TAG, "Mentions: " + mentions);

        if (selectedLocation == null) {
            mModel.location = "";
        }

        uploadToServer();
    }

    private void uploadToServer() {
        sessionManager.setUploadProgress(1);
        Intent startIntent = new Intent(Const.UPLOAD_PROGRESS);
        startIntent.putExtra("progress", 1);
        LocalBroadcastManager.getInstance(this).sendBroadcast(startIntent);

        new Thread(() -> {
            try {
                File originalFile = new File(mVideo);
                long sizeMB = originalFile.length() / (1024 * 1024);
                Log.d(TAG, "Original file size: " + sizeMB + " MB");

                File previewFile = TempUtil.createNewFile(getFilesDir(), ".gif");
                File screenshotFile = TempUtil.createNewFile(getFilesDir(), ".png");

                // Extract a frame from video
                Bitmap frame = VideoUtil.getFrameAtTime(mVideo, TimeUnit.MILLISECONDS.toMicros(100));
                if (frame != null) {
                    try (OutputStream os = new FileOutputStream(screenshotFile)) {
                        frame.compress(Bitmap.CompressFormat.PNG, 75, os);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to save screenshot", e);
                    }
                    frame.recycle();
                }

                // Generate GIF preview
                try {
                    new GifMaker(2).makeGifFromVideo(mVideo, 1000, 3000, 250, previewFile.getAbsolutePath());
                } catch (Exception e) {
                    Log.w(TAG, "Failed to generate reel preview, using screenshot fallback", e);
                    previewFile = screenshotFile;
                }
                if (!previewFile.exists() || previewFile.length() == 0) {
                    previewFile = screenshotFile;
                }

                // Save local video object
                sessionManager.saveLocalVideo(new LocalVideo(
                        mSong,
                        mVideo,
                        screenshotFile.getAbsolutePath(),
                        previewFile.getAbsolutePath(),
                        mModel.description,
                        mModel.location,
                        sessionManager.getUser().getId(),
                        sessionManager.getUser().getUsername(),
                        String.join(",", binding.descriptionView.getHashtags()),
                        String.join(",", binding.descriptionView.getMentions()),
                        mModel.hasComments,
                        getPrivacy()
                ));

                // Back to main thread: update UI and enqueue WorkManager
                runOnUiThread(() -> {
                    OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(StoryUploadWorker.class).build();
                    Log.d(TAG, "Enqueued uploadWorkRequest: =============> " + uploadWorkRequest.getId());

                    Intent intent = new Intent(Const.UPLOAD_PROGRESS);
                    intent.putExtra("progress", 1);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                    WorkManager.getInstance(this).enqueue(uploadWorkRequest);
                    Log.d(TAG, "Upload work enqueued. ID: " + uploadWorkRequest.getId());

                    // Final UI cleanup
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        customDialogClass.dismiss();
                        finish();
                    }, 300);
                });

            } catch (Exception ex) {
                Log.e(TAG, "Upload failed", ex);
                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.upload_failed_) + ex.getMessage(), Toast.LENGTH_LONG).show();
                    customDialogClass.dismiss();
                    binding.btnPost.setEnabled(true);
                });
            }
        }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (customDialogClass != null && customDialogClass.isShowing()) {
            customDialogClass.dismiss();
        }
        customDialogClass = null;

        for (Disposable disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        disposables.clear();
    }

    private int getPrivacy() {
        return privacy == RayziUtils.Privacy.FOLLOWERS ? 1 : 0;
    }

    private void setPrivacy(RayziUtils.Privacy privacy) {
        this.privacy = privacy;
        if (privacy == RayziUtils.Privacy.FOLLOWERS) {
            binding.tvPrivacy.setText(R.string.my_followers);
        } else {
            binding.tvPrivacy.setText(getString(R.string.public_text));
        }
    }

    public static class UploadActivityViewModel extends ViewModel {
        public String description = null;
        public boolean hasComments = true;
        public String location = "";

        public String preview;
        public String screenshot;
        public int privacy;
        public String[] hashtags;
        public String[] mentions;
    }

    public static class LocalVideo {
        private final String songId;
        private final String video;
        private final String screenshot;
        private final String preview;
        private final String description;
        private final String location;
        private final String userId;
        private final String username;
        private final String hashtags;
        private final String mentions;
        private final boolean hasComments;
        private final int privacy;

        public LocalVideo(String songId, String video, String screenshot,
                          String preview, String description, String location,
                          String userId, String username, String hashtags, String mentions,
                          boolean hasComments, int privacy) {
            this.songId = songId;
            this.video = video;
            this.screenshot = screenshot;
            this.preview = preview;
            this.description = description;
            this.location = location;
            this.userId = userId;
            this.username = username;
            this.hashtags = hashtags;
            this.mentions = mentions;
            this.hasComments = hasComments;
            this.privacy = privacy;
        }

        public String getSongId() {
            return songId;
        }

        public String getVideo() {
            return video;
        }

        public String getScreenshot() {
            return screenshot;
        }

        public String getPreview() {
            return preview;
        }

        public String getDescription() {
            return description;
        }

        public String getLocation() {
            return location;
        }

        public String getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getHashtags() {
            return hashtags;
        }

        public String getMentions() {
            return mentions;
        }

        public boolean isHasComments() {
            return hasComments;
        }

        public int getPrivacy() {
            return privacy;
        }
    }
}
