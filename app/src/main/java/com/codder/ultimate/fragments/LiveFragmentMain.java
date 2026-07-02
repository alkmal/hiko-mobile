package com.codder.ultimate.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.activity.MainActivity;
import com.codder.ultimate.adapter.BannerAdapter;
import com.codder.ultimate.adapter.HomeViewPagerAdapter;
import com.codder.ultimate.adapter.LiveUserListAdapter;
import com.codder.ultimate.country.CountryAdapter;
import com.codder.ultimate.country.CountryArray;
import com.codder.ultimate.country.CountryFilterViewModel;
import com.codder.ultimate.country.CountryModel;
import com.codder.ultimate.databinding.FragmentLiveMainBinding;
import com.codder.ultimate.fake.activity.FakeAudioWatchActivity;
import com.codder.ultimate.guestuser.activity.SearchActivity;
import com.codder.ultimate.leaderboard.LeaderBoardActivity;
import com.codder.ultimate.live.activity.GotoLiveActivity;
import com.codder.ultimate.live.activity.HostLiveAudioActivity;
import com.codder.ultimate.live.activity.WatchAudioLiveActivity;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.live.utils.FloatingButtonService;
import com.codder.ultimate.modelclass.LiveListViewModel;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.profile.activity.ProfileActivity;
import com.codder.ultimate.profile.adapter.DotAdapter;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.socket.MySocketManager;
import com.codder.ultimate.viewModel.AppBarControlViewModel;
import com.codder.ultimate.viewModel.HomeRefreshViewModel;
import com.codder.ultimate.viewModel.LiveFragmentViewModel;
import com.codder.ultimate.viewModel.ViewModelFactory;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LiveFragmentMain extends BaseFragment {

    private static final String TAG = "LiveFragmentMain";
    private FragmentLiveMainBinding binding;
    private final BannerAdapter bannerAdapter = new BannerAdapter();
    private LiveFragmentViewModel viewModel;
    private final Handler autoSlideHandler = new Handler(Looper.getMainLooper());
    private final String[] liveUserType = {"AudioLive"};
    private String[] liveUserTypeHeading;
    private Runnable autoSlideRunnable;
    private boolean isFragmentVisible;

    List<PkAudioLiveUserRoot.UsersItem> liveuserList = new ArrayList<>();

    private AppBarControlViewModel appBarBus;

    CountryAdapter countryAdapter;

    LiveUserListAdapter liveUserListAdapter;
    List<PkAudioLiveUserRoot.UsersItem> liveuserlist = new ArrayList<>();

    private CountryFilterViewModel countryFilterVM;
    private HomeRefreshViewModel refreshVM;


    public LiveFragmentMain() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_live_main, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(LiveFragmentViewModel.class);
        refreshVM = new ViewModelProvider(requireActivity()).get(HomeRefreshViewModel.class);
        binding.setViewModel(viewModel);

        initView();
        return binding.getRoot();
    }

    private void initView() {
        liveUserTypeHeading = new String[]{getString(R.string.explore), getString(R.string.party)};
        appBarBus = new ViewModelProvider(this).get(AppBarControlViewModel.class);
        countryFilterVM = new ViewModelProvider(requireActivity()).get(CountryFilterViewModel.class);

        appBarBus.expandEvents().observe(getViewLifecycleOwner(), expand -> {
            if (expand == null) return;
            // animate = true for smoothness
            binding.appBar.setExpanded(expand, true);
        });


        countryAdapter = new CountryAdapter(requireActivity());
        liveUserListAdapter = new LiveUserListAdapter(requireActivity());

        binding.rvliveuser.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvliveuser.setAdapter(liveUserListAdapter);


        binding.refreshLiveUser.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                countryFilterVM.getSelectedCountryCode()
                        .observe(getViewLifecycleOwner(), code -> {

                            viewModel.refreshLiveUsers(
                                    sessionManager.getUser().getId(),
                                    code,
                                    getFakeUsers()
                            );

                            // 🔥 refresh all ViewPager fragments
                            refreshVM.triggerRefresh();
                            fetchuser();
                        });

            }
        });


        viewModel.getLiveUsers().observe(getViewLifecycleOwner(), newList -> {

            List<PkAudioLiveUserRoot.UsersItem> oldList =
                    liveUserListAdapter.getCurrentItems();

            boolean shouldScrollToStart = false;

            if (oldList != null && !oldList.isEmpty() && newList != null) {

                // 🔥 If size increased → new user added
                if (newList.size() > oldList.size()) {
                    shouldScrollToStart = true;
                }
            }

            liveUserListAdapter.submitData(newList);

            if (shouldScrollToStart) {
                binding.rvliveuser.post(() -> {
                    binding.rvliveuser.scrollToPosition(0);
                });
            }

            if (binding.refreshLiveUser.isRefreshing()) {
                binding.refreshLiveUser.finishRefresh();
            }
        });



        binding.rvCountryTab.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCountryTab.setAdapter(countryAdapter);
        List<CountryModel> list = CountryArray.getCountryList();
        countryAdapter.submitList(list);

