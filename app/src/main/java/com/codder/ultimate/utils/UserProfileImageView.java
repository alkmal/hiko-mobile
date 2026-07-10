package com.codder.ultimate.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemUserprofileImageviewBinding;

public class UserProfileImageView extends RelativeLayout {

    ItemUserprofileImageviewBinding binding;

    public UserProfileImageView(Context context) {
        super(context);
        init();
    }

    public UserProfileImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }


    public UserProfileImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public UserProfileImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.item_userprofile_imageview, null, false);
        binding.ivUser.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.profile_round_bg));
        addView(binding.getRoot());
    }

    private void init() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.item_userprofile_imageview, null, false);
        addView(binding.getRoot());
    }

    public void setUserImage(String profileImage, String avatarFrame, int padding) {
        if (binding != null) {
            if (profileImage != null && !profileImage.isEmpty()) {
                Glide.with(this).load(ImageUrlUtil.normalize(profileImage)).placeholder(R.drawable.profile_placeholder).circleCrop().into(binding.ivUser);
            }
            if (avatarFrame != null && !avatarFrame.isEmpty()) {
                binding.ivVip.setVisibility(VISIBLE);
                binding.ivUser.setPadding(padding, padding, padding, padding);
                binding.ivUser.setBackground(ContextCompat.getDrawable(getContext(), R.color.transparent));

                Glide.with(this)
                        .load(ImageUrlUtil.normalize(avatarFrame))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.d("Glide", "❌ Load failed: " + profileImage);
                                binding.ivVip.setVisibility(GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                Log.d("Glide", "✅ Loaded from: " + dataSource.name()); // MEMORY, DISK, or REMOTE
                                return false;
                            }
                        })
                        .into(binding.ivVip);
            } else {
                binding.ivVip.setVisibility(GONE);
//                binding.ivUser.setPadding(1, 1, 1, 1);
                binding.ivUser.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.profile_round_bg));
            }
        }
    }

    public void setProfileUserImage(String profileImage, String avatarFrame, int padding) {
        if (binding != null) {
            if (profileImage != null && !profileImage.isEmpty()) {
                Glide.with(this).load(ImageUrlUtil.normalize(profileImage)).placeholder(R.drawable.profile_placeholder).circleCrop().into(binding.ivUser);
            }
            if (avatarFrame != null && !avatarFrame.isEmpty()) {
                binding.ivVip.setVisibility(VISIBLE);
                binding.ivUser.setPadding(padding, padding, padding, padding);
                binding.ivUser.setBackground(ContextCompat.getDrawable(getContext(), R.color.transparent));

                Glide.with(this)
                        .load(ImageUrlUtil.normalize(avatarFrame))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.d("Glide", "❌ Load failed: " + profileImage);
                                binding.ivVip.setVisibility(GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                Log.d("Glide", "✅ Loaded from: " + dataSource.name()); // MEMORY, DISK, or REMOTE
                                return false;
                            }
                        })
                        .into(binding.ivVip);
            } else {
                binding.ivVip.setVisibility(GONE);
//                binding.ivUser.setPadding(1, 1, 1, 1);
                binding.ivUser.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.profile_white_borderbg));
            }
        }
    }


    public void setHomeUserImage(String profileImage, String avatarFrame, int padding) {
        if (binding != null) {
            if (profileImage != null && !profileImage.isEmpty()) {
                Glide.with(this).load(ImageUrlUtil.normalize(profileImage)).placeholder(R.drawable.profile_placeholder).circleCrop().into(binding.ivUser);
            }
            if (avatarFrame != null && !avatarFrame.isEmpty()) {
                binding.ivVip.setVisibility(VISIBLE);
                binding.ivUser.setPadding(padding, padding, padding, padding);
                binding.ivUser.setBackground(ContextCompat.getDrawable(getContext(), R.color.transparent));

                Glide.with(this)
                        .load(ImageUrlUtil.normalize(avatarFrame))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.d("Glide", "❌ Load failed: " + profileImage);
                                binding.ivVip.setVisibility(GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                Log.d("Glide", "✅ Loaded from: " + dataSource.name()); // MEMORY, DISK, or REMOTE
                                return false;
                            }
                        })
                        .into(binding.ivVip);
            } else {
                binding.ivVip.setVisibility(GONE);
//                binding.ivUser.setPadding(1, 1, 1, 1);
                binding.ivUser.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.home_userprofile_bg));
            }
        }
    }

    public void setChatUserImage(String profileImage, String avatarFrame, int padding) {
        if (binding != null) {
            if (profileImage != null && !profileImage.isEmpty()) {
                Glide.with(this).load(ImageUrlUtil.normalize(profileImage)).placeholder(R.drawable.profile_placeholder).circleCrop().into(binding.ivUser);
            }
            if (avatarFrame != null && !avatarFrame.isEmpty()) {
                binding.ivVip.setVisibility(VISIBLE);
                binding.ivUser.setPadding(padding, padding, padding, padding);
                binding.ivUser.setBackground(ContextCompat.getDrawable(getContext(), R.color.transparent));

                Glide.with(this)
                        .load(ImageUrlUtil.normalize(avatarFrame))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.d("Glide", "❌ Load failed: " + profileImage);
                                binding.ivVip.setVisibility(GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                Log.d("Glide", "✅ Loaded from: " + dataSource.name()); // MEMORY, DISK, or REMOTE
                                return false;
                            }
                        })
                        .into(binding.ivVip);
            } else {
                binding.ivVip.setVisibility(GONE);
//                binding.ivUser.setPadding(1, 1, 1, 1);
                binding.ivUser.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_chatuser_profilebg));
            }
        }
    }

    public void setWithoutbgUserImage(String profileImage, String avatarFrame, int padding) {
        if (binding != null) {
            if (profileImage != null && !profileImage.isEmpty()) {
                Glide.with(this).load(ImageUrlUtil.normalize(profileImage)).placeholder(R.drawable.profile_placeholder).circleCrop().into(binding.ivUser);
            }
            if (avatarFrame != null && !avatarFrame.isEmpty()) {
                binding.ivVip.setVisibility(VISIBLE);
                binding.ivUser.setPadding(padding, padding, padding, padding);
                binding.ivUser.setBackground(ContextCompat.getDrawable(getContext(), R.color.transparent));

                Glide.with(this)
                        .load(ImageUrlUtil.normalize(avatarFrame))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.d("Glide", "❌ Load failed: " + profileImage);
                                binding.ivVip.setVisibility(GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                Log.d("Glide", "✅ Loaded from: " + dataSource.name()); // MEMORY, DISK, or REMOTE
                                return false;
                            }
                        })
                        .into(binding.ivVip);
            } else {
                binding.ivVip.setVisibility(GONE);
//                binding.ivUser.setPadding(1, 1, 1, 1);
                binding.ivUser.setBackground(ContextCompat.getDrawable(getContext(), R.color.transparent));
            }
        }
    }

}
