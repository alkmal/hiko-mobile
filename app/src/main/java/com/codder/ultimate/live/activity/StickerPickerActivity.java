package com.codder.ultimate.live.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityStickerPickerBinding;
import com.codder.ultimate.live.adapter.StickerAdapter;
import com.codder.ultimate.live.model.StickerRoot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import java.util.List;

public class StickerPickerActivity extends BaseActivity {

    public static final String EXTRA_STICKER = "sticker";
    private static final String TAG = "StickerPickerActivity";

    private ActivityStickerPickerBinding binding;
    private StickerAdapter stickerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_sticker_picker);

            stickerAdapter = new StickerAdapter();
            if (binding.rvSongs != null) {
                binding.rvSongs.setAdapter(stickerAdapter);
            } else {
                Log.e(TAG, "RecyclerView not found in layout. Finishing activity.");
                finish();
                return;
            }

            List<StickerRoot.StickerItem> stickers = RayziUtils.getSticker();
            if (stickers == null || stickers.isEmpty()) {
                Log.w(TAG, "No stickers available. Finishing activity.");
                setResult(RESULT_CANCELED);
                finish();
                return;
            }

            stickerAdapter.submitList(stickers);

            stickerAdapter.setOnStickerClickListener(sticker -> {
                if (sticker != null) {
                    closeWithSelection(sticker);
                } else {
                    Log.w(TAG, "Sticker clicked was null.");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error during onCreate", e);
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    public void closeWithSelection(StickerRoot.StickerItem stickerDummy) {
        if (stickerDummy == null) {
            Log.w(TAG, "closeWithSelection called with null stickerDummy");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_STICKER, stickerDummy);
        setResult(RESULT_OK, data);
        finish();
    }
}
