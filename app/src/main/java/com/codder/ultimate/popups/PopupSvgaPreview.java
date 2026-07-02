package com.codder.ultimate.popups;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.PopupSvgaPreviewBinding;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.net.MalformedURLException;
import java.net.URL;

public class PopupSvgaPreview {
    private static final String TAG = "PopupSvgaPreview";
    private SessionManager sessionManager;
    private Dialog dialog;
    private long animationDurationMillis;

    public PopupSvgaPreview(Context context, String svgaImage, String avatarFrame, String userImage) {
        if (context == null) {
            Log.e(TAG, "Context is null.");
            return;
        }

        sessionManager = new SessionManager(context);
        dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            Log.e(TAG, "Failed to get LayoutInflater.");
            return;
        }

        PopupSvgaPreviewBinding binding = DataBindingUtil.inflate(inflater, R.layout.popup_svga_preview, null, false);
        if (binding == null) {
            Log.e(TAG, "Failed to bind layout.");
            return;
        }

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(binding.getRoot());

        SVGAImageView imageView = binding.svgImage;
        if (imageView == null) {
            Log.e(TAG, "SVG ImageView is null.");
            return;
        }

        loadSvgaAnimation(context, svgaImage, imageView, binding);

        binding.userName.setText(sessionManager.getUser() != null ? sessionManager.getUser().getName() : context.getString(R.string.unknown_user));
        loadImages(context, userImage, avatarFrame, binding);

        setupAnimations(context, binding);

        dialog.setOnDismissListener(dialogInterface -> {
            if (imageView != null) {
                imageView.stopAnimation();
                imageView.clearAnimation();
            }
        });

        dialog.show();
    }

    private void loadSvgaAnimation(Context context, String svgaImage, SVGAImageView imageView, PopupSvgaPreviewBinding binding) {
        try {
            SVGAParser parser = new SVGAParser(context);
            URL url = new URL(BuildConfig.BASE_URL + svgaImage);

            parser.decodeFromURL(url, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                    if (svgaVideoEntity == null) {
                        Log.e(TAG, "SVGA video entity is null.");
                        return;
                    }

                    SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
                    dynamicEntity.setDynamicImage(BuildConfig.BASE_URL + svgaImage, "99");

                    SVGADrawable drawable = new SVGADrawable(svgaVideoEntity, dynamicEntity);
                    imageView.setImageDrawable(drawable);
                    imageView.startAnimation();

                    animationDurationMillis = (svgaVideoEntity.getFrames() / svgaVideoEntity.getFPS()) * 1000L;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (imageView != null) {
                            imageView.setVisibility(View.GONE);
                            imageView.clear();
                        }
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }, animationDurationMillis);
                }

                @Override
                public void onError() {
                    Log.e(TAG, "Error loading SVGA animation.");
                }
            }, null);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Malformed URL for SVGA image.", e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error occurred while loading SVGA animation.", e);
        }
    }

    private void loadImages(Context context, String userImage, String avatarFrame, PopupSvgaPreviewBinding binding) {
        try {
            if (context != null) {
                Glide.with(context).load(userImage)
                        .circleCrop()
                        .into(binding.userImage);
                Glide.with(context).load(BuildConfig.BASE_URL + avatarFrame)
                        .into(binding.avatarFrameImage);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading images into the views.", e);
        }
    }

    private void setupAnimations(Context context, PopupSvgaPreviewBinding binding) {
        if (context != null && binding != null) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
            if (animation != null) {
                animation.setFillAfter(true);
                binding.nameLyt.startAnimation(animation);
            }
            binding.btnClose.setOnClickListener(v -> {
                if (dialog != null) {
                    dialog.dismiss();
                }
            });
        } else {
            Log.e(TAG, "Context or binding is null during animation setup.");
        }
    }
}
