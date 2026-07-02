package com.codder.ultimate.live.utils.autoComplete;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.codder.ultimate.R;
import com.codder.ultimate.live.model.HashtagsRoot;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HashtagPresenter extends RecyclerViewPresenter<HashtagsRoot.HashtagItem> {

    private static final String TAG = "HashtagPresenter";
    private final Context mContext;
    private HashtagAdapter mAdapter;
    private Call<HashtagsRoot> hashtagsRootCall;

    public HashtagPresenter(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected HashtagAdapter instantiateAdapter() {
        return mAdapter = new HashtagAdapter(mContext, this::dispatchClick);
    }

    @Override
    protected void onQuery(@Nullable CharSequence q) {
        Log.d(TAG, "Querying '" + q + "' for hashtags autocomplete.");
        if (hashtagsRootCall != null) {
            hashtagsRootCall.cancel();
        }

        hashtagsRootCall = RetrofitBuilder.create().searchHashtag(q.toString());
        hashtagsRootCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<HashtagsRoot> call, Response<HashtagsRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HashtagsRoot hashtagsRoot = response.body();
                    if (hashtagsRoot.isStatus() && !hashtagsRoot.getHashtag().isEmpty()) {
                        Log.d(TAG, "onResponse: Hashtag data: " + hashtagsRoot.getHashtag());
                        mAdapter.submitData(hashtagsRoot.getHashtag());
                    } else {
                        Log.w(TAG, "onResponse: No hashtags found or status is false.");
                    }
                } else {
                    Log.w(TAG, "onResponse: Unsuccessful response, code: " + response.code());
                    Toast.makeText(mContext, getContext().getString(R.string.failed_to_retrieve_hashtags), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HashtagsRoot> call, Throwable t) {
                Log.e(TAG, "onFailure: Error fetching hashtags", t);
            }
        });
    }
}