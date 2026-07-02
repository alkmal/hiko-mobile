package com.codder.ultimate.live.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.databinding.ItemViewBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class LiveViewUserAdapter extends ListAdapter<JSONObject, LiveViewUserAdapter.ChatUserViewHolder> {

    private static final String TAG = "LiveViewUserAdapter";
    private Context context;
    private OnLiveUserAdapterClickListener clickListener;

    public LiveViewUserAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnLiveUserAdapterClickListener(OnLiveUserAdapterClickListener listener) {
        this.clickListener = listener;
    }

    public interface OnLiveUserAdapterClickListener {
        void onUserClick(JSONObject user);
    }

    @NonNull
    @Override
    public ChatUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemViewBinding binding = ItemViewBinding.inflate(inflater, parent, false);
        return new ChatUserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatUserViewHolder holder, int position) {
        JSONObject user = getItem(position);
        holder.bind(user);
    }

    static final DiffUtil.ItemCallback<JSONObject> DIFF_CALLBACK = new DiffUtil.ItemCallback<JSONObject>() {
        @Override
        public boolean areItemsTheSame(@NonNull JSONObject oldItem, @NonNull JSONObject newItem) {
            try {
                return oldItem.getString("userId").equals(newItem.getString("userId"));
            } catch (JSONException e) {
                Log.e(TAG, "areItemsTheSame: " + e.getMessage());
                return false;
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull JSONObject oldItem, @NonNull JSONObject newItem) {
            return oldItem.toString().equals(newItem.toString());
        }
    };

    class ChatUserViewHolder extends RecyclerView.ViewHolder {
        private final ItemViewBinding binding;

        public ChatUserViewHolder(@NonNull ItemViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JSONObject user) {
            if (user == null) return;

            try {
                String name = user.optString("name", "Unknown");
                String image = user.optString("image", null);
                String avatarFrame = user.optString("avatarFrameImage", null);
                boolean isAdd = user.optBoolean("isAdd", false);

                Log.d(TAG, "bind: " + name);

                if (isAdd && image != null) {
                    binding.imgview.setProfileUserImage(image, avatarFrame, 10);
                }

                binding.getRoot().setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onUserClick(user);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "bind: Exception while binding user - " + e.getMessage());
            }
        }
    }
}
