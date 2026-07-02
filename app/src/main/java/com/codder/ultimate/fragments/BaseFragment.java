package com.codder.ultimate.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.dialog.CustomDialogClass;
import com.codder.ultimate.launguagetranslation.TranslationContextWrapper;
import com.codder.ultimate.launguagetranslation.TranslationResources;
import com.codder.ultimate.retrofit.Const;

import java.util.Locale;

public abstract class BaseFragment extends Fragment {

    public SessionManager sessionManager;
    public CustomDialogClass customDialogClass;

    private Context wrappedContext;

    @Override
    public void onAttach(@NonNull Context context) {
        wrappedContext = new TranslationContextWrapper(context);
        super.onAttach(wrappedContext);
    }

    @Override
    public Context getContext() {
        return wrappedContext != null ? wrappedContext : super.getContext();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(requireActivity());
        customDialogClass = new CustomDialogClass(requireContext(), R.style.customStyle);
        customDialogClass.setCancelable(false);
        customDialogClass.setCanceledOnTouchOutside(false);

        applyRTLSupport();
    }

    public void applyGradientToTextView(TextView textView, boolean isActive) {
        if (isActive) {
            Paint paint = textView.getPaint();
            float width = paint.measureText(textView.getText().toString());

            Shader textShader = new LinearGradient(
                    0f, 0f, width, textView.getTextSize(),
                    new int[]{
                            ContextCompat.getColor(requireActivity(), R.color.party_gradient_1),
                            ContextCompat.getColor(requireActivity(), R.color.party_gradient_2)
                    },
                    null,
                    Shader.TileMode.CLAMP
            );
            textView.getPaint().setShader(textShader);
        } else {
            textView.getPaint().setShader(null);
            textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.gray)); // inactive color
        }
        textView.invalidate();
    }


    public void doTransition(int type) {

        if (getActivity() != null) {
            if (type == Const.BOTTOM_TO_UP) {

                getActivity().overridePendingTransition(R.anim.enter_from_bottom, R.anim.exit_none);
            } else if (type == Const.UP_TO_BOTTOM) {
                getActivity().overridePendingTransition(R.anim.exit_none, R.anim.enter_from_up);

            }

        }
    }

    private void applyRTLSupport() {
        String selectedLanguage = sessionManager.getStringValue(Const.SELECTED_LANGUAGE); // Get stored language
        if (selectedLanguage == null || selectedLanguage.isEmpty()) {
            selectedLanguage = Locale.getDefault().getLanguage();
        }

        Locale locale = new Locale(selectedLanguage);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        // Check if the selected language is RTL
        if (isRTL(requireContext())) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }

        // Apply new configuration
        requireActivity().getResources().updateConfiguration(config, requireActivity().getResources().getDisplayMetrics());
    }

    public static boolean isRTL(Context context) {

        Configuration config = context.getResources().getConfiguration();
        if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            return true;
        } else {
            return false;
        }
    }
}
