package com.codder.ultimate.live.bottomsheet;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.BottomSheetBeautyoptionsBinding;
import com.codder.ultimate.utils.CenteredScaleView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Locale;

import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.BeautyOptions;

public class BottomSheetBeautyOptions {

    private static final String TAG = "BottomSheetBeautyOptions";

    private final BottomSheetDialog bottomSheetDialog;
    private final Context context;
    private final RtcEngine rtcEngine;

    private BottomSheetBeautyoptionsBinding binding;

    private CenteredScaleView scaleExposure, scaleSmoothness, scaleRedness, scaleSharpness;

    private int lighteningContrast = BeautyOptions.LIGHTENING_CONTRAST_NORMAL;

    public BottomSheetBeautyOptions(@NonNull Context context, @NonNull RtcEngine rtcEngine) {
        this.context = context;
        this.rtcEngine = rtcEngine;

        bottomSheetDialog = new BottomSheetDialog(this.context, R.style.CustomBottomSheetDialogTheme);
        binding = BottomSheetBeautyoptionsBinding.inflate(LayoutInflater.from(context));
        bottomSheetDialog.setContentView(binding.getRoot());

        try {
            if (bottomSheetDialog.getWindow() != null) {
                bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                bottomSheetDialog.getWindow().setDimAmount(0f);
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not style BottomSheetDialog window.", e);
        }

        binding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        bottomSheetDialog.setOnShowListener(dialog -> {
            try {
                View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            } catch (Exception e) {
                Log.w(TAG, "Error expanding BottomSheetDialog.", e);
            }
        });

        // Spinner + tab icons
        setupSpinnerAndIcons();

        // Bind views
        scaleExposure = binding.scaleExposure;
        scaleSmoothness = binding.scaleSmoothness;
        scaleRedness = binding.scaleRedness;
        scaleSharpness = binding.scaleSharpness;

// ----- Exposure: -100..100, center baseline -----
        if (scaleExposure != null) {

            scaleExposure.setHapticsEnabled(true);
            scaleExposure.setSnapToStep(false);
            scaleExposure.setTickStep(5f);   // draw ticks every 5
            scaleExposure.setDetentStep(5f); // haptics every 5 while sliding

            scaleExposure.configure(-100f, 100f, 1f);

            // Show progress: True [Turn on the in-track progress fill (if you want it visible on the ruler)]
            scaleExposure.setProgressAppearance(false, false, CenteredScaleView.Baseline.ZERO);
            scaleExposure.setOnValueChangeListener(new CenteredScaleView.OnValueChangeListener() {
                @Override
                public void onValueChanged(float v, boolean fromUser) {
                    updateProgressBadge(binding.tvExposureProgress, binding.iconExposure, v,
                            binding.iconExposure.isSelected());
                    apply();
                }

                @Override
                public void onGestureStart() {
                }

                @Override
                public void onGestureEnd(float v) {
                    updateProgressBadge(binding.tvExposureProgress, binding.iconExposure, v,
                            binding.iconExposure.isSelected());
                }
            });
            scaleExposure.setValue(0f, false);
            updateProgressBadge(binding.tvExposureProgress, binding.iconExposure, 0f,
                    binding.iconExposure.isSelected());
        }

// ----- Smoothness: -100..100, center baseline; show text -----
        if (scaleSmoothness != null) {

            scaleSmoothness.setHapticsEnabled(true);
            scaleSmoothness.setSnapToStep(false);
            scaleSmoothness.setTickStep(5f);   // draw ticks every 5
            scaleSmoothness.setDetentStep(5f); // haptics every 5 while sliding

            scaleSmoothness.configure(-100f, 100f, 1f);
            scaleSmoothness.setProgressAppearance(false, false, CenteredScaleView.Baseline.ZERO);
            scaleSmoothness.setOnValueChangeListener(new CenteredScaleView.OnValueChangeListener() {
                @Override
                public void onValueChanged(float v, boolean fromUser) {
                    updateProgressBadge(binding.tvSmoothnessProgress, binding.iconSmoothness, v,
                            binding.iconSmoothness.isSelected());

                    apply();
                }

                @Override
                public void onGestureStart() {
                }

                @Override
                public void onGestureEnd(float v) {
                    updateProgressBadge(binding.tvSmoothnessProgress, binding.iconSmoothness, v,
                            binding.iconSmoothness.isSelected());

                }
            });
            scaleSmoothness.setValue(0f, false);
            updateProgressBadge(binding.tvSmoothnessProgress, binding.iconSmoothness, 0f,
                    binding.iconSmoothness.isSelected());

        }

// ----- Redness: -100..100, center baseline; show text -----
        if (scaleRedness != null) {
            scaleRedness.setHapticsEnabled(true);
            scaleRedness.setSnapToStep(false);
            scaleRedness.setTickStep(5f);   // draw ticks every 5
            scaleRedness.setDetentStep(5f); // haptics every 5 while sliding

            scaleRedness.configure(-100f, 100f, 1f);
            scaleRedness.setProgressAppearance(false, false, CenteredScaleView.Baseline.ZERO);
            scaleRedness.setOnValueChangeListener(new CenteredScaleView.OnValueChangeListener() {
                @Override
                public void onValueChanged(float v, boolean fromUser) {
                    updateProgressBadge(binding.tvRednessProgress, binding.iconRedness, v,
                            binding.iconRedness.isSelected());

                    apply();
                }

                @Override
                public void onGestureStart() {
                }

                @Override
                public void onGestureEnd(float v) {
                    updateProgressBadge(binding.tvRednessProgress, binding.iconRedness, v,
                            binding.iconRedness.isSelected());

                }
            });
            scaleRedness.setValue(0f, false);
            updateProgressBadge(binding.tvRednessProgress, binding.iconRedness, 0f,
                    binding.iconRedness.isSelected());

        }

// ----- Sharpness: -100..100, center baseline; show text -----
        if (scaleSharpness != null) {
            scaleSharpness.setHapticsEnabled(true);
            scaleSharpness.setSnapToStep(false);
            scaleSharpness.setTickStep(5f);   // draw ticks every 5
            scaleSharpness.setDetentStep(5f); // haptics every 5 while sliding

            scaleSharpness.configure(-100f, 100f, 1f);
            scaleSharpness.setProgressAppearance(false, false, CenteredScaleView.Baseline.ZERO);
            scaleSharpness.setOnValueChangeListener(new CenteredScaleView.OnValueChangeListener() {
                @Override
                public void onValueChanged(float v, boolean fromUser) {
                    updateProgressBadge(binding.tvSharpnessProgress, binding.iconSharpness, v,
                            binding.iconSharpness.isSelected());

                    apply();
                }

                @Override
                public void onGestureStart() {
                }

                @Override
                public void onGestureEnd(float v) {
                    updateProgressBadge(binding.tvSharpnessProgress, binding.iconSharpness, v,
                            binding.iconSharpness.isSelected());

                }
            });
            scaleSharpness.setValue(0f, false);
            updateProgressBadge(binding.tvSharpnessProgress, binding.iconSharpness, 0f,
                    binding.iconSharpness.isSelected());

        }

        apply();
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private void apply() {
        if (rtcEngine == null) return;
        try {
            float exposure = (scaleExposure != null) ? scaleExposure.getValue() : 0f;
            // Exposure: -100..100 → [0..1]; negatives = 0 (SDK doesn't support darken)
            float lighteningLevel = exposure <= 0f ? 0f : exposure / 100f;

            float smoothness = (scaleSmoothness != null) ? clamp01(Math.max(0f, scaleSmoothness.getValue()) / 100f) : 0f;
            float redness = (scaleRedness != null) ? clamp01(Math.max(0f, scaleRedness.getValue()) / 100f) : 0f;
            float sharpness = (scaleSharpness != null) ? clamp01(Math.max(0f, scaleSharpness.getValue()) / 100f) : 0f;

            BeautyOptions options = new BeautyOptions(
                    lighteningContrast,
                    lighteningLevel,
                    smoothness,
                    redness,
                    sharpness
            );
            rtcEngine.setBeautyEffectOptions(true, options);
        } catch (Exception e) {
            Log.e(TAG, "apply() failed", e);
        }
    }

    private void setupSpinnerAndIcons() {
        // Spinner (contrast)
        Spinner spinner = binding.spinnerLighteningContrast;
        if (spinner != null) {
            ArrayAdapter<CharSequence> adapter =
                    ArrayAdapter.createFromResource(context, R.array.lightening_contrast_options, R.layout.spinner_item_1);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    switch (pos) {
                        case 0:
                            lighteningContrast = BeautyOptions.LIGHTENING_CONTRAST_LOW;
                            break;
                        case 1:
                            lighteningContrast = BeautyOptions.LIGHTENING_CONTRAST_NORMAL;
                            break;
                        case 2:
                            lighteningContrast = BeautyOptions.LIGHTENING_CONTRAST_HIGH;
                            break;
                        default:
                            lighteningContrast = BeautyOptions.LIGHTENING_CONTRAST_NORMAL;
                    }
                    apply();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        // Groups and icons via binding
        View groupExposure = binding.groupExposure;
        View groupSmoothness = binding.groupSmoothness;
        View groupRedness = binding.groupRedness;
        View groupSharpness = binding.groupSharpness;

        View iconExposure = binding.iconExposure;
        View iconSmoothness = binding.iconSmoothness;
        View iconRedness = binding.iconRedness;
        View iconSharpness = binding.iconSharpness;

        final View[] groups = {groupExposure, groupSmoothness, groupRedness, groupSharpness};
        final View[] icons = {iconExposure, iconSmoothness, iconRedness, iconSharpness};
        final View spinnerRow = binding.layoutSpinnerLighteningContrast;

        View.OnClickListener iconClick = v -> {
            for (int i = 0; i < icons.length; i++) {
                if (icons[i] == v) {
                    setSelectedIndex(i, icons, groups);
                    if (spinnerRow != null)
                        spinnerRow.setVisibility(i == 0 ? View.VISIBLE : View.GONE);
                    break;
                }
            }
        };

        if (binding.tvExposureProgress != null)  binding.tvExposureProgress.setOnClickListener(iconClick);
        if (binding.tvSmoothnessProgress != null) binding.tvSmoothnessProgress.setOnClickListener(iconClick);
        if (binding.tvRednessProgress != null)    binding.tvRednessProgress.setOnClickListener(iconClick);
        if (binding.tvSharpnessProgress != null)  binding.tvSharpnessProgress.setOnClickListener(iconClick);


        if (iconExposure != null) iconExposure.setOnClickListener(iconClick);
        if (iconSmoothness != null) iconSmoothness.setOnClickListener(iconClick);
        if (iconRedness != null) iconRedness.setOnClickListener(iconClick);
        if (iconSharpness != null) iconSharpness.setOnClickListener(iconClick);

        setSelectedIndex(0, icons, groups);
    }

    private void setSelectedIndex(int index, View[] icons, View[] groups) {
        TextView[] labels = {
                binding.tvExposure,
                binding.tvSmoothness,
                binding.tvRedness,
                binding.tvSharpness
        };

        TextView[] progress = {
                binding.tvExposureProgress,
                binding.tvSmoothnessProgress,
                binding.tvRednessProgress,
                binding.tvSharpnessProgress
        };

        int activeBg = ContextCompat.getColor(context, R.color.pink);
        int inactiveBg = ContextCompat.getColor(context, R.color.white_10);
        int activeText = ContextCompat.getColor(context, R.color.pink);
        int inactiveText = ContextCompat.getColor(context, R.color.inactiveText);

        for (int i = 0; i < icons.length; i++) {
            boolean selected = (i == index);

            if (groups[i] != null) groups[i].setVisibility(selected ? View.VISIBLE : View.GONE);

            if (icons[i] != null) {
                icons[i].setBackgroundTintList(ColorStateList.valueOf(selected ? activeBg : inactiveBg));
                icons[i].setSelected(selected);
                if (icons[i] instanceof android.widget.ImageView) {
                    ((android.widget.ImageView) icons[i]).setImageTintList(
                            ColorStateList.valueOf(selected ? activeText : inactiveText)
                    );
                }
            }

            if (labels[i] != null) labels[i].setTextColor(selected ? activeText : inactiveText);

            switch (i) {
                case 0:
                    updateProgressBadge(binding.tvExposureProgress, binding.iconExposure,
                            scaleExposure != null ? scaleExposure.getValue() : 0f, selected);
                    break;
                case 1:
                    updateProgressBadge(binding.tvSmoothnessProgress, binding.iconSmoothness,
                            scaleSmoothness != null ? scaleSmoothness.getValue() : 0f, selected);
                    break;
                case 2:
                    updateProgressBadge(binding.tvRednessProgress, binding.iconRedness,
                            scaleRedness != null ? scaleRedness.getValue() : 0f, selected);
                    break;
                case 3:
                    updateProgressBadge(binding.tvSharpnessProgress, binding.iconSharpness,
                            scaleSharpness != null ? scaleSharpness.getValue() : 0f, selected);
                    break;
            }

        }
    }

    public void show() {
        if (!bottomSheetDialog.isShowing()) {
            try {
                bottomSheetDialog.show();
            } catch (Exception e) {
                Log.e(TAG, "Failed to show BottomSheetDialog.", e);
            }
        }
    }

    public void dismiss() {
        if (bottomSheetDialog.isShowing()) {
            try {
                bottomSheetDialog.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Failed to dismiss BottomSheetDialog.", e);
            }
        }
    }

    private void updateProgressBadge(TextView tvBadge, ImageView icon, float v, boolean isSelected) {
        if (tvBadge == null || icon == null) return;

        int rounded = Math.round(v);
        if (isSelected && rounded != 0) {
            tvBadge.setText(String.format(Locale.US, "%+d", rounded));
            tvBadge.setVisibility(View.VISIBLE);
            icon.setVisibility(View.GONE);
        } else {
            tvBadge.setVisibility(View.GONE);
            icon.setVisibility(View.VISIBLE);
        }
    }

}
