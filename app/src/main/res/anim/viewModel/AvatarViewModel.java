package com.example.rayzi.viewModel;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.rayzi.R;
import com.example.rayzi.SessionManager;
import com.example.rayzi.adapter.AvatarListAdapter;
import com.example.rayzi.databinding.ItemAvatarListBinding;
import com.example.rayzi.modelclass.SvgaListRoot;
import com.example.rayzi.modelclass.UserRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.google.gson.JsonObject;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvatarViewModel extends ViewModel implements Observable {
    private static final String TAG = "SvgaViewModel";
    public ObservableBoolean isFirstTimeLoading = new ObservableBoolean(true);
    public ObservableBoolean noData = new ObservableBoolean(true);
    public MutableLiveData<Boolean> isLoadingComplete = new MutableLiveData<>();
    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();
    public AvatarListAdapter avatarListAdapter = new AvatarListAdapter();
    public MutableLiveData<Boolean> isPurchased = new MutableLiveData<>();
    private int start = 0;
    private SessionManager sessionManager;
    Context context;

    public void init(Context context) {
        this.context = context;
        sessionManager = new SessionManager(context);
    }

    public void getAvatarList(boolean isLoadMore) {
        if (isLoadMore) {
            start += Const.LIMIT;
        } else {
            start = 0;
            avatarListAdapter.clear();
            isFirstTimeLoading.set(true);
        }
        noData.set(false);
        Call<SvgaListRoot> call = RetrofitBuilder.create().getSvgaList(sessionManager.getUser().getId(), "frame", start, Const.LIMIT);
        call.enqueue(new Callback<SvgaListRoot>() {
            @Override
            public void onResponse(Call<SvgaListRoot> call, Response<SvgaListRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && response.body().getData() != null) {
                        if (!response.body().getData().isEmpty()) {
                            avatarListAdapter.addData(response.body().getData());
                            Log.d(TAG, "onResponse: " + response.body().getData());
                        } else {
                            if (start == 0) {
                                noData.set(true);
                            }
                        }
                        isFirstTimeLoading.set(false);
                        isLoadingComplete.postValue(true);
                    }
                }
            }
            @Override
            public void onFailure(Call<SvgaListRoot> call, Throwable t) {
                Log.d(TAG, "onFailure: SvgaListRoot =====" + t.getMessage());
            }
        });
    }

    public void purchaseSvga(String id, String type, ItemAvatarListBinding binding1, SvgaListRoot.DataItem svgaItem) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", sessionManager.getUser().getId());
        jsonObject.addProperty("Id", id);
        Call<UserRoot> call = RetrofitBuilder.create().purchaseSvga(type, jsonObject);
        call.enqueue(new Callback<UserRoot>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                Log.d(TAG, "onResponse:    ===== ");
                if (response.code() == 200) {
                    if (response.body().isStatus() && response.body().getUser() != null) {
                        Log.d(TAG, "onResponse:  ======= ");
                        sessionManager.saveUser(response.body().getUser());
                        svgaItem.setPurchase(true);
                        binding1.btnpurchase.setImageResource(R.drawable.select_btn);
                        avatarListAdapter.notifyDataSetChanged();
                        isPurchased.postValue(true);
                    }
                }
            }
            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                Log.d(TAG, "onFailure: UserRoot " + t.getMessage());
            }
        });
    }

    public void selectSvga(String id, String type, ItemAvatarListBinding binding1, SvgaListRoot.DataItem svgaItem, boolean isSelected) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", sessionManager.getUser().getId());
        jsonObject.addProperty("Id", id);
        jsonObject.addProperty("selectType", isSelected);
        jsonObject.addProperty("type", type);
        Call<UserRoot> call = RetrofitBuilder.create().selectSvga(jsonObject);
        call.enqueue(new Callback<UserRoot>() {
            @Override
            public void onResponse(@NonNull Call<UserRoot> call, @NonNull Response<UserRoot> response) {
                if (response.code() == 200) {
                    if (Objects.requireNonNull(response.body()).isStatus() && response.body().getUser() != null) {
                        sessionManager.saveUser(response.body().getUser());
                        if (!isSelected) {
                            svgaItem.setSelected(false);
                            binding1.btnpurchase.setImageResource(R.drawable.select_btn);
                        } else {
                            svgaItem.setSelected(true);
                            binding1.btnpurchase.setImageResource(R.drawable.selected_btn);
                        }
                        avatarListAdapter.notifyDataSetChanged();
                        isPurchased.postValue(true);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserRoot> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: UserRoot " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        avatarListAdapter.clear();
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }
}
