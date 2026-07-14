package com.codder.ultimate.chat.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.bottomsheets.BottomSheetReport;
import com.codder.ultimate.bottomsheets.BottomSheetReportOption;
import com.codder.ultimate.bottomsheets.BottomsheetCallType;
import com.codder.ultimate.chat.adapter.ChatAdapter;
import com.codder.ultimate.chat.adapter.ChatSuggestionAdapter;
import com.codder.ultimate.chat.modelclass.ChatItem;
import com.codder.ultimate.chat.modelclass.ChatSuggestion;
import com.codder.ultimate.chat.modelclass.ChatUserListRoot;
import com.codder.ultimate.chat.modelclass.UploadImageRoot;
import com.codder.ultimate.chat.viewmodel.ChatViewModel;
import com.codder.ultimate.databinding.ActivityChatBinding;
import com.codder.ultimate.guestuser.activity.GuestActivity;
import com.codder.ultimate.live.fragment.EmojiBottomSheetFragment;
import com.codder.ultimate.live.model.GiftRoot;
import com.codder.ultimate.live.viewModel.EmojiSheetViewModel;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.retrofit.UserApiCall;
import com.codder.ultimate.socket.ChatHandler;
import com.codder.ultimate.socket.MySocketManager;
import com.codder.ultimate.utils.SvgaCacheManager;
import com.codder.ultimate.viewModel.ViewModelFactory;
import com.google.gson.Gson;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.yalantis.ucrop.UCrop;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private static final String TAG = "ChatActivity";
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "cropimage";
    public static String otherUserId = "";
    public static String topicId = "";
    public static boolean isOPEN = false;
    ActivityChatBinding binding;
    SessionManager sessionManager;
    boolean isSend = false;
    private ChatViewModel viewModel;
    private GuestProfileRoot.User guestUser;
    private String picturePath;
    private Uri selectedImage;
    private boolean isFromUserProfileSheet = false;
    private MediaRecorder voiceRecorder;
    private File voiceRecordFile;
    private boolean isRecordingVoice = false;
    private long voiceRecordStartedAt = 0L;

    private EmojiSheetViewModel giftViewModel;
    private EmojiBottomSheetFragment emojiBottomsheetFragment;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;

    private ChatItem lastReceivedChat = null;

    private final ChatHandler chatHandler = new ChatHandler() {

        @Override
        public void onChat(Object[] args) {
            if (args == null || args.length == 0 || args[0] == null) return;

            runOnUiThread(() -> {
                ChatItem item = new Gson().fromJson(args[0].toString(), ChatItem.class);
                if (item == null) return;

                // ❶ Ignore messages for other conversations
                if (item.getTopic() == null || !item.getTopic().equals(viewModel.chatTopic)) {
                    Log.d(TAG, "Ignored message for another topic: " + item.getTopic());
                    return;
                }

                // ❷ Prefer stable duplicate check by DB id (_id) if available
                if (lastReceivedChat != null
                        && lastReceivedChat.getId() != null
                        && lastReceivedChat.getId().equals(item.getId())) {
                    Log.d(TAG, "Duplicate chat skipped (id).");
                    return;
                }

                lastReceivedChat = item;
                viewModel.chatAdapter.addSingleChat(item);
                binding.rvChat.scrollToPosition(0);  // newest at top
                isSend = true;
            });
        }

        private Queue<JSONObject> giftQueue = new LinkedList<>();
        private boolean isGiftDisplaying = false;

        @Override
        public void chatOrCallGiftSent(Object[] args) {
            runOnUiThread(() -> {
                try {
                    if (args == null || args.length == 0 || args[0] == null) return;

                    String raw = args[0].toString();
                    Log.d(TAG, "chatOrCallGiftSent payload: " + raw);

                    JSONObject root = new JSONObject(raw);
                    JSONObject data = root.optJSONObject("data");
                    if (data == null) return; // nothing useful

                    Log.d(TAG, "chatOrCallGiftSent: raw ==> " + data);
                    String eventType = data != null ? data.optString("eventType", "") : "";
                    if (!"chat".equalsIgnoreCase(eventType)) return;

                    String incomingTopic = data.optString("chatTopicId",
                            data.optString("topic", "")); // tolerate either key
                    String currentTopic = viewModel != null ? viewModel.chatTopic : "";
                    if (incomingTopic == null || currentTopic == null || !incomingTopic.equals(currentTopic)) {
                        // Not for this chat screen → ignore quietly
                        return;
                    }

                    // 1) Enqueue for display if gift is present
                    if (data.has("gift")) {
                        giftQueue.add(data); // enqueue just the data object
                        if (!isGiftDisplaying) processNextGift();
                    }

                    // 2) Extract sender and gift info from *data*
                    String senderId = data.optString("senderId", "");
                    String giftStr = data.optString("gift", null);
                    int giftCount = data.optInt("giftCount", 1);

                    GiftRoot.GiftItem giftItem = null;
                    if (giftStr != null && giftStr.trim().startsWith("{")) {
                        giftItem = new Gson().fromJson(giftStr, GiftRoot.GiftItem.class);
                    } else {
                        JSONObject giftObj = data.optJSONObject("gift");
                        if (giftObj != null)
                            giftItem = new Gson().fromJson(giftObj.toString(), GiftRoot.GiftItem.class);
                    }
                    if (giftItem != null) giftItem.setCount(giftCount);

                    Log.d(TAG, "senderId=" + senderId + " localUser=" + sessionManager.getUser().getId());

                    // 3) Deduct diamonds only if local user sent the gift
                    final String senderIdStr = String.valueOf(data.opt("senderId")); // handles numbers or strings
                    final String localIdStr = String.valueOf(sessionManager.getUser().getId()); // or getUserId() if that's what server uses

                    if (sessionManager != null
                            && sessionManager.getUser() != null
                            && giftItem != null
                            && java.util.Objects.equals(senderIdStr, localIdStr)) {

                        double cost = giftItem.getCoin() * giftItem.getCount();
                        double current = sessionManager.getUser().getDiamond();
                        double newBalance = Math.max(0, current - cost);

                        NumberFormat nf = NumberFormat.getInstance(Locale.US);
                        nf.setMaximumFractionDigits(2);
                        nf.setMinimumFractionDigits(2);

                        String costStr = nf.format(cost);
                        String currentStr = nf.format(current);
                        String newBalanceStr = nf.format(newBalance);

                        Log.d(TAG, "chatOrCallGiftSent: cost " + costStr);
                        Log.d(TAG, "chatOrCallGiftSent: current " + currentStr);
                        Log.d(TAG, "chatOrCallGiftSent: newBalance " + newBalanceStr);

                        Log.d(TAG, "💎 Diamonds deducted. senderId=" + senderIdStr
                                + " localId=" + localIdStr
                                + " cost=" + costStr
                                + " newBalance=" + newBalanceStr);

                        UserRoot.User user = sessionManager.getUser();
                        user.setDiamond(newBalance);
                        sessionManager.saveUser(user);

                        if (giftViewModel != null) {
                            giftViewModel.localUserCoin.setValue(newBalance);
                        }

                        Log.d(TAG, "chatOrCallGiftSent: Diamonds deducted, new balance ==> " + newBalanceStr);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "chatOrCallGiftSent error", e);
                }
            });
        }

        private void processNextGift() {
            if (giftQueue.isEmpty()) {
                isGiftDisplaying = false;
                return;
            }
            isGiftDisplaying = true;
            JSONObject giftJson = giftQueue.poll();
            if (giftJson == null) {
                processNextGift();
                return;
            }
            try {
                Object giftObj = giftJson.opt("gift");
                if (giftObj == null) {
                    processNextGift();
                    return;
                }
                GiftRoot.GiftItem giftData = new Gson().fromJson(giftObj.toString(), GiftRoot.GiftItem.class);
                if (giftData == null) {
                    processNextGift();
                    return;
                }

                String finalGiftLink = null;
                List<GiftRoot.GiftItem> giftItemList = sessionManager != null ? sessionManager.getGiftsList(giftData.getCategory()) : null;
                if (giftItemList != null) {
                    for (GiftRoot.GiftItem item : giftItemList) {
                        if (item != null && item.getId() != null && item.getId().equals(giftData.getId())) {
                            finalGiftLink = BuildConfig.BASE_URL + item.getImage();
                            break;
                        }
                    }
                }

                String senderIdForChat = giftJson.optString("senderId", "");
                if (!sessionManager.getUser().getId().equals(senderIdForChat)) {
                    addGiftToChatList(giftData, senderIdForChat);
                    binding.rvChat.scrollToPosition(0);
                }

                if (giftData.getType() == 2) {
                    handleSVGAGift(finalGiftLink, giftJson, giftData);
                } else if (giftData.getType() == 0 || giftData.getType() == 1) {
                    displayImageGift(finalGiftLink, giftJson, giftData);
                } else {
                    processNextGift();
                }
            } catch (Exception e) {
                e.printStackTrace();
                processNextGift();
            }
        }

        private void displayImageGift(String giftLink, JSONObject jsonObject, GiftRoot.GiftItem giftData) throws JSONException {
            if (isDestroyed() || isFinishing() || binding == null) {
                processNextGift();
                return;
            }
            Glide.with(ChatActivity.this).load(giftLink).into(binding.imgGift);
            Glide.with(ChatActivity.this).load(RayziUtils.getImageFromNumber(giftData.getCount())).into(binding.imgGiftCount);
            String name = jsonObject.optString("userName", "");
            binding.tvGiftUserName.setText(name + getString(R.string.sent_a_gift));
            binding.lytGift.setVisibility(VISIBLE);
            binding.tvGiftUserName.setVisibility(VISIBLE);

            new Handler().postDelayed(() -> {
                if (binding != null) {
                    binding.lytGift.setVisibility(GONE);
                    binding.tvGiftUserName.setVisibility(GONE);
                    binding.tvGiftUserName.setText("");
                    binding.imgGift.setImageDrawable(null);
                    binding.imgGiftCount.setImageDrawable(null);
                }
                processNextGift();
            }, 4000);
        }


        private void handleSVGAGift(String giftLink, JSONObject jsonObject, GiftRoot.GiftItem giftData) {
            binding.lytGift.setVisibility(GONE);
            binding.tvGiftUserName.setVisibility(GONE);
            binding.svgaImage.setVisibility(View.VISIBLE);
            SVGAImageView imageView = binding.svgaImage;

            SvgaCacheManager.decodeSvgaFromCache(ChatActivity.this, giftLink, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                    Log.d(TAG, "✅ Loaded SVGA from cache or fallback: " + giftLink);

                    SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                    imageView.setImageDrawable(drawable);
                    imageView.startAnimation();

                    binding.lytSvgagift.setVisibility(View.VISIBLE);
                    String name = jsonObject.optString("userName", "");
                    binding.tvSvgaGiftUserName.setText(name + " " + getString(R.string.sent_a_gift));
                    Glide.with(binding.imgSvgaGiftCount)
                            .load(RayziUtils.getImageFromNumber(giftData.getCount()))
                            .into(binding.imgSvgaGiftCount);

                    long duration = svgaVideoEntity.getFrames() * 1000L / svgaVideoEntity.getFPS();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        binding.lytSvgagift.setVisibility(GONE);
                        imageView.clear();
                        imageView.setVisibility(GONE);
                        processNextGift();
                    }, duration);
                }

                @Override
                public void onError() {
                    Log.w(TAG, "⚠️ SVGA not found in cache, falling back to URL: " + giftLink);
                    try {
                        SVGAParser parser = new SVGAParser(ChatActivity.this);
                        parser.decodeFromURL(new URL(giftLink), new SVGAParser.ParseCompletion() {
                            @Override
                            public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                                SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                                imageView.setImageDrawable(drawable);
                                imageView.startAnimation();

                                binding.lytSvgagift.setVisibility(View.VISIBLE);
                                String name = jsonObject.optString("userName", "");
                                binding.tvSvgaGiftUserName.setText(name + " " + getString(R.string.sent_a_gift));
                                Glide.with(binding.imgSvgaGiftCount)
                                        .load(RayziUtils.getImageFromNumber(giftData.getCount()))
                                        .into(binding.imgSvgaGiftCount);

                                long duration = svgaVideoEntity.getFrames() * 1000L / svgaVideoEntity.getFPS();

                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    binding.lytSvgagift.setVisibility(GONE);
                                    imageView.clear();
                                    imageView.setVisibility(GONE);
                                    processNextGift();
                                }, duration);
                            }

                            @Override
                            public void onError() {
                                Log.e(TAG, "❌ SVGA failed to load from URL: " + giftLink);
                                processNextGift();
                            }
                        }, null);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        processNextGift();
                    }
                }
            });
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        isOPEN = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);

        ViewCompat.setOnApplyWindowInsetsListener(binding.topLayout, (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), sb.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(binding.rvChat, (v, insets) -> {
            Insets nb = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(),
                    v.getPaddingRight(), nb.bottom);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(binding.lytBottom, (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            int bottom = Math.max(imeInsets.bottom, navInsets.bottom);

            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(),
                    v.getPaddingRight(), bottom);

            return insets;
        });


        WindowInsetsControllerCompat c =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        c.setAppearanceLightStatusBars(false);

        sessionManager = new SessionManager(this);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new ChatViewModel()).createFor()).get(ChatViewModel.class);
        viewModel.chatAdapter = new ChatAdapter(this);
        binding.rvChat.setAdapter(viewModel.chatAdapter);
        binding.setViewmodel(viewModel);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedImage = uri;
                        startCropActivity(uri);
                    } else {
                        Log.i(TAG, "onCreate: No image selected");
                    }
                }
        );

        cropImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri resultUri = UCrop.getOutput(result.getData());
                        if (resultUri != null) {
                            selectedImage = resultUri;
                            picturePath = getRealPathFromURI(resultUri);
                            showAndUploadImage(resultUri);
                        }
                    } else if (result.getResultCode() == UCrop.RESULT_ERROR) {
                        Throwable cropError = UCrop.getError(result.getData());
                        if (cropError != null) {
                            Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );


        setupSocketHandler();
        initView();
        loadChatSuggestions();
        handleIntent();
        observeViewModel();
    }

    private void loadChatSuggestions() {
        userApiCall.getChatSuggestions(new UserApiCall.OnChatSuggestionsListener() {
            @Override
            public void onSuggestionsFetched(List<ChatSuggestion.DataItem> suggestions) {
                if (suggestions != null && !suggestions.isEmpty()) {
                    ChatSuggestionAdapter adapter = new ChatSuggestionAdapter(suggestions, message -> {
                        binding.etChat.setText(message);
                        binding.etChat.setSelection(message.length());
                        binding.tvSend.performClick();
                    });
                    binding.rvChatSuggestions.setVisibility(View.VISIBLE);
                    binding.rvChatSuggestions.setAdapter(adapter);
                    binding.rvChatSuggestions.setLayoutManager(new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.HORIZONTAL, false));
                }
            }

            @Override
            public void onFailure() {
                Log.e("ChatActivity", "Failed to load chat suggestions");
            }
        });
    }

    private void handleIntent() {
        Intent intent = getIntent();

        String chatRootStr = intent.getStringExtra(Const.CHATROOM);
        isFromUserProfileSheet = getIntent().getBooleanExtra("fromUserProfileSheet", false);

        if (chatRootStr != null && !chatRootStr.isEmpty()) {
            Log.d(TAG, "handleIntent: ---> " + chatRootStr.toString());
            ChatUserListRoot.ChatUserItem chatRoot = new Gson().fromJson(chatRootStr, ChatUserListRoot.ChatUserItem.class);
            otherUserId = chatRoot.getUserId();
            topicId = chatRoot.getTopic();
            userApiCall.getGuestProfile(chatRoot.getUserId(), new UserApiCall.OnGuestUserApiListener() {
                @Override
                public void onUserGot(GuestProfileRoot.User user) {
                    guestUser = user;
                    if (!isFinishing()) {
                        binding.imgUser.setWithoutbgUserImage(guestUser.getImage(), guestUser.getAvatarFrameImage(), 10);
                    }
                }

                @Override
                public void onFailure() {
                }
            });

            if (!isFinishing()) {
                binding.imgUser.setUserImage(chatRoot.getImage(), chatRoot.getAvatarFrameImage(), 10);
            }
            binding.tvUserName.setText(chatRoot.getName());

            binding.tvStatus.setText(chatRoot.isOnline() ? "Online" : "Offline");
            if (chatRoot.isOnline()) {
                binding.dot.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green));
            }else {
                binding.dot.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
            }

            viewModel.chatAdapter.initGuestUser(chatRoot.getName(), chatRoot.getImage(), chatRoot.getAvatarFrameImage());

            Log.d(TAG, "handleIntent: ==>getImage " +chatRoot.getImage());
            Log.d(TAG, "handleIntent: ==>getAvatarFrameImage " +chatRoot.getAvatarFrameImage());
            viewModel.chatTopic = chatRoot.getTopic();
            initListener();
            viewModel.getOldChat(false,sessionManager.getUser().getId());

            binding.btnCamera.setOnClickListener(v -> choosePhoto());
        }

        String userStr = intent.getStringExtra(Const.USER);
        if (userStr != null && !userStr.isEmpty()) {
            guestUser = new Gson().fromJson(userStr, GuestProfileRoot.User.class);

            otherUserId = guestUser.getUserId();

            if (!isFinishing()) {
                binding.imgUser.setUserImage(guestUser.getImage(), guestUser.getAvatarFrameImage(), 10);
            }
            binding.tvUserName.setText(guestUser.getName());
            userApiCall.createChatTopic(sessionManager.getUser().getId(), guestUser.getUserId(), topic -> {
                viewModel.chatTopic = topic;
                initListener();
                viewModel.getOldChat(false,sessionManager.getUser().getId());
            });
        }

        if (isFromUserProfileSheet) {
            binding.btnVideoCall.setVisibility(View.GONE);
        }
    }

    private void observeViewModel() {
        viewModel.lastMessageId.observe(this, lastMessageId -> {
            if (lastMessageId != null) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("messageId", lastMessageId);
                    MySocketManager.getInstance().getSocket().emit("messageReadStatus", jsonObject);
                    Log.d(TAG, "onCreate: ====message event emitted... " + jsonObject);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        viewModel.isLoadingComplete.observe(this, aBoolean -> {
            if (aBoolean) {
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();
                if (viewModel.start == 0) {
                    binding.rvChat.postDelayed(() -> binding.rvChat.scrollToPosition(0), 100);
                }
            }
        });
    }

    private void initView() {
        giftViewModel = new ViewModelProvider(this, new ViewModelFactory(new EmojiSheetViewModel()).createFor()).get(EmojiSheetViewModel.class);
        giftViewModel.initEmojiSheet(this);
        giftViewModel.getGiftCategory();
        emojiBottomsheetFragment = new EmojiBottomSheetFragment();
        binding.imggift2.setOnClickListener(v -> {
            if (!emojiBottomsheetFragment.isAdded())
                emojiBottomsheetFragment.show(getSupportFragmentManager(), "emojiSheet");
        });

        giftViewModel.finalGift.observe(this, giftItem -> {
            if (giftItem == null) return;

            int count = giftItem.getCount();
            double totalCost = giftItem.getCoin() * count;
            double current = sessionManager.getUser().getDiamond();

            if (current < totalCost) {
                Toast.makeText(ChatActivity.this, getString(R.string.you_not_have_enough_diamonds_to_send_gift), Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("senderId", sessionManager.getUser().getId());
                jsonObject.put("receiverId", otherUserId);
                jsonObject.put("chatTopicId", viewModel.chatTopic);
                jsonObject.put("giftId", giftItem.getId());
                jsonObject.put("giftCount", giftItem.getCount());
                jsonObject.put("eventType", "chat");
                jsonObject.put("userName", sessionManager.getUser().getName());
                jsonObject.put("receiverUserName", guestUser.getName());
                jsonObject.put("coin", giftItem.getCoin() * giftItem.getCount());
                jsonObject.put("gift", new Gson().toJson(giftItem));

                Log.d(TAG, "initView: ===> " +new Gson().toJson(giftItem).toString());

                double totalDiamond = sessionManager.getUser().getDiamond();

                if (totalDiamond >= totalCost) {
                    MySocketManager.getInstance().getSocket().emit(Const.EVENT_CHAT_OR_CALL_GIFT_SENT, jsonObject);
                    Log.d(TAG, "chatOrCallGiftSent Emit called successfully. ===> " + jsonObject.toString());

                    ChatItem giftChat = new ChatItem();
                    giftChat.setSenderId(sessionManager.getUser().getId());
                    giftChat.setMessageType("chatGift");
                    giftChat.setGiftsvgaImage(giftItem.getSvgaImage());
                    giftChat.setGiftImage(giftItem.getImage());
                    giftChat.setType(giftItem.getType());

                    viewModel.chatAdapter.addSingleChat(giftChat);
                    binding.rvChat.scrollToPosition(0);

                } else {
                    Toast.makeText(ChatActivity.this, getString(R.string.you_not_have_enough_diamonds_to_send_gift), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        binding.rvChat.setHasFixedSize(true);
        binding.rvChat.setItemViewCacheSize(20);
        Log.d(TAG, "initView: ==> " + sessionManager.getUser().getName());
        viewModel.chatAdapter.initLocalUser(sessionManager.getUser().getName(), sessionManager.getUser().getImage(), sessionManager.getUser().getAvatarFrameImage());
        viewModel.chatAdapter.initLocalUserId(sessionManager.getUser().getId());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);   // Newest message at top (position 0)
        layoutManager.setStackFromEnd(true);    // Scroll to bottom automatically
        binding.rvChat.setLayoutManager(layoutManager);

        binding.ivBack.setOnClickListener(v -> onBackPressed());



        binding.etChat.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.sendBtnEnable.postValue(!s.toString().isEmpty());
            }

            public void afterTextChanged(Editable s) {
            }
        });

        binding.tvSend.setOnClickListener(v -> {
            String message = binding.etChat.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, R.string.enter_valid_message, Toast.LENGTH_SHORT).show();
                return;
            }

            binding.etChat.setText("");

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("senderId", sessionManager.getUser().getId());
                jsonObject.put("messageType", "message");
                jsonObject.put("topic", viewModel.chatTopic);
                jsonObject.put("message", message);

                Log.d(TAG, "initView: ========" + viewModel.chatTopic);
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_CHAT, jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        binding.btnVoiceMessage.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                startVoiceRecording();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                finishVoiceRecording(true);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                finishVoiceRecording(false);
                return true;
            }
            return true;
        });


        binding.swipeRefresh.setOnRefreshListener(refreshLayout -> viewModel.getOldChat(true,sessionManager.getUser().getId()));

        final View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
            if (heightDiff > dpToPx(ChatActivity.this, 200)) {
                binding.rvChat.post(() -> binding.rvChat.scrollToPosition(0));
            }
        });

        binding.ivCallType.setOnClickListener(view -> {


            if (sessionManager.getIsUserBackgroundLive()) {
                new PopupBuilder(ChatActivity.this).showSimplePopup(getString(R.string.you_are_currently_in_audioroom_please_exit_room_and_make_call), getString(R.string.dismiss), new PopupBuilder.OnPopupClickListener() {
                    @Override
                    public void onClickContinue() {

                    }
                });
                return;
            }

            if (guestUser == null) return;

            double charge = sessionManager.getUser().getGender().equals("Male") ?
                    sessionManager.getSetting().getAudioCallChargeMale() :
                    sessionManager.getSetting().getAudioCallChargeFemale();

            if (sessionManager.getUser().getDiamond() < charge) {
                Toast.makeText(ChatActivity.this, getString(R.string.insufficient_coins), Toast.LENGTH_SHORT).show();
            } else {
                if (sessionManager.getBlockedUserIds().contains(guestUser.getId())) {
                    Toast.makeText(ChatActivity.this, getString(R.string.sorry_this_call_can_t_be_completed_right_now), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                startActivity(new Intent(ChatActivity.this, CallRequestActivity.class)
                        .putExtra(Const.USER, new Gson().toJson(guestUser))
                        .putExtra("type",  guestUser.getGender())
                        .putExtra("Calltype", "audio")
                        .putExtra("random", false));
            }


//             new BottomsheetCallType(this, new BottomsheetCallType.OnCallTypeListener() {
//                 @Override
//                 public void onAudioCall() {
//
//
//                     if (sessionManager.getIsUserBackgroundLive()) {
//                         new PopupBuilder(ChatActivity.this).showSimplePopup(getString(R.string.you_are_currently_in_audioroom_please_exit_room_and_make_call), getString(R.string.dismiss), new PopupBuilder.OnPopupClickListener() {
//                             @Override
//                             public void onClickContinue() {
//
//                             }
//                         });
//                         return;
//                     }
//
//                     if (guestUser == null) return;
//
//                     double charge = sessionManager.getUser().getGender().equals("Male") ?
//                             sessionManager.getSetting().getAudioCallChargeMale() :
//                             sessionManager.getSetting().getAudioCallChargeFemale();
//
//                     if (sessionManager.getUser().getDiamond() < charge) {
//                         Toast.makeText(ChatActivity.this, getString(R.string.insufficient_coins), Toast.LENGTH_SHORT).show();
//                     } else {
//                         if (sessionManager.getBlockedUserIds().contains(guestUser.getId())) {
//                             Toast.makeText(ChatActivity.this, getString(R.string.sorry_this_call_can_t_be_completed_right_now), Toast.LENGTH_SHORT).show();
//                             finish();
//                             return;
//                         }
//
//                         startActivity(new Intent(ChatActivity.this, CallRequestActivity.class)
//                                 .putExtra(Const.USER, new Gson().toJson(guestUser))
//                                 .putExtra("type",  guestUser.getGender())
//                                 .putExtra("Calltype", "audio")
//                                 .putExtra("random", false));
//                     }
//
//                 }
//
//
//
//                 @Override
//                 public void onVideoCall() {
//                     if (sessionManager.getIsUserBackgroundLive()) {
//                         new PopupBuilder(ChatActivity.this).showSimplePopup(getString(R.string.you_are_currently_in_audioroom_please_exit_room_and_make_call), getString(R.string.dismiss), new PopupBuilder.OnPopupClickListener() {
//                             @Override
//                             public void onClickContinue() {
//
//                             }
//                         });
//                         return;
//                     }
//
//                     if (guestUser == null) return;
//
//                     double charge = sessionManager.getUser().getGender().equals("Male") ?
//                             sessionManager.getSetting().getMaleCallCharge() :
//                             sessionManager.getSetting().getFemaleCallCharge();
//
//                     if (sessionManager.getUser().getDiamond() < charge) {
//                         Toast.makeText(ChatActivity.this, getString(R.string.insufficient_coins), Toast.LENGTH_SHORT).show();
//                     } else {
//                         if (sessionManager.getBlockedUserIds().contains(guestUser.getId())) {
//                             Toast.makeText(ChatActivity.this, getString(R.string.sorry_this_call_can_t_be_completed_right_now), Toast.LENGTH_SHORT).show();
//                             finish();
//                             return;
//                         }
//
//                         startActivity(new Intent(ChatActivity.this, CallRequestActivity.class)
//                                 .putExtra(Const.USER, new Gson().toJson(guestUser))
//                                 .putExtra("type", guestUser.getGender())
//                                 .putExtra("Calltype", "video")
//                                 .putExtra("random", false));
//                     }
//                 }
//             });
        });

    }

    private void addGiftToChatList(GiftRoot.GiftItem giftData, String senderId) {
        ChatItem ci = new ChatItem();
        ci.setSenderId(senderId);
        ci.setMessageType("chatGift");
        ci.setGiftsvgaImage(giftData.getSvgaImage());
        ci.setGiftImage(giftData.getImage());
        ci.setType(giftData.getType());
        viewModel.chatAdapter.addSingleChat(ci);
    }

    private void setupSocketHandler() {
        MySocketManager.getInstance().addChatHandler(chatHandler);
    }

    private void initListener() {
        binding.imgUser.setOnClickListener(v -> {
            if (guestUser != null) {
                startActivity(new Intent(this, GuestActivity.class).putExtra(Const.USERID, guestUser.getUserId()));
            }
        });

        viewModel.chatAdapter.setOnChatItemClickLister(new ChatAdapter.OnChatItemClickLister() {
            @Override
            public void onLongPress(ChatItem chatDummy, int position) {
                new PopupBuilder(ChatActivity.this).deletePopup(getString(R.string.message_delete_confirmation), new PopupBuilder.OnMultiButtonPopupLister() {
                    @Override
                    public void onClickContinue() {
                        viewModel.deleteChat(chatDummy, position);
                    }

                    @Override
                    public void onClickCancel() {

                    }
                });
            }

            @Override
            public void onImageClick(ChatItem chatDummy, int position, ImageView mainImage) {
                Intent intent = new Intent(ChatActivity.this, ImagePreviewActivity.class);
                intent.putExtra("preview_image", BuildConfig.BASE_URL + chatDummy.getImage());

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        ChatActivity.this,
                        mainImage,
                        ViewCompat.getTransitionName(mainImage)
                );

                ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());

            }
        });

        binding.btnAudioCall.setOnClickListener(view -> {
            if (sessionManager.getIsUserBackgroundLive()) {
                new PopupBuilder(ChatActivity.this).showSimplePopup(getString(R.string.you_are_currently_in_audioroom_please_exit_room_and_make_call), getString(R.string.dismiss), new PopupBuilder.OnPopupClickListener() {
                    @Override
                    public void onClickContinue() {

                    }
                });
                return;
            }

            if (guestUser == null) return;

            double charge = sessionManager.getUser().getGender().equals("Male") ?
                    sessionManager.getSetting().getAudioCallChargeMale() :
                    sessionManager.getSetting().getAudioCallChargeFemale();

            if (sessionManager.getUser().getDiamond() < charge) {
                Toast.makeText(this, getString(R.string.insufficient_coins), Toast.LENGTH_SHORT).show();
            } else {
                if (sessionManager.getBlockedUserIds().contains(guestUser.getId())) {
                    Toast.makeText(this, getString(R.string.sorry_this_call_can_t_be_completed_right_now), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                startActivity(new Intent(this, CallRequestActivity.class)
                        .putExtra(Const.USER, new Gson().toJson(guestUser))
                        .putExtra("type",  guestUser.getGender())
                        .putExtra("Calltype", "audio")
                        .putExtra("random", false));
            }
        });

    }

    public void onClickVideoCall(View view) {
        if (sessionManager.getIsUserBackgroundLive()) {
            new PopupBuilder(ChatActivity.this).showSimplePopup(getString(R.string.you_are_currently_in_audioroom_please_exit_room_and_make_call), getString(R.string.dismiss), new PopupBuilder.OnPopupClickListener() {
                @Override
                public void onClickContinue() {

                }
            });
            return;
        }

        if (guestUser == null) return;

        double charge = sessionManager.getUser().getGender().equals("Male") ?
                sessionManager.getSetting().getMaleCallCharge() :
                sessionManager.getSetting().getFemaleCallCharge();

        if (sessionManager.getUser().getDiamond() < charge) {
            Toast.makeText(this, getString(R.string.insufficient_coins), Toast.LENGTH_SHORT).show();
        } else {
            if (sessionManager.getBlockedUserIds().contains(guestUser.getId())) {
                Toast.makeText(this, getString(R.string.sorry_this_call_can_t_be_completed_right_now), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            startActivity(new Intent(this, CallRequestActivity.class)
                    .putExtra(Const.USER, new Gson().toJson(guestUser))
                    .putExtra("type", guestUser.getGender())
                    .putExtra("Calltype", "video")
                    .putExtra("random", false));
        }
    }


    public void onClickCamera(View view) {
        Log.d(TAG, "onClickCamara triggered");
        choosePhoto();
    }


    private void choosePhoto() {
        PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build();
        pickImageLauncher.launch(request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UCrop.REQUEST_CROP) {
            handleCropResult(data);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data);
        }
    }

    public void startCropActivity(@NotNull Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), SAMPLE_CROPPED_IMAGE_NAME + ".png"));

        UCrop.Options options = new UCrop.Options();
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.pink));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.pink));
        options.setToolbarColor(ContextCompat.getColor(this, R.color.lightBlack));
        options.setCropFrameColor(ContextCompat.getColor(this, R.color.colorBlack));
        options.setDimmedLayerColor(ContextCompat.getColor(this, R.color.colorBlack));
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);

        Intent cropIntent = UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .getIntent(this);

        cropImageLauncher.launch(cropIntent);
    }

    private void showAndUploadImage(Uri uri) {
        binding.lytImage.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(uri)
                .signature(new ObjectKey(System.currentTimeMillis()))
                .placeholder(R.drawable.ic_user_place)
                .into(binding.imageview);

        binding.rvChat.post(() -> binding.rvChat.scrollToPosition(0));

        uploadImage();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleCropResult(@NotNull Intent result) {
        Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {
            selectedImage = resultUri;
            Glide.with(this)
                    .load(selectedImage)
                    .placeholder(R.drawable.ic_user_place)
                    .error(R.drawable.ic_user_place)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.imageview);
            picturePath = getRealPathFromURI(selectedImage);
            uploadImage();
        } else {
            Toast.makeText(this, R.string.toast_cannot_retrieve_cropped_image, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCropError(@NotNull Intent result) {
        Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.toast_unexpected_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage() {
        if (picturePath == null || picturePath.trim().isEmpty()) {
            Log.i(TAG, "uploadImage: No image selected");
            return;
        }

        File file = new File(picturePath);
        if (!file.exists()) {
            Toast.makeText(this, getString(R.string.image_file_does_not_exist), Toast.LENGTH_SHORT).show();
            return;
        }

        binding.lytImage.setVisibility(View.VISIBLE);

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("messageType", RequestBody.create(MediaType.parse("text/plain"), "image"));
        map.put("topic", RequestBody.create(MediaType.parse("text/plain"), viewModel.chatTopic));
        map.put("senderId", RequestBody.create(MediaType.parse("text/plain"), sessionManager.getUser().getId()));

        RetrofitBuilder.create().uploadChatImage(map, body).enqueue(new Callback<>() {
            public void onResponse(Call<UploadImageRoot> call, Response<UploadImageRoot> response) {
                binding.lytImage.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    ChatItem chat = response.body().getChat();

                    try {
                        emitChatMedia(chat, "image");
                        Log.d(TAG, "Image uploaded and emitted via socket.");
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON exception in uploadImage: " + e.getMessage(), e);
                    }
                } else {
                    Log.w(TAG, "Image upload failed: " + (response.body() != null ? response.body().getMessage() : "No response body"));
                    Toast.makeText(ChatActivity.this, getString(R.string.failed_to_upload_image), Toast.LENGTH_SHORT).show();
                }
            }

            public void onFailure(Call<UploadImageRoot> call, Throwable t) {
                binding.lytImage.setVisibility(View.GONE);
                Log.e(TAG, "uploadImage API call failed", t);
                Toast.makeText(ChatActivity.this, getString(R.string.failed_to_upload_image_please_try_again), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startVoiceRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 4407);
            Toast.makeText(this, "Microphone permission is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isRecordingVoice) return;

        try {
            voiceRecordFile = new File(getCacheDir(), "voice_message_" + System.currentTimeMillis() + ".m4a");
            voiceRecorder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? new MediaRecorder(this) : new MediaRecorder();
            voiceRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            voiceRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            voiceRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            voiceRecorder.setAudioEncodingBitRate(128000);
            voiceRecorder.setAudioSamplingRate(44100);
            voiceRecorder.setOutputFile(voiceRecordFile.getAbsolutePath());
            voiceRecorder.prepare();
            voiceRecorder.start();
            voiceRecordStartedAt = System.currentTimeMillis();
            isRecordingVoice = true;
            Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Unable to start voice recording", e);
            releaseVoiceRecorder();
            Toast.makeText(this, "Unable to record voice", Toast.LENGTH_SHORT).show();
        }
    }

    private void finishVoiceRecording(boolean send) {
        if (!isRecordingVoice) return;

        long duration = System.currentTimeMillis() - voiceRecordStartedAt;
        File recordedFile = voiceRecordFile;
        try {
            if (voiceRecorder != null) {
                voiceRecorder.stop();
            }
        } catch (Exception e) {
            Log.w(TAG, "Voice recording stopped without valid audio", e);
            send = false;
        } finally {
            releaseVoiceRecorder();
        }

        if (!send || duration < 700 || recordedFile == null || !recordedFile.exists() || recordedFile.length() <= 0) {
            if (recordedFile != null && recordedFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                recordedFile.delete();
            }
            if (duration < 700) Toast.makeText(this, "Hold to record", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadVoiceMessage(recordedFile);
    }

    private void releaseVoiceRecorder() {
        if (voiceRecorder != null) {
            try {
                voiceRecorder.release();
            } catch (Exception ignored) {
            }
        }
        voiceRecorder = null;
        isRecordingVoice = false;
        voiceRecordStartedAt = 0L;
    }

    private void uploadVoiceMessage(File file) {
        if (file == null || !file.exists()) return;

        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/mp4"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("audio", file.getName(), requestFile);

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("messageType", RequestBody.create(MediaType.parse("text/plain"), "audio"));
        map.put("topic", RequestBody.create(MediaType.parse("text/plain"), viewModel.chatTopic));
        map.put("senderId", RequestBody.create(MediaType.parse("text/plain"), sessionManager.getUser().getId()));

        RetrofitBuilder.create().uploadChatImage(map, body).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UploadImageRoot> call, Response<UploadImageRoot> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    ChatItem chat = response.body().getChat();
                    try {
                        emitChatMedia(chat, "audio");
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON exception in uploadVoiceMessage", e);
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "Failed to send voice message", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UploadImageRoot> call, Throwable t) {
                Log.e(TAG, "Voice upload failed", t);
                Toast.makeText(ChatActivity.this, "Failed to send voice message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void emitChatMedia(ChatItem chat, String messageType) throws JSONException {
        if (chat == null) return;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("senderId", sessionManager.getUser().getId());
        jsonObject.put("messageType", messageType);
        jsonObject.put("topic", viewModel.chatTopic);
        jsonObject.put("message", "audio".equals(messageType) ? "Voice message" : "image");
        jsonObject.put("date", chat.getDate());
        jsonObject.put("_id", chat.getId());
        jsonObject.put("messageId", chat.getId());
        if ("audio".equals(messageType)) {
            jsonObject.put("audio", chat.getAudio());
        } else {
            jsonObject.put("image", chat.getImage());
        }
        MySocketManager.getInstance().getSocket().emit(Const.EVENT_CHAT, jsonObject);
    }

    public String getRealPathFromURI(Uri contentURI) {
        if (contentURI == null) return null;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor != null) {
            try {
                int columnIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                if (columnIndex != -1 && cursor.moveToFirst()) {
                    return cursor.getString(columnIndex);
                }
            } finally {
                cursor.close();
            }
        }
        return contentURI.getPath();
    }

    public void onClickReport(View view) {
        if (guestUser == null) return;
        new BottomSheetReportOption(this, new BottomSheetReportOption.OnReportedListener() {
            public void onReported() {
                new BottomSheetReport(ChatActivity.this, guestUser.getUserId(), () -> {
                    View layout = LayoutInflater.from(getApplicationContext()).inflate(R.layout.toast_layout, findViewById(R.id.layout_custom_toast));
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                });
            }

            public void onBlocked() {
                userApiCall.blockUnblock(guestUser.getId(), new UserApiCall.OnBlockUnblockListener() {
                    public void onBlockSuccess() {
                        finish();
                    }

                    public void onUnblockSuccess() {
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRecordingVoice) {
            finishVoiceRecording(false);
        } else {
            releaseVoiceRecorder();
        }
        MySocketManager.getInstance().removeChatHandler(chatHandler);
        viewModel.chatAdapter.releaseSound();
        isOPEN = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        viewModel.getOldChat(false,sessionManager.getUser().getId());
    }

    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

}
