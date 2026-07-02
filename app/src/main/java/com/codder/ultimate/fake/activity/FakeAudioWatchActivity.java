package com.codder.ultimate.fake.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.SvgaEntryManager;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.agora.AgoraBaseActivity;
import com.codder.ultimate.databinding.ActivityFakeAudioWatchBinding;
import com.codder.ultimate.databinding.ItemSeatBinding;
import com.codder.ultimate.fake.adapter.FakeSeatAdapter;
import com.codder.ultimate.fake.utils.FakeUserProfileBottomSheet;
import com.codder.ultimate.guestuser.activity.GuestActivity;
import com.codder.ultimate.live.activity.HostLiveAudioActivity;
import com.codder.ultimate.live.activity.WatchAudioLiveActivity;
import com.codder.ultimate.live.adapter.LiveViewUserAdapter;
import com.codder.ultimate.live.bottomsheet.BottomSheetGameCasino;
import com.codder.ultimate.live.bottomsheet.BottomSheetGameList;
import com.codder.ultimate.live.bottomsheet.BottomSheetGameTeenPatti;
import com.codder.ultimate.live.bottomsheet.BottomSheetReactions;
import com.codder.ultimate.live.bottomsheet.DialogGame;
import com.codder.ultimate.live.fragment.EmojiBottomSheetFragment;
import com.codder.ultimate.live.model.GiftCategoryRoot;
import com.codder.ultimate.live.model.GiftRoot;
import com.codder.ultimate.live.model.LiveStramComment;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.live.model.ReactionsViewModel;
import com.codder.ultimate.live.model.SeatItem;
import com.codder.ultimate.live.utils.FloatingButtonService;
import com.codder.ultimate.live.utils.UserSelectableClass;
import com.codder.ultimate.live.viewModel.EmojiSheetViewModel;
import com.codder.ultimate.live.viewModel.HostLiveViewModel;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.SeatModalClass;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.socket.MySocketManager;
import com.codder.ultimate.utils.Demo_contents;
import com.codder.ultimate.viewModel.ViewModelFactory;
import com.google.gson.Gson;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import io.agora.rtc2.IRtcEngineEventHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FakeAudioWatchActivity extends BaseActivity {

    private static final String TAG = "FakeAudioWatchActivity";
    private ActivityFakeAudioWatchBinding binding;
    private PkAudioLiveUserRoot.UsersItem host;
    private EmojiSheetViewModel giftViewModel;
    private HostLiveViewModel viewModel;
    private EmojiBottomSheetFragment emojiBottomsheetFragment;
    private BottomSheetReactions bottomSheetReactions;
    private final FakeSeatAdapter seatAdapter = new FakeSeatAdapter();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler viewHandler = new Handler(Looper.getMainLooper());

    private final Handler seatAnimationHandler = new Handler(Looper.getMainLooper());
    private final Random seatRandom = new Random();

    private final Handler svgaHandler = new Handler(Looper.getMainLooper());

    private LiveViewUserAdapter adapter;

    private final Handler commentHandler = new Handler(Looper.getMainLooper());
    private final Random commentRandom = new Random();

    private boolean firstLoadDone = false;
    SessionManager sessionManager;
    private final List<GiftRoot.GiftItem> allGiftList = new ArrayList<>();

    private boolean isUserAtTop = true;
    private Runnable svgaNextRunnable;
    private boolean isEntrySvgaPlaying = false;
    private int entrySvgaIndex = -1;
    private boolean isEntryLoopRunning = false;

    private int currentSvgaPlayToken = 0;
    private Runnable svgaSafetyRunnable;

    private final Handler fakeBannerHandler = new Handler(Looper.getMainLooper());
    private int fakeBannerStep = 0; // 0=gift, 1=game, 2=luckygift, cycle repeat
    private final Random fakeGiftRandom = new Random();

    private void startSeatAnimations() {
        seatAnimationHandler.postDelayed(seatAnimationRunnable, getRandomDelay());
    }

    private final Runnable seatAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            List<SeatModalClass> currentList = seatAdapter.getCurrentList();

            // Filter only seats with image (occupied)
            List<Integer> occupiedPositions = new ArrayList<>();
            for (int i = 0; i < currentList.size(); i++) {
                if (currentList.get(i).isReserved()) {
                    occupiedPositions.add(i);
                }
            }

            if (!occupiedPositions.isEmpty()) {
                int randomIndex = seatRandom.nextInt(occupiedPositions.size());
                int selectedPosition = occupiedPositions.get(randomIndex);

                RecyclerView.ViewHolder holder = binding.rvSeat.findViewHolderForAdapterPosition(selectedPosition);
                if (holder instanceof FakeSeatAdapter.SeatViewHolder) {
                    ItemSeatBinding seatBinding = ((FakeSeatAdapter.SeatViewHolder) holder).binding;

                    seatBinding.animationView1.setVisibility(VISIBLE);
                    seatBinding.animationView1.playAnimation();

                    seatAnimationHandler.postDelayed(() -> {
                        seatBinding.animationView1.cancelAnimation();
                        seatBinding.animationView1.setVisibility(GONE);
                    }, 2000); // Hide after 2 seconds
                }
            }

            // Schedule the next run
            seatAnimationHandler.postDelayed(this, getRandomDelay());
        }
    };

    private long getRandomDelay() {
        return (seatRandom.nextInt(3) + 3) * 1000L; // 3–5 seconds
    }

    @Override
    protected void onStart() {
        super.onStart();
        resetEntrySvgaSystem();
    }

    @Override
    protected void onStop() {
        super.onStop();
        resetEntrySvgaSystem();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fake_audio_watch);
        sessionManager = new SessionManager(this);
        handleIntentData();
        ReactionsViewModel reactionsViewModel = ViewModelProviders.of(this, new ViewModelFactory(new ReactionsViewModel()).createFor()).get(ReactionsViewModel.class);
        bottomSheetReactions = new BottomSheetReactions(this);
        reactionsViewModel.loadReactions(bottomSheetReactions::loadData);
    }

    private void handleIntentData() {
        String dataStr = getIntent().getStringExtra(Const.DATA);
        if (dataStr != null && !dataStr.isEmpty()) {
            host = new Gson().fromJson(dataStr, PkAudioLiveUserRoot.UsersItem.class);
            initView();
            initListeners();
        } else {
            Toast.makeText(this, getString(R.string.invalid_host_data), Toast.LENGTH_SHORT).show();
            finish();
        }

        if (isMyServiceRunning()) {
            stopService(new Intent(FakeAudioWatchActivity.this, FloatingButtonService.class));
        }

        setupRecyclerView();
        loadDummyData();
    }

    private void setupRecyclerView() {
        adapter = new LiveViewUserAdapter();
        binding.rvViewUsers.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvViewUsers.setAdapter(adapter);

        adapter.setOnLiveUserAdapterClickListener(user -> {
            // Handle user click
            String name = user.optString("name", "Unknown");
            Log.d("MainActivity", "Clicked on user: " + name);
        });
    }

    private void loadDummyData() {
        List<JSONObject> dummyUsers = new ArrayList<>();

        // Use Demo_contents.getUsers(true) to fetch shuffled user list
        List<UserRoot.User> userList = Demo_contents.getUsers(this,true);

        for (UserRoot.User user : userList) {
            dummyUsers.add(convertUserToJson(user));
        }

        adapter.submitList(dummyUsers);
    }

    private JSONObject convertUserToJson(UserRoot.User user) {
        JSONObject json = new JSONObject();
        try {
            json.put("userId", user.getId());
            json.put("name", user.getName());
            json.put("image", user.getImage());
            json.put("avatarFrameImage", ""); // Add frame if available
            json.put("isAdd", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }


    @SuppressLint("SetTextI18n")
    private void initView() {
        viewModel = new ViewModelProvider(this, new ViewModelFactory(new HostLiveViewModel()).createFor()).get(HostLiveViewModel.class);
        giftViewModel = new ViewModelProvider(this, new ViewModelFactory(new EmojiSheetViewModel()).createFor()).get(EmojiSheetViewModel.class);

        binding.setViewmodel(viewModel);
        viewModel.initLister();
        giftViewModel.initEmojiSheet(this);
        giftViewModel.getGiftCategory();

        viewModel.liveStramCommentAdapter.addSingleComment(null);

        emojiBottomsheetFragment = new EmojiBottomSheetFragment(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        layoutManager.setStackFromEnd(true);
        binding.rvComments.setLayoutManager(layoutManager);
        binding.rvComments.setHasFixedSize(true);

        binding.rvComments.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                    isUserAtTop = firstVisibleItem <= 1; // Near top means we can auto scroll
                }
            }
        });
        addStartupCommentsOnce();
