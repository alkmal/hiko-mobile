package com.codder.ultimate.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.country.CountryFilterViewModel;
import com.codder.ultimate.databinding.FragmentLiveListBinding;
import com.codder.ultimate.databinding.ItemPkInviteHostBinding;
import com.codder.ultimate.databinding.ItemVideoGridBinding;
import com.codder.ultimate.fake.activity.FakeAudioWatchActivity;
import com.codder.ultimate.guestuser.activity.SearchActivity;
import com.codder.ultimate.live.activity.WatchAudioLiveActivity;
import com.codder.ultimate.live.adapter.LiveListAdapter;
import com.codder.ultimate.live.model.LiveStreamRoot;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.live.utils.FloatingButtonService;
import com.codder.ultimate.modelclass.LiveListViewModel;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.profile.activity.ProfileActivity;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.socket.MySocketManager;
import com.codder.ultimate.viewModel.AppBarControlViewModel;
import com.codder.ultimate.viewModel.HomeRefreshViewModel;
import com.codder.ultimate.viewModel.ViewModelFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LiveListFragment extends BaseFragment {

    private static final String TAG = "LiveListFragment";

    private FragmentLiveListBinding binding;
    private LiveListViewModel viewModel;
    private static final String ARG_TYPE = "arg_type";
    private String type = "All";
    private boolean isGone = false;
    private AppBarControlViewModel appBarBus;
    private boolean lastExpandedState = true;
    private boolean scrollToTopOnNextContent = false;

    private HomeRefreshViewModel refreshVM;

    private CountryFilterViewModel countryFilterVM;

    public static LiveListFragment newInstance(@NonNull String type) {
        LiveListFragment f = new LiveListFragment();
        Bundle b = new Bundle();
        b.putString(ARG_TYPE, type);
        f.setArguments(b);
        return f;
    }
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            type = savedInstanceState.getString(ARG_TYPE, type);
        } else if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE, type);
        }
    }

    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putString(ARG_TYPE, type);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_live_list, container, false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        viewModel = new ViewModelProvider(this, new ViewModelFactory(new LiveListViewModel()).createFor())
                .get(LiveListViewModel.class);
        refreshVM = new ViewModelProvider(requireActivity())
                .get(HomeRefreshViewModel.class);

        appBarBus = new ViewModelProvider(requireParentFragment())
                .get(AppBarControlViewModel.class);


        setupRecyclerView();

        binding.setViewModel(viewModel);



        refreshVM.getRefreshTrigger()
                .observe(getViewLifecycleOwner(), trigger -> {
                    Log.d(TAG, "Parent refresh received → reload list");

                    showLoading();
                    appBarBus.requestExpand();
                    viewModel.noDataFound.set(false);
                    scrollToTopOnNextContent = true;
                    viewModel.liveListAdapter.submitList(new ArrayList<>());
                    viewModel.getData(false); // HARD refresh
                });

        viewModel.init(requireContext(), type);

        countryFilterVM = new ViewModelProvider(requireActivity())
                .get(CountryFilterViewModel.class);

        countryFilterVM.getSelectedCountryCode()
                .observe(getViewLifecycleOwner(), countryCode -> {

                    Log.d(TAG, "Country changed → reload list: " + countryCode);

                    showLoading();          // ✅ shimmer ON
                    scrollToTopOnNextContent = true;

                    viewModel.onCountryChanged(countryCode);
                });


        initLister();
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), type.equals("AudioLive") ? 1 : 2);
        binding.rvVideos.setLayoutManager(layoutManager);
        binding.rvVideos.setAdapter(viewModel.liveListAdapter);

        binding.rvVideos.setNestedScrollingEnabled(true);
        binding.rvVideos.setItemViewCacheSize(20);
        binding.rvVideos.setHasFixedSize(true);

        // Prevent change animations flicker
        RecyclerView.ItemAnimator ia = binding.rvVideos.getItemAnimator();
        if (ia instanceof androidx.recyclerview.widget.SimpleItemAnimator) {
            ((androidx.recyclerview.widget.SimpleItemAnimator) ia).setSupportsChangeAnimations(false);
        }

        binding.rvVideos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    if (lastExpandedState) {
                        appBarBus.requestCollapse();
                        lastExpandedState = false;
                    }
                }

                if (dy < 0 && !binding.rvVideos.canScrollVertically(-1)) {
                    if (!lastExpandedState) {
                        appBarBus.requestExpand();
                        lastExpandedState = true;
                    }
                }

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                int visibleThreshold = Const.LIMIT; // Preload next page 4 items before end


                if (!viewModel.isLoading() && viewModel.hasMoreData() &&
                        totalItemCount <= (lastVisibleItemPosition + visibleThreshold)) {
                    viewModel.getData(true);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        isGone = false;

        if (!requireActivity().isFinishing()) {
            binding.ivProfile.setUserImage(
                    sessionManager.getUser().getImage(),
                    sessionManager.getUser().getAvatarFrameImage(),
                    15
            );
        }

        // SILENT refresh to pick up newly started lives without shimmer
        viewModel.refreshSilentlyIfStale();

        if (binding.shimmer.getVisibility() == View.VISIBLE) {
            binding.shimmer.startShimmer();
        }
    }

    private void initLister() {

        binding.swipeRefresh.setOnRefreshListener((refreshLayout) -> {
            showLoading();
            appBarBus.requestExpand();
            viewModel.noDataFound.set(false);
            scrollToTopOnNextContent = true;
            viewModel.getData(false);
        });
        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> {
            viewModel.getData(true);
        });

        viewModel.isLoadingComplete.observe(getViewLifecycleOwner(), done -> {
            if (Boolean.TRUE.equals(done)) {

                binding.swipeRefresh.finishLoadMore();
                binding.swipeRefresh.finishRefresh();

                if (!viewModel.noDataFound.get()) {
                    showContent();
                } else {
                    showEmpty();
                }
            }
        });


        binding.ivProfile.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ProfileActivity.class));
            requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        binding.ivSearch.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SearchActivity.class));
            doTransition(Const.BOTTOM_TO_UP);
        });

        viewModel.liveListAdapter.setOnHostClickLister(new LiveListAdapter.OnHostClickLister() {
            @Override
            public void onHostItemClick(PkAudioLiveUserRoot.UsersItem userDummy, ItemVideoGridBinding itemVideoGridBinding, ItemPkInviteHostBinding itemPkInviteHostBinding) {
                Log.d(TAG, "onHostItemClick: userDummy.isAudio()  " + userDummy.isAudio());

                Log.d(TAG, "onHostItemClick: ===" + userDummy.getBlockedUsers());

                if (userDummy.getBlockedUsers() != null) {

                    for (int i = 0; i < userDummy.getBlockedUsers().size(); i++) {
                        if (userDummy.getBlockedUsers().get(i).getBlockedUserId().equals(sessionManager.getUser().getId())) {
                            Toast.makeText(requireActivity(), getString(R.string.you_are_blocked_by_host), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

                if (viewModel.getBlockedUserIds().contains(userDummy.getLiveUserId())) {
                    Toast.makeText(requireActivity(), getString(R.string.you_are_blocked_by_host), Toast.LENGTH_SHORT).show();
                    return;
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
                                    openAudioLiveAfterCheck(userDummy);
                                } else if (userDummy.isIsFake() && !userDummy.isIsPkMode() && userDummy.isAudio()) {
                                    Log.d(TAG, "onHostItemClick: fake audio");

                                    startActivity(new Intent(requireActivity(), FakeAudioWatchActivity.class).putExtra(Const.DATA, new Gson().toJson(userDummy)));
                                } else {
                                    singleLiveUserEventFire(userDummy);
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
                            jsonObject.addProperty("liveHistoryId", sessionManager.getUserAudioBgModel().getLiveStreamingId());

                            MySocketManager.getInstance().getSocket().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject);

                            ((MainApplication) requireActivity().getApplication()).rtcEngine().leaveChannel();
                            JSONObject jsonObject1 = new JSONObject();
                            try {
                                jsonObject1.put("liveStreamingId", sessionManager.getUserAudioBgModel().getLiveStreamingId());
                                jsonObject1.put("liveHistoryId", sessionManager.getUserAudioBgModel().getLiveStreamingId());
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
                            openAudioLiveAfterCheck(userDummy);
                        } else if (userDummy.isIsFake() && !userDummy.isIsPkMode() && userDummy.isAudio()) {
                            Log.d(TAG, "onHostItemClick: fake audio" + userDummy);
                            startActivity(new Intent(requireActivity(), FakeAudioWatchActivity.class).putExtra(Const.DATA, new Gson().toJson(userDummy)));
                        } else {
                            singleLiveUserEventFire(userDummy);
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
        });
    }

    private void openAudioLiveAfterCheck(PkAudioLiveUserRoot.UsersItem userDummy) {
        if (userDummy == null || !isAdded()) return;

        if (userDummy.isIsFake()) {
            startActivity(new Intent(requireActivity(), FakeAudioWatchActivity.class).putExtra(Const.DATA, new Gson().toJson(userDummy)));
            return;
        }

        if (!userDummy.isAudio() || userDummy.isIsPkMode() || userDummy.getLiveUserId() == null) {
            return;
        }

        customDialogClass.show();
        RetrofitBuilder.create()
                .checkUserLiveOrNot(userDummy.getLiveUserId())
                .enqueue(new Callback<LiveStreamRoot>() {
                    @Override
                    public void onResponse(Call<LiveStreamRoot> call, Response<LiveStreamRoot> response) {
                        if (!isAdded() || requireActivity().isFinishing()) return;
                        customDialogClass.dismiss();

                        LiveStreamRoot body = response.body();
                        if (response.isSuccessful() && body != null && body.isStatus() && body.getLiveUser() != null) {
                            PkAudioLiveUserRoot.UsersItem liveUser = new Gson().fromJson(new Gson().toJson(body.getLiveUser()), PkAudioLiveUserRoot.UsersItem.class);
                            startActivity(new Intent(requireActivity(), WatchAudioLiveActivity.class).putExtra(Const.DATA, new Gson().toJson(liveUser)));
                            return;
                        }

                        showLiveEndedAndRefresh(userDummy);
                    }

                    @Override
                    public void onFailure(Call<LiveStreamRoot> call, Throwable t) {
                        if (!isAdded() || requireActivity().isFinishing()) return;
                        customDialogClass.dismiss();
                        showLiveEndedAndRefresh(userDummy);
                    }
                });
    }

    private void showLiveEndedAndRefresh(PkAudioLiveUserRoot.UsersItem userDummy) {
        Toast.makeText(requireContext(),
                userDummy.getName() + getString(R.string.s_live_has_ended),
                Toast.LENGTH_SHORT).show();
        showLoading();
        viewModel.getData(false);
        refreshVM.triggerRefresh();
    }

    private void singleLiveUserEventFire(PkAudioLiveUserRoot.UsersItem user) {
        customDialogClass.show();

        try {
            JSONObject json = new JSONObject();
            json.put("userId", user.getLiveUserId());
            json.put("joinUserId", sessionManager.getUser().getId());
            json.put("liveStreamingId", user.getLiveStreamingId());
            json.put("type", user.isAudio() ? "audio" : "other");

            MySocketManager.getInstance().getSocket().emit("singleLiveUser", json);
            Log.d(TAG, "singleLiveUserEventFire: ======emit event" + json);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for socket event", e);
        }

        MySocketManager.getInstance().getSocket().on(Const.DUMMY, args -> {
            requireActivity().runOnUiThread(() -> {
                if (args[0] != null) {
                    Log.d(TAG, "DUMMY event received: ==> " + args[0].toString());

                    try {
                        PkAudioLiveUserRoot.UsersItem socketLiveUser = new Gson().fromJson(args[0].toString(), PkAudioLiveUserRoot.UsersItem.class);

                        if (!isGone) {
                            isGone = true;
                            Log.d(TAG, "singleLiveUserEventFire: ====" + socketLiveUser.getLiveStreamingId());
//                            LiveListFragment.this.startActivity(new Intent(LiveListFragment.this.getActivity(), HostPKLiveActivity.class).putExtra(Const.IS_HOST, false).putExtra(Const.DATA, new Gson().toJson(socketLiveUser)));
                            customDialogClass.dismiss();
                        }
                        Log.d("TAG", "onHostItemClick: userDummy.isPkView() =============== " + socketLiveUser.isIsPkMode());

                    } catch (JsonSyntaxException e) {
                        Log.e(TAG, "Error deserializing the received data", e);
                    }
                } else {
                    viewModel.getData(false);
                    Log.d(TAG, "DUMMY event received but no data found ==> ");
                }
                customDialogClass.dismiss();
            });
        });

        MySocketManager.getInstance().getSocket().once(Const.IS_LIVE_USER, args -> {
            requireActivity().runOnUiThread(() -> {
                if (args.length > 0 && args[0] != null && !requireActivity().isFinishing()) {
                    Toast.makeText(requireContext(),
                            user.getName() + getString(R.string.s_live_has_ended),
                            Toast.LENGTH_SHORT).show();
                    showLoading();
                    viewModel.getData(false);
                }
                customDialogClass.dismiss();
            });
        });
    }

    private void showLoading() {
        boolean isAudio = "AudioLive".equals(type);

        binding.shimmer.setVisibility(View.VISIBLE);
        binding.shimmerForParty.setVisibility(isAudio ? View.VISIBLE : View.GONE);

        binding.shimmer.bringToFront();
        binding.shimmer.startShimmer();

        binding.rvVideos.setVisibility(View.GONE);   // hide list while loading
        viewModel.noDataFound.set(false);            // ensure empty state hidden during load

        scrollToTopOnNextContent = true;
    }

    private void showContent() {
        stopAndHideShimmer();
        binding.rvVideos.setVisibility(View.VISIBLE);
        viewModel.noDataFound.set(false);
    }

    private void showEmpty() {
        stopAndHideShimmer();
        binding.rvVideos.setVisibility(View.GONE);
        viewModel.noDataFound.set(true);
    }

    private void stopAndHideShimmer() {
        binding.shimmer.stopShimmer();
        binding.shimmer.setVisibility(View.GONE);
        binding.shimmerForParty.setVisibility(View.GONE);
    }

    private void scrollGridToTop() {
        binding.rvVideos.stopScroll();
        RecyclerView.LayoutManager lm = binding.rvVideos.getLayoutManager();
        if (lm instanceof GridLayoutManager glm) {
            glm.scrollToPositionWithOffset(0, 0); // first row aligned to top
        } else {
            binding.rvVideos.scrollToPosition(0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding != null && binding.shimmer != null) binding.shimmer.stopShimmer();
    }

    @Override
    public void onDestroyView() {
        if (binding != null && binding.shimmer != null) {
            binding.shimmer.stopShimmer();
        }
        super.onDestroyView();
    }
}
