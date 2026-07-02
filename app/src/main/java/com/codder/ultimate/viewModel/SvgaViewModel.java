package com.codder.ultimate.viewModel;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.ItemSvgaListBinding;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.profile.adapter.SvgaListAdapter;
import com.codder.ultimate.profile.modelclass.SvgaListRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SvgaViewModel extends AndroidViewModel implements Observable {

    private static final String TAG = "SvgaViewModel";

    public final ObservableBoolean isFirstTimeLoading = new ObservableBoolean(true);
    public final ObservableBoolean noData = new ObservableBoolean(true);
    public final MutableLiveData<Boolean> isLoadingComplete = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> isPurchased = new MutableLiveData<>(false);

    public final SvgaListAdapter svgaListAdapter;

    private final PropertyChangeRegistry callbacks = new PropertyChangeRegistry();
    private final SessionManager sessionManager;
    private int start = 0;

    public SvgaViewModel(@NonNull Application application) {
        super(application);
        this.sessionManager = new SessionManager(application.getApplicationContext());
        this.svgaListAdapter = new SvgaListAdapter();
    }

    public void getSvgaList(boolean isLoadMore, @NonNull String type) {
        if (sessionManager.getUser() == null) {
            Log.w(TAG, "SessionManager or User is null.");
            return;
        }

        if (isLoadMore) {
            start += Const.LIMIT;
        } else {
            start = 0;
            svgaListAdapter.submitList(new ArrayList<>());
            isFirstTimeLoading.set(true);
        }

        noData.set(false);

        Call<SvgaListRoot> call = RetrofitBuilder.create().getSvgaList(sessionManager.getUser().getId(), type, start, Const.LIMIT);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<SvgaListRoot> call, Response<SvgaListRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SvgaListRoot result = response.body();

                    if (result.isStatus() && result.getData() != null) {
                        List<SvgaListRoot.DataItem> data = result.getData();

                        if (!data.isEmpty()) {
                            List<SvgaListRoot.DataItem> currentList = new ArrayList<>(svgaListAdapter.getCurrentList());
                            currentList.addAll(data);
                            svgaListAdapter.submitList(currentList);
                            Log.d(TAG, "onResponse: " + result.getData());
                        }  else if (start == 0) {
                            noData.set(true);  // Show "no data" if no items for a fresh load
                            Log.d(TAG, "onResponse: No data found.");
                        }
                    }else {
                        Log.e(TAG, "onResponse: Error status or null data in response");
                        noData.set(true);
                    }
                    isFirstTimeLoading.set(false);
                    isLoadingComplete.postValue(true);
                }else {
                    Log.e(TAG, "onResponse: Failed with status code: " + response.code());
                    noData.set(true);
                }
            }

            @Override
            public void onFailure(Call<SvgaListRoot> call, Throwable t) {
                Log.e(TAG, "onFailure: Error fetching Svga list", t);
                isFirstTimeLoading.set(false);
                isLoadingComplete.postValue(true);
                noData.set(true);
            }
        });
    }

    public void purchaseSvga(@NonNull String id, @NonNull String type, @NonNull ItemSvgaListBinding binding, @NonNull SvgaListRoot.DataItem svgaItem) {
        if (sessionManager.getUser() == null) {
            Log.w(TAG, "Cannot purchase SVGA: session or user is null.");
            return;
        }
        binding.btnPurchase.setEnabled(false);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", sessionManager.getUser().getId());
        jsonObject.addProperty("Id", id);

        RetrofitBuilder.create().purchaseSvga(type, jsonObject)
                .enqueue(new Callback<UserRoot>() {
                    @Override
                    public void onResponse(@NonNull Call<UserRoot> call, @NonNull Response<UserRoot> response) {
                        binding.btnPurchase.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            sessionManager.saveUser(response.body().getUser());
                            svgaItem.setPurchase(true);
                            binding.btnPurchase.setText(R.string.select);
                            binding.btnPurchase.setBackground(ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.btn_bg));
                            svgaListAdapter.notifyDataSetChanged();
                            isPurchased.postValue(true);
                            Log.d(TAG, "SVGA item purchased successfully.");
                        } else {
                            Log.w(TAG, "Purchase failed or invalid response.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserRoot> call, @NonNull Throwable t) {
                        binding.btnPurchase.setEnabled(true);
                        Log.e(TAG, "Error during SVGA purchase: ", t);
                    }
                });
    }

    public void selectSvga(@NonNull String id, @NonNull String type, @NonNull ItemSvgaListBinding binding, @NonNull SvgaListRoot.DataItem svgaItem, boolean isSelected) {
        if (sessionManager.getUser() == null) {
            Log.w(TAG, "Cannot select SVGA: session or user is null.");
            return;
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", sessionManager.getUser().getId());
        jsonObject.addProperty("Id", id);
        jsonObject.addProperty("selectType", isSelected);
        jsonObject.addProperty("type", type);

        RetrofitBuilder.create().selectSvga(jsonObject)
                .enqueue(new Callback<UserRoot>() {
                    @Override
                    public void onResponse(@NonNull Call<UserRoot> call, @NonNull Response<UserRoot> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            sessionManager.saveUser(response.body().getUser());
                            svgaItem.setSelected(isSelected);
                            if (!isSelected) {
                                svgaItem.setSelected(false);
                                binding.btnPurchase.setText(R.string.select);
                                binding.btnPurchase.setBackground(ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.btn_bg));
                                binding.btnPurchase.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                            } else {
                                svgaItem.setSelected(true);
                                binding.btnPurchase.setText(R.string.selected);
                                binding.btnPurchase.setBackground(ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.selected_btn_bg));

                                Drawable endDrawable = ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.icon_selected);
                                binding.btnPurchase.setCompoundDrawablesWithIntrinsicBounds(null, null, endDrawable, null);
                            }
                            svgaListAdapter.notifyDataSetChanged();
                            isPurchased.postValue(true);
                            Log.d(TAG, "SVGA item selection updated.");
                        } else {
                            Log.w(TAG, "Selection failed or invalid response.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserRoot> call, @NonNull Throwable t) {
                        Log.e(TAG, "Error during SVGA selection: ", t);
                    }
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        svgaListAdapter.submitList(new ArrayList<>());
        callbacks.clear();
        Log.d(TAG, "ViewModel cleared.");
    }

    @Override
    public void addOnPropertyChangedCallback(@NonNull OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(@NonNull OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    public double getUserDiamonds() {
        UserRoot.User user = sessionManager.getUser();
        return (user != null) ? user.getDiamond() : 0;
    }

    public String getAvatarFrameImage() {
        UserRoot.User user = sessionManager.getUser();
        return (user != null) ? String.valueOf(user.getAvatarFrame()) : "";
    }

    public String getUserImage() {
        UserRoot.User user = sessionManager.getUser();
        return (user != null) ? user.getImage() : "";
    }

}
