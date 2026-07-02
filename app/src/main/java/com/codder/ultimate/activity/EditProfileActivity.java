package com.codder.ultimate.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ActivityEditProfileBinding;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends BaseActivity {

    private static final String TAG = "EditProfileActivity";
    ActivityEditProfileBinding binding;
    private static final int GALLERY_CODE_PROFILE = 1001;
    private static final int GALLERY_CODE_COVER = 1002;
    private UserRoot.User user;
    private String gender = Const.MALE;
    private String picturePath = "";
    private String coverPhotoPath = "";
    String name;

    private ActivityResultLauncher<PickVisualMediaRequest> pickProfileImageLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickCoverImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);

        user = sessionManager.getUser();
        if (user == null) {
            Toast.makeText(this, getString(R.string.user_data_not_found_please_login_again), Toast.LENGTH_SHORT).show();
            finishAffinity();
            return;
        }

        setupUI();
        setupListeners();

    }

    private void setupUI() {
        binding.pd1.setVisibility(View.GONE);

        binding.imgUser.setUserImage(user.getImage(),"",5);

        binding.etName.setText(capitalize(safe(user.getName())));
        binding.etBio.setText(capitalize(safe(user.getBio())));

        binding.etUserName.setText(safe(user.getUniqueId()));
        binding.etAge.setText(String.valueOf(user.getAge()));

        binding.etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                name = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if (Const.FEMALE.equalsIgnoreCase(user.getGender())) {
            selectFemale();
        } else {
            selectMale();
        }

        pickProfileImageLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        picturePath = copyUriToInternalStorage(uri, ".jpg");
                        binding.imgUser.setUserImage(String.valueOf(uri),"",5);
                    }
                }
        );

        pickCoverImageLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        coverPhotoPath = copyUriToInternalStorage(uri, ".jpg");
                    }
                }
        );

    }

    private void setupListeners() {
        binding.lytMale.setOnClickListener(v -> selectMale());
        binding.lytFemale.setOnClickListener(v -> selectFemale());

        binding.imgUser.setOnClickListener(v -> choosePhoto(GALLERY_CODE_PROFILE));
        binding.btnPencil.setOnClickListener(v -> choosePhoto(GALLERY_CODE_PROFILE));

        binding.ivBack.setOnClickListener(v -> onBackPressed());

        binding.tvSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void validateAndSubmit() {
        String name = binding.etName.getText().toString().trim();
        String username = binding.etUserName.getText().toString().trim();
        String bio = binding.etBio.getText().toString().trim();
        int age = Integer.parseInt(binding.etAge.getText().toString().trim());

        if (name.isEmpty()) {
            Toast.makeText(this, R.string.enter_your_name, Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.isEmpty()) {
            Toast.makeText(this, R.string.enter_username_first, Toast.LENGTH_SHORT).show();
            return;
        }

        if (gender.isEmpty()) {
            Toast.makeText(this, R.string.select_your_gender, Toast.LENGTH_SHORT).show();
            return;
        }

        if (age < 18) {
            Toast.makeText(this, R.string.minimum_age_must_be_18_years, Toast.LENGTH_SHORT).show();
            return;
        }

        updateProfile(name, username, bio, age);
    }

    private void updateProfile(String name, String username, String bio, int age) {
        if (customDialogClass != null && !customDialogClass.isShowing()) {
            customDialogClass.show();
        }

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("name", RequestBody.create(MediaType.parse("text/plain"), name));
        map.put("username", RequestBody.create(MediaType.parse("text/plain"), username));
        map.put("bio", RequestBody.create(MediaType.parse("text/plain"), bio));
        map.put("userId", RequestBody.create(MediaType.parse("text/plain"), sessionManager.getUser().getId()));
        map.put("gender", RequestBody.create(MediaType.parse("text/plain"), gender));
        map.put("age", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(age)));

        MultipartBody.Part profileImage = prepareFilePart("image", picturePath);
        MultipartBody.Part coverImage = prepareFilePart("coverImage", coverPhotoPath);

        Log.d(TAG, "POST Data:");
        Log.d(TAG, "Name: " + name);
        Log.d(TAG, "Username: " + username);
        Log.d(TAG, "Bio: " + bio);
        Log.d(TAG, "UserID: " + sessionManager.getUser().getId());
        Log.d(TAG, "Gender: " + gender);
        Log.d(TAG, "Age: " + age);

        RetrofitBuilder.create().updateUser(map, profileImage, coverImage).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (customDialogClass != null) customDialogClass.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    UserRoot userRoot = response.body();
                    if (userRoot.isStatus()) {
                        sessionManager.saveUser(userRoot.getUser());
                        sessionManager.saveBooleanValue(Const.IS_LOGIN, true);
                        startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                        finishAffinity();
                    } else {
                        Log.w(TAG, getString(R.string.update_failed_) + userRoot.getMessage());
                        Toast.makeText(EditProfileActivity.this,
                                getString(R.string.update_failed_) + (userRoot.getMessage() != null ? userRoot.getMessage() : getString(R.string.unknown_error)),
                                Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Log.e(TAG, "Update failed: HTTP " + response.code() + " - " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(EditProfileActivity.this, getString(R.string.update_failed_) + response.message(), Toast.LENGTH_SHORT).show();

                }
            }


            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                if (customDialogClass != null) customDialogClass.dismiss();
                Log.e(TAG, "Update API failed: " + t.getLocalizedMessage(), t);
                Toast.makeText(EditProfileActivity.this, getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private MultipartBody.Part prepareFilePart(String partName, String filePath) {
        if (filePath == null || filePath.isEmpty()) return null;

        File file = new File(filePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    private void choosePhoto(int code) {
        PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build();

        if (code == GALLERY_CODE_PROFILE) {
            pickProfileImageLauncher.launch(request);
        } else if (code == GALLERY_CODE_COVER) {
            pickCoverImageLauncher.launch(request);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;

        Uri selectedImage = data.getData();
        String realPath = getRealPathFromURI(selectedImage);

        if (realPath != null) {
            if (requestCode == GALLERY_CODE_PROFILE) {
                picturePath = realPath;
                binding.imgUser.setUserImage(String.valueOf(selectedImage),"",5);
            } else if (requestCode == GALLERY_CODE_COVER) {
                coverPhotoPath = realPath;
            }
        }
    }

    private void selectMale() {
        gender = Const.MALE;

        Glide.with(this).load(R.drawable.radio_selected).into(binding.rbMale);
        Glide.with(this).load(R.drawable.radio_unselected).into(binding.rbFemale);
    }

    private void selectFemale() {
        gender = Const.FEMALE;

        Glide.with(this).load(R.drawable.radio_selected).into(binding.rbFemale);
        Glide.with(this).load(R.drawable.radio_unselected).into(binding.rbMale);

    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private String copyUriToInternalStorage(Uri uri, String extension) {
        try {
            File outFile = new File(getCacheDir(), "image_" + System.currentTimeMillis() + extension);
            try (InputStream in = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return outFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "File copy failed", e);
            return null;
        }
    }

}