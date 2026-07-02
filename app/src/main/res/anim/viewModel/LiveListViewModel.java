package com.example.rayzi.viewModel;

import android.content.Context;
import android.util.Log;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.rayzi.SessionManager;
import com.example.rayzi.liveStreamming.LiveListAdapter;
import com.example.rayzi.pkRoot.PkAudioLiveUserRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LiveListViewModel extends ViewModel {
    private static final String TAG = "LiveListViewModel";
    public ObservableBoolean noDataFound = new ObservableBoolean(false);
    public ObservableBoolean isFirstTimeLoading = new ObservableBoolean(false);
    public MutableLiveData<Boolean> isLoadingComplete = new MutableLiveData<>();
    public LiveListAdapter liveListAdapter = new LiveListAdapter(LiveListAdapter.LIVELIST_MODE);
    List<PkAudioLiveUserRoot.UsersItem> liveUserList = new ArrayList<>();
    private int start = 0;
    private SessionManager sessionManager;
    private String type;

    public void init(Context context, String type) {
        sessionManager = new SessionManager(context);
        this.type = type;
    }

    public void getData(boolean isLoadMore) {
        if (isLoadMore) {
            start = start + Const.LIMIT;
        } else {
            isFirstTimeLoading.set(true);
            start = 0;
            liveUserList.clear();
            liveListAdapter.clear();
        }

        if (type.equals("AudioLive")) {
            liveListAdapter.updateViewMode(LiveListAdapter.PARTY_MODE);
        } else {
            liveListAdapter.updateViewMode(LiveListAdapter.LIVELIST_MODE);
        }

//        noDataFound.set(true);
        Call<PkAudioLiveUserRoot> call = RetrofitBuilder.create().getLiveUsersList(sessionManager.getUser().getId(), type, "", start, Const.LIMIT,"global");
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PkAudioLiveUserRoot> call, Response<PkAudioLiveUserRoot> response) {
                if (response.code() == 200) {
                    assert response.body() != null;
                    Log.d(TAG, "onResponse: ========");
                    if (response.body().isStatus()) {
                        if (!response.body().getUsers().isEmpty()) {
//                            noDataFound.set(false);
                            if (liveUserList != null) {
                                liveUserList.clear();
                                liveUserList.addAll(response.body().getUsers());
                                Log.d(TAG, "onResponse: ============" + sessionManager.getShuffledFakeLiveList());
                                switch (type) {

                                    case "All" ->
                                            liveUserList.addAll(sessionManager.getShuffledFakeLiveList());

                                    case "NormalLive" -> {
                                        for (int i = 0; i < sessionManager.getShuffledFakeLiveList().size(); i++) {
                                            if (!sessionManager.getShuffledFakeLiveList().get(i).isAudio() && !sessionManager.getShuffledFakeLiveList().get(i).isIsPkMode()) {
                                                liveUserList.add(sessionManager.getShuffledFakeLiveList().get(i));
                                            }
                                        }
                                    }
                                    case "AudioLive" -> {
                                        for (int i = 0; i < sessionManager.getShuffledFakeLiveList().size(); i++) {
                                            if (sessionManager.getShuffledFakeLiveList().get(i).isAudio()) {
                                                liveUserList.add(sessionManager.getShuffledFakeLiveList().get(i));
                                            }
                                        }
                                    }
                                    case "PkLive" -> {
                                        for (int i = 0; i < sessionManager.getShuffledFakeLiveList().size(); i++) {
                                            if (sessionManager.getShuffledFakeLiveList().get(i).isIsPkMode()) {
                                                liveUserList.add(sessionManager.getShuffledFakeLiveList().get(i));
                                            }
                                        }
                                    }
                                }
                                if(liveUserList.isEmpty()){
                                    noDataFound.set(true);
                                }
                                liveListAdapter.addData(liveUserList);
                            }
                        } else if (start == 0 && response.body().getUsers().isEmpty()) {
//                            noDataFound.set(false);
                            if (liveUserList != null) {
                                switch (type) {
                                    case "All" ->
                                            liveUserList.addAll(sessionManager.getShuffledFakeLiveList());
                                    case "NormalLive" -> {
                                        for (int i = 0; i < sessionManager.getShuffledFakeLiveList().size(); i++) {
                                            if (!sessionManager.getShuffledFakeLiveList().get(i).isAudio() && !sessionManager.getShuffledFakeLiveList().get(i).isIsPkMode()) {
                                                liveUserList.add(sessionManager.getShuffledFakeLiveList().get(i));
                                            }
                                        }
                                    }
                                    case "AudioLive" -> {
                                        for (int i = 0; i < sessionManager.getShuffledFakeLiveList().size(); i++) {
                                            if (sessionManager.getShuffledFakeLiveList().get(i).isAudio()) {
                                                liveUserList.add(sessionManager.getShuffledFakeLiveList().get(i));
                                            }
                                        }
                                    }
                                    case "PkLive" -> {
                                        for (int i = 0; i < sessionManager.getShuffledFakeLiveList().size(); i++) {
                                            if (sessionManager.getShuffledFakeLiveList().get(i).isIsPkMode()) {
                                                liveUserList.add(sessionManager.getShuffledFakeLiveList().get(i));
                                            }
                                        }
                                    }
                                }
                                if(liveUserList.isEmpty()){
                                    noDataFound.set(true);
                                }
                                liveListAdapter.addData(liveUserList);
                            }
                        }
                    }
                }
                isFirstTimeLoading.set(false);
                isLoadingComplete.postValue(true);
            }

            @Override
            public void onFailure(Call<PkAudioLiveUserRoot> call, Throwable t) {
                isLoadingComplete.postValue(true);
            }
        });
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        liveListAdapter.clear();
    }
}
