package com.codder.ultimate.activity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.databinding.ActivityCreateComplainBinding;
import com.codder.ultimate.viewModel.CreateComplainViewModel;

import java.io.File;

public class CreateComplainActivity extends BaseActivity {
    private CreateComplainViewModel viewModel;
    private ActivityCreateComplainBinding binding;
    private Uri selectedImage;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImage = uri;
                    loadImage(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_complain);

        viewModel = new ViewModelProvider(this).get(CreateComplainViewModel.class);

        initView();
        initListeners();
        observeViewModel();
    }

    private void initListeners() {
        binding.btnOpenGallery.setOnClickListener(v -> launchPhotoPicker());

        binding.tvSubmit.setOnClickListener(v -> onSubmitClick());
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(this, isLoading -> {
            if (isLoading) {
                customDialogClass.show();
                binding.tvSubmit.setEnabled(false);
            } else {
                customDialogClass.dismiss();
                binding.tvSubmit.setEnabled(true);
            }
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getComplainResponse().observe(this, response -> {
            if (response != null && response.isStatus()) {
                Toast.makeText(this, R.string.complain_send_successfully, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.something_went_wrong_text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onSubmitClick() {
        String message = binding.etMessage.getText().toString().trim();
        String contact = binding.etContact.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(this, R.string.please_enter_message, Toast.LENGTH_SHORT).show();
            return;
        }

        if (contact.isEmpty()) {
            Toast.makeText(this, R.string.please_enter_contact, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(contact) && !isValidPhone(contact)) {
            Toast.makeText(this, R.string.please_enter_valid_input, Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = sessionManager.getUser().getId();
        File imageFile = null;
        if (selectedImage != null) {
            imageFile = RayziUtils.createTempFileFromUri(this, selectedImage);
        }
        viewModel.submitComplain(message, contact, userId, imageFile);
    }

    private void launchPhotoPicker() {
        pickImageLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void loadImage(Uri uri) {
        binding.cvImage.setVisibility(View.VISIBLE);
        RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(16));
        Glide.with(this).load(uri).apply(requestOptions).into(binding.image);
    }

    private void initView() {

    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches() && phone.length() >= 6 && phone.length() <= 15;
    }

}
