package com.codder.ultimate.live.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.databinding.ItemAudioUsersBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class AudioUsersAdapter extends ListAdapter<JSONObject, AudioUsersAdapter.ChatUserViewHolder> {

    private static final String TAG = "LiveViewUserAdapter";

    private Context context;
    private OnLiveUserAdapterClickListener onLiveUserAdapterClickListener;

    public AudioUsersAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ChatUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemAudioUsersBinding binding = ItemAudioUsersBinding.inflate(inflater, parent, false);
        return new ChatUserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatUserViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public void setOnLiveUserAdapterClickListener(OnLiveUserAdapterClickListener listener) {
        this.onLiveUserAdapterClickListener = listener;
    }

    public interface OnLiveUserAdapterClickListener {
        void onUserClick(JSONObject userDummy);
    }

    public class ChatUserViewHolder extends RecyclerView.ViewHolder {
        private final ItemAudioUsersBinding binding;

        public ChatUserViewHolder(@NonNull ItemAudioUsersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JSONObject userDummy) {
            try {
                Log.d(TAG, "bind: " + userDummy.toString());

                String avatarFrame = userDummy.optString("avatarFrameImage", "");
                binding.imgUser1.setUserImage(userDummy.getString("image"), avatarFrame, 13);
                binding.userName.setText(userDummy.getString("name"));
                binding.gender.setText(userDummy.getString("gender"));
                binding.location.setText(userDummy.getString("country"));

                binding.getRoot().setOnClickListener(v -> {
                    if (onLiveUserAdapterClickListener != null) {
                        onLiveUserAdapterClickListener.onUserClick(userDummy);
                    }
                });
            } catch (JSONException e) {
                Log.e(TAG, "bind: JSON error", e);
            }
        }
    }

    public static final DiffUtil.ItemCallback<JSONObject> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull JSONObject oldItem, @NonNull JSONObject newItem) {
            return oldItem.optString("userId").equals(newItem.optString("userId"));
        }

        @Override
        public boolean areContentsTheSame(@NonNull JSONObject oldItem, @NonNull JSONObject newItem) {
            return oldItem.toString().equals(newItem.toString());
        }
    };
}