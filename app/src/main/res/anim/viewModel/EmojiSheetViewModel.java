package com.example.rayzi.viewModel;

import android.content.Context;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.rayzi.SessionManager;
import com.example.rayzi.emoji.UserListAdapter;
import com.example.rayzi.emoji.UserSelectableClass;
import com.example.rayzi.modelclass.GiftCategoryRoot;
import com.example.rayzi.modelclass.GiftRoot;

import java.util.ArrayList;
import java.util.List;

public class EmojiSheetViewModel extends ViewModel {

    public MutableLiveData<GiftRoot.GiftItem> selectedGift = new MutableLiveData<>();
    public MutableLiveData<GiftRoot.GiftItem> finelGift = new MutableLiveData<>();
    public MutableLiveData<Long> localUserCoin = new MutableLiveData<>();
    public MutableLiveData<Boolean> giftCategoryGetted = new MutableLiveData<>();
    public ObservableBoolean isLoading = new ObservableBoolean();
    public ObservableBoolean noData = new ObservableBoolean(false);
    public UserListAdapter userListAdapter=new UserListAdapter();
    public List<UserSelectableClass> users=new ArrayList<>();

    public MutableLiveData<List<GiftCategoryRoot.CategoryItem>> categoryItemMutableLiveData = new MutableLiveData<>();
    private SessionManager sessionManager;

    public void initEmojiSheet(Context context) {
        sessionManager = new SessionManager(context);
    }

    public void getGiftCategory() {
        isLoading.set(true);
        if (!sessionManager.getGiftCategoriesList().isEmpty()) {
            categoryItemMutableLiveData.setValue(sessionManager.getGiftCategoriesList());
            giftCategoryGetted.setValue(true);
        }else {
            noData.set(true);
            giftCategoryGetted.setValue(false);
        }
        isLoading.set(false);
    }

}
