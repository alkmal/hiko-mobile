package com.codder.ultimate.guestuser.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.caverock.androidsvg.SVG;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemFollowersBinding;
import com.codder.ultimate.guestuser.activity.GuestActivity;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.retrofit.Const;

import java.net.URL;

public class FollowersUsersAdapter extends ListAdapter<GuestProfileRoot.User, FollowersUsersAdapter.FollowerUserViewHolder> {

    public FollowersUsersAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<GuestProfileRoot.User> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull GuestProfileRoot.User oldItem, @NonNull GuestProfileRoot.User newItem) {
                    return oldItem.getUserId().equals(newItem.getUserId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull GuestProfileRoot.User oldItem, @NonNull GuestProfileRoot.User newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public FollowerUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemFollowersBinding binding = ItemFollowersBinding.inflate(inflater, parent, false);
        return new FollowerUserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowerUserViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class FollowerUserViewHolder extends RecyclerView.ViewHolder {

        private final ItemFollowersBinding binding;

        public FollowerUserViewHolder(@NonNull ItemFollowersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(GuestProfileRoot.User user) {
            Context context = itemView.getContext();

            binding.imageUser.setUserImage(user.getImage(), user.getAvatarFrameImage(), 5);
            binding.tvName.setText(user.getName());

            String userName = user.getUsername();
            if (userName != null && !userName.isEmpty()) {
                binding.tvUsername.setText("ID: " +userName);
            } else {
                binding.tvUsername.setText(user.getBio());
            }


            String flagUrl = user.getCountryFlagImage();
            if (flagUrl != null && !flagUrl.isEmpty() && context instanceof Activity) {
                AsyncTask.execute(() -> {
                    try {
                        URL url = new URL(flagUrl);
                        SVG svg = SVG.getFromInputStream(url.openStream());

                        Picture picture;
                        float width = svg.getDocumentWidth();
                        float height = svg.getDocumentHeight();

                        if (width > 0 && height > 0) {
                            picture = svg.renderToPicture();
                        } else {
                            picture = svg.renderToPicture(100, 60); // fallback dimensions
                        }

                        PictureDrawable drawable = new PictureDrawable(picture);

                        ((Activity) context).runOnUiThread(() -> {
                            binding.svgWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                            binding.svgWebView.setImageDrawable(drawable);
                            binding.svgWebView.invalidate();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }


            if (user.isFollow()) {
                binding.tvFollow.setText(R.string.following);
                binding.tvFollow.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient_bg_radius_50));
                binding.tvFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_following, 0, 0, 0);
            } else {
                if (user.getFollowStatus() != null) {
                    binding.tvFollow.setText(R.string.follow);
                } else {
                    binding.tvFollow.setText(R.string.follow_back);
                }
                binding.tvFollow.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_bg));
                binding.tvFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_follow, 0, 0, 0);
            }

            binding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(context, GuestActivity.class);
                intent.putExtra(Const.USERID, user.getUserId());
                context.startActivity(intent);
            });
        }
    }
}
