package com.codder.ultimate.chat.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ActivityImagePreviewBinding;

public class ImagePreviewActivity extends AppCompatActivity {

    ActivityImagePreviewBinding binding;
    private static final String PREVIEW_IMAGE = "preview_image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSharedElementEnterTransition(new android.transition.TransitionSet()
                .addTransition(new android.transition.ChangeBounds())
                .addTransition(new android.transition.ChangeTransform())
                .addTransition(new android.transition.ChangeImageTransform()));

        getWindow().setSharedElementReturnTransition(new android.transition.TransitionSet()
                .addTransition(new android.transition.ChangeBounds())
                .addTransition(new android.transition.ChangeTransform())
                .addTransition(new android.transition.ChangeImageTransform()));

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_preview);

        String imageUrl = getIntent().getStringExtra(PREVIEW_IMAGE);
        String tn = getIntent().getStringExtra("transition_name");
        if (tn != null) ViewCompat.setTransitionName(binding.fullScreenImage, tn);

        supportPostponeEnterTransition();
        if (imageUrl == null || imageUrl.isEmpty()) {
            supportStartPostponedEnterTransition();
            finish();
            return;
        }

        Glide.with(this)
                .load(imageUrl)
                .dontTransform()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(binding.fullScreenImage);

        binding.imageContainer.setOnClickListener(v -> onBackPressed());
    }
}
