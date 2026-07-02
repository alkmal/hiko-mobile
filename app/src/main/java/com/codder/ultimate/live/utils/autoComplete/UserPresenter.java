package com.codder.ultimate.live.utils.autoComplete;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.codder.ultimate.SessionManager;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.GuestUsersListRoot;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UserPresenter extends RecyclerViewPresenter<GuestProfileRoot.User> {

    private static final String TAG = "UserPresenter";
    private final Context mContext;
    SessionManager sessionManager;
    private UserAdapter mAdapter;
    private Call<GuestUsersListRoot> call;

    public UserPresenter(Context context) {
        super(context);
        mContext = context;
        sessionManager = new SessionManager(context);
    }

    @Override
    protected UserAdapter instantiateAdapter() {
        return mAdapter = new UserAdapter(this::dispatchClick);
    }

    @Override
    protected void onQuery(@Nullable CharSequence q) {
        Log.v(TAG, "Querying '" + q + "' for users autocomplete.");
        if (call != null) {
            call.cancel();
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", sessionManager.getUser().getId());
        jsonObject.addProperty("value", q.toString());
        call = RetrofitBuilder.create().searchUser(jsonObject);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GuestUsersListRoot> call, Response<GuestUsersListRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GuestUsersListRoot result = response.body();
                    if (result.isStatus() && result.getUser() != null && !result.getUser().isEmpty()) {
                        Log.d(TAG, "Autocomplete returned " + result.getUser().size() + " users.");
                        mAdapter.submitData(result.getUser());
                    } else {
                        Log.i(TAG, "No users found for query: " + q);
                        mAdapter.submitData(new ArrayList<>());
                    }
                } else {
                    Log.e(TAG, "Autocomplete failed: HTTP " + response.code() + " - " + response.message());
                    mAdapter.submitData(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<GuestUsersListRoot> call, Throwable t) {
                Log.e(TAG, "Autocomplete network error: " + t.getLocalizedMessage(), t);
                mAdapter.submitData(new ArrayList<>());
            }
        });
    }
}
