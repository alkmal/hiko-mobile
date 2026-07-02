package com.codder.ultimate.modelclass;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.SessionManager;
import com.codder.ultimate.live.adapter.LiveListAdapter;
import com.codder.ultimate.live.model.BlockedUserResponse;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LiveListViewModel extends ViewModel {

    private static final String TAG = "LiveListViewModel";
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private long lastLoadedAt = 0L;

    private int requestVersion = 0;
    private static final long FOREGROUND_REFRESH_WINDOW_MS = 10_000L; // 10s; tweak as needed

    public ObservableBoolean noDataFound = new ObservableBoolean(false);
    public ObservableBoolean isFirstTimeLoading = new ObservableBoolean(false);
    public MutableLiveData<Boolean> isLoadingComplete = new MutableLiveData<>();
    public final LiveListAdapter liveListAdapter = new LiveListAdapter(LiveListAdapter.LIVE_LIST_MODE);

    private final List<PkAudioLiveUserRoot.UsersItem> liveUserList = new ArrayList<>();
    private int start = 0;
    private SessionManager sessionManager;
    private String type = "All";

    private String countryCode = "global";

    private final List<String> blockedUserIds = new ArrayList<>();
    public List<String> getBlockedUserIds() {
        return blockedUserIds;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean hasMoreData() {
        return hasMoreData;
    }



    public void init(@NonNull Context context, @Nullable String type) {
        this.sessionManager = new SessionManager(context);
        this.type = type != null ? type : "All";
    }

    public void setCountryCode(String code) {
        this.countryCode = code != null ? code : "";
    }

    public void getData(boolean isLoadMore) {
        getData(isLoadMore, false);
    }

    public void getData(boolean isLoadMore, boolean silent) {

        final int callVersion = requestVersion;
        if (isLoading || (isLoadMore && !hasMoreData)) {
            Log.d("PreloadTest", "Skipping load: isLoading=" + isLoading + ", hasMoreData=" + hasMoreData);
//            if (!isLoadMore && !silent) {
//                isFirstTimeLoading.set(false);
//            }
            isLoadingComplete.postValue(true);
            return;
        }

        if (!isLoadMore) {
            if (!silent) {
                // FIRST load (show shimmer path controlled in Fragment)
                noDataFound.set(false);
                resetPagination();
                fetchBlockedByUserData();
            } else {
                // SILENT foreground refresh: keep list, keep pagination cursor at 0
                start = 0;
                hasMoreData = true;
            }
        } else {
            start += Const.LIMIT;
        }

        Log.d("PreloadTest", "Fetching data. isLoadMore = " + isLoadMore + ", start = " + start);

        isLoading = true;
        updateAdapterMode(type);

        Call<PkAudioLiveUserRoot> call = RetrofitBuilder.create()
                .getLiveUsersList(sessionManager.getUser().getId(), type, "", start,Const.LIMIT,countryCode);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PkAudioLiveUserRoot> call,
                                   @NonNull Response<PkAudioLiveUserRoot> response) {

                if (callVersion != requestVersion) {
                    Log.d(TAG, "Ignoring stale pagination response");
                    return;
                }
                isFirstTimeLoading.set(false);

                isLoading = false;

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<PkAudioLiveUserRoot.UsersItem> users = response.body().getUsers();
                    if (users == null) users = new ArrayList<>();
                    handleUsersResponse(users, isLoadMore);

                    // ADD: mark last successful load to prevent refresh spam on every onResume()
                    lastLoadedAt = System.currentTimeMillis();
                } else {
                    Log.w(TAG, "Unsuccessful response or invalid data: " + response.code());
                }
                isLoadingComplete.postValue(true);
            }

            @Override
            public void onFailure(@NonNull Call<PkAudioLiveUserRoot> call, @NonNull Throwable t) {
                Log.e(TAG, "Data fetch failed", t);
                isFirstTimeLoading.set(false);
                isLoadingComplete.postValue(true);
                isLoading = false;
            }
        });
    }

    public void refreshSilentlyIfStale() {
//        long now = System.currentTimeMillis();
//        if (now - lastLoadedAt >= FOREGROUND_REFRESH_WINDOW_MS) {
//            getData(false,true);
//        }

        if (liveUserList.isEmpty()) {
            // don't silent refresh before first real fetch
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastLoadedAt >= FOREGROUND_REFRESH_WINDOW_MS) {
            getData(false,true);
        }
    }

    private void fetchBlockedByUserData() {
        Call<BlockedUserResponse> call = RetrofitBuilder.create()
                .getUsersWhoBlockedMe(sessionManager.getUser().getId());

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BlockedUserResponse> call, @NonNull Response<BlockedUserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BlockedUserResponse.BlockedByUsersItem> blockedUsers = response.body().getBlockedByUsers();
                    if (blockedUsers == null) blockedUsers = new ArrayList<>();
                    List<String> tempBlockedIds = new ArrayList<>();
                    for (BlockedUserResponse.BlockedByUsersItem blockedUser : blockedUsers) {
                        if (blockedUser != null && blockedUser.getUserId() != null) {
                            tempBlockedIds.add(blockedUser.getUserId().getId());
                        }
                    }
                    blockedUserIds.clear();
                    blockedUserIds.addAll(tempBlockedIds);
                    sessionManager.setBlockedUserIds(tempBlockedIds);

                    Log.d(TAG, "Blocked user IDs updated: " + blockedUserIds);
                } else {
                    Log.e(TAG, "Failed to fetch blocked users: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BlockedUserResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error fetching blocked users", t);
            }
        });
    }


    public void onCountryChanged(String newCountry) {
        if (newCountry == null) return;
        if (newCountry.equals(this.countryCode)) return;

        this.countryCode = newCountry;

        requestVersion++; // 🔥 invalidate old requests

        isLoading = false;
        start = 0;
        hasMoreData = true;

        liveUserList.clear();
        liveListAdapter.submitList(new ArrayList<>());

        noDataFound.set(false);
        isFirstTimeLoading.set(true);

        fetchBlockedByUserData();
        fetchFirstPageForce();
    }



    private void fetchFirstPageForce() {

        final int callVersion = requestVersion; // ✅ capture version

        if (callVersion != requestVersion) {
            Log.d(TAG, "Ignoring stale response");
            return;
        }

        isLoading = true;
        updateAdapterMode(type);

        Call<PkAudioLiveUserRoot> call = RetrofitBuilder.create()
                .getLiveUsersList(
                        sessionManager.getUser().getId(),
                        type,
                        "",
                        0,
                        Const.LIMIT,
                        countryCode
                );

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(
                    @NonNull Call<PkAudioLiveUserRoot> call,
                    @NonNull Response<PkAudioLiveUserRoot> response
            ) {

                // 🔥 IGNORE OLD RESPONSE
                if (callVersion != requestVersion) {
                    Log.d(TAG, "Ignoring stale response");
                    return;
                }

                isLoading = false;
                isFirstTimeLoading.set(false);

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().isStatus()) {

                    handleUsersResponse(response.body().getUsers(), false);
                    lastLoadedAt = System.currentTimeMillis();

                } else {
                    noDataFound.set(true);
                }

                isLoadingComplete.postValue(true);
            }

            @Override
            public void onFailure(@NonNull Call<PkAudioLiveUserRoot> call, @NonNull Throwable t) {

                if (callVersion != requestVersion) return;

                isLoading = false;
                isFirstTimeLoading.set(false);
                noDataFound.set(true);
                isLoadingComplete.postValue(true);
            }
        });
    }




    public void resetPagination() {
        isFirstTimeLoading.set(true);
        start = 0;
        hasMoreData = true;
        liveUserList.clear();
//        liveListAdapter.submitList(new ArrayList<>());
    }



    private void updateAdapterMode(String type) {
        switch (type) {
            case "AudioLive" -> liveListAdapter.updateViewMode(LiveListAdapter.PARTY_MODE);
            default -> liveListAdapter.updateViewMode(LiveListAdapter.LIVE_LIST_MODE);
        }
    }

    private void handleUsersResponse(
            @Nullable List<PkAudioLiveUserRoot.UsersItem> users,
            boolean isLoadMore
    ) {

        List<PkAudioLiveUserRoot.UsersItem> mergedList = new ArrayList<>();

        // 🔹 First page reset
        if (!isLoadMore) {
            liveUserList.clear();
        }

        // 🔹 Add real users if available
        if (users != null && !users.isEmpty()) {
            mergedList.addAll(users);
            hasMoreData = users.size() >= Const.LIMIT;
        } else {
            hasMoreData = false;
        }

        // 🔹 ALWAYS add fake users on first page
        if (!isLoadMore) {
            addFilteredFakeUsers(mergedList);
        }

        // 🔥 EVEN INDEX ONLY ON FIRST PAGE
        if (!isLoadMore) {
            mergedList = filterEvenIndexOnly(mergedList);
        }

        // 🔹 Append result
        liveUserList.addAll(mergedList);

        noDataFound.set(liveUserList.isEmpty());

        liveListAdapter.submitList(new ArrayList<>(liveUserList));
    }



    private void addFilteredFakeUsers(
            List<PkAudioLiveUserRoot.UsersItem> targetList
    ) {
        List<PkAudioLiveUserRoot.UsersItem> fakeList =
                sessionManager.getShuffledFakeLiveList();

        if (fakeList == null || fakeList.isEmpty()) return;

        for (PkAudioLiveUserRoot.UsersItem fake : fakeList) {
            if (shouldIncludeFakeUser(fake)) {
                targetList.add(fake);
            }
        }
    }


    private boolean shouldIncludeFakeUser(PkAudioLiveUserRoot.UsersItem user) {
        return switch (type) {
            case "NormalLive" -> !user.isAudio() && !user.isIsPkMode();
            case "AudioLive" -> user.isAudio();
            case "PkLive" -> user.isIsPkMode();
            default -> true; // "All"
        };
    }

    @Override
    protected void onCleared() {
        liveListAdapter.submitList(new ArrayList<>());
        super.onCleared();
    }

    private List<PkAudioLiveUserRoot.UsersItem> filterEvenIndexOnly(
            List<PkAudioLiveUserRoot.UsersItem> list
    ) {
        List<PkAudioLiveUserRoot.UsersItem> filtered = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (i % 2 == 0) {
                filtered.add(list.get(i));
            }
        }
        return filtered;
    }



}
