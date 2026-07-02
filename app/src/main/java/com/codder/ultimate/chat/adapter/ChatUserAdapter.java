package com.codder.ultimate.chat.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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

import com.caverock.androidsvg.SVG;
import com.codder.ultimate.R;
import com.codder.ultimate.chat.modelclass.ChatUserListRoot;
import com.codder.ultimate.databinding.ItemChatUsersBinding;

import java.net.URL;

public class ChatUserAdapter extends ListAdapter<ChatUserListRoot.ChatUserItem, ChatUserAdapter.ChatUserViewHolder> {

    private OnClickListener onClickListener;
    private Context context;

    String[] FAKE_MESSAGES;

    String[] FAKE_TIMES;

    public interface OnClickListener {
        void onClick(int position, ChatUserListRoot.ChatUserItem user);
    }

    public ChatUserAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    private static final DiffUtil.ItemCallback<ChatUserListRoot.ChatUserItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatUserListRoot.ChatUserItem oldItem,
                                               @NonNull ChatUserListRoot.ChatUserItem newItem) {
                    return oldItem.getUserId().equals(newItem.getUserId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatUserListRoot.ChatUserItem oldItem,
                                                  @NonNull ChatUserListRoot.ChatUserItem newItem) {
                    return safeEquals(oldItem.getUserId(), newItem.getUserId()) &&
                            safeEquals(oldItem.getMessage(), newItem.getMessage()) &&
                            safeEquals(oldItem.getName(), newItem.getName()) &&
                            safeEquals(oldItem.getTime(), newItem.getTime()) &&
                            oldItem.getUnreadCount() == newItem.getUnreadCount();
                }

                private boolean safeEquals(String a, String b) {
                    return (a == null && b == null) || (a != null && a.equals(b));
                }


            };

    @NonNull
    @Override
    public ChatUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        FAKE_MESSAGES = context.getResources().getStringArray(R.array.fake_messages);
        FAKE_TIMES = context.getResources().getStringArray(R.array.fake_times);
        ItemChatUsersBinding binding = ItemChatUsersBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ChatUserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatUserViewHolder holder, int position) {
        ChatUserListRoot.ChatUserItem user = getItem(position);
        if (user != null) {
            holder.bind(user, position);
        }
    }

    class ChatUserViewHolder extends RecyclerView.ViewHolder {

        private final ItemChatUsersBinding binding;

        public ChatUserViewHolder(@NonNull ItemChatUsersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(@NonNull ChatUserListRoot.ChatUserItem user, int position) {
            try {
                binding.imgUser.setProfileUserImage(
                        user.getImage() != null ? user.getImage() : "",
                        user.getAvatarFrameImage() != null ? user.getAvatarFrameImage() : "",
                        20
                );

                binding.tvUserName.setText(user.getName() != null ? user.getName() : context.getString(R.string.unknown));

                // static message/time for fake users
                if (user.isFake()) {
                    int idx = Math.abs(position) % FAKE_MESSAGES.length;
                    String fakeMsg = FAKE_MESSAGES[idx];
                    String fakeTime = FAKE_TIMES[idx % FAKE_TIMES.length];
                    binding.tvLastChat.setText(fakeMsg);
                    binding.tvTime.setText(fakeTime);
                } else {
                    binding.tvLastChat.setText(user.getMessage() != null ? user.getMessage().trim() : "");
                    binding.tvTime.setText(user.getTime() != null ? user.getTime() : "");
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


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                int unread = user.getUnreadCount();
                binding.layCount.setVisibility(unread > 0 ? VISIBLE : GONE);
                binding.tvCount.setText(String.valueOf(unread));

                binding.getRoot().setOnClickListener(v -> {
                    if (onClickListener != null) {
                        onClickListener.onClick(getBindingAdapterPosition(), user);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
