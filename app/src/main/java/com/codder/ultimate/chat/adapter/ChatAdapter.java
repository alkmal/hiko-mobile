package com.codder.ultimate.chat.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.chat.modelclass.ChatItem;
import com.codder.ultimate.databinding.ItemChatCallBinding;
import com.codder.ultimate.databinding.ItemChatGiftBinding;
import com.codder.ultimate.databinding.ItemChatImageBinding;
import com.codder.ultimate.databinding.ItemChatStikerBinding;
import com.codder.ultimate.databinding.ItemChatTextBinding;
import com.codder.ultimate.socket.MySocketManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int PHOTO_TYPE = 1;
    private static final int EMOJI_TYPE = 2;
    private static final int TEXT_TYPE = 3;
    private static final int GIFT_TYPE = 4;
    private static final int CALL_TYPE = 5;
    private static final String TAG = "ChatAdapter";
    private Context context;
    String localUserId = "";
    private List<ChatItem> chatDummyList = new ArrayList<>();
    private String guestUserName;
    private String guestUserImage;
    private String localUserName;
    private String localUserImage;
    private String guestUserAvatarImage;
    private String localUserAvatarImage;

    OnChatItemClickLister onChatItemClickLister;
    private SessionManager sessionManager;

    private SoundPool soundPool;
    private int sendSoundId;
    private boolean soundLoaded = false;
    private MediaPlayer audioPlayer;
    private String playingAudioUrl = "";
    private String playingMessageId = "";
    private int playingPosition = RecyclerView.NO_POSITION;
    private boolean audioPreparing = false;
    private final Handler audioHandler = new Handler(Looper.getMainLooper());
    private Runnable audioProgressRunnable;
    private final Map<String, Integer> audioDurationCache = new HashMap<>();
    private final Set<String> loadingAudioDurations = new HashSet<>();

    public ChatAdapter(Context context) {
        this.context = context;
        soundPool = new SoundPool.Builder().setMaxStreams(2).build();
        sendSoundId = soundPool.load(context, R.raw.send_message, 1);

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) {
                soundLoaded = true;
            }
        });

    }

    public void setOnChatItemClickLister(OnChatItemClickLister onChatItemClickLister) {
        this.onChatItemClickLister = onChatItemClickLister;
    }

    @Override
    public int getItemViewType(int position) {
//        Log.d("TAG", "getItemViewType: " + chatDummyList.get(position).getMessageType());

        ChatItem item = chatDummyList.get(position);
        String type = item.getMessageType();
        if (type == null) return TEXT_TYPE;

        switch (type) {
            case "message":
                return TEXT_TYPE;
            case "image":
                return PHOTO_TYPE;
            case "audio":
                return TEXT_TYPE;
            case "sticker":
            case "emoji":
                return EMOJI_TYPE;
            case "chatGift":     // NEW
                return GIFT_TYPE;
            case "Audio call":
                return CALL_TYPE;
            case "Video call":
                return CALL_TYPE;
            default:
                return TEXT_TYPE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (sessionManager == null) {
            sessionManager = new SessionManager(context.getApplicationContext());
        }
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TEXT_TYPE) {
            return new ChatTextViewHolder(inflater.inflate(R.layout.item_chat_text, parent, false));
        } else if (viewType == PHOTO_TYPE) {
            return new ChatImageViewHolder(inflater.inflate(R.layout.item_chat_image, parent, false));
        } else if (viewType == EMOJI_TYPE) {
            return new ChatStikerViewHolder(inflater.inflate(R.layout.item_chat_stiker, parent, false));
        } else if (viewType == GIFT_TYPE) { // NEW
            return new ChatGiftViewHolder(inflater.inflate(R.layout.item_chat_gift, parent, false));
        } else if (viewType == CALL_TYPE) { // NEW
            return new ChatCallViewHolder(inflater.inflate(R.layout.item_chat_call, parent, false));
        } else if (viewType == CALL_TYPE) { // NEW
            return new ChatCallViewHolder(inflater.inflate(R.layout.item_chat_call, parent, false));
        } else {
            // Fallback
            return new ChatTextViewHolder(inflater.inflate(R.layout.item_chat_text, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ChatItem chatItem = chatDummyList.get(position);
        int vt = getItemViewType(position);
        if (vt == TEXT_TYPE) {
            ((ChatTextViewHolder) holder).setData(position);
        } else if (vt == PHOTO_TYPE) {
            ((ChatImageViewHolder) holder).setData(position);
        } else if (vt == EMOJI_TYPE) {
            ((ChatStikerViewHolder) holder).setData(position);
        } else if (vt == GIFT_TYPE) {  // NEW
            ((ChatGiftViewHolder) holder).setData(position);
        }else if (vt == CALL_TYPE) {  // NEW
            ((ChatCallViewHolder) holder).setData(position);
        }

        emitMessageReadStatus(chatItem.getId());
    }

    @Override
    public int getItemCount() {
        return chatDummyList.size();
    }

    public void addData(List<ChatItem> chatDummyList) {
        int start = this.chatDummyList.size();
        this.chatDummyList.addAll(chatDummyList);
        notifyItemRangeInserted(start, chatDummyList.size());
    }

    private void playSendSound() {
        if (soundLoaded) {
            soundPool.play(sendSoundId, 1f, 1f, 1, 0, 1f);
        }
    }

    public void releaseSound() {
        if (soundPool != null) soundPool.release();
        releaseAudioPlayer();
    }

    public void initGuestUser(String guestUserName, String guestUserDummy, String guestAvatarImage) {
        Log.d(TAG, "initGuestUserImage: ChatActivity ==> " + guestUserDummy);
        this.guestUserName = guestUserName;
        this.guestUserImage = guestUserDummy;
        this.guestUserAvatarImage = guestAvatarImage;
    }

    public void initLocalUser(String localUserName, String userDummy, String userAvatarImage) {
        Log.d(TAG, "initLocalUser: ChatActivity ==>" + localUserName);
        this.localUserName = localUserName;
        this.localUserAvatarImage = userAvatarImage;
        this.localUserImage = userDummy;
    }

    public void initLocalUserId(String localUserId) {
        this.localUserId = localUserId;
    }

    public void addSingleChat(ChatItem chatUserItem) {
        chatDummyList.add(0, chatUserItem);
        if (playingPosition != RecyclerView.NO_POSITION) playingPosition++;
        playSendSound();
        notifyItemInserted(0);

    }

    public void removeSingleItem(int position) {
        if (position == playingPosition) {
            releaseAudioPlayer(false);
        } else if (playingPosition != RecyclerView.NO_POSITION && position < playingPosition) {
            playingPosition--;
        }
        chatDummyList.remove(chatDummyList.get(position));
        notifyDataSetChanged();
    }

    public void clear() {
        releaseAudioPlayer(false);
        chatDummyList.clear();
        notifyDataSetChanged();
    }

    public interface OnChatItemClickLister {
        void onLongPress(ChatItem chatDummy, int position);

        void onImageClick(ChatItem chatDummy, int position, ImageView mainImage);
    }

    public class ChatTextViewHolder extends RecyclerView.ViewHolder {
        ItemChatTextBinding binding;

        public ChatTextViewHolder(View itemView) {
            super(itemView);
            binding = ItemChatTextBinding.bind(itemView);
        }

        public void setData(int position) {
            ChatItem chatDummy = chatDummyList.get(position);
            binding.imgUser1.setChatUserImage(guestUserImage, guestUserAvatarImage, 10);
            binding.imgUser2.setChatUserImage(localUserImage, localUserAvatarImage, 10);

            if (chatDummy.getSenderId().equals(localUserId)) {  // match with local means sender is local user
                binding.imgUser1.setVisibility(View.INVISIBLE);
                binding.imgUser2.setVisibility(VISIBLE);
                binding.space2.setVisibility(GONE);
                binding.space1.setVisibility(VISIBLE);
                binding.tvText.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_chat_right));
                binding.tvText.setTextColor(ContextCompat.getColor(context, R.color.white));
                binding.tvText.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.chat_right));
                binding.lytAudio.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_chat_right));
                binding.lytAudio.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.chat_right));
            } else {
                binding.imgUser2.setVisibility(View.INVISIBLE);
                binding.imgUser1.setVisibility(VISIBLE);
                binding.space1.setVisibility(GONE);
                binding.space2.setVisibility(VISIBLE);
                binding.tvText.setTextColor(ContextCompat.getColor(context, R.color.white));
                binding.tvText.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_chat_left));
                binding.tvText.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.pink));
                binding.lytAudio.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_chat_left));
                binding.lytAudio.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.pink));
            }
            boolean isAudio = "audio".equalsIgnoreCase(chatDummy.getMessageType());
            binding.tvText.setVisibility(isAudio ? GONE : VISIBLE);
            binding.lytAudio.setVisibility(isAudio ? VISIBLE : GONE);
            binding.tvText.setText(chatDummy.getMessage());
            binding.tvText.setOnClickListener(null);
            if (isAudio) {
                bindAudioMessage(binding, chatDummy, position);
            }
            binding.getRoot().setOnLongClickListener(v -> {
                if (chatDummy.getSenderId().equals(localUserId)) {
                    onChatItemClickLister.onLongPress(chatDummy, position);
                }
                return true;
            });
        }
    }

    private void bindAudioMessage(ItemChatTextBinding binding, ChatItem chatDummy, int position) {
        String url = resolveMediaUrl(chatDummy.getAudio());
        String key = getAudioKey(chatDummy, url);
        int knownDuration = getKnownAudioDuration(chatDummy, url);
        boolean isCurrent = !key.isEmpty() && key.equals(playingMessageId);
        int progress = getCurrentAudioProgress(isCurrent);
        int max = knownDuration > 0 ? knownDuration : Math.max(progress, 1000);

        binding.seekAudio.setMax(max);
        binding.seekAudio.setProgress(Math.min(progress, max));
        binding.seekAudio.setEnabled(false);
        binding.btnPlayAudio.setImageResource(isCurrent && audioPlayer != null && audioPlayer.isPlaying()
                ? R.drawable.icon_pause
                : R.drawable.icon_play);

        if (knownDuration > 0) {
            binding.tvAudioDuration.setText(isCurrent && progress > 0
                    ? formatDuration(progress) + " / " + formatDuration(knownDuration)
                    : formatDuration(knownDuration));
        } else {
            binding.tvAudioDuration.setText("--:--");
            ensureAudioDuration(chatDummy, position, url);
        }

        binding.lytAudio.setOnClickListener(v -> playAudioMessage(chatDummy, position));
        binding.btnPlayAudio.setOnClickListener(v -> playAudioMessage(chatDummy, position));
    }

    private void playAudioMessage(ChatItem chatDummy, int position) {
        String url = resolveMediaUrl(chatDummy.getAudio());
        if (url.isEmpty() || audioPreparing) return;

        String key = getAudioKey(chatDummy, url);
        if (audioPlayer != null && key.equals(playingMessageId)) {
            if (audioPlayer.isPlaying()) {
                audioPlayer.pause();
                stopAudioProgressUpdates();
            } else {
                audioPlayer.start();
                startAudioProgressUpdates();
            }
            notifyItemChanged(position);
            return;
        }

        int previousPosition = playingPosition;
        releaseAudioPlayer(false);
        if (previousPosition != RecyclerView.NO_POSITION) notifyItemChanged(previousPosition);

        try {
            audioPlayer = new MediaPlayer();
            playingAudioUrl = url;
            playingMessageId = key;
            playingPosition = position;
            audioPreparing = true;
            audioPlayer.setDataSource(context, Uri.parse(url));
            audioPlayer.setOnPreparedListener(mp -> {
                audioPreparing = false;
                int duration = mp.getDuration();
                if (duration > 0) {
                    audioDurationCache.put(url, duration);
                    chatDummy.setAudioDuration(duration);
                }
                mp.start();
                startAudioProgressUpdates();
                notifyItemChanged(position);
            });
            audioPlayer.setOnCompletionListener(mp -> {
                int oldPosition = playingPosition;
                releaseAudioPlayer(false);
                if (oldPosition != RecyclerView.NO_POSITION) notifyItemChanged(oldPosition);
            });
            audioPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Unable to play audio message. what=" + what + ", extra=" + extra);
                int oldPosition = playingPosition;
                releaseAudioPlayer(false);
                if (oldPosition != RecyclerView.NO_POSITION) notifyItemChanged(oldPosition);
                return true;
            });
            audioPlayer.prepareAsync();
            notifyItemChanged(position);
        } catch (Exception e) {
            Log.e(TAG, "Unable to play audio message", e);
            releaseAudioPlayer(true);
        }
    }

    public String resolveMediaUrl(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return "";
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed;
        return BuildConfig.BASE_URL + trimmed.replaceFirst("^/+", "");
    }

    private String getAudioKey(ChatItem chatDummy, String url) {
        if (chatDummy.getId() != null && !chatDummy.getId().trim().isEmpty()) return chatDummy.getId();
        return url;
    }

    private int getKnownAudioDuration(ChatItem chatDummy, String url) {
        if (chatDummy.getAudioDuration() > 0) return (int) chatDummy.getAudioDuration();
        Integer cachedDuration = audioDurationCache.get(url);
        return cachedDuration != null ? cachedDuration : 0;
    }

    private int getCurrentAudioProgress(boolean isCurrent) {
        if (!isCurrent || audioPlayer == null) return 0;
        try {
            return audioPlayer.getCurrentPosition();
        } catch (Exception ignored) {
            return 0;
        }
    }

    private void ensureAudioDuration(ChatItem chatDummy, int position, String url) {
        if (url.isEmpty() || loadingAudioDurations.contains(url)) return;
        loadingAudioDurations.add(url);
        new Thread(() -> {
            int duration = readAudioDuration(url);
            audioHandler.post(() -> {
                loadingAudioDurations.remove(url);
                if (duration > 0) {
                    audioDurationCache.put(url, duration);
                    chatDummy.setAudioDuration(duration);
                    notifyItemChanged(position);
                }
            });
        }).start();
    }

    private int readAudioDuration(String url) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                retriever.setDataSource(url, new HashMap<>());
            } else {
                retriever.setDataSource(url);
            }
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return duration == null ? 0 : Integer.parseInt(duration);
        } catch (Exception e) {
            Log.w(TAG, "Unable to read audio duration.", e);
            return 0;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
    }

    private void startAudioProgressUpdates() {
        stopAudioProgressUpdates();
        audioProgressRunnable = new Runnable() {
            @Override
            public void run() {
                if (audioPlayer != null && playingPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(playingPosition);
                    audioHandler.postDelayed(this, 500);
                }
            }
        };
        audioHandler.post(audioProgressRunnable);
    }

    private void stopAudioProgressUpdates() {
        if (audioProgressRunnable != null) {
            audioHandler.removeCallbacks(audioProgressRunnable);
            audioProgressRunnable = null;
        }
    }

    private String formatDuration(long durationMs) {
        long totalSeconds = Math.max(0, Math.round(durationMs / 1000.0));
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    private void releaseAudioPlayer() {
        releaseAudioPlayer(true);
    }

    private void releaseAudioPlayer(boolean notifyChanged) {
        int oldPosition = playingPosition;
        stopAudioProgressUpdates();
        if (audioPlayer != null) {
            try {
                if (audioPlayer.isPlaying()) audioPlayer.stop();
            } catch (Exception ignored) {
            }
            audioPlayer.release();
            audioPlayer = null;
        }
        playingAudioUrl = "";
        playingMessageId = "";
        playingPosition = RecyclerView.NO_POSITION;
        audioPreparing = false;
        if (notifyChanged && oldPosition != RecyclerView.NO_POSITION) notifyItemChanged(oldPosition);
    }

    public class ChatStikerViewHolder extends RecyclerView.ViewHolder {
        ItemChatStikerBinding binding;

        public ChatStikerViewHolder(View itemView) {
            super(itemView);
            binding = ItemChatStikerBinding.bind(itemView);
        }

        public void setData(int position) {
            ChatItem chatDummy = chatDummyList.get(position);
            binding.imgUser1.setUserImage(guestUserImage, guestUserAvatarImage, 10);
            binding.imgUser2.setUserImage(localUserImage, localUserAvatarImage, 10);
            Glide.with(itemView).load(chatDummy.getImage())
                    .apply(MainApplication.requestOptions)
                    .into(binding.tvImage);


            if (chatDummy.getSenderId().equals(localUserId)) {
                binding.imgUser1.setVisibility(View.INVISIBLE);
                binding.imgUser2.setVisibility(VISIBLE);
                binding.space2.setVisibility(GONE);
                binding.space1.setVisibility(VISIBLE);
                binding.tvImage.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_chat_right));
                binding.tvImage.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.tintColor));

            } else {
                binding.imgUser2.setVisibility(View.INVISIBLE);
                binding.imgUser1.setVisibility(VISIBLE);
                binding.space1.setVisibility(GONE);
                binding.space2.setVisibility(VISIBLE);
                binding.tvImage.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_chat_left));
            }
        }
    }

    private void emitMessageReadStatus(String messageId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("messageId", messageId);
            MySocketManager.getInstance().getSocket().emit("messageReadStatus", jsonObject);
            Log.d(TAG, "Emitted messageReadStatus for: " + messageId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class ChatImageViewHolder extends RecyclerView.ViewHolder {
        ItemChatImageBinding binding;

        public ChatImageViewHolder(View itemView) {
            super(itemView);
            binding = ItemChatImageBinding.bind(itemView);
        }

        public void setData(int position) {
            ChatItem chatDummy = chatDummyList.get(position);
            String imageUrl = resolveMediaUrl(chatDummy.getImage());

            binding.imgUser1.setChatUserImage(guestUserImage, guestUserAvatarImage, 10);
            binding.imgUser2.setChatUserImage(localUserImage, localUserAvatarImage, 10);
            Glide.with(itemView).load(imageUrl)
                    .placeholder(R.drawable.placeholder_live)
                    .error(R.drawable.placeholder_live)
                    .dontAnimate()
                    .into(binding.mainImage);
            binding.mainImage.setAdjustViewBounds(true);

            if (chatDummy.getSenderId().equals(localUserId)) {  // match with local means sender is local user
                binding.imgUser1.setVisibility(View.INVISIBLE);
                binding.imgUser2.setVisibility(VISIBLE);
                binding.space2.setVisibility(GONE);
                binding.space1.setVisibility(VISIBLE);
            } else {
                binding.imgUser2.setVisibility(View.INVISIBLE);
                binding.imgUser1.setVisibility(VISIBLE);
                binding.space1.setVisibility(GONE);
                binding.space2.setVisibility(VISIBLE);
            }

            String transitionKey = chatDummy.getId() != null && !chatDummy.getId().isEmpty()
                    ? chatDummy.getId()
                    : String.valueOf(position);
            ViewCompat.setTransitionName(binding.mainImage, "sharedImage_" + transitionKey);
            binding.getRoot().setOnClickListener(view -> {
                if (onChatItemClickLister != null && !imageUrl.isEmpty()) {
                    onChatItemClickLister.onImageClick(chatDummy, position, binding.mainImage);
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (chatDummy.getSenderId().equals(localUserId)) {
                    onChatItemClickLister.onLongPress(chatDummy, position);
                }
                return true;
            });

        }
    }

    public class ChatGiftViewHolder extends RecyclerView.ViewHolder {
        ItemChatGiftBinding binding;

        public ChatGiftViewHolder(View itemView) {
            super(itemView);
            binding = ItemChatGiftBinding.bind(itemView);
        }

        public void setData(int position) {
            ChatItem chatDummy = chatDummyList.get(position);

            // 1) Avatars
            binding.imgUser1.setChatUserImage(guestUserImage, guestUserAvatarImage, 10);
            binding.imgUser2.setChatUserImage(localUserImage, localUserAvatarImage, 10);

            // 2) Align + tint
            boolean isLocal = chatDummy.getSenderId().equals(localUserId);
            if (isLocal) {
                binding.imgUser1.setVisibility(View.INVISIBLE);
                binding.imgUser2.setVisibility(VISIBLE);
                binding.space2.setVisibility(GONE);
                binding.space1.setVisibility(VISIBLE);
                binding.lytMain.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.tintColor));

                String safeLocalName = localUserName != null ? localUserName : "User";
                String giftNote = "\uD83C\uDF81 " + safeLocalName + context.getString(R.string.sent_a_gift);
                binding.giftNote.setText(giftNote);
            } else {
                binding.imgUser2.setVisibility(View.INVISIBLE);
                binding.imgUser1.setVisibility(VISIBLE);
                binding.space1.setVisibility(GONE);
                binding.space2.setVisibility(VISIBLE);
                binding.lytMain.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.white));

                String safeGuestName = guestUserName != null ? guestUserName : "User";
                String giftNote = "\uD83C\uDF81 " + safeGuestName + context.getString(R.string.sent_a_gift);
                binding.giftNote.setText(giftNote);
            }

            // 4) Reset media views (avoid recycler ghosts)
            binding.giftSvga.stopAnimation();
            binding.giftSvga.clear();
            binding.giftSvga.setVisibility(GONE);
            binding.giftImage.setVisibility(VISIBLE);

            Log.d(TAG, "setData: ==> getGiftImage " + chatDummy.getGiftImage());
            Log.d(TAG, "setData: ==> getSvgaImage " + chatDummy.getSvgaImage());
            Log.d(TAG, "setData: ==> getGiftsvgaImage " + chatDummy.getGiftsvgaImage());

            // 5) Pick the best static image to show
            String thumbRel = chatDummy.getGiftsvgaImage();

            if (thumbRel == null) {
                String imageUrl = (chatDummy.getType() == 2) ? chatDummy.getGiftsvgaImage() : chatDummy.getGiftImage();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    String url = BuildConfig.BASE_URL + imageUrl;
                    if (chatDummy.getType() == 2) {
                        Glide.with(binding.getRoot())
                                .load(url)
                                .placeholder(R.drawable.gift_placeholder)
                                .centerCrop()
                                .into(binding.giftImage);
                    } else {
                        Glide.with(binding.getRoot())
                                .load(url)
                                .apply(MainApplication.requestOptions)
                                .placeholder(R.drawable.gift_placeholder)
                                .into(binding.giftImage);
                    }
                }

                return;
            }

            String imageRel = chatDummy.getGiftImage();
            String url = null;

            if (thumbRel != null && !thumbRel.trim().isEmpty()) {
                url = BuildConfig.BASE_URL + thumbRel;         // prefer explicit thumbnail
            } else if (imageRel != null && !imageRel.trim().isEmpty() && !imageRel.toLowerCase().endsWith(".svga")) {
                url = BuildConfig.BASE_URL + imageRel;         // fallback to static if not .svga
            }

            // 6) Load thumbnail (or placeholder if nothing available)
            Glide.with(itemView)
                    .load(url)                                    // may be null → Glide shows placeholder
                    .placeholder(R.drawable.gift_placeholder)
                    .error(R.drawable.gift_placeholder)
                    .dontAnimate()
                    .into(binding.giftImage);

        }
    }


    public class ChatCallViewHolder extends RecyclerView.ViewHolder {
        ItemChatCallBinding binding;

        public ChatCallViewHolder(View itemView) {
            super(itemView);
            binding = ItemChatCallBinding.bind(itemView);
        }

        public void setData(int position) {
            ChatItem chatDummy = chatDummyList.get(position);

            if (chatDummy.getMessageType().equals("Video call")) {

                if (chatDummy.getCallStatus() == 1) {


                    binding.tvCallType.setText("Video Call");
                    binding.tvDuration.setText(chatDummy.getCallDuration() + " Sec");
                    binding.tvDuration.setVisibility(VISIBLE);
                    binding.tvCallTime.setVisibility(VISIBLE);
                    binding.tvCallTime2.setVisibility(GONE);

                    String raw = chatDummy.getDate(); // "1/24/2026, 3:03:06 PM"
                    String[] parts = raw.split(",");
                    String time = parts[1].trim(); // "3:03:06 PM"
                    String finalTime = time.substring(0, time.lastIndexOf(":")) + time.substring(time.lastIndexOf(" "));
                    binding.tvCallTime.setText(finalTime); // "3:03 PM"

                    double coin = chatDummy.getCoin();
                    if (coin == (long) coin) {
                        binding.tvCallCoin.setText(String.valueOf((long) coin));
                    } else {
                        binding.tvCallCoin.setText(String.valueOf(coin));
                    }

                    if (chatDummy.getSenderId().equals(localUserId)) {  // match with local means sender is local user
                        binding.space2.setVisibility(GONE);
                        binding.space1.setVisibility(VISIBLE);
                        Glide.with(context).load(R.drawable.ic_outgoing_videocall).into(binding.ivCalltype);
                    } else {
                        binding.space1.setVisibility(GONE);
                        binding.space2.setVisibility(VISIBLE);
                        Glide.with(context).load(R.drawable.ic_incoming_videocall).into(binding.ivCalltype);
                    }

                } else if (chatDummy.getCallStatus() == 2) {

                    if (chatDummy.getSenderId().equals(localUserId)) {  // match with local means sender is local user
                        binding.space2.setVisibility(GONE);
                        binding.space1.setVisibility(VISIBLE);
                    } else {
                        binding.space1.setVisibility(GONE);
                        binding.space2.setVisibility(VISIBLE);
                    }

                    binding.tvCallType.setText("Decline Video Call");
                    Glide.with(context).load(R.drawable.ic_missed_videocall).into(binding.ivCalltype);
                    binding.tvDuration.setVisibility(GONE);
                    binding.tvCallTime.setVisibility(GONE);
                    binding.tvCallTime2.setVisibility(VISIBLE);

                    String raw = chatDummy.getDate();
                    String[] parts = raw.split(",");
                    String time = parts[1].trim();
                    String finalTime = time.substring(0, time.lastIndexOf(":")) + time.substring(time.lastIndexOf(" "));
                    binding.tvCallTime2.setText(finalTime);

                    binding.tvCallCoin.setText(sessionManager.getSetting().getFemaleCallCharge() + "/Min");
                } else if (chatDummy.getCallStatus() == 3) {

                    if (chatDummy.getSenderId().equals(localUserId)) {  // match with local means sender is local user
                        binding.space2.setVisibility(GONE);
                        binding.space1.setVisibility(VISIBLE);
                    } else {
                        binding.space1.setVisibility(GONE);
                        binding.space2.setVisibility(VISIBLE);
                    }

                    binding.tvCallType.setText("Missed Video Call");
                    Glide.with(context).load(R.drawable.ic_missed_videocall).into(binding.ivCalltype);
                    binding.tvDuration.setVisibility(GONE);
                    binding.tvCallTime.setVisibility(GONE);
                    binding.tvCallTime2.setVisibility(VISIBLE);

                    String raw = chatDummy.getDate();
                    String[] parts = raw.split(",");
                    String time = parts[1].trim();
                    String finalTime = time.substring(0, time.lastIndexOf(":")) + time.substring(time.lastIndexOf(" "));
                    binding.tvCallTime2.setText(finalTime);

                    binding.tvCallCoin.setText(sessionManager.getSetting().getFemaleCallCharge() + "/Min");
                }

            }else {

                if (chatDummy.getCallStatus() == 1) {


                    binding.tvCallType.setText("Audio Call");
                    binding.tvDuration.setText(chatDummy.getCallDuration() + " Sec");
                    binding.tvDuration.setVisibility(VISIBLE);
                    binding.tvCallTime.setVisibility(VISIBLE);
                    binding.tvCallTime2.setVisibility(GONE);

                    String raw = chatDummy.getDate();
                    String[] parts = raw.split(",");
                    String time = parts[1].trim();
                    String finalTime = time.substring(0, time.lastIndexOf(":")) + time.substring(time.lastIndexOf(" "));
                    binding.tvCallTime.setText(finalTime);

                    double coin = chatDummy.getCoin();
                    if (coin == (long) coin) {
                        binding.tvCallCoin.setText(String.valueOf((long) coin));
                    } else {
                        binding.tvCallCoin.setText(String.valueOf(coin));
                    }

                    if (chatDummy.getSenderId().equals(localUserId)) {  // match with local means sender is local user
                        binding.space2.setVisibility(GONE);
                        binding.space1.setVisibility(VISIBLE);
                        Glide.with(context).load(R.drawable.ic_outgoing_audiocall).into(binding.ivCalltype);
                    } else {
                        binding.space1.setVisibility(GONE);
                        binding.space2.setVisibility(VISIBLE);
                        Glide.with(context).load(R.drawable.ic_incoming_audiocall).into(binding.ivCalltype);
                    }

                } else if (chatDummy.getCallStatus() == 2) {

                    if (chatDummy.getSenderId().equals(localUserId)) {  // match with local means sender is local user
                        binding.space2.setVisibility(GONE);
                        binding.space1.setVisibility(VISIBLE);
                    } else {
                        binding.space1.setVisibility(GONE);
                        binding.space2.setVisibility(VISIBLE);
                    }

                    binding.tvCallType.setText("Decline Audio Call");
                    Glide.with(context).load(R.drawable.ic_missed_audiocall).into(binding.ivCalltype);
                    binding.tvDuration.setVisibility(GONE);
                    binding.tvCallTime.setVisibility(GONE);
                    binding.tvCallTime2.setVisibility(VISIBLE);

                    String raw = chatDummy.getDate();
                    String[] parts = raw.split(",");
                    String time = parts[1].trim();
                    String finalTime = time.substring(0, time.lastIndexOf(":")) + time.substring(time.lastIndexOf(" "));
                    binding.tvCallTime2.setText(finalTime);

                    binding.tvCallCoin.setText(sessionManager.getSetting().getAudioCallChargeFemale() + "/Min");
                } else if (chatDummy.getCallStatus() == 3) {

                    if (chatDummy.getSenderId().equals(localUserId)) {  // match with local means sender is local user
                        binding.space2.setVisibility(GONE);
                        binding.space1.setVisibility(VISIBLE);
                    } else {
                        binding.space1.setVisibility(GONE);
                        binding.space2.setVisibility(VISIBLE);
                    }

                    binding.tvCallType.setText("Missed Audio Call");
                    Glide.with(context).load(R.drawable.ic_missed_audiocall).into(binding.ivCalltype);
                    binding.tvDuration.setVisibility(GONE);
                    binding.tvCallTime.setVisibility(GONE);
                    binding.tvCallTime2.setVisibility(VISIBLE);

                    String raw = chatDummy.getDate();
                    String[] parts = raw.split(",");
                    String time = parts[1].trim();
                    String finalTime = time.substring(0, time.lastIndexOf(":")) + time.substring(time.lastIndexOf(" "));
                    binding.tvCallTime2.setText(finalTime);

                    binding.tvCallCoin.setText(sessionManager.getSetting().getAudioCallChargeFemale() + "/Min");
                }
            }
        }
    }


    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof ChatGiftViewHolder) {
            ChatGiftViewHolder h = (ChatGiftViewHolder) holder;
            h.binding.giftSvga.stopAnimation();
            h.binding.giftSvga.clear();
        }
    }
}
