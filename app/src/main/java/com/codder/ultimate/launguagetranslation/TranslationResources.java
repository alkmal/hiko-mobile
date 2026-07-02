package com.codder.ultimate.launguagetranslation;

import android.content.res.Resources;
import androidx.annotation.NonNull;

public class TranslationResources extends Resources {

    public TranslationResources(Resources res) {
        super(res.getAssets(), res.getDisplayMetrics(), res.getConfiguration());
    }

    @NonNull
    @Override
    public String getString(int id) throws NotFoundException {
        try {
            String key = getResourceEntryName(id);
            String translated = TranslationManager.getInstance().get(key);
            if (translated != null && !translated.isEmpty()) {
                return translated;
            }
        } catch (Exception ignored) {}
        return super.getString(id);
    }

    @NonNull
    @Override
    public String getString(int id, Object... formatArgs) throws NotFoundException {
        try {
            String key = getResourceEntryName(id);
            String translated = TranslationManager.getInstance().get(key);
            if (translated != null && !translated.isEmpty()) {
                return String.format(translated, formatArgs);
            }
        } catch (Exception ignored) {}
        return super.getString(id, formatArgs);
    }
}