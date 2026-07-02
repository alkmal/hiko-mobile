package com.codder.ultimate.live.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.databinding.ItemViewBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LiveViewAdapter extends RecyclerView.Adapter<LiveViewAdapter.ChatUserViewHolder> {

    private static final String TAG = "LiveViewAdapter";

    private Context context;
    private JSONArray users = new JSONArray();

    private OnLiveUserAdapterClickListener onLiveUserAdapterClickListener;

    @NonNull
    @Override
    public ChatUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ChatUserViewHolder(ItemViewBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatUserViewHolder holder, int position) {
        holder.bindData(position);
    }

    @Override
    public int getItemCount() {
        return users.length();
    }

    public void setData(@NonNull JSONArray newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    public void addData(@NonNull JSONArray newUsers) {
        if (newUsers.length() == 0) return;

        try {
            JSONArray combinedUsers = new JSONArray();

            for (int i = 0; i < users.length(); i++) {
                JSONObject existingUser = users.optJSONObject(i);
                if (existingUser != null) {
                    combinedUsers.put(existingUser);
                }
            }

            for (int i = 0; i < newUsers.length(); i++) {
                JSONObject newUser = newUsers.optJSONObject(i);
                if (newUser == null) continue;

                String newUserId = newUser.optString("userId", null);
                if (newUserId == null) continue;

                boolean exists = false;
                for (int j = 0; j < combinedUsers.length(); j++) {
                    JSONObject existingUser = combinedUsers.optJSONObject(j);
                    if (existingUser != null && newUserId.equals(existingUser.optString("userId"))) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    combinedUsers.put(newUser);
                }
            }

            this.users = combinedUsers;
            notifyDataSetChanged();

        } catch (Exception e) {
            Log.e(TAG, "addData: error merging user lists", e);
        }
    }

    public void setOnLiveUserAdapterClickListener(OnLiveUserAdapterClickListener listener) {
        this.onLiveUserAdapterClickListener = listener;
    }

    public interface OnLiveUserAdapterClickListener {
        void onUserClick(@NonNull JSONObject user);
    }

    public class ChatUserViewHolder extends RecyclerView.ViewHolder {

        private final ItemViewBinding binding;

        public ChatUserViewHolder(@NonNull ItemViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindData(int position) {
            JSONObject userObject = null;
            try {
                userObject = users.getJSONObject(position);
            } catch (JSONException e) {
                Log.e(TAG, "bindData: invalid JSON at position " + position, e);
                return;
            }

            if (userObject == null) return;

            try {
                boolean isAdd = userObject.optBoolean("isAdd", false);
                String image = userObject.optString("image", "");
                String avatarFrameImage = userObject.optString("avatarFrameImage", "");

                if (isAdd) {
                    binding.imgview.setUserImage(image, avatarFrameImage, 10);
                } else {
                    Log.d(TAG, "bindData: clear Image");
                }

                JSONObject finalUserObject = userObject;
                binding.imgview.setOnClickListener(v -> {
                    if (onLiveUserAdapterClickListener != null) {
                        onLiveUserAdapterClickListener.onUserClick(finalUserObject);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "bindData: error setting user image or click listener", e);
            }
        }
    }
}

