package com.codder.ultimate.chat.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.bottomsheets.BottomSheetReport;
import com.codder.ultimate.bottomsheets.BottomSheetReportOption;
import com.codder.ultimate.chat.adapter.ChatSuggestionAdapter;
import com.codder.ultimate.chat.adapter.FakeChatAdapter;
import com.codder.ultimate.chat.modelclass.ChatRootFake;
import com.codder.ultimate.chat.modelclass.ChatSuggestion;
import com.codder.ultimate.chat.modelclass.ChatUserListRoot;
import com.codder.ultimate.databinding.ActivityFakeChatBinding;
import com.codder.ultimate.guestuser.activity.GuestActivity;
import com.codder.ultimate.live.fragment.EmojiBottomSheetFragment;
import com.codder.ultimate.live.model.GiftRoot;
import com.codder.ultimate.live.viewModel.EmojiSheetViewModel;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.retrofit.UserApiCall;
import com.codder.ultimate.utils.SvgaCacheManager;
import com.codder.ultimate.viewModel.ViewModelFactory;
import com.google.gson.Gson;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FakeChatActivity extends BaseActivity {

    public static final String TAG = "FakeChatActivity";
    private ActivityFakeChatBinding binding;
    private  FakeChatAdapter chatAdapter;
    private SessionManager sessionManager;
    private ChatUserListRoot.ChatUserItem chatUser;
    private Uri selectedImage;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;
    private EmojiSheetViewModel giftViewModel;
    private EmojiBottomSheetFragment emojiBottomsheetFragment;
    private LinearLayoutManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fake_chat);

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
        chatAdapter = new FakeChatAdapter(this);
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
                        handleCropResult(result.getData());
                    } else if (result.getResultCode() == UCrop.RESULT_ERROR) {
                        handleCropError(result.getData());
                    }
                }
        );

        initUserFromIntent();
        setupChatUI();
        setupListeners();
        loadChatSuggestions();
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
                    binding.rvChatSuggestions.setLayoutManager(new LinearLayoutManager(FakeChatActivity.this, LinearLayoutManager.HORIZONTAL, false));
                }
            }

            @Override
            public void onFailure() {
                Log.e("ChatActivity", "Failed to load chat suggestions");
            }
        });
    }

    private void initUserFromIntent() {
        String userStr = getIntent().getStringExtra(Const.CHATROOM);
        if (userStr != null && !userStr.isEmpty()) {
            Log.d(TAG, "initUserFromIntent: ---> " + userStr);
            chatUser = new Gson().fromJson(userStr, ChatUserListRoot.ChatUserItem.class);
        } else {
            Toast.makeText(this, getString(R.string.invalid_chat_user), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // --- scrolling helpers ---
    private boolean isNearBottom() {
        // consider near-bottom if less than ~72dp from the end
        int range = binding.rvChat.computeVerticalScrollRange();
        int extent = binding.rvChat.computeVerticalScrollExtent();
        int offset = binding.rvChat.computeVerticalScrollOffset();
        int pxThreshold = (int) (72 * getResources().getDisplayMetrics().density);
        return (range - (extent + offset)) < pxThreshold;
    }

    private void scrollToBottom(boolean smooth) {
        int last = Math.max(chatAdapter.getItemCount() - 1, 0);
        if (smooth) binding.rvChat.smoothScrollToPosition(last);
        else binding.rvChat.scrollToPosition(last);
    }

    /** Append a message; scroll if user was at bottom OR forceScroll is true */
    private void appendMessage(ChatRootFake msg, boolean forceScroll) {
        final boolean wasNearBottom = isNearBottom();
        chatAdapter.addSingleMessage(msg, () -> {
            if (forceScroll || wasNearBottom) {
                binding.rvChat.post(() -> scrollToBottom(false));
            }
        });
    }

    private void setupChatUI() {
        binding.imgUser.setUserImage(chatUser.getImage(), "", 10);
        binding.tvUserName.setText(chatUser.getName());
        lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        lm.setStackFromEnd(true); // classic chat behavior; viewport hugs bottom
        binding.rvChat.setLayoutManager(lm);
        binding.rvChat.setAdapter(chatAdapter);

        chatAdapter.setOnClickListener((position, imageUrl, mainImage) -> {
            if (imageUrl == null) return;

            Intent intent = new Intent(FakeChatActivity.this, ImagePreviewActivity.class);
            intent.putExtra("preview_image", imageUrl); // << use clicked URL, not chatUser avatar

            // pass the transitionName so the target can set the same name
            String tn = ViewCompat.getTransitionName(mainImage);
            intent.putExtra("transition_name", tn);

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    FakeChatActivity.this,
                    mainImage,
                    tn
            );
            ActivityCompat.startActivity(FakeChatActivity.this, intent, options.toBundle());
        });

        // Load initial fake data and jump to bottom ONCE after layout is ready
        chatAdapter.submitList(new ArrayList<>(generateFakeChatData()), () ->
                binding.rvChat.post(() -> {
                    // small delay lets layout settle with images
                    binding.rvChat.postDelayed(() -> scrollToBottom(false), 16);
                })
        );
    }

    private void setupListeners() {
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

            double totalCoin = giftItem.getCoin() * giftItem.getCount();
            if (sessionManager.getUser().getDiamond() < totalCoin) {
                Toast.makeText(FakeChatActivity.this, getString(R.string.you_not_have_enough_diamonds_to_send_gift), Toast.LENGTH_SHORT).show();
                return;
            }

            sendGiftFakeHost(giftItem);

            // Find this gift in the cached list to read its fields
            List<GiftRoot.GiftItem> list = sessionManager.getGiftsList(giftItem.getCategory());
            GiftRoot.GiftItem full = null;
            if (list != null) for (GiftRoot.GiftItem it : list) {
                if (giftItem.getId().equals(it.getId())) {
                    Log.d(TAG, "setupListeners: ==> 11 " + giftItem.getId());
                    Log.d(TAG, "setupListeners: ==> 22 " + it.getId());
                    Log.d(TAG, "setupListeners: ==> 33 " + it.getImage());
                    Log.d(TAG, "setupListeners: ==> 44 " + it.getSvgaImage());
                    full = it;
                    break;
                }
            }
            if (full == null) return;

            // Absolute URLs
            String animAbs = BuildConfig.BASE_URL + full.getImage();             // may be .svga for type==2
            playSvga(animAbs);

            final String thumbAbs = BuildConfig.BASE_URL +
                    ((full.getType() == 2 && full.getSvgaImage() != null && !full.getSvgaImage().isEmpty())
                            ? full.getSvgaImage()                                      // static preview for SVGA
                            : full.getImage());                                        // normal image gifts

            Log.d(TAG, "setupListeners: ==> 111 " + animAbs);
            Log.d(TAG, "setupListeners: ==> 222 " + thumbAbs);
            Log.d(TAG, "setupListeners: ==> 333 " + BuildConfig.BASE_URL + full.getImage());
            Log.d(TAG, "setupListeners: ==> 444 " + BuildConfig.BASE_URL + full.getSvgaImage());

            // Show big overlay
            if (full.getType() == 2) {
                animAbs = BuildConfig.BASE_URL + full.getImage();
                playSvga(animAbs);
            } else {
                Glide.with(FakeChatActivity.this).load(animAbs).into(binding.imgGift);
                Glide.with(FakeChatActivity.this).load(RayziUtils.getImageFromNumber(giftItem.getCount())).into(binding.imgGiftCount);
                binding.tvGiftUserName.setText(sessionManager.getUser().getName() + getString(R.string.sent_a_gift));
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
                }, 4000);
            }

            // Send ONE simple payload to the list: always a static image (thumbAbs)
            appendMessage(
                    new ChatRootFake(/*flag=*/1, /*message=*/"gift_abs:" + thumbAbs, /*avatar:*/sessionManager.getUser().getImage(), sessionManager.getUser().getAvatarFrameImage()),
                    /*forceScroll=*/true
            );

            emojiBottomsheetFragment.dismiss();
        });

        binding.tvSend.setOnClickListener(v -> {
            String message = binding.etChat.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, R.string.type_message_first, Toast.LENGTH_SHORT).show();
                return;
            }
            binding.etChat.setText("");

            // do NOT force; respect user's scroll if they scrolled up
            appendMessage(new ChatRootFake(
                    1,
                    message,
                    sessionManager.getUser().getImage(),
                    sessionManager.getUser().getAvatarFrameImage()
            ), /*forceScroll=*/false);
        });
    }

    private void sendGiftFakeHost(GiftRoot.GiftItem selectedGift) {
        Call<UserRoot> call = RetrofitBuilder.create().sendGiftFakeHost(sessionManager.getUser().getId(), selectedGift.getCoin(), "", Const.CHAT_GIFT);
        call.enqueue(new Callback<UserRoot>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && response.body().getUser() != null) {
                        sessionManager.saveUser(response.body().getUser());
                        Log.d(TAG, "onResponse: diamonds = " + String.format(Locale.US, "%.2f", sessionManager.getUser().getDiamond()));
                    }
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void onClickCamera(View view) {
        choosePhoto();
    }

    private void choosePhoto() {
        PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build();
        pickImageLauncher.launch(request);
    }

    public void startCropActivity(@NonNull Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_image_" + System.currentTimeMillis() + ".png"));

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setToolbarColor(ContextCompat.getColor(this, R.color.lightBlack));
//        options.setStatusBarColor(ContextCompat.getColor(this, R.color.lightBlack));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.white));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.pink));

        Intent cropIntent = UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .getIntent(this);

        cropImageLauncher.launch(cropIntent);
    }

    private void handleCropResult(@NonNull Intent result) {
        Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {
            selectedImage = resultUri;
            Glide.with(this).load(selectedImage).into(binding.imageview);

            appendMessage(new ChatRootFake(
                    1,
                    "image_abs:" + resultUri,
                    sessionManager.getUser().getImage(),
                    sessionManager.getUser().getAvatarFrameImage()
            ), /*forceScroll=*/true); // << force bottom for images
        } else {
            Toast.makeText(this, R.string.toast_cannot_retrieve_cropped_image, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCropError(@NonNull Intent result) {
        Throwable error = UCrop.getError(result);
        Toast.makeText(this, error != null ? error.getMessage() : getString(R.string.toast_unexpected_error), Toast.LENGTH_LONG).show();
    }

    public void onClickVideoCall(View view) {
        Intent intent = new Intent(this, FakeCallRequestActivity.class);
        intent.putExtra(Const.CHATROOM, new Gson().toJson(chatUser));
        startActivity(intent);
    }


    public void onClickReport(View view) {
        if (chatUser == null) return;

        new BottomSheetReportOption(this, new BottomSheetReportOption.OnReportedListener() {
            @Override
            public void onReported() {
                new BottomSheetReport(FakeChatActivity.this, chatUser.getUserId(), () -> {
                    View layout = LayoutInflater.from(FakeChatActivity.this)
                            .inflate(R.layout.toast_layout, findViewById(R.id.layout_custom_toast));
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                });
            }

            @Override
            public void onBlocked() {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private List<ChatRootFake> generateFakeChatData() {
        String[] senderMessages = getResources().getStringArray(R.array.fake_chat_sender);
        String[] receiverMessages = getResources().getStringArray(R.array.fake_chat_receiver);
        Random random = new Random();

        List<ChatRootFake> fakeMessages = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            String msg = (i % 2 == 0)
                    ? senderMessages[random.nextInt(senderMessages.length)]
                    : receiverMessages[random.nextInt(receiverMessages.length)];

            fakeMessages.add(new ChatRootFake(
                    i % 2 == 0 ? 1 : 2,
                    msg,
                    i % 2 == 0 ? sessionManager.getUser().getImage() : chatUser.getImage(),
                    sessionManager.getUser().getAvatarFrameImage()
            ));
        }
        return fakeMessages;
    }

    private void playSvga(@NonNull String svgaAbsUrl) {
        if (svgaAbsUrl.trim().isEmpty()) return;

        binding.svgaImage.clear();
        binding.svgaImage.setLoops(1);
        binding.svgaImage.setVisibility(View.VISIBLE);
        binding.svgaImage.bringToFront();
        binding.svgaImage.setElevation(20f);

        // 1) Try local cache
        SvgaCacheManager.decodeSvgaFromCache(this, svgaAbsUrl, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(@NonNull SVGAVideoEntity video) {
                Log.d(TAG, "SVGA loaded from cache: " + svgaAbsUrl);
                startSvga(video);
            }

            @Override
            public void onError() {
                // 2) Fallback to network
                SVGAParser parser = new SVGAParser(FakeChatActivity.this);
                try {
                    parser.decodeFromURL(new URL(svgaAbsUrl), new SVGAParser.ParseCompletion() {
                        @Override
                        public void onComplete(@NonNull SVGAVideoEntity video) {
                            Log.d(TAG, "SVGA loaded from network: " + svgaAbsUrl);
                            startSvga(video);
                        }

                        @Override
                        public void onError() {
                            Log.e(TAG, "SVGA decode error (network): " + svgaAbsUrl);
                            binding.svgaImage.setVisibility(GONE);
                        }
                    }, null);
                } catch (Exception e) {
                    Log.e(TAG, "SVGA URL error", e);
                    binding.svgaImage.setVisibility(GONE);
                }
            }
        });
    }

    private void startSvga(@NonNull SVGAVideoEntity video) {
        binding.svgaImage.setImageDrawable(new SVGADrawable(video));
        binding.svgaImage.startAnimation();
        long dur = (long) (video.getFrames() * 1000f / video.getFPS());
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.svgaImage.stopAnimation();
            binding.svgaImage.clear();
            binding.svgaImage.setVisibility(GONE);
        }, Math.max(dur, 1500));
    }


}
