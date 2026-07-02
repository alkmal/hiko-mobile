package com.codder.ultimate.adapter;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.RecyclerView;

import com.caverock.androidsvg.SVG;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemSearchHistoryBinding;
import com.codder.ultimate.databinding.ItemSearchUsersBinding;
import com.codder.ultimate.databinding.ItemSearchUsersHistoryBinding;
import com.codder.ultimate.guestuser.adapter.SearchUserAdapter;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.SearchHistoryRoot;

import java.net.URL;

public class SearchHistoryAdapter extends ListAdapter<SearchHistoryRoot.DataItem, SearchHistoryAdapter.SearchUserViewHolder> {
    private OnUserClickListener onUserClickListener;

    public SearchHistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.onUserClickListener = listener;
    }

    @NonNull
    @Override
    public SearchHistoryAdapter.SearchUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSearchHistoryBinding binding = ItemSearchHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SearchHistoryAdapter.SearchUserViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(@NonNull SearchHistoryAdapter.SearchUserViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public interface OnUserClickListener {
        void onDeleteClick(@NonNull SearchHistoryRoot.DataItem user, @NonNull ItemSearchHistoryBinding binding, int position);

        void onUserClick(@NonNull SearchHistoryRoot.DataItem user, @NonNull ItemSearchHistoryBinding binding, int position);

    }

    public class SearchUserViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {

        private final ItemSearchHistoryBinding binding;

        public SearchUserViewHolder(@NonNull ItemSearchHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(@NonNull SearchHistoryRoot.DataItem user, int position) {
            Context context = binding.getRoot().getContext();

            binding.imageUser.setUserImage(user.getSearchedUser().getImage(), "", 10);
            binding.tvName.setText(user.getSearchedUser().getName());

            String userName = user.getSearchedUser().getUsername();
            String displayName = "";

            if (userName != null && !userName.isEmpty()) {
                // Check if the username is NOT all digits
                if (!userName.matches("\\d+")) {
                    displayName = "@" + userName;
                } else {
                    displayName = context.getString(R.string.id_) + userName;
                }
            }

            binding.tvUsername.setText(displayName);

            binding.pd.setVisibility(View.GONE);
            binding.ivDelete.setOnClickListener(view -> {
                onUserClickListener.onDeleteClick(user,binding,position);
            });

            String flagUrl = user.getSearchedUser().getCountryFlagImage();
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

    private static final DiffUtil.ItemCallback<SearchHistoryRoot.DataItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<SearchHistoryRoot.DataItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull SearchHistoryRoot.DataItem oldItem, @NonNull SearchHistoryRoot.DataItem newItem) {
            return oldItem.getSearchedUser().getId() != null && oldItem.getSearchedUser().getId().equals(newItem.getSearchedUser().getId());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull SearchHistoryRoot.DataItem oldItem, @NonNull SearchHistoryRoot.DataItem newItem) {
            return oldItem.equals(newItem);
        }
    };
}