// Turn off change animations to prevent flicker
        if (binding.rvComments.getItemAnimator() instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) binding.rvComments.getItemAnimator()).setSupportsChangeAnimations(false);
        }
        binding.rvComments.smoothScrollToPosition(0);
        viewModel.liveStramCommentAdapter.addSingleComment(new LiveStramComment("", sessionManager.getUser(), true, null, "", "comment", ""));
        viewHandler.postDelayed(viewCountRunnable, 7000);

        if (!isFinishing() && host != null) {
            binding.mainHostProfileImage.setUserImage(host.getImage(), host.getAvatarFrameImage(), 30);
            binding.mainHostnameCount.setText(host.getName());
            binding.tvName.setText(host.getRoomName());
            Glide.with(this).load(host.getRoomImage()).into(binding.imgProfile);
            RayziUtils.marqueeText(binding.tvName);

            String uniqueId = (host.getUniqueId() != null && !host.getUniqueId().isEmpty())
                    ? host.getUniqueId()
                    : String.valueOf(new Random().nextInt(900000) + 100000);

            binding.tvUniqueId.setText(getString(R.string.id_) + uniqueId);
            binding.tvViewUserCount.setText(String.valueOf(host.getView()));

            Random random = new Random();
            int randomRcoin = random.nextInt(901) + 100; // Generates a number from 100 to 1000
            binding.tvRcoins.setText(String.valueOf(randomRcoin));

        }

        binding.rvSeat.setAdapter(seatAdapter);


        binding.btnReaction.setOnClickListener(view -> bottomSheetReactions.show());

        seatAdapter.setOnSeatClickListener((seatModalClass, position, binding1) -> {

            // 1. Ignore locked seats
            if (seatModalClass.getImage().equals(drawableToUri(R.drawable.audio_lock))) {
                Toast.makeText(this, "Seat is locked by host", Toast.LENGTH_SHORT).show();
                return;
            }

            UserRoot.User me = sessionManager.getUser();
            List<SeatModalClass> current = new ArrayList<>(seatAdapter.getCurrentList());

            // 2. Find if user already seated
            Integer currentSeat = null;
            for (int i = 0; i < current.size(); i++) {
                if (current.get(i).isReserved() &&
                        me.getName().equals(current.get(i).getName())) {
                    currentSeat = i;
                    break;
                }
            }

            boolean tappedEmpty = !seatModalClass.isReserved();

            // 3. CASE A: User not seated yet & taps empty → take seat
            if (currentSeat == null && tappedEmpty) {
                current.set(position, new SeatModalClass(
                        String.valueOf(position),
                        me.getImage(),
                        me.getName(),
                        true
                ));
                seatAdapter.submitList(current);
                return;
            }

            // 4. CASE B: User already seated & taps another empty seat → switch seat
            if (currentSeat != null && tappedEmpty) {
                // remove from old seat
                current.set(currentSeat, new SeatModalClass(
                        String.valueOf(currentSeat),
                        drawableToUri(R.drawable.audio_seat),
                        "",
                        false
                ));

                // place user in new seat
                current.set(position, new SeatModalClass(
                        String.valueOf(position),
                        me.getImage(),
                        me.getName(),
                        true
                ));

                seatAdapter.submitList(current);
                return;
            }

            // 5. CASE C: Seat is occupied by someone else → show profile sheet
            if (seatModalClass.isReserved() && !me.getName().equals(seatModalClass.getName())) {

                FakeUserProfileBottomSheet sheet = new FakeUserProfileBottomSheet(this);
                GuestProfileRoot.User fake = buildFakeUserFromSeat(seatModalClass);
                String liveId = (host != null) ? host.getLiveStreamingId() : "";

                sheet.setOnUserTapListener(user -> {
                    Toast.makeText(this, getString(R.string.blocked_successfully), Toast.LENGTH_SHORT).show();
                });

                sheet.show(false, fake, liveId, true);
            }
        });


        binding.svgGiftImage.setOnClickListener(v -> {
            if (binding.svgGiftImage.getVisibility() != VISIBLE) return;

            binding.svgGiftImage.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(700)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .withEndAction(() -> {
                        safeFinishEntrySvga();
                        binding.svgGiftImage.setScaleX(1f);
                        binding.svgGiftImage.setScaleY(1f);
                        binding.svgGiftImage.setAlpha(1f);
                    })
                    .start();
        });

        setupSeatData();
        entryEffectShow();
        startSeatAnimations();
        startFakeBannerLoop();
        binding.getRoot().post(() -> startEntrySvgaLoop());
    }

    // Turn a drawable resource into an android.resource:// URI (Glide can load these)
    private static Uri toResourceUri(Context ctx, @DrawableRes int resId) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + ctx.getResources().getResourcePackageName(resId) + "/"
                + ctx.getResources().getResourceTypeName(resId) + "/"
                + ctx.getResources().getResourceEntryName(resId));
    }

    private final Random fakeRandom = new Random();

    private GuestProfileRoot.User buildFakeUserFromSeat(SeatModalClass seat) {
        // Create a JSON object with all fake data
        JSONObject obj = new JSONObject();
        try {
            obj.put("userId", "fake_" + seat.getSeat_id());
            obj.put("uniqueId", String.valueOf(100000 + fakeRandom.nextInt(900000)));
            obj.put("name", (seat.getName() == null || seat.getName().isEmpty()) ? "Guest" : seat.getName());
            obj.put("age", 18 + fakeRandom.nextInt(15)); // 18–32
            obj.put("country", new String[]{"India", "USA", "UAE", "UK", "Philippines"}[fakeRandom.nextInt(5)]);
            obj.put("follow", false);
            obj.put("post", fakeRandom.nextInt(200));
            obj.put("followers", 50 + fakeRandom.nextInt(5000));
            obj.put("video", fakeRandom.nextInt(100));
            obj.put("link", fakeRandom.nextInt(100));

            // badges
            obj.put("host", false);
            obj.put("agency", false);
            obj.put("VIP", fakeRandom.nextBoolean());
            obj.put("coinSeller", false);

            // avatar + frame
//            Uri img = toResourceUri(this, Integer.parseInt(seat.getImage()));
//            obj.put("image", img.toString());
            obj.put("image", seat.getImage());
            obj.put("avatarFrameImage", "");

            // optional level
            JSONObject level = new JSONObject();
            level.put("name", "Lv. " + (1 + fakeRandom.nextInt(50)));
            obj.put("level", level);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Convert JSON → GuestProfileRoot.User via Gson (no setters needed)
        return new Gson().fromJson(obj.toString(), GuestProfileRoot.User.class);
    }


    private void setupSeatData() {
        List<List<SeatModalClass>> seatLists = List.of(getSeatList1(), getSeatList2(), getSeatList3());
        seatAdapter.submitList(new ArrayList<>(seatLists.get(new Random().nextInt(seatLists.size()))));
        seatAdapter.assignFramedSeats();
    }

    private List<SeatModalClass> getSeatList1() {
        return List.of(
                new SeatModalClass("0", drawableToUri(R.drawable.audio_seat), "", false),
                new SeatModalClass("1", drawableToUri(R.drawable.img1), getString(R.string.miya), true),
                new SeatModalClass("2", drawableToUri(R.drawable.img2), getString(R.string.riya), true),
                new SeatModalClass("3", drawableToUri(R.drawable.audio_lock), "", false),
                new SeatModalClass("4", drawableToUri(R.drawable.img5), getString(R.string.luliya), true),
                new SeatModalClass("5", drawableToUri(R.drawable.audio_seat), "", false),
                new SeatModalClass("6", drawableToUri(R.drawable.img7), getString(R.string.rehan), true),
                new SeatModalClass("7", drawableToUri(R.drawable.audio_seat), "", false)
        );
    }

    private List<SeatModalClass> getSeatList2() {
        return List.of(
                new SeatModalClass("0", drawableToUri(R.drawable.audio_seat), "", false),
                new SeatModalClass("1", drawableToUri(R.drawable.img1), getString(R.string.susmita), true),
                new SeatModalClass("2", drawableToUri(R.drawable.audio_seat), "", false),
                new SeatModalClass("3", drawableToUri(R.drawable.img2), getString(R.string.rahi), true),
                new SeatModalClass("4", drawableToUri(R.drawable.audio_seat), "", false),
                new SeatModalClass("5", drawableToUri(R.drawable.img5), getString(R.string.nirma), true),
                new SeatModalClass("6", drawableToUri(R.drawable.audio_lock), "", false),
                new SeatModalClass("7", drawableToUri(R.drawable.img7), getString(R.string.jashvi), true)
        );
    }

    private List<SeatModalClass> getSeatList3() {
        return List.of(
                new SeatModalClass("0", drawableToUri(R.drawable.audio_seat), "", false),
                new SeatModalClass("1", drawableToUri(R.drawable.audio_lock), "", false),
                new SeatModalClass("2", drawableToUri(R.drawable.img2), getString(R.string.hely), true),
                new SeatModalClass("3", drawableToUri(R.drawable.img1), getString(R.string.shruti), true),
                new SeatModalClass("4", drawableToUri(R.drawable.img5), getString(R.string.dhruvi), true),
                new SeatModalClass("5", drawableToUri(R.drawable.img7), getString(R.string.mamta), true),
                new SeatModalClass("6", drawableToUri(R.drawable.audio_seat), "", false),
                new SeatModalClass("7", drawableToUri(R.drawable.img10), getString(R.string.zeel), true)
        );
    }

    private void entryEffectShow() {
        var joinEffect = sessionManager.getUser().getLiveJoinSvga();
        if (joinEffect == null || joinEffect.getImage().isEmpty()) return;

        binding.layEntry.setVisibility(VISIBLE);
        SVGAParser parser = new SVGAParser(this);
        try {
            parser.decodeFromURL(new URL(BuildConfig.BASE_URL + joinEffect.getImage()), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity entity) {
                    SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
                    dynamicEntity.setDynamicImage(BuildConfig.BASE_URL + joinEffect.getImage(), "99");
                    binding.svgImage.setImageDrawable(new SVGADrawable(entity, dynamicEntity));
                    binding.svgImage.startAnimation();

                    long duration = entity.getFrames() * 1000L / entity.getFPS();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        binding.svgImage.setVisibility(GONE);
                        binding.layEntry.setVisibility(GONE);
                        binding.svgImage.clear();
                    }, duration);
                }

                @Override
                public void onError() {
                    Log.e(TAG, "SVGA animation load failed.");
                }
            }, null);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        binding.userName.setText(sessionManager.getUser().getName());
        Glide.with(this).load(sessionManager.getUser().getImage()).circleCrop().into(binding.userImage);
        Glide.with(this)
                .load(sessionManager.getUser().getAvatarFrameImage())
                .into(binding.avatarFrameImage);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        animation.setFillAfter(true);
        binding.nameLyt.startAnimation(animation);
    }


    private final Runnable fakeCommentRunnable = new Runnable() {
        @Override
        public void run() {
            // Get 1 random comment
            LiveStramComment base = Demo_contents.getLiveStreamComment(FakeAudioWatchActivity.this)
                    .get(new Random().nextInt(Demo_contents.getLiveStreamComment(FakeAudioWatchActivity.this).size()));
            UserRoot.User randomUser = Demo_contents.getUsers(FakeAudioWatchActivity.this,true)
                    .get(new Random().nextInt(Demo_contents.getUsers(FakeAudioWatchActivity.this,true).size()));

            LiveStramComment comment = new LiveStramComment(
                    base.getComment(),
                    randomUser,
                    false,
                    host != null ? host.getLiveStreamingId() : null,
                    "",
                    "comment",
                    ""
            );

            // Add new comment
            viewModel.liveStramCommentAdapter.addSingleComment(comment);

            if (isUserAtTop) {
                binding.rvComments.smoothScrollToPosition(0);
            }

            // Schedule next comment in 3 sec
            handler.postDelayed(this, 3000);
        }
    };



    private final Runnable viewCountRunnable = new Runnable() {
        @Override
        public void run() {
            int count = new Random().nextInt(46) + 5;
            binding.tvViewUserCount.setText(String.valueOf(count));
            viewHandler.postDelayed(this, 7000);
        }
    };

    private void initListeners() {

        binding.ivShare.setOnClickListener(v -> {
            binding.ivShare.setEnabled(false);

            String deepLink = BuildConfig.BASE_URL + "open?type=FAKE_AUDIO_LIVE";

            Log.d(TAG, "onShareClick: ==" + deepLink);

            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, deepLink);
                startActivity(Intent.createChooser(shareIntent, "Share"));
            } catch (Exception e) {
                Log.e(TAG, "Share error: " + e.getMessage());
            }

        });


        binding.lytHost.setOnClickListener(v -> {
            Intent intent = new Intent(this, GuestActivity.class);
            intent.putExtra(Const.USERID, host.getId());
            startActivity(intent);
        });

        binding.imgUser1.setOnClickListener(v -> {
            Intent intent = new Intent(FakeAudioWatchActivity.this, GuestActivity.class);
            intent.putExtra(Const.USERID, host.getId());
            startActivity(intent);
        });

        binding.btnClose.setOnClickListener(v -> showExitConfirmation());

        binding.btnMute.setOnClickListener(v -> {
            viewModel.isMuted = !viewModel.isMuted;
            binding.btnMute.setImageDrawable(ContextCompat.getDrawable(this,
                    viewModel.isMuted ? R.drawable.ic_mute : R.drawable.ic_unmute));
        });

        binding.btnSend.setOnClickListener(v -> {
            String comment = binding.etComment.getText().toString();
            if (!comment.isEmpty()) {
                LiveStramComment liveStramComment = new LiveStramComment(comment, sessionManager.getUser(), false, host.getLiveStreamingId(), "", "comment", "");
                viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment);
                binding.rvComments.scrollToPosition(0);
                binding.etComment.setText("");
            }
        });

