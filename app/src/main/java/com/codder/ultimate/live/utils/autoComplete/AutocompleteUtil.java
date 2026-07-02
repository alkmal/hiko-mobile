package com.codder.ultimate.live.utils.autoComplete;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import com.codder.ultimate.live.model.HashtagsRoot;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.google.android.material.color.MaterialColors;

import java.util.Locale;


final public class AutocompleteUtil {

    public static void setupForHashtags(Context context, EditText input) {
        int color = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, Color.BLACK);
        Autocomplete.<HashtagsRoot.HashtagItem>on(input)
                .with(5)
                .with(new ColorDrawable(color))
                .with(new CharPolicy('#'))
                .with(new HashtagPresenter(context))
                .with(new AutocompleteCallback<HashtagsRoot.HashtagItem>() {

                    @Override
                    public boolean onPopupItemClicked(Editable editable, HashtagsRoot.HashtagItem item) {
                        int[] range = CharPolicy.getQueryRange(editable);
                        if (range == null) return false;

                        int adjustedStart = range[0];
                        if (adjustedStart > 0 && editable.charAt(adjustedStart - 1) == '#') {
                            adjustedStart -= 1;
                        }

                        String hashtag = item.getHashtag().trim();
                        if (hashtag.startsWith("#")) {
                            hashtag = hashtag.substring(1);
                        }

                        editable.replace(adjustedStart, range[1], "#" + hashtag.toLowerCase(Locale.ROOT));

                        Log.d("Autocomplete", "Adjusting from " + adjustedStart + " to " + range[1]);

                        return true;
                    }

                    @Override
                    public void onPopupVisibilityChanged(boolean shown) {
                    }
                })
                .build();
    }

    public static void setupForUsers(Context context, EditText input) {
        int color = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, Color.BLACK);
        Autocomplete.<GuestProfileRoot.User>on(input)
                .with(5)
                .with(new ColorDrawable(color))
                .with(new CharPolicy('@'))
                .with(new UserPresenter(context))
                .with(new AutocompleteCallback<GuestProfileRoot.User>() {

                    @Override
                    public boolean onPopupItemClicked(Editable editable, GuestProfileRoot.User item) {
                        int[] range = CharPolicy.getQueryRange(editable);
                        if (range == null) {
                            return false;
                        }

                        editable.replace(range[0], range[1], item.getUsername());
                        return true;
                    }

                    @Override
                    public void onPopupVisibilityChanged(boolean shown) {
                    }
                })
                .build();
    }
}
