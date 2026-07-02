package com.codder.ultimate.chat.adapter;

import android.content.Context;
import android.media.SoundPool;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.chat.modelclass.ChatRootFake;
import com.codder.ultimate.databinding.FakeItemChatBinding;
import com.codder.ultimate.databinding.FakeItemChatGiftBinding;
import com.codder.ultimate.databinding.FakeItemChatImageBinding;

import java.util.ArrayList;
import java.util.List;

public class FakeChatAdapter extends ListAdapter<ChatRootFake, RecyclerView.ViewHolder> {

    private OnClickListener onClickListener;
    private static final int VIEW_TYPE_TEXT = 0;
    private static final int VIEW_TYPE_IMAGE = 1; // NEW
    private static final int VIEW_TYPE_GIFT = 4;

    private SoundPool soundPool;
    private int sendSoundId;
    private boolean soundLoaded = false;
    private Context context;

    public interface OnClickListener {
        void onImageClick(int position, String imageUrl, ImageView mainImage);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    public FakeChatAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        soundPool = new SoundPool.Builder().setMaxStreams(2).build();
        sendSoundId = soundPool.load(context, R.raw.send_message, 1);

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) {
                soundLoaded = true;
            }
        });

    }

    private static final DiffUtil.ItemCallback<ChatRootFake> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatRootFake o, @NonNull ChatRootFake n) {
                    return o.equals(n);
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatRootFake o, @NonNull ChatRootFake n) {
                    return o.equals(n);
                }
            };

    @Override
    public int getItemViewType(int position) {
        ChatRootFake item = getItem(position);
        if (item == null) return VIEW_TYPE_TEXT;
        String msg = item.getMessage();
        if (msg != null) {
            if (msg.startsWith("gift:") || msg.startsWith("gift_abs:")) return VIEW_TYPE_GIFT;
            if (msg.startsWith("image:") || msg.startsWith("image_abs:"))
                return VIEW_TYPE_IMAGE; // NEW
        }
        return VIEW_TYPE_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_GIFT) {
            return new GiftViewHolder(FakeItemChatGiftBinding.inflate(inf, parent, false));
        } else if (viewType == VIEW_TYPE_IMAGE) { // NEW
            return new ImageViewHolder(FakeItemChatImageBinding.inflate(inf, parent, false));
        } else {
            return new ChatTextViewHolder(FakeItemChatBinding.inflate(inf, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatRootFake chat = getItem(position);
        if (chat == null) return;
        int vt = getItemViewType(position);
        if (vt == VIEW_TYPE_GIFT) {
            ((GiftViewHolder) holder).bindGift(chat);
        } else if (vt == VIEW_TYPE_IMAGE) {
            ((ImageViewHolder) holder).bindImage(chat);
        } else {
            ((ChatTextViewHolder) holder).bind(chat);
        }
    }

    private void playSendSound() {
        if (soundLoaded) {
            soundPool.play(sendSoundId, 1f, 1f, 1, 0, 1f);
        }
    }


    public void addSingleMessage(ChatRootFake msg, @Nullable Runnable commitCallback) {
        List<ChatRootFake> newList = new ArrayList<>(getCurrentList());
        newList.add(msg);
        playSendSound();
        submitList(newList, commitCallback);
    }

    /** -------- TEXT -------- */
    class ChatTextViewHolder extends RecyclerView.ViewHolder {
        private final FakeItemChatBinding b;

        ChatTextViewHolder(@NonNull FakeItemChatBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(@NonNull ChatRootFake chat) {
            String message = chat.getMessage() != null ? chat.getMessage() : "";
            String avatar = chat.getImage() != null ? chat.getImage() : "";
            String avatarFrame = chat.getAvtarImage() != null ? chat.getAvtarImage() : "";

            b.tvText.setText(message);
            b.imgUser1.setChatUserImage(avatar, "", 10);
            b.imgUser2.setChatUserImage(avatar, avatarFrame, 10);

            boolean isSender = chat.getFlag() == 1;
            b.imgUser1.setVisibility(isSender ? View.INVISIBLE : View.VISIBLE);
            b.imgUser2.setVisibility(isSender ? View.VISIBLE : View.INVISIBLE);
            b.space1.setVisibility(isSender ? View.VISIBLE : View.GONE);
            b.space2.setVisibility(isSender ? View.GONE : View.VISIBLE);

            b.tvText.setBackgroundResource(isSender ? R.drawable.bg_chat_right : R.drawable.bg_chat_left);
            b.tvText.setBackgroundTintList(ContextCompat.getColorStateList(b.getRoot().getContext(),
                    isSender ? R.color.tintColor : R.color.pink));
        }
    }

    /** -------- IMAGE (NEW) -------- */
    class ImageViewHolder extends RecyclerView.ViewHolder {
        private final FakeItemChatImageBinding b;

        ImageViewHolder(@NonNull FakeItemChatImageBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bindImage(@NonNull ChatRootFake chat) {
            String avatar = chat.getImage() != null ? chat.getImage() : "";
            String avatarFrame = chat.getAvtarImage() != null ? chat.getAvtarImage() : "";
            boolean isSender = chat.getFlag() == 1;

            b.imgUser1.setChatUserImage(avatar, "", 10);
            b.imgUser2.setChatUserImage(avatar, avatarFrame, 10);
            b.imgUser1.setVisibility(isSender ? View.INVISIBLE : View.VISIBLE);
            b.imgUser2.setVisibility(isSender ? View.VISIBLE : View.INVISIBLE);
            b.space1.setVisibility(isSender ? View.VISIBLE : View.GONE);
            b.space2.setVisibility(isSender ? View.GONE : View.VISIBLE);

            String msg = chat.getMessage() != null ? chat.getMessage() : "";
            String loadUrl = null;
            if (msg.startsWith("image_abs:")) loadUrl = msg.substring("image_abs:".length());
            else if (msg.startsWith("image:"))
                loadUrl = BuildConfig.BASE_URL + msg.substring("image:".length());

            Glide.with(b.getRoot())
                    .load(loadUrl)
                    .placeholder(R.drawable.gift_placeholder)
                    .error(R.drawable.gift_placeholder)
                    .into(b.mainImage);

            String tn = "chat_image_" + (loadUrl != null ? loadUrl.hashCode() : getBindingAdapterPosition());
            ViewCompat.setTransitionName(b.mainImage, tn);

            String finalUrl = loadUrl;
            b.mainImage.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (onClickListener != null && pos != RecyclerView.NO_POSITION) {
                    onClickListener.onImageClick(pos, finalUrl, b.mainImage);
                }
            });
        }
    }

    /** -------- GIFT -------- */
    class GiftViewHolder extends RecyclerView.ViewHolder {
        private final FakeItemChatGiftBinding g;

        GiftViewHolder(@NonNull FakeItemChatGiftBinding b) {
            super(b.getRoot());
            this.g = b;
        }

        void bindGift(@NonNull ChatRootFake chat) {
            String avatarUrl = chat.getImage() != null ? chat.getImage() : "";
            String avatarFrame = chat.getAvtarImage() != null ? chat.getAvtarImage() : "";

            g.imgUser1.setChatUserImage(avatarUrl, "", 10);
            g.imgUser2.setChatUserImage(avatarUrl, avatarFrame, 10);

            boolean isSender = chat.getFlag() == 1;
            g.imgUser1.setVisibility(isSender ? View.INVISIBLE : View.VISIBLE);
            g.imgUser2.setVisibility(isSender ? View.VISIBLE : View.INVISIBLE);
            g.space1.setVisibility(isSender ? View.VISIBLE : View.GONE);
            g.space2.setVisibility(isSender ? View.GONE : View.VISIBLE);

            // Note text (optional)
            g.giftNote.setText("\uD83C\uDF81 " + itemView.getContext().getString(R.string.you) + itemView.getContext().getString(R.string.sent_a_gift));

            // Payload is always a static image URL
            String msg = chat.getMessage() != null ? chat.getMessage() : "";
            String loadUrl = null;
            if (msg.startsWith("gift_abs:")) {
                loadUrl = msg.substring("gift_abs:".length());
            } else if (msg.startsWith("gift:")) {
                loadUrl = BuildConfig.BASE_URL + msg.substring("gift:".length());
            }

            Log.d("FakeChatAdapter", "bindGift: " + loadUrl);

            Glide.with(g.getRoot())
                    .load(loadUrl)
                    .placeholder(R.drawable.gift_placeholder)
                    .error(R.drawable.gift_placeholder)
                    .centerCrop()
                    .into(g.giftImage);
        }
    }

}
