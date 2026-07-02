package com.codder.ultimate.live.viewModel;

import android.content.Context;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.SessionManager;
import com.codder.ultimate.live.adapter.UserListAdapter;
import com.codder.ultimate.live.model.GiftCategoryRoot;
import com.codder.ultimate.live.model.GiftRoot;
import com.codder.ultimate.live.utils.UserSelectableClass;

import java.util.ArrayList;
import java.util.List;

public class EmojiSheetViewModel extends ViewModel {

    public MutableLiveData<GiftRoot.GiftItem> selectedGift = new MutableLiveData<>();
    public MutableLiveData<GiftRoot.GiftItem> finalGift = new MutableLiveData<>();
    public MutableLiveData<Double> localUserCoin = new MutableLiveData<>();
    public MutableLiveData<Boolean> giftCategoryGot = new MutableLiveData<>();
    public ObservableBoolean isLoading = new ObservableBoolean();
    public ObservableBoolean noData = new ObservableBoolean(false);
    public UserListAdapter userListAdapter;
    public List<UserSelectableClass> users = new ArrayList<>();
    private Context context;

    public MutableLiveData<List<GiftCategoryRoot.CategoryItem>> categoryItemMutableLiveData = new MutableLiveData<>();
    private SessionManager sessionManager;

    public void initEmojiSheet(Context context) {
        this.context = context.getApplicationContext();
        sessionManager = new SessionManager(this.context);
        if (userListAdapter == null) {
            userListAdapter = new UserListAdapter(this.context, userSelectableClass -> {
                // handle click if needed (or leave empty, handled in Fragment)
            });
        }
    }

    public void setUsers(List<UserSelectableClass> userList) {
        this.users = userList != null ? new ArrayList<>(userList) : new ArrayList<>();
        updateAdapterUserList();
    }

    public void updateAdapterUserList() {
        if (sessionManager != null && userListAdapter != null && users != null) {
            List<UserSelectableClass> filtered = userListAdapter.filterOutSessionUser(users);
            userListAdapter.submitList(filtered);
        }
    }

    public void getGiftCategory() {
        isLoading.set(true);
        if (!sessionManager.getGiftCategoriesList().isEmpty()) {
            categoryItemMutableLiveData.setValue(sessionManager.getGiftCategoriesList());
            giftCategoryGot.setValue(true);
        } else {
            noData.set(true);
            giftCategoryGot.setValue(false);
        }
        isLoading.set(false);
    }

}
