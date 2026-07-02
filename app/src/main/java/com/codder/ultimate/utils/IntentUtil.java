package com.codder.ultimate.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.codder.ultimate.R;

public class IntentUtil {

    public static void startChooser(Activity activity, int code, String... mimes) {
        activity.startActivityForResult(createIntent(activity, mimes), code);
    }

    private static Intent createIntent(Context context, String[] mimes) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        if (mimes.length > 1) {
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimes);
        } else {
            intent.setType(mimes[0]);
        }

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return Intent.createChooser(intent, context.getString(R.string.browse_file_title));
    }
}
