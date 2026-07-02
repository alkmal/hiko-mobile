package com.codder.ultimate.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.caverock.androidsvg.SVG;
import com.codder.ultimate.databinding.ItemBannedListBinding;
import com.codder.ultimate.modelclass.BlockedUserListRoot;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BlockedUserListAdapter extends ListAdapter<BlockedUserListRoot.BlockedUsersItem, BlockedUserListAdapter.MyViewHolder> {

    private final Context context;
    private final onUnblockListener onUnblockListener;

    public BlockedUserListAdapter(Context context, onUnblockListener onUnblockListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.onUnblockListener = onUnblockListener;
    }

    private static final DiffUtil.ItemCallback<BlockedUserListRoot.BlockedUsersItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull BlockedUserListRoot.BlockedUsersItem oldItem,
                                               @NonNull BlockedUserListRoot.BlockedUsersItem newItem) {
                    return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull BlockedUserListRoot.BlockedUsersItem oldItem,
                                                  @NonNull BlockedUserListRoot.BlockedUsersItem newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBannedListBinding binding = ItemBannedListBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        BlockedUserListRoot.BlockedUsersItem item = getItem(position);

        if (item.getToUserId() != null) {
            holder.binding.ivUserProfile.setUserImage(item.getToUserId().getImage(), "", 5);
            holder.binding.tvName.setText(item.getToUserId().getName());

            String userName = item.getToUserId().getUsername();
            String displayName = "";

            if (userName != null && !userName.isEmpty()) {
                if (!userName.matches("\\d+")) {
                    displayName = "@" + userName;
                } else {
                    displayName = userName;
                }
            }
            holder.binding.tvUsername.setText("ID: " + displayName);

            String flagUrl = item.getToUserId().getCountryFlagImage();
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
                            holder.binding.svgWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                            holder.binding.svgWebView.setImageDrawable(drawable);
                            holder.binding.svgWebView.invalidate();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }


            holder.binding.tvUnblock.setOnClickListener(v -> {
                onUnblockListener.onUnblock(item.getToUserId().getId(), position);
            });
        }
    }

    public void removeItem(int position) {
        List<BlockedUserListRoot.BlockedUsersItem> currentList = new ArrayList<>(getCurrentList());
        if (position >= 0 && position < currentList.size()) {
            currentList.remove(position);
            submitList(null);
            submitList(new ArrayList<>(currentList));
        }
    }

    public interface onUnblockListener {
        void onUnblock(String id, int position);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        final ItemBannedListBinding binding;

        public MyViewHolder(@NonNull ItemBannedListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}