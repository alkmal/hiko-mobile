package com.codder.ultimate.guestuser.adapter;

import android.app.Activity;
import android.content.Context;
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

import com.caverock.androidsvg.SVG;
import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.ItemSearchUsersBinding;
import com.codder.ultimate.modelclass.GuestProfileRoot;

import java.net.URL;


public class SearchUserAdapter extends ListAdapter<GuestProfileRoot.User, SearchUserAdapter.SearchUserViewHolder> {

    private OnUserClickListener onUserClickListener;

    public SearchUserAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.onUserClickListener = listener;
    }

    @NonNull
    @Override
    public SearchUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSearchUsersBinding binding = ItemSearchUsersBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SearchUserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchUserViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public interface OnUserClickListener {
        void onFollowClick(@NonNull GuestProfileRoot.User user, @NonNull ItemSearchUsersBinding binding, int position);

        void onUserClick(@NonNull GuestProfileRoot.User user, @NonNull ItemSearchUsersBinding binding, int position);

    }

    public class SearchUserViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {

        private final ItemSearchUsersBinding binding;

        public SearchUserViewHolder(@NonNull ItemSearchUsersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(@NonNull GuestProfileRoot.User user, int position) {
            Context context = binding.getRoot().getContext();

            binding.imageUser.setUserImage(user.getImage(), user.getAvatarFrameImage(), 10);
            binding.tvName.setText(user.getName());

            String userName = user.getUsername();
            String displayName;

            if (userName != null && !userName.isEmpty()) {
                // Check if the username is NOT all digits
                if (!userName.matches("\\d+")) {
                    displayName = "@" + userName;
                } else {
                    displayName = context.getString(R.string.id_) + userName;
                }
            } else {
                displayName = user.getBio();
            }

            binding.tvUsername.setText(displayName);

            binding.pd.setVisibility(View.GONE);
            SessionManager sessionManager = new SessionManager(context.getApplicationContext());
            String localUserId = sessionManager.getUser() != null ? sessionManager.getUser().getId() : "";
            boolean isSelf = localUserId != null && !localUserId.isEmpty()
                    && localUserId.equals(user.getUserId());

            if (isSelf) {
                binding.tvFollow.setVisibility(View.GONE);
                binding.tvFollow.setOnClickListener(null);
            } else {
                binding.tvFollow.setVisibility(View.VISIBLE);
            }

            if (user.isFollow()) {
                binding.tvFollow.setText(R.string.following);
                binding.tvFollow.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient_bg_radius_50));
                binding.tvFollow.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.icon_following, 0, 0, 0);
            } else {
                binding.tvFollow.setText(R.string.follow);
                binding.tvFollow.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_bg));
                binding.tvFollow.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.icon_follow, 0, 0, 0);
            }

            binding.tvFollow.setOnClickListener(v -> {
                if (isSelf) return;
                if (onUserClickListener != null) {
                    onUserClickListener.onFollowClick(user, binding, position);
                }
            });

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


            binding.getRoot().setOnClickListener(v -> {
                if (onUserClickListener != null) {
                    onUserClickListener.onUserClick(user, binding, position);
                }
            });
        }
    }

    private static final DiffUtil.ItemCallback<GuestProfileRoot.User> DIFF_CALLBACK = new DiffUtil.ItemCallback<GuestProfileRoot.User>() {
        @Override
        public boolean areItemsTheSame(@NonNull GuestProfileRoot.User oldItem, @NonNull GuestProfileRoot.User newItem) {
            return oldItem.getUserId() != null && oldItem.getUserId().equals(newItem.getUserId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull GuestProfileRoot.User oldItem, @NonNull GuestProfileRoot.User newItem) {
            return oldItem.equals(newItem);
        }
    };
}

