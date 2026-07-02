package com.codder.ultimate.live.activity;

import static android.provider.MediaStore.MediaColumns.DATA;
import static com.codder.ultimate.live.activity.LocationChooseActivity.REQ_CODE_LOCATION;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityUploadPostBinding;
import com.codder.ultimate.databinding.BottomSheetPrivacyBinding;
import com.codder.ultimate.live.model.SearchLocationRoot;
import com.codder.ultimate.live.utils.SocialSpanUtil;
import com.codder.ultimate.live.utils.autoComplete.AutocompleteUtil;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.socialView.SocialEditText;
import com.codder.ultimate.worker.PostUploadWorker;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.jakewharton.rxbinding4.view.RxView;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class UploadPostActivity extends BaseActivity {

    private static final String TAG = "UploadPostActivity";
    private static final int REQ_CODE_HASHTAG = 122;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int RESULT_LOAD_IMAGE = 201;
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "cropimage";
    private static final String CROPPED_IMAGE_EXTENSION = ".png";

    private ActivityUploadPostBinding binding;
    private UploadActivityViewModel mModel;

    private RayziUtils.Privacy privacy = RayziUtils.Privacy.PUBLIC;
    private Uri selectedImage;

    private boolean isUploading = false;

    private String picturePath = "";
    private SearchLocationRoot.DataItem selectedLocation;
    private boolean allowComments = true;
    private boolean isCaptured = false;
    private String capturedImage = "";

    private RequestOptions requestOptions;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload_post);
        mModel = new ViewModelProvider(this).get(UploadActivityViewModel.class);

        setupRequestOptions();
        handleIntentDataSafely();
        setPrivacy(privacy);
        initListeners();

        loadInitialLocation();

        setupDescriptionEditTextObserver();

        // Manage disposables lifecycle to prevent leaks
        getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            if (event == Lifecycle.Event.ON_DESTROY) {
                compositeDisposable.clear();
            }
        });
    }

    private void setupRequestOptions() {
        requestOptions = new RequestOptions()
                .transform(new CenterCrop(), new RoundedCorners(15));
    }

    private void handleIntentDataSafely() {
        Intent intent = getIntent();
        if (intent == null) {
            Log.w(TAG, "Intent is null, no data to handle.");
            return;
        }

        capturedImage = intent.getStringExtra(Const.CAPTURED_POST_IMAGE);
        isCaptured = intent.getBooleanExtra(Const.IS_CAPTURED, false);

        if (capturedImage != null && !capturedImage.isEmpty()) {
            binding.btnDelete.setVisibility(View.VISIBLE);
            Glide.with(this).load(capturedImage).apply(requestOptions).into(binding.imageview);
            selectedImage = Uri.parse(capturedImage);
            picturePath = capturedImage;
        } else {
            picturePath = intent.getStringExtra(Const.GALLERY_PHOTO_PATH);
            if (picturePath != null && !picturePath.isEmpty()) {
                binding.btnDelete.setVisibility(View.VISIBLE);
                Glide.with(this).load(picturePath).apply(requestOptions).into(binding.imageview);
                selectedImage = Uri.parse(picturePath);
            } else {
                binding.btnDelete.setVisibility(View.GONE);
                selectedImage = null;
                picturePath = "";
            }
        }

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        // Reset old intent image state
                        capturedImage = null;
                        isCaptured = false;

                        selectedImage = uri;
                        picturePath = copyUriToInternalStorage(uri, ".jpg");
                        loadImageIntoView(uri);
                        binding.btnDelete.setVisibility(View.VISIBLE);
                        binding.btnAdd.setVisibility(View.GONE);
                    } else {
                        Log.i(TAG, "handleIntentDataSafely: No image selected");
//                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    private void loadInitialLocation() {
        String currentCity = sessionManager.getStringValue(Const.CURRENT_CITY);
        String country = sessionManager.getStringValue(Const.COUNTRY);

        if (currentCity != null && !currentCity.isEmpty()) {
            binding.tvLocation.setText(currentCity);
        } else if (country != null && !country.isEmpty()) {
            binding.tvLocation.setText(country);
        } else {
            binding.tvLocation.setText(getString(R.string.location_not_set));
        }
    }

    private void setupDescriptionEditTextObserver() {
        SocialEditText description = binding.descriptionView;
        if (mModel.description != null) {
            description.setText(mModel.description);
        }

        Disposable disposable = RxTextView.afterTextChangeEvents(description)
                .skipInitialValue()
                .subscribe(event -> {
                    Editable editable = event.getEditable();
                    mModel.description = editable != null ? editable.toString() : null;

                }, throwable -> Log.e(TAG, "Error observing description text changes", throwable));

        compositeDisposable.add(disposable);

        SocialSpanUtil.apply(description, mModel.description, null);
        AutocompleteUtil.setupForHashtags(this, description);
        AutocompleteUtil.setupForUsers(this, description);
    }

    private void initListeners() {
        binding.lytPrivacy.setOnClickListener(v -> showPrivacyBottomSheet());

        // Debounced post click using RxBinding
        Disposable postClickDisposable = RxView.clicks(binding.tvPostClick)
                .throttleFirst(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unit -> postUploaded());
        compositeDisposable.add(postClickDisposable);

        binding.ivBack.setOnClickListener(v -> finish());

        binding.switchComments.setOnCheckedChangeListener((buttonView, isChecked) -> allowComments = isChecked);

        binding.btnAdd.setOnClickListener(v -> choosePhoto());

        binding.btnDelete.setOnClickListener(v -> {
            binding.imageview.setImageDrawable(null);
            selectedImage = null;
            picturePath = "";

            capturedImage = null;
            isCaptured = false;

            binding.btnDelete.setVisibility(View.GONE);
            binding.btnAdd.setVisibility(View.VISIBLE);
        });

        binding.lytLocation.setOnClickListener(v -> startActivityForResult(
                new Intent(this, LocationChooseActivity.class)
                        .putExtra(Const.DATA, binding.tvLocation.getText().toString()),
                REQ_CODE_LOCATION));
    }

    private void showPrivacyBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.customStyle);

        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet == null) return;

        });

        BottomSheetPrivacyBinding sheetPrivacyBinding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.bottom_sheet_privacy, null, false);
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

        sheetPrivacyBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
    }

    private void postUploaded() {
        if (isUploading) {
            Log.w(TAG, "Upload already in progress");
            return;
        }

        isUploading = true;
        binding.tvPostClick.setEnabled(false); // Disable button
        binding.progressbar.setVisibility(View.VISIBLE);

        if (!sessionManager.getUser().isHost() &&
                !sessionManager.getUser().getLevel().getAccessibleFunction().isUploadPost()) {
            showLevelRestrictionPopup();
            binding.progressbar.setVisibility(View.GONE);
            return;
        }

        if (!sessionManager.getUser().getLevel().getAccessibleFunction().isUploadPost()) {
            showLevelRestrictionPopup();
            binding.progressbar.setVisibility(View.GONE);
            return;
        }

        String finalCaption = binding.descriptionView.getText() != null ? binding.descriptionView.getText().toString() : "";

        Log.d(TAG, "Description: " + finalCaption);
        Log.d(TAG, "Hashtags: " + binding.descriptionView.getHashtags());
        Log.d(TAG, "Mentions: " + binding.descriptionView.getMentions());

        String finalUploadingImageLink = getFinalImageLink();
        if (finalUploadingImageLink == null || finalUploadingImageLink.isEmpty()) {
            Toast.makeText(this, R.string.select_image_first, Toast.LENGTH_SHORT).show();
            binding.progressbar.setVisibility(View.GONE);
            return;
        }

        StringBuilder finalHashTag = new StringBuilder();
        for (String hashtag : binding.descriptionView.getHashtags()) {
            finalHashTag.append(hashtag).append(",");
        }

        StringBuilder finalMentionPeople = new StringBuilder();
        for (String mention : binding.descriptionView.getMentions()) {
            finalMentionPeople.append(mention).append(",");
        }

        Data data = new Data.Builder()
                .putString(Const.POST_IMAGE_LINK, finalUploadingImageLink)
                .putString(Const.SELECTED_LOCATION, binding.tvLocation.getText() != null ? binding.tvLocation.getText().toString().trim() : "")
                .putString(Const.CAPTION, finalCaption)
                .putString(Const.HASH_TAG, finalHashTag.toString())
                .putString(Const.MENTION_PEOPLE, finalMentionPeople.toString())
                .putString(Const.SHOW_POST, String.valueOf(getPrivacy()))
                .putString(Const.ALLOW_COMMENT, String.valueOf(allowComments))
                .build();

        // Cancel existing upload jobs just in case
        WorkManager.getInstance(this).cancelAllWorkByTag("UPLOAD_POST");

        WorkRequest workRequest = new OneTimeWorkRequest.Builder(PostUploadWorker.class)
                .setInputData(data)
                .addTag("UPLOAD_POST")
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);

        Toast.makeText(this, getString(R.string.uploading), Toast.LENGTH_SHORT).show();

        binding.progressbar.postDelayed(() -> {
            resetUploadState();
            onBackPressed();
        }, 800);
    }

    private void resetUploadState() {
        isUploading = false;
        binding.tvPostClick.setEnabled(true);
        binding.progressbar.setVisibility(View.GONE);
    }

    private void showLevelRestrictionPopup() {
        new PopupBuilder(this)
                .showSimplePopup(getString(R.string.you_are_not_able_to_post_at_your_level), getString(R.string.dismiss), () -> {
                });
    }

    private String getFinalImageLink() {
        if (isCaptured && capturedImage != null && !capturedImage.isEmpty()) {
            return capturedImage;
        } else if (picturePath != null && !picturePath.isEmpty()) {
            return picturePath;
        }
        return null;
    }

    private void choosePhoto() {
        pickImageLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission Granted.");
                choosePhoto();
            } else {
                Log.w(TAG, "Permission Denied.");
                Toast.makeText(this, R.string.permission_denied_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) {
            return;
        }

        if (requestCode == RESULT_LOAD_IMAGE) {
            handleImageSelection(data);
        } else if (requestCode == UCrop.REQUEST_CROP) {
            handleCropResult(data);
        } else if (requestCode == UCrop.RESULT_ERROR) {
            handleCropError(data);
        } else if (requestCode == REQ_CODE_LOCATION) {
            handleLocationResult(data);
        } else if (requestCode == REQ_CODE_HASHTAG) {
            // Handle hashtag result if needed
        }
    }

    private void handleImageSelection(@NonNull Intent data) {
        Uri imageUri = data.getData();
        if (imageUri == null) {
            Toast.makeText(this, R.string.error_selecting_image, Toast.LENGTH_SHORT).show();
            return;
        }

        // Reset intent image data
        capturedImage = null;
        isCaptured = false;

        selectedImage = imageUri;
        startCropActivity(imageUri);
        loadImageIntoView(imageUri);
        picturePath = getRealPathFromURI(imageUri);
        binding.btnDelete.setVisibility(View.VISIBLE);
    }

    public void startCropActivity(@NonNull Uri uri) {
        String destinationUri = SAMPLE_CROPPED_IMAGE_NAME + CROPPED_IMAGE_EXTENSION;
        UCrop.Options options = new UCrop.Options();
        // Customize UCrop options here if needed

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            UCrop.of(uri, Uri.fromFile(getCacheDir().toPath().resolve(destinationUri).toFile()))
                    .withOptions(options)
                    .start(this);
        }
    }

    private void loadImageIntoView(Uri uri) {
        Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_user_place)
                .error(R.drawable.ic_user_place)
                .apply(requestOptions)
                .into(binding.imageview);
        binding.imageview.setAdjustViewBounds(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleCropResult(@NonNull Intent result) {
        Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {
            selectedImage = resultUri;
            loadImageIntoView(resultUri);
            picturePath = getRealPathFromURI(resultUri);
            binding.btnDelete.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, R.string.toast_cannot_retrieve_cropped_image, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCropError(@NonNull Intent result) {
        Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e(TAG, "Crop error", cropError);
            Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.toast_unexpected_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLocationResult(@NonNull Intent data) {
        String locationData = data.getStringExtra(Const.DATA);

        Log.d(TAG, "handleLocationResult: =======> " + locationData.toString());
        if (locationData.isEmpty()) {
            Log.w(TAG, "Location data is empty");
            return;
        }

        if (locationData.trim().startsWith("{")) {
            SearchLocationRoot.DataItem location = new Gson().fromJson(locationData, SearchLocationRoot.DataItem.class);
            if (location != null) {
                selectedLocation = location;
                binding.tvLocation.setText(location.getLabel());
            }
        } else {
            Log.e(TAG, "Location data is not valid JSON: " + locationData);
        }

    }

    /**
     * Safely retrieves the real file path from the content URI.
     * @param contentUri Uri of the image
     * @return String path or empty string if not found
     */
    public String getRealPathFromURI(Uri contentUri) {
        if (contentUri == null) return "";
        String[] projection = {DATA};
        try (Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(DATA);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting real path from URI", e);
        }
        return "";
    }

    private int getPrivacy() {
        return privacy == RayziUtils.Privacy.FOLLOWERS ? 1 : 0;
    }

    private void setPrivacy(@NonNull RayziUtils.Privacy privacy) {
        this.privacy = privacy;
        binding.tvPrivacy.setText(privacy == RayziUtils.Privacy.FOLLOWERS ? getString(R.string.my_followers) : getString(R.string.public_));
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    public static class UploadActivityViewModel extends ViewModel {
        public String description = null;
    }

    private String copyUriToInternalStorage(Uri uri, String extension) {
        try {
            File output = new File(getCacheDir(), "upload_" + System.currentTimeMillis() + extension);
            try (InputStream in = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(output)) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }
            }
            return output.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error copying file from URI", e);
            return "";
        }
    }

}
