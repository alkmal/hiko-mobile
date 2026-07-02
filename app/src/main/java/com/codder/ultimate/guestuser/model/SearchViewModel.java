package com.codder.ultimate.guestuser.model;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.SessionManager;

import com.codder.ultimate.adapter.SearchHistoryAdapter;
import com.codder.ultimate.guestuser.adapter.SearchUserAdapter;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.GuestUsersListRoot;
import com.codder.ultimate.modelclass.SearchHistoryRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchViewModel extends ViewModel {

    public SearchUserAdapter searchUserAdapter = new SearchUserAdapter();
    public SearchHistoryAdapter searchHistoryAdapter = new SearchHistoryAdapter();

    private final SessionManager sessionManager;

    public MutableLiveData<SearchState> searchState = new MutableLiveData<>(SearchState.SHOW_HISTORY);
    public MutableLiveData<Boolean> isLoadCompleted = new MutableLiveData<>(false);

    public MutableLiveData<List<SearchHistoryRoot.DataItem>> historyList = new MutableLiveData<>();

    public ObservableBoolean noUserData = new ObservableBoolean(false);
    public ObservableBoolean noHistoryData = new ObservableBoolean(false);
    public ObservableBoolean showUserShimmer = new ObservableBoolean(false);
    public ObservableBoolean showHistoryShimmer = new ObservableBoolean(false);


    public String keyword = "";

    private int userStart = 0;
    private int historyStart = 1;
    private final List<GuestProfileRoot.User> allUsers = new ArrayList<>();
    private final List<SearchHistoryRoot.DataItem> allHistory = new ArrayList<>();

    public enum SearchState { SHOW_HISTORY, SHOW_RESULTS }

    public SearchViewModel(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    public void onKeywordChanged(String text) {
        keyword = text;

        if (searchRunnable != null) handler.removeCallbacks(searchRunnable);

        searchRunnable = () -> {
            if (text.isEmpty()) {
                searchState.postValue(SearchState.SHOW_HISTORY);
                showUserShimmer.set(false);      // ← stop shimmer
                getHistory(false);
            } else {
                searchState.postValue(SearchState.SHOW_RESULTS);

                // DO NOT SHOW shimmer on user search
                showUserShimmer.set(false);      // ← important

                getUsers(false);
            }
        };

        handler.postDelayed(searchRunnable, 300);
    }



    // =============== USERS API ===============
    private Call<GuestUsersListRoot> userCall;
    public void getUsers(boolean isLoadMore) {
        if (isLoadMore) {
            userStart += Const.LIMIT;
        } else {
            userStart = 0;
            allUsers.clear();
            noUserData.set(false);
        }

        String userId = sessionManager.getUser() != null ? sessionManager.getUser().getId() : "";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("value", keyword);
        jsonObject.addProperty("start", userStart);
        jsonObject.addProperty("limit", Const.LIMIT);

        if (userCall != null) userCall.cancel();

        noUserData.set(false);
        userCall = RetrofitBuilder.create().searchUser(jsonObject);
        userCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GuestUsersListRoot> call, Response<GuestUsersListRoot> response) {
                isLoadCompleted.postValue(true);
                showUserShimmer.set(false);
                if (!keyword.equals(jsonObject.get("value").getAsString())) return;
                if (response.isSuccessful() && response.body() != null) {
                    GuestUsersListRoot result = response.body();
                    List<GuestProfileRoot.User> users = result.getUser();

                    if (result.isStatus() && users != null && !users.isEmpty()) {
                        allUsers.addAll(users);
                        showUserShimmer.set(false);
                        noUserData.set(false);
                        searchUserAdapter.submitList(new ArrayList<>(allUsers));
                    } else if (userStart == 0) {
                        noUserData.set(true);
                        showUserShimmer.set(false);
                        searchUserAdapter.submitList(new ArrayList<>());

                        Log.d("SearchUser", "No users found for keyword: " + keyword);
                    }
                } else if (userStart == 0) {
                    noUserData.set(true);
                    showUserShimmer.set(false);
                    searchUserAdapter.submitList(new ArrayList<>());
                }

                isLoadCompleted.postValue(true);
            }

            @Override
            public void onFailure(Call<GuestUsersListRoot> call, Throwable t) {
                isLoadCompleted.postValue(true);
                Log.e("SearchUser", "Search failed: " + t.getLocalizedMessage(), t);
                if (userStart == 0) {
                    noUserData.set(true);
                    showUserShimmer.set(false);
                    searchUserAdapter.submitList(new ArrayList<>());
                }
            }
        });
    }
    // =============== HISTORY API ===============
    public void getHistory(boolean loadMore) {
        searchState.postValue(SearchState.SHOW_HISTORY);
        if (loadMore) {
            historyStart += Const.LIMIT;
        } else {
            historyStart = 1;
            allHistory.clear();
            noHistoryData.set(false);
            showHistoryShimmer.set(true);
        }

        RetrofitBuilder.create().getSearchHistory(
                sessionManager.getUser().getId(),
                historyStart,
                Const.LIMIT
        ).enqueue(new Callback<SearchHistoryRoot>() {
            @Override
            public void onResponse(Call<SearchHistoryRoot> call, Response<SearchHistoryRoot> res) {

                isLoadCompleted.postValue(true);
                showHistoryShimmer.set(false);

                if (res.isSuccessful() && res.body() != null) {
                    List<SearchHistoryRoot.DataItem> list = res.body().getData();

                    if (list != null && !list.isEmpty()) {
                        allHistory.clear();
                        allHistory.addAll(list);
                        noHistoryData.set(false);
                        searchHistoryAdapter.submitList(new ArrayList<>(allHistory));
                        historyList.postValue(new ArrayList<>(allHistory));
                    } else if (historyStart == 1) {
                        noHistoryData.set(true);
                        searchHistoryAdapter.submitList(new ArrayList<>());
                        historyList.postValue(new ArrayList<>());
                    }
                }
            }

            @Override
            public void onFailure(Call<SearchHistoryRoot> call, Throwable t) {
                isLoadCompleted.postValue(true);
                showHistoryShimmer.set(false);
                noHistoryData.set(true);
                historyList.postValue(new ArrayList<>());
                searchHistoryAdapter.submitList(new ArrayList<>());
            }
        });
    }
}



