package com.codder.ultimate.live.viewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.live.adapter.LiveStramCommentAdapter;
import com.codder.ultimate.live.adapter.LiveViewAdapter;
import com.codder.ultimate.live.adapter.LiveViewUserAdapter;
import com.codder.ultimate.live.adapter.StickerAdapter;
import com.codder.ultimate.live.model.StickerRoot;
import com.codder.ultimate.modelclass.UserRoot;

import org.json.JSONObject;

public class HostLiveViewModel extends ViewModel {

    public static final String TAG = "HostLiveViewModel";

    public StickerAdapter stickerAdapter = new StickerAdapter();
    public LiveViewUserAdapter liveViewUserAdapter = new LiveViewUserAdapter();
    public LiveStramCommentAdapter liveStramCommentAdapter = new LiveStramCommentAdapter();
    public MutableLiveData<Boolean> isShowFilterSheet = new MutableLiveData<>(false);
    public MutableLiveData<StickerRoot.StickerItem> selectedSticker = new MutableLiveData<StickerRoot.StickerItem>();
    public MutableLiveData<UserRoot.User> clickedComment = new MutableLiveData<UserRoot.User>();
    public MutableLiveData<JSONObject> clickedUser = new MutableLiveData<>();
    public boolean isMuted = false;
    public LiveViewAdapter liveViewAdapter = new LiveViewAdapter();

    public void onClickSheetClose() {
        isShowFilterSheet.setValue(false);
    }

    public void initLister() {
        stickerAdapter.setOnStickerClickListener(filterRoot -> {
            Log.d(TAG + " viewmodel", "onBindViewHolder: ===========" + filterRoot.getSticker());
            selectedSticker.setValue(filterRoot);
        });
        liveStramCommentAdapter.setOnCommentClickListener((UserRoot.User userDummy) -> clickedComment.setValue(userDummy));
        liveViewUserAdapter.setOnLiveUserAdapterClickListener((JSONObject userDummy) -> clickedUser.setValue(userDummy));
        liveViewAdapter.setOnLiveUserAdapterClickListener(userDummy -> clickedUser.setValue(userDummy));
    }
}