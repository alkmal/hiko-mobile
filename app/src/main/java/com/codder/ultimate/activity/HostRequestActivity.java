package com.codder.ultimate.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.ActivityHostRequestBinding;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HostRequestActivity extends BaseActivity {

    public static final String TAG = "HostRequestActivity";
    private ActivityHostRequestBinding binding;
    private SessionManager sessionManager;
    private static final int REQUEST_FRONT_IMAGE = 100;
    private static final int REQUEST_BACK_IMAGE = 110;
    private String frontImagePath = "", backImagePath = "";
    private boolean isFrontPhoto;

    private final ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncher =
            registerForActivityResult(new PickVisualMedia(), uri -> {
                if (uri != null) {
                    String imagePath = getRealPathFromURI(uri);
                    RequestOptions requestOptions = new RequestOptions()
                            .transforms(new CenterCrop(), new RoundedCorners(25));

                    if (isFrontPhoto) {
                        frontImagePath = imagePath;
                        binding.ivSelectedImage.setVisibility(View.VISIBLE);
                        binding.layUploadFront.setVisibility(View.GONE);
                        Glide.with(this).load(frontImagePath).apply(requestOptions).into(binding.ivSelectedImage);
                    } else {
                        backImagePath = imagePath;
                        binding.ivSelectedImageBack.setVisibility(View.VISIBLE);
                        binding.layUploadBack.setVisibility(View.GONE);
                        Glide.with(this).load(backImagePath).apply(requestOptions).into(binding.ivSelectedImageBack);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_host_request);
        sessionManager = new SessionManager(this);

        setupInitialViews();
        setupClickListeners();
    }

    private void setupInitialViews() {
        binding.etName.setText(Objects.requireNonNullElse(sessionManager.getUser().getName(), ""));
        binding.etBio.setText(Objects.requireNonNullElse(sessionManager.getUser().getBio(), ""));

        String agencyCode = getIntent() != null ? getIntent().getStringExtra(Const.DATA) : "";
        binding.etAgencyCode.setText(Objects.requireNonNullElse(agencyCode, ""));
    }

    private void setupClickListeners() {
        binding.ivBack.setOnClickListener(v -> onBackPressed());

        binding.layFrontPhoto.setOnClickListener(v -> {
            isFrontPhoto = true;
            launchImagePicker();
        });

        binding.layBackPhoto.setOnClickListener(v -> {
            isFrontPhoto = false;
            launchImagePicker();
        });

        binding.txtNext.setOnClickListener(v -> validateAndSubmitForm());
    }

    private void launchImagePicker() {
        imagePickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());

    }

    private void validateAndSubmitForm() {
        String name = binding.etName.getText().toString().trim();
        String bio = binding.etBio.getText().toString().trim();
        String agency = binding.etAgencyCode.getText().toString().trim();
        String mobileNumber = binding.etMobileNumber.getText().toString().trim();
        String bankDetails = binding.etBankDetails.getText().toString().trim();

        if (name.isEmpty()) {
            showToast(R.string.enter_name);
        } else if (mobileNumber.isEmpty()) {
            showToast(R.string.enter_mobile_number);
        } else if (bio.isEmpty()) {
            showToast(R.string.enter_bio_text);
        } else if (bankDetails.isEmpty()) {
            showToast(R.string.enter_your_bank_details);
        } else if (frontImagePath.isEmpty()) {
            showToast(R.string.select_personal_photo);
        } else {
            submitHostRequest(name, mobileNumber, bio, bankDetails, agency);
        }
    }

    private void submitHostRequest(String name, String mobile, String bio, String bank, String agency) {
        customDialogClass.show();

        if (name == null || name.isEmpty() || mobile == null || mobile.isEmpty() || bio == null || bio.isEmpty()) {
            showToast(R.string.all_fields_required);
            customDialogClass.dismiss();
            return;
        }

        File imageFile = new File(frontImagePath);
        if (!imageFile.exists()) {
            showToast(R.string.image_file_not_found);
            customDialogClass.dismiss();
            return;
        }

        RequestBody imageRequest = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("profileImage", imageFile.getName(), imageRequest);

        HashMap<String, RequestBody> formFields = new HashMap<>();
        formFields.put("userId", toPlainText(sessionManager.getUser().getId()));
        formFields.put("name", toPlainText(name));
        formFields.put("mobileNumber", toPlainText(mobile));
        formFields.put("bio", toPlainText(bio));
        formFields.put("bankDetails", toPlainText(bank));
        formFields.put("agencyCode", toPlainText(agency));

        RetrofitBuilder.create().addHostRequest(formFields, imagePart)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<RestResponse> call, @NonNull Response<RestResponse> response) {
                        customDialogClass.dismiss();

                        if (response.isSuccessful() && response.body() != null) {
                            RestResponse body = response.body();
                            if (body.isStatus()) {
                                showToast(R.string.host_request_send);
                                finish();
                            } else {
                                Toast.makeText(HostRequestActivity.this, body.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "Host request failed: " + body.getMessage());
                            }
                        } else {
                            // If the server sent an HTTP error, the details are often in errorBody()
                            String errorMsg = "Please try again later.";
                            try {
                                if (response.errorBody() != null) {
                                    errorMsg = response.errorBody().string();
                                }
                            } catch (Exception ignored) {
                            }
                            Toast.makeText(HostRequestActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Failed to submit host request. HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<RestResponse> call, @NonNull Throwable t) {
                        customDialogClass.dismiss();
                        showToast(R.string.try_after_some_time);
                        Log.e(TAG, "Failed to submit host request: " + t.getMessage(), t);
                    }
                });
    }

    private RequestBody toPlainText(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value != null ? value : "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            return;
        }

        Uri selectedImageUri = data.getData();
        String imagePath = getRealPathFromURI(selectedImageUri);
        RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(25));

        if (requestCode == REQUEST_FRONT_IMAGE) {
            frontImagePath = imagePath;
            binding.ivSelectedImage.setVisibility(View.VISIBLE);
            binding.layUploadFront.setVisibility(View.GONE);
            Glide.with(this).load(frontImagePath).apply(requestOptions).into(binding.ivSelectedImage);
        } else if (requestCode == REQUEST_BACK_IMAGE) {
            backImagePath = imagePath;
            binding.ivSelectedImageBack.setVisibility(View.VISIBLE);
            binding.layUploadBack.setVisibility(View.GONE);
            Glide.with(this).load(backImagePath).apply(requestOptions).into(binding.ivSelectedImageBack);
        }
    }

    private void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    @Nullable
    public String getRealPathFromURI(Uri uri) {
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            try (var cursor = getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    return cursor.getString(columnIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getRealPathFromURI: ", e);
        }
        return "";
    }
}