//        binding.btnGift.setOnClickListener(v -> {
//            if (!emojiBottomsheetFragment.isAdded()) {
//                if (giftViewModel.users.isEmpty()) {
//
//                    // Find first reserved seat
//                    List<SeatModalClass> seats = seatAdapter.getCurrentList();
//                    String reservedName = "Guest"; // fallback
//                    String reservedImage = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=500&auto=format&fit=crop&q=60";
//
//                    for (SeatModalClass seat : seats) {
//                        if (seat.isReserved()) {
//                            reservedName = seat.getName();
//                            reservedImage = String.valueOf(seat.getImage());
//                            break; // take the first reserved seat
//                        }
//                    }
//
//                    giftViewModel.users.add(new UserSelectableClass(
//                            new PkAudioLiveUserRoot.UsersItem.SeatItem(
//                                    reservedImage, host.getCountry(), true,
//                                    reservedName, false, 900001, 0, true,
//                                    "10001", -1, false, "live_10001")));
//                }
//
//                emojiBottomsheetFragment.show(getSupportFragmentManager(), "emojifragfmetn");
//            }
//        });

        binding.btnGift.setOnClickListener(v -> {

            if (!emojiBottomsheetFragment.isAdded()) {

                if (giftViewModel.users.isEmpty()) {

                    List<SeatModalClass> seats = seatAdapter.getCurrentList();

                    for (SeatModalClass seat : seats) {

                        if (seat.isReserved()) {

                            String name = seat.getName();
                            String image = String.valueOf(seat.getImage());

                            giftViewModel.users.add(
                                    new UserSelectableClass(
                                            new PkAudioLiveUserRoot.UsersItem.SeatItem(
                                                    image,
                                                    host.getCountry(),
                                                    true,
                                                    name,
                                                    false,
                                                    900001,
                                                    0,
                                                    true,
                                                    "10001",
                                                    -1,
                                                    false,
                                                    "live_10001"
                                            )
                                    )
                            );
                        }
                    }
                }

                emojiBottomsheetFragment.show(getSupportFragmentManager(), "emojifragfmetn");
            }
        });



        binding.imgGame.setOnClickListener(v -> {
            new BottomSheetGameList(this, (gameItem, position) -> {
                if (position == 0) {
                    new BottomSheetGameCasino(this, gameItem.getLink(), () -> {
                        MySocketManager.getInstance().getSocket().emit(Const.USER_COIN_UPDATE, sessionManager.getUser().getId());
                        Log.d(TAG, "onDismiss: couns:..." + sessionManager.getUser().getDiamond());
                    });
                } else if (position == 1) {
                    new DialogGame(this, gameItem.getLink(), () -> MySocketManager.getInstance().getSocket().emit(Const.USER_COIN_UPDATE, sessionManager.getUser().getId()));
                } else {
                    new BottomSheetGameTeenPatti(this, gameItem.getLink(), () -> MySocketManager.getInstance().getSocket().emit(Const.USER_COIN_UPDATE, sessionManager.getUser().getId()));
                }
            });
        });

        giftViewModel.finalGift.observe(this, giftItem -> {
            if (giftItem != null) {
                double totalCoin = giftItem.getCoin() * giftItem.getCount();
                if (sessionManager.getUser().getDiamond() < totalCoin) {
                    Toast.makeText(FakeAudioWatchActivity.this, getString(R.string.you_not_have_enough_diamonds_to_send_gift), Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "initListener: giftViewModel.finelGift");
                sendGiftFakeHost(giftItem);

                if (!giftViewModel.userListAdapter.getCurrentList().isEmpty()) {

                    List<String> selectedUsers = giftViewModel.userListAdapter.getCurrentList().stream()
                            .filter(UserSelectableClass::isSelected)
                            .map(user -> user.getSeatItem().getUserId())
                            .collect(Collectors.toList());

                    if (selectedUsers.isEmpty()) {
                        Toast.makeText(this, getString(R.string.select_at_least_one_user), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String finalGiftLink = null;
                    List<GiftRoot.GiftItem> giftItemList = sessionManager.getGiftsList(giftItem.getCategory());
                    for (int i = 0; i < giftItemList.size(); i++) {
                        if (giftItem.getId().equals(giftItemList.get(i).getId())) {
                            finalGiftLink = BuildConfig.BASE_URL + giftItemList.get(i).getImage();
                        }
                    }

                    if (giftItem.getType() == 2) {
                        binding.svgaImage.setVisibility(VISIBLE);
                        SVGAImageView imageView = binding.svgaImage;
                        SVGAParser parser = new SVGAParser(FakeAudioWatchActivity.this);
                        try {
                            parser.decodeFromURL(new URL(finalGiftLink), new SVGAParser.ParseCompletion() {
                                @Override
                                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                                    SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                                    imageView.setImageDrawable(drawable);
                                    imageView.startAnimation();
                                    Log.d("TAG", "setData: " + giftItem.getImage());
                                    new Handler(Looper.myLooper()).postDelayed(() -> {
                                        binding.svgaImage.setVisibility(GONE);
                                        binding.svgaImage.clear();
                                    }, 5000);
                                }

                                @Override
                                public void onError() {

                                }
                            },null);

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                    } else {
                        binding.lytGift.setVisibility(VISIBLE);
                        binding.tvGiftUserName.setVisibility(VISIBLE);
                        binding.tvGiftUserName.setText(sessionManager.getUser().getName() + getString(R.string.sent_a_gift));

                        Glide.with(binding.imgGiftCount)
                                .load(RayziUtils.getImageFromNumber(giftItem.getCount()))
                                .into(binding.imgGiftCount);
                        if (!isFinishing()) {
                            assert finalGiftLink != null;
                            if (finalGiftLink.contains(".gif")) {
                                Glide.with(this).asGif().load(finalGiftLink).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.imgGift);
                            } else {
                                Glide.with(this).load(finalGiftLink).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.imgGift);
                            }
                        }
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            binding.imgGift.setImageDrawable(null);
                            binding.lytGift.setVisibility(GONE);
                            binding.tvGiftUserName.setVisibility(GONE);
                            binding.tvGiftUserName.setText("");
                        }, 4000);
                    }

                    emojiBottomsheetFragment.dismiss();
                } else {
                    Toast.makeText(this, getString(R.string.don_t_have_user_to_sent_a_gift_wait_for_user), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void sendGiftFakeHost(GiftRoot.GiftItem selectedGift) {
        Call<UserRoot> call = RetrofitBuilder.create().sendGiftFakeHost(sessionManager.getUser().getId(), (selectedGift.getCoin() * selectedGift.getCount()) * giftViewModel.users.size(), "", Const.LIVE);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && response.body().getUser() != null) {
                        sessionManager.saveUser(response.body().getUser());
                    }
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                Log.d(TAG, "onFailure: getCoin ==  " + t.getMessage());
            }
        });
    }

    private void showExitConfirmation() {
        new PopupBuilder(this).showLiveEndPopup(new PopupBuilder.OnMultiButtonPopupLister() {
            @Override
            public void onClickContinue() {
                confirmEndLive();
                sessionManager.setIsUserBackgroundLive(false);
                finish();
            }

            @Override
            public void onClickCancel() {

                if (checkOverlayDisplayPermission()) {

                    sessionManager.setIsUserBackgroundLive(true);
                    sessionManager.saveUserAudioBgModel(host);
                    sessionManager.saveBooleanValue("isUserKeep", true);
                    startService(new Intent(FakeAudioWatchActivity.this, FloatingButtonService.class).putExtra("image", host.getImage()));
                    finish();
                } else {
                    requestOverlayDisplayPermission();
                }



            }
        });
    }

    public boolean checkOverlayDisplayPermission() {
        return Settings.canDrawOverlays(this);
    }

    public void requestOverlayDisplayPermission() {

        new PopupBuilder(this).showReliteDiscardPopup(R.drawable.vector_info, getString(R.string.screen_overlay_permission_needed), getString(R.string.enable_display_over_other_apps_from_system_settings), "Open Settings", "cancel", new PopupBuilder.OnPopupClickListener() {
            @Override
            public void onClickContinue() {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, RESULT_OK);

//                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                intent.setData(Uri.parse("package:" + getPackageName()));
//                startActivity(intent);

//                Intent intent = new Intent(
//                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                        Uri.parse("package:" + getPackageName())
//                );
//                startActivity(intent);
            }
        });
    }

    private void confirmEndLive() {
        isEntryLoopRunning = false;
        isEntrySvgaPlaying = false;

        handler.removeCallbacksAndMessages(null);
        viewHandler.removeCallbacksAndMessages(null);
        seatAnimationHandler.removeCallbacksAndMessages(null);
        svgaHandler.removeCallbacksAndMessages(null);
        fakeBannerHandler.removeCallbacksAndMessages(null);
        binding.svgGiftImage.stopAnimation(true);
        binding.svgGiftImage.clear();
        binding.svgGiftImage.setCallback(null);
    }


    private void addStartupCommentsOnce() {
        List<LiveStramComment> fakeList = Demo_contents.getLiveStreamComment(FakeAudioWatchActivity.this);
        Collections.shuffle(fakeList);

        int count = 4 + new Random().nextInt(2); // 6 or 7
        List<LiveStramComment> selected = fakeList.subList(0, Math.min(count, fakeList.size()));

        List<UserRoot.User> users = Demo_contents.getUsers(FakeAudioWatchActivity.this,true);
        Random random = new Random();

        for (LiveStramComment base : selected) {
            UserRoot.User randomUser = users.get(random.nextInt(users.size()));
            LiveStramComment comment = new LiveStramComment(
                    base.getComment(),
                    randomUser,
                    false,
                    host != null ? host.getLiveStreamingId() : null,
                    "",
                    "comment",
                    ""
            );
            viewModel.liveStramCommentAdapter.addSingleComment(comment);
        }

        // Scroll to top to show newest comment
        handler.postDelayed(() -> {
            if (isUserAtTop) {
                binding.rvComments.scrollToPosition(0);
            }
        }, 100);

        // ✅ Start showing 1 comment every 3 seconds AFTER initial comments
        handler.postDelayed(fakeCommentRunnable, 3000);
    }

    private void startEntrySvgaLoop() {

        List<String> list = SvgaEntryManager.getSvgaList();

        if (list == null || list.isEmpty()) {
            Log.w("SVGA", "SVGA list empty, retrying...");
            svgaHandler.postDelayed(this::startEntrySvgaLoop, 2000);
            return;
        }

        if (isEntryLoopRunning) return;

        isEntryLoopRunning = true;
        entrySvgaIndex = -1;
        isEntrySvgaPlaying = false;

        svgaHandler.post(this::moveToNextEntrySvga);
    }




    private void moveToNextEntrySvga() {

        if (!isEntryLoopRunning || isEntrySvgaPlaying) return;

        List<String> list = SvgaEntryManager.getSvgaList();
        if (list == null || list.isEmpty()) return;

        entrySvgaIndex++;
        if (entrySvgaIndex >= list.size()) {
            entrySvgaIndex = 0; // 🔁 repeat list
        }

        playEntrySvga(list.get(entrySvgaIndex));
    }

    private void playEntrySvga(String url) {

        if (!isEntryLoopRunning || isEntrySvgaPlaying) return;

        isEntrySvgaPlaying = true;

        // 🔐 unique token for this play
        int playToken = ++currentSvgaPlayToken;

        // reset view
        binding.svgGiftImage.stopAnimation(true);
        binding.svgGiftImage.clear();
        binding.svgGiftImage.setCallback(null);
        binding.svgGiftImage.setImageDrawable(null);
        binding.svgGiftImage.setVisibility(VISIBLE);

        SVGAParser parser = new SVGAParser(this);

        try {
            URL finalUrl = new URL(url + "?v=" + System.currentTimeMillis());

            parser.decodeFromURL(finalUrl, new SVGAParser.ParseCompletion() {

                @Override
                public void onComplete(@NonNull SVGAVideoEntity entity) {

                    // 🔴 ignore stale callbacks
                    if (playToken != currentSvgaPlayToken) return;

                    SVGADrawable drawable = new SVGADrawable(entity);
                    binding.svgGiftImage.setImageDrawable(drawable);
                    binding.svgGiftImage.setLoops(1);

                    binding.svgGiftImage.setCallback(new SVGACallback() {
                        @Override
                        public void onFinished() {
                            if (playToken == currentSvgaPlayToken) {
                                safeFinishEntrySvga();
                            }
                        }

                        @Override public void onPause() {}
                        @Override public void onRepeat() {}
                        @Override public void onStep(int i, double v) {}
                    });

                    binding.svgGiftImage.startAnimation();

                    // 🛡 SAFETY TIMEOUT (CRITICAL FIX)
                    long duration =
                            (long) ((entity.getFrames() * 1000f / entity.getFPS()) + 500);

                    svgaSafetyRunnable = () -> {
                        if (playToken == currentSvgaPlayToken) {
                            safeFinishEntrySvga();
                        }
                    };

                    svgaHandler.postDelayed(svgaSafetyRunnable, duration);
                }

                @Override
                public void onError() {
                    if (playToken == currentSvgaPlayToken) {
                        safeFinishEntrySvga();
                    }
                }

            }, null);

        } catch (Exception e) {
            safeFinishEntrySvga();
        }
    }


    private void safeFinishEntrySvga() {
        currentSvgaPlayToken++;
        svgaHandler.removeCallbacks(svgaSafetyRunnable);

        binding.svgGiftImage.stopAnimation(true);
        binding.svgGiftImage.clear();
        binding.svgGiftImage.setCallback(null);
        binding.svgGiftImage.setImageDrawable(null);
        binding.svgGiftImage.setVisibility(GONE);

        // ✅ આ add કરો - scale & alpha reset
        binding.svgGiftImage.setScaleX(1f);
        binding.svgGiftImage.setScaleY(1f);
        binding.svgGiftImage.setAlpha(1f);

        isEntrySvgaPlaying = false;

        if (isEntryLoopRunning) {
            svgaHandler.postDelayed(this::moveToNextEntrySvga, 5000);
        }
    }

