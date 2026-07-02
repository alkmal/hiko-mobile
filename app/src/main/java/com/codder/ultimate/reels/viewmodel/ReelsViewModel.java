package com.codder.ultimate.reels.viewmodel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.SessionManager;
import com.codder.ultimate.reels.adapter.ReelsAdapter;
import com.codder.ultimate.reels.model.ReliteRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReelsViewModel extends ViewModel {

    public final ReelsAdapter reelsAdapter = new ReelsAdapter();
    public final ObservableBoolean isFirstTimeLoading = new ObservableBoolean(true);
    public final ObservableBoolean isLoadMoreLoading = new ObservableBoolean(true);
    public final ObservableBoolean noData = new ObservableBoolean(false);
    public final MutableLiveData<Boolean> isLoadCompleted = new MutableLiveData<>();
    private final HashMap<String, ReliteRoot.VideoItem> likeStateCache = new HashMap<>();

    public int start = 0;
    private String type = "ALL";
    private SessionManager sessionManager;
    private Context context;

    public void init(Context context) {
        this.context = context;
        this.sessionManager = new SessionManager(context);
    }

    public void getReliteData(boolean isLoadMore, String userId, boolean isFromMainFragment, boolean isFromOtherProfile, int pos) {
        if (sessionManager == null || userId == null) {
            Log.e("ReelsViewModel", "SessionManager or userId is null");
            return;
        }

        if (userId.equals(sessionManager.getUser().getId())) type = "profile";
        if (isFromOtherProfile) type = "profile";
        if (isFromMainFragment) type = "ALL";

        if (isLoadMore) {
            start += 1;
            isLoadMoreLoading.set(true);
        } else {
            start = 0;
            isFirstTimeLoading.set(true);
        }

        noData.set(false);
        isLoadCompleted.postValue(false);

        Call<ReliteRoot> call = RetrofitBuilder.create().getRelites(userId, type, start, Const.LIMIT);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ReliteRoot> call, Response<ReliteRoot> response) {
                isLoadCompleted.postValue(true);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<ReliteRoot.VideoItem> videos = response.body().getVideo();
                    if (videos != null && !videos.isEmpty()) {

                        for (ReliteRoot.VideoItem item : videos) {
                            if (likeStateCache.containsKey(item.getId())) {
                                ReliteRoot.VideoItem cached = likeStateCache.get(item.getId());
                                item.setLike(cached.isLike());
                                item.setLikeCount(cached.getLikeCount());
                            }
                        }

                        if (isLoadMore) {
                            List<ReliteRoot.VideoItem> currentList = new ArrayList<>(reelsAdapter.getCurrentList());
                            currentList.addAll(videos);
                            reelsAdapter.submitList(currentList);
                        } else {
                            reelsAdapter.submitList(new ArrayList<>(videos));
                            reelsAdapter.playVideoAt(pos);
                        }
                    } else if (!isLoadMore) {
                        noData.set(true);
                        Log.d("ReelsViewModel", "No videos found.");
                    }
                } else if (!isLoadMore) {
                    noData.set(true);
                    Log.w("ReelsViewModel", "API response failed or no data.");
                }
            }

            @Override
            public void onFailure(Call<ReliteRoot> call, Throwable t) {
                isLoadCompleted.postValue(true);
                noData.set(true);
                Log.e("ReelsViewModel", "API call failed: " + t.getMessage(), t);
            }
        });
    }

    public void cacheLikeState(String videoId, boolean isLiked, int likeCount) {
        ReliteRoot.VideoItem item = likeStateCache.getOrDefault(videoId, new ReliteRoot.VideoItem());
        item.setId(videoId);
        item.setLike(isLiked);
        item.setLikeCount(likeCount);
        likeStateCache.put(videoId, item);
    }

}

