package com.codder.ultimate.viewModel;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.ItemAvatarListBinding;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.profile.adapter.AvatarListAdapter;
import com.codder.ultimate.profile.modelclass.SvgaListRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvatarViewModel extends ViewModel {
    private static final String TAG = "AvatarViewModel";

    private final MutableLiveData<List<SvgaListRoot.DataItem>> avatarListMutable = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Boolean> isFirstTimeLoadingMutable = new MutableLiveData<>(true);
    public final ObservableBoolean isFirstTimeLoading = new ObservableBoolean(true);


    private final MutableLiveData<Boolean> isLoadingMutable = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = isLoadingMutable;

    private final MutableLiveData<Boolean> isPurchasedMutable = new MutableLiveData<>(false);
    public LiveData<Boolean> isPurchased = isPurchasedMutable;

    private final MutableLiveData<Boolean> mutableNoData = new MutableLiveData<>(false);
    public LiveData<Boolean> noData = mutableNoData;

    private final MutableLiveData<Boolean> isLoadingCompleteMutable = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoadingComplete = isLoadingCompleteMutable;

    public AvatarListAdapter avatarListAdapter;
    private SessionManager sessionManager;
    private int start = 0;
    private final List<SvgaListRoot.DataItem> currentList = new ArrayList<>();
    private Context context;

    public void init(Context context) {
        this.context = context.getApplicationContext();
        this.sessionManager = new SessionManager(this.context);
        this.avatarListAdapter = new AvatarListAdapter(sessionManager, this::handleAvatarClick);
    }

    public void getAvatarList(boolean isLoadMore) {
        if (isLoadingMutable.getValue() != null && isLoadingMutable.getValue()) return;

        isLoadingMutable.setValue(true);
        if (isLoadMore) {
            start += Const.LIMIT;
        } else {
            start = 0;
            currentList.clear();
            isFirstTimeLoading.set(true);
        }

        Call<SvgaListRoot> call = RetrofitBuilder.create().getSvgaList(sessionManager.getUser().getId(), "frame", start, Const.LIMIT);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SvgaListRoot> call, @NonNull Response<SvgaListRoot> response) {
                isLoadingMutable.setValue(false);
                isLoadingCompleteMutable.postValue(true);
                isFirstTimeLoadingMutable.setValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<SvgaListRoot.DataItem> data = response.body().getData();
                    if (data != null && !data.isEmpty()) {
                        currentList.addAll(data);
                        avatarListMutable.setValue(new ArrayList<>(currentList));
                        avatarListAdapter.submitList(new ArrayList<>(currentList));
                        mutableNoData.setValue(false);
                    } else if (start == 0) {
                        Log.w(TAG, "getAvatarList: Error with response - Status: " + response.code() + ", Message: " + response.message());
                        mutableNoData.setValue(true);
                    }
                    isFirstTimeLoading.set(false);

                } else {
                    Log.w(TAG, "getAvatarList: Error with response - Status: " + response.code() + ", Message: " + response.message());
                    mutableNoData.setValue(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SvgaListRoot> call, @NonNull Throwable t) {
                Log.e(TAG, "getAvatarList: onFailure", t);
                isLoadingMutable.setValue(false);
                isLoadingCompleteMutable.postValue(true);
                isFirstTimeLoadingMutable.setValue(false);
                isFirstTimeLoading.set(false);
                mutableNoData.setValue(true);
            }
        });
    }

    public void purchaseSvga(String id, String type, ItemAvatarListBinding binding, SvgaListRoot.DataItem svgaItem) {
        if (svgaItem == null || id == null || id.isEmpty()) {
            Log.e(TAG, "purchaseSvga: Invalid parameters. svgaItem or id is null or empty.");
            return;
        }

        binding.btnPurchase.setEnabled(false);

        JsonObject json = new JsonObject();
        json.addProperty("userId", sessionManager.getUser().getId());
        json.addProperty("Id", id);

        Call<UserRoot> call = RetrofitBuilder.create().purchaseSvga(type, json);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserRoot> call, @NonNull Response<UserRoot> response) {
                binding.btnPurchase.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    sessionManager.saveUser(response.body().getUser());
                    svgaItem.setPurchase(true);
                    binding.btnPurchase.setText(R.string.select);
                    binding.btnPurchase.setBackground(ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.btn_bg));
                    avatarListAdapter.submitList(new ArrayList<>(currentList));
                    isPurchasedMutable.postValue(true);
                    Log.d(TAG, "onResponse: Purchase successful ");
                } else {
                    Log.w(TAG, "purchaseSvga: Purchase failed. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserRoot> call, @NonNull Throwable t) {
                binding.btnPurchase.setEnabled(true);  // Re-enable button on failure
                Log.e(TAG, "purchaseSvga: onFailure", t);

            }
        });
    }

    public void selectSvga(String id, String type, ItemAvatarListBinding binding, SvgaListRoot.DataItem svgaItem, boolean isSelected) {
        JsonObject json = new JsonObject();
        json.addProperty("userId", sessionManager.getUser().getId());
        json.addProperty("Id", id);
        json.addProperty("selectType", isSelected);
        json.addProperty("type", type);

        binding.btnPurchase.setEnabled(false);

        Call<UserRoot> call = RetrofitBuilder.create().selectSvga(json);
        call.enqueue(new Callback<UserRoot>() {
            @Override
            public void onResponse(@NonNull Call<UserRoot> call, @NonNull Response<UserRoot> response) {
                binding.btnPurchase.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    sessionManager.saveUser(response.body().getUser());

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
                    avatarListAdapter.notifyDataSetChanged();
                    isPurchasedMutable.postValue(true);
                }else {
                    Log.w(TAG, "selectSvga: Failed to update selection. Response: " + response.message());

                }
            }

            @Override
            public void onFailure(@NonNull Call<UserRoot> call, @NonNull Throwable t) {
                binding.btnPurchase.setEnabled(true);
                Log.e(TAG, "selectSvga: onFailure", t);
            }
        });
    }

    private void handleAvatarClick(SvgaListRoot.DataItem item, ItemAvatarListBinding binding) {
        if (item == null || binding == null) return;

        if (!item.isIsPurchase()) {
            if (item.getDiamond() <= sessionManager.getUser().getDiamond()) {
                purchaseSvga(item.getId(), item.getType(), binding, item);
            } else {
                Toast.makeText(context, R.string.you_don_t_have_required_diamonds, Toast.LENGTH_SHORT).show();
            }
        } else if (item.isIsPurchase() && !item.isIsSelected()) {
            selectSvga(item.getId(), item.getType(), binding, item, true);
        } else if (item.isIsPurchase() && item.isIsSelected()) {
            selectSvga(item.getId(), item.getType(), binding, item, false);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        currentList.clear();
    }
}

