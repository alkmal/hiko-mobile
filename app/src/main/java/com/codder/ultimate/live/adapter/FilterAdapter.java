package com.codder.ultimate.live.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;


import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ItemFilterBinding;
import com.codder.ultimate.utils.VideoFilter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorInvertFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageExposureFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGammaFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHazeFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageMonochromeFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImagePixelationFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImagePosterizeFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSharpenFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSolarizeFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVignetteFilter;

public class FilterAdapter extends ListAdapter<VideoFilter, FilterAdapter.FilterViewHolder> {

    private final Context mContext;
    private OnFilterSelectListener mListener;
    private final Map<VideoFilter, Bitmap> filteredThumbnails = new HashMap<>();
    private VideoFilter selectedFilter = null;


    public FilterAdapter(Context context, Bitmap thumbnail) {
        super(new VideoFilterDiffCallback());
        mContext = context;
        for (VideoFilter filter : VideoFilter.values()) {
            GPUImage gpuImage = new GPUImage(context);
            gpuImage.setImage(thumbnail);
            gpuImage.setFilter(createGPUFilter(filter));
            Bitmap filteredBitmap = gpuImage.getBitmapWithFilterApplied();
            filteredThumbnails.put(filter, filteredBitmap);
        }
        submitList(java.util.Arrays.asList(VideoFilter.values()));
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFilterBinding binding = ItemFilterBinding.inflate(
                LayoutInflater.from(mContext), parent, false
        );
        FilterViewHolder holder = new FilterViewHolder(binding);
        holder.setIsRecyclable(false);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        final VideoFilter filter = getItem(position);

        holder.binding.image.setImageBitmap(filteredThumbnails.get(filter));

        String name = filter.name().toLowerCase(Locale.US);
        TextView nameView = holder.binding.name;
        FrameLayout layout = holder.binding.layBg;
        nameView.setText(Character.toUpperCase(name.charAt(0)) + name.substring(1));

        if (filter == selectedFilter) {
            layout.setBackground(ContextCompat.getDrawable(mContext,R.drawable.bg_selected_filter));
        } else {
            layout.setBackground(null);
        }

        holder.itemView.setOnClickListener(view -> {
            if (mListener != null) {
                selectedFilter = filter;
                notifyDataSetChanged();
                mListener.onSelectFilter(filter);
            }
        });
    }


    private GPUImageFilter createGPUFilter(VideoFilter filter) {
        switch (filter) {
            case BRIGHTNESS:
                GPUImageBrightnessFilter brightnessFilter = new GPUImageBrightnessFilter();
                brightnessFilter.setBrightness(0.2f);
                return brightnessFilter;
            case EXPOSURE:
                return new GPUImageExposureFilter();
            case GAMMA:
                GPUImageGammaFilter gammaFilter = new GPUImageGammaFilter();
                gammaFilter.setGamma(2f);
                return gammaFilter;
            case GRAYSCALE:
                return new GPUImageGrayscaleFilter();
            case HAZE:
                GPUImageHazeFilter hazeFilter = new GPUImageHazeFilter();
                hazeFilter.setSlope(-0.5f);
                return hazeFilter;
            case INVERT:
                return new GPUImageColorInvertFilter();
            case MONOCHROME:
                return new GPUImageMonochromeFilter();
            case PIXELATED:
                GPUImagePixelationFilter pixelFilter = new GPUImagePixelationFilter();
                pixelFilter.setPixel(5f);
                return pixelFilter;
            case POSTERIZE:
                return new GPUImagePosterizeFilter();
            case SEPIA:
                return new GPUImageSepiaToneFilter();
            case SHARP:
                GPUImageSharpenFilter sharpFilter = new GPUImageSharpenFilter();
                sharpFilter.setSharpness(1f);
                return sharpFilter;
            case SOLARIZE:
                return new GPUImageSolarizeFilter();
            case VIGNETTE:
                return new GPUImageVignetteFilter();
            default:
                return new GPUImageFilter(); // No-op
        }
    }


    public void setListener(OnFilterSelectListener listener) {
        mListener = listener;
    }

    public interface OnFilterSelectListener {
        void onSelectFilter(VideoFilter filter);
    }

    static class FilterViewHolder extends RecyclerView.ViewHolder {
        final ItemFilterBinding binding;

        public FilterViewHolder(@NonNull ItemFilterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class VideoFilterDiffCallback extends DiffUtil.ItemCallback<VideoFilter> {
        @Override
        public boolean areItemsTheSame(@NonNull VideoFilter oldItem, @NonNull VideoFilter newItem) {
            return oldItem == newItem;
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull VideoFilter oldItem, @NonNull VideoFilter newItem) {
            return oldItem == newItem;
        }
    }
}

