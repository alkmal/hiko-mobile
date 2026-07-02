package com.codder.ultimate.live.model;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.retrofit.RetrofitBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReactionsViewModel extends ViewModel {
    private static final String TAG = "ReactionsViewModel";
    public MutableLiveData<List<ReactionRoot.DataItem>> reactionsMutableLiveData = new MutableLiveData<>();

    public void loadReactions(OnLoadComplete onLoadComplete) {

        RetrofitBuilder.create().getReactions().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ReactionRoot> call, Response<ReactionRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReactionRoot reactionRoot = response.body();
                    if (reactionRoot.isStatus() && !reactionRoot.getData().isEmpty()) {
                        reactionsMutableLiveData.setValue(reactionRoot.getData());
                        onLoadComplete.onLoadComplete(reactionRoot.getData());
                    } else {
                        Log.w(TAG, "onResponse: No data or status is false.");
                    }
                } else {
                    Log.w(TAG, "onResponse: Unsuccessful response, code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ReactionRoot> call, Throwable t) {
                Log.e(TAG, "onFailure: Error loading reactions", t);
            }
        });
    }

    public interface OnLoadComplete {
        void onLoadComplete(List<ReactionRoot.DataItem> data);
    }
}
