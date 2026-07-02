package com.codder.ultimate.launguagetranslation;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.LayoutInflaterCompat;

public class TranslationInflaterFactory implements LayoutInflater.Factory2 {

    private final AppCompatDelegate delegate;
    private final Context context;

    // TextView ma aa attributes check karva — text ane hint
    private static final int[] TEXT_ATTRS = {android.R.attr.text};
    private static final int[] HINT_ATTRS = {android.R.attr.hint};

    public TranslationInflaterFactory(AppCompatDelegate delegate, Context context) {
        this.delegate = delegate;
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name,
                             @NonNull Context ctx, @NonNull AttributeSet attrs) {

        // AppCompat delegate thi view create karo (styling, theme correct rave)
        View view = delegate.createView(parent, name, ctx, attrs);

        if (view == null) {
            // Delegate e na banavi to manually try karo
            try {
                view = LayoutInflater.from(ctx).createView(name, null, attrs);
            } catch (Exception e) {
                return null;
            }
        }

        // Sirf TextView (ane subclasses — Button, EditText) handle kariye
        if (view instanceof TextView) {
            applyTranslation((TextView) view, ctx, attrs);
        }

        return view;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context ctx,
                             @NonNull AttributeSet attrs) {
        return onCreateView(null, name, ctx, attrs);
    }

    private void applyTranslation(TextView view, Context ctx, AttributeSet attrs) {

        // ── Text attribute check ──────────────────────────────────
        try {
            TypedArray ta = ctx.obtainStyledAttributes(attrs, TEXT_ATTRS);
            int resId = ta.getResourceId(0, 0);
            ta.recycle();

            if (resId != 0) {
                String key = ctx.getResources().getResourceEntryName(resId);
                String translated = TranslationManager.getInstance().get(key);
                if (translated != null && !translated.isEmpty()) {
                    view.setText(translated);
                }
            }
        } catch (Exception ignored) {}

        // ── Hint attribute check (EditText mate) ─────────────────
        try {
            TypedArray ta = ctx.obtainStyledAttributes(attrs, HINT_ATTRS);
            int resId = ta.getResourceId(0, 0);
            ta.recycle();

            if (resId != 0) {
                String key = ctx.getResources().getResourceEntryName(resId);
                String translated = TranslationManager.getInstance().get(key);
                if (translated != null && !translated.isEmpty()) {
                    view.setHint(translated);
                }
            }
        } catch (Exception ignored) {}
    }
}