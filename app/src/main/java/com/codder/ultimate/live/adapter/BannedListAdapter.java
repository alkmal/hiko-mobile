package com.codder.ultimate.live.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemBannedListBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BannedListAdapter extends RecyclerView.Adapter<BannedListAdapter.Myviewholder> {

    private static final String TAG = "BannedListAdapter";

    private JSONArray blockedList = new JSONArray();
    private Context context;
    private final onUnblockListener onUnblockListener;

    public BannedListAdapter(@NonNull Context context, @NonNull onUnblockListener onUnblockListener) {
        this.context = context;
        this.onUnblockListener = onUnblockListener;
    }

    @NonNull
    @Override
    public Myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemBannedListBinding binding = ItemBannedListBinding.inflate(inflater, parent, false);
        return new Myviewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Myviewholder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return blockedList != null ? blockedList.length() : 0;
    }

    /**
     * Replaces adapter data with new list.
     */
    public void addData(JSONArray newBlockedlist) {
        blockedList = newBlockedlist;
        notifyDataSetChanged();
    }

    public class Myviewholder extends RecyclerView.ViewHolder {
        private final ItemBannedListBinding binding;

        public Myviewholder(@NonNull ItemBannedListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(int position) {
            if (blockedList == null || position < 0 || position >= blockedList.length()) {
                Log.w(TAG, "setData: Invalid position or data is null");
                binding.tvName.setText(R.string.unknown);
                binding.tvUsername.setText("");
                binding.tvUnblock.setOnClickListener(null);
                return;
            }

            try {
                JSONObject blockedUserList = blockedList.getJSONObject(position);
                JSONObject userJson = blockedUserList.optJSONObject("blockedUserId");

                String name = userJson != null ? userJson.optString("name", "Unknown") : "Unknown";
                String username = userJson != null ? userJson.optString("username", "") : "";
                String image = userJson != null ? userJson.optString("image", "") : "";

                binding.tvName.setText(name);

                String displayName = "";
                if (username != null && !username.isEmpty()) {
                    if (!username.matches("\\d+")) {
                        displayName = "@" + username;
                    } else {
                        displayName = context.getString(R.string.id_) + username;
                    }
                }
                binding.tvUsername.setText(displayName);

                if (image != null && !image.isEmpty()) {
                    binding.ivUserProfile.setUserImage(image, "", 5);
                }

                binding.tvUnblock.setOnClickListener(v -> {
                    if (onUnblockListener != null && userJson != null) {
                        String id = userJson.optString("_id", "");
                        if (!id.isEmpty()) {
                            onUnblockListener.onUnblock(id, getAdapterPosition());
                        } else {
                            Log.w(TAG, "Unblock id is empty.");
                        }
                    } else {
                        Log.w(TAG, "Unblock listener or userJson is null.");
                    }
                });

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON at position: " + position, e);
                binding.tvName.setText(R.string.unknown);
                binding.tvUsername.setText("");
                binding.tvUnblock.setOnClickListener(null);
            }
        }
    }

    public interface onUnblockListener {
        void onUnblock(String id, int position);
    }
}

