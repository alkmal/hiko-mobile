package com.codder.ultimate.live.viewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.live.adapter.LiveViewUserAdapter;
import com.codder.ultimate.live.adapter.PKLiveStreamCommentAdapter;
import com.codder.ultimate.live.adapter.StickerAdapter;
import com.codder.ultimate.live.model.FilterRoot;
import com.codder.ultimate.live.model.StickerRoot;
import com.codder.ultimate.modelclass.UserRoot;

import org.json.JSONObject;

public class PKLiveViewModel extends ViewModel {

    public static final String TAG = "PKLiveViewModel";
    public final StickerAdapter stickerAdapter = new StickerAdapter();
    public final LiveViewUserAdapter liveViewUserAdapter = new LiveViewUserAdapter();
    public final PKLiveStreamCommentAdapter pkLiveStreamCommentAdapter = new PKLiveStreamCommentAdapter();
    public final MutableLiveData<Boolean> isShowFilterSheet = new MutableLiveData<>(false);
    public final MutableLiveData<FilterRoot> selectedFilter = new MutableLiveData<>();
    public final MutableLiveData<StickerRoot.StickerItem> selectedSticker = new MutableLiveData<>();
    public final MutableLiveData<UserRoot.User> clickedComment = new MutableLiveData<>();
    public final MutableLiveData<JSONObject> clickedUser = new MutableLiveData<>();

    public boolean isMuted = false;
    public boolean isCameraOff = false;

    public void onClickSheetClose() {
        isShowFilterSheet.setValue(false);
    }

    public void initLister() {
        stickerAdapter.setOnStickerClickListener(stickerItem -> {
            if (stickerItem != null) {
                Log.d(TAG + " viewmodel", "Selected sticker: " + stickerItem.getSticker());
                selectedSticker.setValue(stickerItem);
            }
        });
        pkLiveStreamCommentAdapter.setOnCommentClickListener(userDummy -> {
            if (userDummy != null) {
                clickedComment.setValue(userDummy);
            }
        });
        liveViewUserAdapter.setOnLiveUserAdapterClickListener(userDummy -> {
            if (userDummy != null) {
                clickedUser.setValue(userDummy);
            }
        });
    }
}