//    private void safeFinishEntrySvga() {
//
//        currentSvgaPlayToken++; // 🛑 invalidate all old callbacks
//
//        svgaHandler.removeCallbacks(svgaSafetyRunnable);
//
//        binding.svgGiftImage.stopAnimation(true);
//        binding.svgGiftImage.clear();
//        binding.svgGiftImage.setCallback(null);
//        binding.svgGiftImage.setImageDrawable(null);
//        binding.svgGiftImage.setVisibility(GONE);
//
//        isEntrySvgaPlaying = false;
//
//        if (isEntryLoopRunning) {
//            svgaHandler.postDelayed(this::moveToNextEntrySvga, 5000);
//        }
//    }


    private void resetEntrySvgaSystem() {

        isEntryLoopRunning = false;
        isEntrySvgaPlaying = false;
        entrySvgaIndex = -1;
        currentSvgaPlayToken++;

        svgaHandler.removeCallbacksAndMessages(null);

        if (binding != null) {
            binding.svgGiftImage.stopAnimation(true);
            binding.svgGiftImage.clear();
            binding.svgGiftImage.setCallback(null);
            binding.svgGiftImage.setImageDrawable(null);
            binding.svgGiftImage.setVisibility(GONE);
        }
    }

    private void startFakeBannerLoop() {
        fakeBannerHandler.postDelayed(fakeBannerRunnable, 8000); // first banner 8s baad
    }

    private final Runnable fakeBannerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isFinishing() || isDestroyed()) return;

            switch (fakeBannerStep % 3) {
                case 0: showFakeGiftBanner(); break;
                case 1: showFakeGameBanner(); break;
//                case 2: showFakeLuckyGiftBanner(); break;
            }
            fakeBannerStep++;

            // Next banner 8-12 sec baad
            long delay = (fakeGiftRandom.nextInt(5) + 8) * 1000L;
            fakeBannerHandler.postDelayed(this, delay);
        }
    };

    private void showFakeGiftBanner() {
        // Random sender + receiver name
        List<UserRoot.User> users = Demo_contents.getUsers(this, true);
        if (users.isEmpty()) return;
        binding.icGiftImage.setVisibility(VISIBLE);

        String sender = users.get(fakeGiftRandom.nextInt(users.size())).getName();
        String senderImage = users.get(fakeGiftRandom.nextInt(users.size())).getImage();

        // Random occupied seat name as receiver
        List<SeatModalClass> seats = seatAdapter.getCurrentList();
        List<String> occupiedNames = new ArrayList<>();
        for (SeatModalClass s : seats) {
            if (s.isReserved() && s.getName() != null && !s.getName().isEmpty()) {
                occupiedNames.add(s.getName());
            }
        }
        String receiver = occupiedNames.isEmpty() ? "Guest"
                : occupiedNames.get(fakeGiftRandom.nextInt(occupiedNames.size()));

        // Random coin amount
        int[] coins = {50, 100, 150, 200, 300, 500, 700, 1000};
        int coin = coins[fakeGiftRandom.nextInt(coins.length)];

        String message = sender + " sent " + coin + " Coin(s) to " + receiver;

        // Load random banner background
        List<com.codder.ultimate.modelclass.BroadcastBannerRoot.BroadcastBannerItem> bannerList = sessionManager.getBroadcastBannerList();


        for (GiftCategoryRoot.CategoryItem category :
                sessionManager.getGiftCategoriesList()) {

            List<GiftRoot.GiftItem> gifts =
                    sessionManager.getGiftsList(category.getId());

            if (gifts != null) {
                allGiftList.addAll(gifts);
            }
        }

        if (!allGiftList.isEmpty()) {

            GiftRoot.GiftItem randomGift =
                    allGiftList.get(fakeGiftRandom.nextInt(allGiftList.size()));

            Glide.with(this)
                    .load(BuildConfig.BASE_URL + randomGift.getImage())
                    .placeholder(R.drawable.gift_placeholder)
                    .into(binding.icGiftImage);
        }


        Glide.with(this)
                .load(senderImage)
                .placeholder(R.drawable.profile_placeholder)
                .circleCrop()
                .into(binding.ivUserImage);

        binding.tvNotification.setText(message);

        if (bannerList != null && !bannerList.isEmpty()) {
            String bannerUrl = BuildConfig.BASE_URL + bannerList.get(fakeGiftRandom.nextInt(bannerList.size())).getImageUrl();
            Glide.with(this).load(bannerUrl).into(new com.bumptech.glide.request.target.CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Drawable> transition) {
                    binding.lytNotification.setBackground(resource);
                    animateFakeBanner(binding.lytNotification);
                }
                @Override public void onLoadCleared(@Nullable Drawable placeholder) {}
            });
        } else {
            animateFakeBanner(binding.lytNotification);
        }
    }

    private void showFakeGameBanner() {
        List<UserRoot.User> users = Demo_contents.getUsers(this, true);
        if (users.isEmpty()) return;

        String winner = users.get(fakeGiftRandom.nextInt(users.size())).getName();
        String winnerImage = users.get(fakeGiftRandom.nextInt(users.size())).getImage();

        String[] games = {"Roullete Casino 🎰", "Ferrywheel 🎡", "Teen Patti 🃏"};
        String game = games[fakeGiftRandom.nextInt(games.length)];

        int[] coins = {100, 200, 300, 500, 700, 1000, 1500, 2000};
        int coin = coins[fakeGiftRandom.nextInt(coins.length)];

        String message = winner + " won " + coin + " Coin(s) in " + game + " game";

        List<com.codder.ultimate.modelclass.BroadcastBannerRoot.BroadcastBannerItem> bannerList = sessionManager.getGameBroadcastBannerList();

        Glide.with(this)
                .load(winnerImage)
                .placeholder(R.drawable.profile_placeholder)
                .circleCrop()
                .into(binding.ivUserImage);

        binding.tvNotification.setText(message);
        binding.icGiftImage.setVisibility(GONE);

        if (bannerList != null && !bannerList.isEmpty()) {
            String bannerUrl = BuildConfig.BASE_URL + bannerList.get(fakeGiftRandom.nextInt(bannerList.size())).getImageUrl();
            Glide.with(this).load(bannerUrl).into(new com.bumptech.glide.request.target.CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Drawable> transition) {
                    binding.lytNotification.setBackground(resource);
                    animateFakeBanner(binding.lytNotification);
                }
                @Override public void onLoadCleared(@Nullable Drawable placeholder) {}
            });
        } else {
            animateFakeBanner(binding.lytNotification);
        }
    }

    private void showFakeLuckyGiftBanner() {
        List<UserRoot.User> users = Demo_contents.getUsers(this, true);
        if (users.isEmpty()) return;

        String winner = users.get(fakeGiftRandom.nextInt(users.size())).getName();
        String winnerImage = users.get(fakeGiftRandom.nextInt(users.size())).getImage();

        int[] coins = {50, 100, 150, 200, 300, 500};
        int coin = coins[fakeGiftRandom.nextInt(coins.length)];

        String message = winner + " has won a\nLucky Gift worth " + coin + " coins!";

        List<com.codder.ultimate.modelclass.BroadcastBannerRoot.BroadcastBannerItem> bannerList = sessionManager.getBroadcastBannerList();

        Glide.with(this)
                .load(winnerImage)
                .placeholder(R.drawable.profile_placeholder)
                .circleCrop()
                .into(binding.ivUserImage);

        binding.tvNotification.setText(message);

        if (bannerList != null && !bannerList.isEmpty()) {
            String bannerUrl = BuildConfig.BASE_URL + bannerList.get(fakeGiftRandom.nextInt(bannerList.size())).getImageUrl();
            Glide.with(this).load(bannerUrl).into(new com.bumptech.glide.request.target.CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Drawable> transition) {
                    binding.lytNotification.setBackground(resource);
                    animateFakeBanner(binding.lytNotification);
                }
                @Override public void onLoadCleared(@Nullable Drawable placeholder) {}
            });
        } else {
            animateFakeBanner(binding.lytNotification);
        }
    }

    private boolean isBannerVisible = false;

    private void animateFakeBanner(View bannerView) {
        if (isFinishing() || isDestroyed()) return;
        if (isBannerVisible) return;

        isBannerVisible = true;

        // ✅ Reset state completely before showing
        bannerView.clearAnimation();
        bannerView.animate().cancel();
        bannerView.setTranslationX(0f);
        bannerView.setTranslationY(0f);
        bannerView.setAlpha(1f);
        bannerView.setVisibility(View.VISIBLE);

        // ✅ Slide in via XML anim
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.anim_slide_right_to_left);
        slideIn.setFillAfter(true);

        final boolean[] slideInDone = {false};     // ✅ track karo ke slide-in tamam thyu ke nahi
        final boolean[] userDismissed = {false};   // ✅ user swipe/tap karyu ke nahi
        final Runnable[] autoDismissRunnable = {null};

        // ✅ Single dismiss logic — direction parameter: "left" or "top" or "auto"
        Runnable doLeftDismiss = () -> {
            if (userDismissed[0] || isFinishing() || isDestroyed()) return;
            userDismissed[0] = true;
            bannerView.setOnClickListener(null);
            bannerView.setOnTouchListener(null);
            if (autoDismissRunnable[0] != null)
                fakeBannerHandler.removeCallbacks(autoDismissRunnable[0]);
            bannerView.clearAnimation();
            bannerView.animate().cancel();
            bannerView.animate()
                    .translationX(-bannerView.getWidth() - 50f)
                    .alpha(0f)
                    .setDuration(220)
                    .setInterpolator(new android.view.animation.AccelerateInterpolator())
                    .withEndAction(() -> {
                        bannerView.setVisibility(View.GONE);
                        bannerView.setTranslationX(0f);
                        bannerView.setTranslationY(0f);
                        bannerView.setAlpha(1f);
                        isBannerVisible = false;
                    }).start();
        };

        Runnable doTopDismiss = () -> {
            if (userDismissed[0] || isFinishing() || isDestroyed()) return;
            userDismissed[0] = true;
            bannerView.setOnClickListener(null);
            bannerView.setOnTouchListener(null);
            if (autoDismissRunnable[0] != null)
                fakeBannerHandler.removeCallbacks(autoDismissRunnable[0]);
            bannerView.clearAnimation();
            bannerView.animate().cancel();
            bannerView.animate()
                    .translationY(-bannerView.getHeight() - 50f)
                    .alpha(0f)
                    .setDuration(220)
                    .setInterpolator(new android.view.animation.AccelerateInterpolator())
                    .withEndAction(() -> {
                        bannerView.setVisibility(View.GONE);
                        bannerView.setTranslationX(0f);
                        bannerView.setTranslationY(0f);
                        bannerView.setAlpha(1f);
                        isBannerVisible = false;
                    }).start();
        };

        // ✅ Auto dismiss (original XML slide-out) — slideInDone check
        Runnable autoSlideOutDismiss = () -> {
            if (userDismissed[0] || isFinishing() || isDestroyed()) return;
            userDismissed[0] = true;
            bannerView.setOnClickListener(null);
            bannerView.setOnTouchListener(null);
            // Original XML animation for auto dismiss
            Animation slideOut = AnimationUtils.loadAnimation(
                    FakeAudioWatchActivity.this, R.anim.slide_right_to_left);
            slideOut.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation a) {}
                @Override public void onAnimationRepeat(Animation a) {}
                @Override public void onAnimationEnd(Animation a) {
                    bannerView.setVisibility(View.GONE);
                    bannerView.setTranslationX(0f);
                    bannerView.setTranslationY(0f);
                    bannerView.setAlpha(1f);
                    isBannerVisible = false;
                }
            });
            bannerView.startAnimation(slideOut);
        };

        autoDismissRunnable[0] = autoSlideOutDismiss;

        // ✅ Touch — swipe detect, slideIn chalti hoy to ignore
        final float[] rawStartX = {0f};
        final float[] rawStartY = {0f};
        final boolean[] dirLocked = {false};
        final boolean[] isLeftSwipe = {false};
        final boolean[] touchActive = {false};

        bannerView.setOnTouchListener((v, event) -> {
            // ✅ slideIn tamam na thay tyar sudhi swipe ignore
            if (!slideInDone[0]) return false;

            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    rawStartX[0] = event.getRawX();
                    rawStartY[0] = event.getRawY();
                    dirLocked[0] = false;
                    isLeftSwipe[0] = false;
                    touchActive[0] = true;
                    bannerView.animate().cancel();
                    return false; // click pn work thay

                case android.view.MotionEvent.ACTION_MOVE:
                    if (!touchActive[0]) return false;
                    float dx = event.getRawX() - rawStartX[0];
                    float dy = event.getRawY() - rawStartY[0];

                    if (!dirLocked[0] && (Math.abs(dx) > 12 || Math.abs(dy) > 12)) {
                        dirLocked[0] = true;
                        isLeftSwipe[0] = dx < 0 && Math.abs(dx) >= Math.abs(dy);
                    }

                    if (!dirLocked[0]) return false;

                    if (isLeftSwipe[0] && dx < 0) {
                        bannerView.setTranslationX(dx);
                        bannerView.setAlpha(Math.max(0f, 1f + dx / (float) bannerView.getWidth()));
                        return true;
                    } else if (!isLeftSwipe[0] && dy < 0) {
                        bannerView.setTranslationY(dy);
                        bannerView.setAlpha(Math.max(0f, 1f + dy / (float) bannerView.getHeight()));
                        return true;
                    }
                    return false;

                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    if (!touchActive[0]) return false;
                    touchActive[0] = false;
                    float upDx = event.getRawX() - rawStartX[0];
                    float upDy = event.getRawY() - rawStartY[0];

                    if (dirLocked[0] && isLeftSwipe[0] && upDx < -80f) {
                        doLeftDismiss.run();
                        return true;
                    } else if (dirLocked[0] && !isLeftSwipe[0] && upDy < -80f) {
                        doTopDismiss.run();
                        return true;
                    } else if (dirLocked[0]) {
                        // Snap back
                        bannerView.animate()
                                .translationX(0f)
                                .translationY(0f)
                                .alpha(1f)
                                .setDuration(180)
                                .start();
                        return true;
                    }
                    return false;
            }
            return false;
        });

        bannerView.setOnClickListener(v -> {
            if (slideInDone[0]) doLeftDismiss.run();
        });

        slideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation a) {}
            @Override public void onAnimationRepeat(Animation a) {}
            @Override
            public void onAnimationEnd(Animation a) {
                slideInDone[0] = true; // ✅ hu swipe enable karo
                fakeBannerHandler.postDelayed(autoSlideOutDismiss, 3000);
            }
        });

        bannerView.startAnimation(slideIn);
    }


    private String drawableToUri(@DrawableRes int res) {
        return Uri.parse("android.resource://" + getPackageName() + "/" + res).toString();
    }


    @Override
    public void onBackPressed() {
        showExitConfirmation();
    }

    @Override
    protected void onDestroy() {
        confirmEndLive();
        super.onDestroy();
    }
}