// ✅ DEFAULT selection = All
        countryAdapter.setSelectedPosition(0);

// ✅ DEFAULT countryCode
        countryFilterVM.setCountryCode("global");

        countryAdapter.setOnCountryClickListener((model, position) -> {

            String countryCode;// "" for All
            if (model.getName().equals("All")) {
                countryCode = "global";
            } else {
                countryCode = model.getName();
            }

            Log.d(TAG, "Selected country code = " + countryCode);

            countryFilterVM.setCountryCode(countryCode);

        });


        Log.d(TAG, "initView: " + sessionManager.getLiveUserForBackground());

        binding.viewPager.setAdapter(new HomeViewPagerAdapter(getChildFragmentManager(), liveUserType));
        binding.viewPager.setOffscreenPageLimit(liveUserType.length); // keep all tabs alive

        bannerAdapter.submitList(sessionManager.getBannerList());
        binding.rvBanner.setAdapter(bannerAdapter);
        new PagerSnapHelper().attachToRecyclerView(binding.rvBanner);

        if (bannerAdapter.getItemCount() >= 2) {
            setupAutoSlider();
        }

        binding.ivProfile.setOnClickListener(v -> {

            ((MainActivity) getActivity()).openProfileFragment();

        });

        binding.etSearch.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SearchActivity.class));
            doTransition(Const.BOTTOM_TO_UP);
        });

        binding.ivLeaderboard.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), LeaderBoardActivity.class));
            doTransition(Const.BOTTOM_TO_UP);
        });


        liveUserListAdapter.setOnHostClickLister(new LiveUserListAdapter.OnHostClickLister() {
            @Override
            public void onHostItemClick(PkAudioLiveUserRoot.UsersItem userDummy) {


                if (userDummy.getBlockedUsers() != null) {

                    for (int i = 0; i < userDummy.getBlockedUsers().size(); i++) {
                        if (userDummy.getBlockedUsers().get(i).getBlockedUserId().equals(sessionManager.getUser().getId())) {
                            Toast.makeText(requireActivity(), getString(R.string.you_are_blocked_by_host), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }


                if (sessionManager.getIsUserBackgroundLive()) {
                    if (userDummy.getLiveUserId() != null) {

                        String currentUserId = userDummy.getLiveUserId();
                        String backgroundLiveChannel = (sessionManager.getUserAudioBgModel() != null) ? sessionManager.getUserAudioBgModel().getLiveUserId() : "";

                        if (!currentUserId.isEmpty() && !backgroundLiveChannel.isEmpty() && !currentUserId.equals(backgroundLiveChannel)) {

                            if (sessionManager.getUser().getImage() != null) {
                                getContext().startService(new Intent(getContext(), FloatingButtonService.class).putExtra("image", sessionManager.getUser().getImage()));
                            }


                            new PopupBuilder(requireActivity()).showSimplePopup(getString(R.string.you_are_currently_in_audioroom_please_exit_room_and_watch_live), getString(R.string.dismiss), new PopupBuilder.OnPopupClickListener() {
                                @Override
                                public void onClickContinue() {

                                }
                            });
                        } else {
                            if (sessionManager.getIsAudioRoomExit()) {

                                if (userDummy.isAudio() && !userDummy.isIsFake() && !userDummy.isIsPkMode()) {
                                    startActivity(new Intent(requireActivity(), WatchAudioLiveActivity.class).putExtra(Const.DATA, new Gson().toJson(userDummy)));
                                } else if (userDummy.isIsFake() && !userDummy.isIsPkMode() && userDummy.isAudio()) {
                                    Log.d(TAG, "onHostItemClick: fake audio");
                                    startActivity(new Intent(requireActivity(), FakeAudioWatchActivity.class).putExtra(Const.DATA, new Gson().toJson(userDummy)));
                                }
                            } else {
                                new PopupBuilder(requireActivity()).showSimplePopup(getString(R.string.you_are_currently_in_audioroom_please_exit_room_and_watch_live), getString(R.string.dismiss), new PopupBuilder.OnPopupClickListener() {
                                    @Override
                                    public void onClickContinue() {

                                    }
                                });
                            }
                        }
                    } else {


                        if (sessionManager.getUser().getImage() != null) {
                            getContext().startService(new Intent(getContext(), FloatingButtonService.class).putExtra("image", sessionManager.getUser().getImage()));
                        }

                        new PopupBuilder(requireActivity()).showSimplePopup(getString(R.string.you_are_currently_in_audioroom_please_exit_room_and_watch_live), getString(R.string.dismiss), new PopupBuilder.OnPopupClickListener() {
                            @Override
                            public void onClickContinue() {

                            }
                        });
                    }

                } else {

                    if (sessionManager.getIsAudioRoomExit()) {
                        if (sessionManager.getIsUserBackgroundLive()) {
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("userId", sessionManager.getUser().getId());
                            jsonObject.addProperty("liveUserMongoId", sessionManager.getUserAudioBgModel().getId());
                            jsonObject.addProperty("liveStreamingId", sessionManager.getUserAudioBgModel().getLiveStreamingId());

                            MySocketManager.getInstance().getSocket().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject);

                            ((MainApplication) requireActivity().getApplication()).rtcEngine().leaveChannel();
                            JSONObject jsonObject1 = new JSONObject();
                            try {
                                jsonObject1.put("liveStreamingId", sessionManager.getUserAudioBgModel().getLiveStreamingId());
                                jsonObject1.put("liveUserMongoId", sessionManager.getUserAudioBgModel().getId());
                                jsonObject1.put("userId", sessionManager.getUser().getId());
                                jsonObject1.put("isVIP", sessionManager.getUser().isIsVIP());
                                jsonObject1.put("image", sessionManager.getUser().getImage());
                                jsonObject1.put("name", sessionManager.getUser().getName());
                                jsonObject1.put("gender", sessionManager.getUser().getGender());
                                jsonObject1.put("country", sessionManager.getUser().getCountry());
                                jsonObject1.put("userName", sessionManager.getUser().getName());
                                jsonObject1.put("avatarFrame", sessionManager.getUser().getAvatarFrameImage());
                                jsonObject1.put("entrySvga", sessionManager.getUser().getSvgaImage());
                                MySocketManager.getInstance().getSocket().emit(Const.EVENT_LESS_VIEW, jsonObject1);
                            } catch (JSONException e) {
                                Log.e(TAG, "onHostItemClick: ", e);
                            }
                        }
                        if (userDummy.isAudio() && !userDummy.isIsFake() && !userDummy.isIsPkMode()) {
                            startActivity(new Intent(requireActivity(), WatchAudioLiveActivity.class).putExtra(Const.DATA, new Gson().toJson(userDummy)));
                        } else if (userDummy.isIsFake() && !userDummy.isIsPkMode() && userDummy.isAudio()) {
                            Log.d(TAG, "onHostItemClick: fake audio");
                            startActivity(new Intent(requireActivity(), FakeAudioWatchActivity.class).putExtra(Const.DATA, new Gson().toJson(userDummy)));
                        }
                    } else {
                        new PopupBuilder(requireActivity()).showSimplePopup(getString(R.string.you_are_currently_in_audioroom_please_exit_room_and_watch_live), getString(R.string.dismiss), new PopupBuilder.OnPopupClickListener() {
                            @Override
                            public void onClickContinue() {

                            }
                        });
                    }
                }

            }

            @Override
            public void onQuickJoin(PkAudioLiveUserRoot.UsersItem userDummy) {

                    if (sessionManager.getIsUserBackgroundLive()) {
                        new PopupBuilder(requireActivity())
                                .showSimplePopup(
                                        getString(R.string.you_are_currently_in_audioroom_please_exit_room_and_watch_live),
                                        getString(R.string.dismiss),
                                        () -> {}
                                );
                        return;
                    }

                    handleQuickJoin();

            }

            @Override
            public void onMyRoom() {
                if (sessionManager.getIsAudioRoomBackground()) {

                    if (sessionManager.getIsUserBackgroundLive()) {
                        new PopupBuilder(requireActivity()).showSimplePopup(getString(R.string.you_are_currently_in_audioroom_please_exit_room_and_watch_live), getString(R.string.dismiss), new PopupBuilder.OnPopupClickListener() {
                            @Override
                            public void onClickContinue() {

                            }
                        });
                    } else {
                        Intent intent = new Intent(requireActivity(), HostLiveAudioActivity.class);
                        intent.putExtra(Const.DATA, new Gson().toJson(sessionManager.getLiveUserForBackground()));
                        Log.d(TAG, "onResponse: ------------- 22 " + new Gson().toJson(sessionManager.getLiveUserForBackground()));
                        intent.putExtra(Const.PRIVACY, "Public");
                        startActivity(intent);
                    }

                } else {
                    startActivity(
                            new Intent(requireActivity(), GotoLiveActivity.class)

                    );
                }
            }
        });


    }

    private void setupAutoSlider() {
        final DotAdapter dotAdapter = new DotAdapter(bannerAdapter.getItemCount(), R.color.white);
        binding.rvDots.setAdapter(dotAdapter);

        binding.rvBanner.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int pos = layoutManager.findFirstVisibleItemPosition();
                    dotAdapter.changeDot(pos);
                }
            }
        });

        autoSlideRunnable = new Runnable() {
            int pos = 0;
            boolean forward = true;

            @Override
            public void run() {
                if (!isFragmentVisible || bannerAdapter.getItemCount() <= 1) return;

                if (pos == bannerAdapter.getItemCount() - 1) forward = false;
                else if (pos == 0) forward = true;

                pos = forward ? ++pos : --pos;
                binding.rvBanner.smoothScrollToPosition(pos);
                autoSlideHandler.postDelayed(this, 2000);
            }
        };
        autoSlideHandler.postDelayed(autoSlideRunnable, 2000);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: ==> " + sessionManager.getUser().getAvatarFrameImage());
        isFragmentVisible = true;

        countryFilterVM.getSelectedCountryCode()
                .observe(getViewLifecycleOwner(), code -> {

                    viewModel.refreshLiveUsers(
                            sessionManager.getUser().getId(),
                            code,
                            getFakeUsers()
                    );

                    // 🔥 refresh all ViewPager fragments
                    refreshVM.triggerRefresh();

                    fetchuser();
                });

        if (!requireActivity().isFinishing()) {
            binding.ivProfile.setHomeUserImage(sessionManager.getUser().getImage(), sessionManager.getUser().getAvatarFrameImage(), 15);

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentVisible = false;
        autoSlideHandler.removeCallbacks(autoSlideRunnable);
    }

    private List<PkAudioLiveUserRoot.UsersItem> getFakeUsers() {
        List<PkAudioLiveUserRoot.UsersItem> fake =
                sessionManager.getShuffledFakeLiveList();

        return fake == null ? new ArrayList<>() : fake;
    }

    private void handleQuickJoin() {
        List<PkAudioLiveUserRoot.UsersItem> list = liveuserList;

        Log.d(TAG, "handleQuickJoin: ======" + list);

        if (list == null || list.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No live rooms available",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        List<PkAudioLiveUserRoot.UsersItem> realUsers = new ArrayList<>();
        List<PkAudioLiveUserRoot.UsersItem> fakeUsers = new ArrayList<>();

        // ================= COLLECT USERS =================
        for (PkAudioLiveUserRoot.UsersItem user : list) {

            // skip static items
            if (user.getItemType() != PkAudioLiveUserRoot.UsersItem.TYPE_USER)
                continue;

            // -------- REAL USER CHECK --------
            boolean isRealUser =
                    user.isAudio()
                            && !user.isIsFake()
                            && !user.isIsPkMode()
                            && user.getLiveUserId() != null;

            if (isRealUser) {

                // block check
                boolean blocked = false;
                if (user.getBlockedUsers() != null) {
                    for (int i = 0; i < user.getBlockedUsers().size(); i++) {
                        if (sessionManager.getUser().getId()
                                .equals(user.getBlockedUsers().get(i).getBlockedUserId())) {
                            blocked = true;
                            break;
                        }
                    }
                }

                if (!blocked) {
                    realUsers.add(user);
                }
            }

            // -------- FAKE USER --------
            if (user.isIsFake()) {
                fakeUsers.add(user);
            }
        }

        // ================= 1️⃣ RANDOM REAL USER =================
        if (!realUsers.isEmpty()) {

            PkAudioLiveUserRoot.UsersItem randomReal =
                    realUsers.get(new Random().nextInt(realUsers.size()));

            Log.d(TAG, "QuickJoin Real User = " + new Gson().toJson(randomReal));

            startActivity(
                    new Intent(requireActivity(), WatchAudioLiveActivity.class)
                            .putExtra(Const.DATA, new Gson().toJson(randomReal))
            );
            return;
        }

        // ================= 2️⃣ RANDOM FAKE USER =================
        if (!fakeUsers.isEmpty()) {

            PkAudioLiveUserRoot.UsersItem randomFake =
                    fakeUsers.get(new Random().nextInt(fakeUsers.size()));

            Log.d(TAG, "QuickJoin Fake User = " + new Gson().toJson(randomFake));

            startActivity(
                    new Intent(requireActivity(), FakeAudioWatchActivity.class)
                            .putExtra(Const.DATA, new Gson().toJson(randomFake))
            );
            return;
        }

        // ================= 3️⃣ NOTHING FOUND =================
        Toast.makeText(requireContext(),
                "No available room to join",
                Toast.LENGTH_SHORT).show();
    }

    public void fetchuser(){
        RetrofitBuilder.create()
                .getLiveUsersList(sessionManager.getUser().getId(), "AudioLive", "", 0, Const.LIMIT, "global")
                .enqueue(new Callback<PkAudioLiveUserRoot>() {

                    @Override
                    public void onResponse(Call<PkAudioLiveUserRoot> call,
                                           Response<PkAudioLiveUserRoot> response) {

                        if (response.body() == null || response.body().getUsers() == null) {
                            liveuserList = new ArrayList<>();
                        } else {
                            liveuserList = response.body().getUsers();
                        }
                        liveuserList.addAll(getFakeUsers());


                    }

                    @Override
                    public void onFailure(Call<PkAudioLiveUserRoot> call, Throwable t) {

                    }
                });
    }


}
