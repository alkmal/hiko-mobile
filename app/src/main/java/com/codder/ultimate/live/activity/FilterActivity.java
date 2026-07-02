package com.codder.ultimate.live.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityFilterBinding;
import com.codder.ultimate.dialog.CustomDialogClass;
import com.codder.ultimate.live.adapter.FilterAdapter;
import com.codder.ultimate.utils.BitmapUtil;
import com.codder.ultimate.utils.SharedConstants;
import com.codder.ultimate.utils.TempUtil;
import com.codder.ultimate.utils.VideoFilter;
import com.codder.ultimate.utils.VideoUtil;
import com.daasuu.gpuv.composer.FillMode;
import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.daasuu.gpuv.egl.filter.GlBrightnessFilter;
import com.daasuu.gpuv.egl.filter.GlExposureFilter;
import com.daasuu.gpuv.egl.filter.GlGammaFilter;
import com.daasuu.gpuv.egl.filter.GlGrayScaleFilter;
import com.daasuu.gpuv.egl.filter.GlHazeFilter;
import com.daasuu.gpuv.egl.filter.GlInvertFilter;
import com.daasuu.gpuv.egl.filter.GlMonochromeFilter;
import com.daasuu.gpuv.egl.filter.GlPixelationFilter;
import com.daasuu.gpuv.egl.filter.GlPosterizeFilter;
import com.daasuu.gpuv.egl.filter.GlSepiaFilter;
import com.daasuu.gpuv.egl.filter.GlSharpenFilter;
import com.daasuu.gpuv.egl.filter.GlSolarizeFilter;
import com.daasuu.gpuv.egl.filter.GlVignetteFilter;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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


public class FilterActivity extends BaseActivity {

    public static final String EXTRA_SONG = "song";
    public static final String EXTRA_VIDEO = "video";
    private static final String TAG = "FilterActivity";
    public static final String EXTRA_AUDIO = "audio";
    private FilterActivityViewModel mModel;
    private SimpleExoPlayer mVideoPlayer;
    private SimpleExoPlayer mAudioPlayer;
    private String mAudio;
    private String mSong;
    private String mVideo;
    ActivityFilterBinding binding;
    private CustomDialogClass progress;

    private TextureView textureView;
    private GPUImageView gpuOverlay;
    private Handler previewHandler = new Handler(Looper.getMainLooper());
    private Runnable previewRunnable;

    // Video filter executor and handler for UI callbacks
    private final ExecutorService filterExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_filter);

        initViews();
    }

    private void initViews() {
        textureView = binding.textureView;
        gpuOverlay = binding.gpuOverlay;

        mModel = new ViewModelProvider(this).get(FilterActivityViewModel.class);

        mAudio = getIntent().getStringExtra(EXTRA_AUDIO);
        mSong = getIntent().getStringExtra(EXTRA_SONG);
        mVideo = getIntent().getStringExtra(EXTRA_VIDEO);
        Log.d(TAG, "onCreate:songid " + mSong);

        Bitmap frame = VideoUtil.getFrameAtTime(mVideo, TimeUnit.SECONDS.toMicros(3));
        if (frame != null) {
            Bitmap square = BitmapUtil.getSquareThumbnail(frame, 250);
            frame.recycle();
            Bitmap rounded = BitmapUtil.addRoundCorners(square, 25);
            square.recycle();
            FilterAdapter adapter = new FilterAdapter(this, rounded);
            adapter.setListener(this::applyFilter);
            binding.filters.setAdapter(adapter);
        } else {
            Toast.makeText(this, getString(R.string.cannot_load_video_preview), Toast.LENGTH_SHORT).show();
        }

        binding.btnDone.setOnClickListener(v -> submitForFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (previewHandler != null && previewRunnable != null) {
            previewHandler.removeCallbacks(previewRunnable);
        }
        if (mVideoPlayer != null) {
            mVideoPlayer.pause();
        }
        if (mVideoPlayer != null) {
            mVideoPlayer.setPlayWhenReady(false);
            mVideoPlayer.stop();
        }
        if (mAudioPlayer != null) {
            mAudioPlayer.setPlayWhenReady(false);
            mAudioPlayer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoPlayer != null) {
            mVideoPlayer.release();
            mVideoPlayer = null;
        }
        if (mAudioPlayer != null) {
            mAudioPlayer.release();
            mAudioPlayer = null;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mVideoPlayer == null && mVideo != null) {
            mVideoPlayer = new SimpleExoPlayer.Builder(this).build();
            mVideoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_OFF);
//            mVideoPlayer.setThrowsWhenUsingWrongThread(false);
            File videoFile = new File(mVideo);
            if (videoFile.exists()) {
                DefaultDataSourceFactory factory = new DefaultDataSourceFactory(this, getString(R.string.app_name));
                ProgressiveMediaSource videoSource = new ProgressiveMediaSource.Factory(factory)
                        .createMediaSource(MediaItem.fromUri(Uri.fromFile(videoFile)));
                mVideoPlayer.setMediaSource(videoSource);
                mVideoPlayer.prepare();
                mVideoPlayer.setPlayWhenReady(true);
                mVideoPlayer.setVolume(1f);

                // Sync the audio with the video
                mVideoPlayer.addListener(new Player.Listener() {
                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        // When video is playing, play the audio
                        if (playbackState == ExoPlayer.STATE_READY && playWhenReady) {
                            if (mAudioPlayer != null) {
                                mAudioPlayer.setPlayWhenReady(true);
                            }
                        } else {
                            // Pause the audio when video is paused or stopped
                            if (mAudioPlayer != null) {
                                mAudioPlayer.setPlayWhenReady(false);
                            }
                        }
                    }
                });


                textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                        Surface surface = new Surface(surfaceTexture);
                        mVideoPlayer.setVideoSurface(surface);

                        startPreviewUpdater();   // 👈 start live preview
                    }

                    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int w, int h) {}
                    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) { return false; }
                    @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
                });



            } else {
                Toast.makeText(this, getString(R.string.video_file_missing), Toast.LENGTH_SHORT).show();
            }
        }

        if (mAudioPlayer == null && mAudio != null && !mAudio.isEmpty()) {
            File audioFile = new File(mAudio);
            if (audioFile.exists()) {
                mAudioPlayer = new SimpleExoPlayer.Builder(this).build();
                DefaultDataSourceFactory factory = new DefaultDataSourceFactory(this, getString(R.string.app_name));
                ProgressiveMediaSource audioSource = new ProgressiveMediaSource.Factory(factory)
                        .createMediaSource(MediaItem.fromUri(Uri.fromFile(audioFile)));
                mAudioPlayer.setMediaSource(audioSource);
                mAudioPlayer.prepare();
                mAudioPlayer.setPlayWhenReady(false);  // Start paused initially
                mAudioPlayer.setVolume(1f);  // Adjust volume as required
            } else {
                Toast.makeText(this, getString(R.string.audio_file_missing), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startPreviewUpdater() {
        previewRunnable = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = textureView.getBitmap();
                if (bitmap != null) {
                    gpuOverlay.setImage(bitmap);
                }
                previewHandler.postDelayed(this, 80);
            }
        };
        previewHandler.post(previewRunnable);
    }


    public void applyFilter(VideoFilter filter) {

        mModel.filter = filter;

        switch (filter) {

            case BRIGHTNESS:
                GPUImageBrightnessFilter brightness = new GPUImageBrightnessFilter();
                brightness.setBrightness(0.2f);
                gpuOverlay.setFilter(brightness);
                break;

            case EXPOSURE:
                GPUImageExposureFilter exposure = new GPUImageExposureFilter();
                exposure.setExposure(1.0f);
                gpuOverlay.setFilter(exposure);
                break;

            case GAMMA:
                GPUImageGammaFilter gamma = new GPUImageGammaFilter();
                gamma.setGamma(2.0f);
                gpuOverlay.setFilter(gamma);
                break;

            case GRAYSCALE:
                gpuOverlay.setFilter(new GPUImageGrayscaleFilter());
                break;

            case HAZE:
                GPUImageHazeFilter haze = new GPUImageHazeFilter();
                haze.setSlope(-0.5f);
                gpuOverlay.setFilter(haze);
                break;

            case INVERT:
                gpuOverlay.setFilter(new GPUImageColorInvertFilter());
                break;

            case MONOCHROME:
                gpuOverlay.setFilter(new GPUImageMonochromeFilter());
                break;

            case PIXELATED:
                GPUImagePixelationFilter pixel = new GPUImagePixelationFilter();
                pixel.setPixel(5f);
                gpuOverlay.setFilter(pixel);
                break;

            case POSTERIZE:
                gpuOverlay.setFilter(new GPUImagePosterizeFilter());
                break;

            case SEPIA:
                gpuOverlay.setFilter(new GPUImageSepiaToneFilter());
                break;

            case SHARP:
                GPUImageSharpenFilter sharp = new GPUImageSharpenFilter();
                sharp.setSharpness(1.0f);
                gpuOverlay.setFilter(sharp);
                break;

            case SOLARIZE:
                gpuOverlay.setFilter(new GPUImageSolarizeFilter());
                break;

            case VIGNETTE:
                gpuOverlay.setFilter(new GPUImageVignetteFilter());
                break;

            default:
                gpuOverlay.setFilter(new GPUImageFilter());
        }
    }


    private void closeFinally(File clip) {
        Intent intent = new Intent(FilterActivity.this, UploadActivity.class);
        intent.putExtra(UploadActivity.EXTRA_SONG, mSong);
        intent.putExtra(UploadActivity.EXTRA_VIDEO, clip.getAbsolutePath());
        startActivity(intent);
        finish();
    }

    private void submitForFilter() {
        Log.d(TAG, "submitForFilter: ");
        if (mVideoPlayer != null) mVideoPlayer.setPlayWhenReady(false);

        if (progress != null && progress.isShowing()) progress.dismiss();
        progress = new CustomDialogClass(this, R.style.customStyle);
        progress.setCancelable(false);
        progress.show();

        File filtered = TempUtil.createNewFile(this, ".mp4");
        String inputPath = mVideo;
        String outputPath = filtered.getAbsolutePath();
        String filterName = mModel.filter != null ? mModel.filter.name() : VideoFilter.NONE.name();

        filterExecutor.submit(() -> {
            boolean result = processVideoFilter(this, filterName, inputPath, outputPath, filtered);
            mainHandler.post(() -> {
                if (progress != null && progress.isShowing()) progress.dismiss();
                if (result) {
                    closeFinally(filtered);
                } else {
                    Toast.makeText(this, getString(R.string.failed_to_process_video), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Merging video files failed.");
                }
            });
        });
    }

    /**
     * Runs video filtering, blocking. Called from background thread.
     */
    private boolean processVideoFilter(Context context, String filter, String inputPath, String outputPath, File filteredFile) {
        try {
            File input = new File(inputPath);
            File output = new File(outputPath);
            Size size = VideoUtil.getDimensions(input.getAbsolutePath());
            int width = size.getWidth();
            int height = size.getHeight();
            if (width > SharedConstants.MAX_RESOLUTION || height > SharedConstants.MAX_RESOLUTION) {
                if (width > height) {
                    height = SharedConstants.MAX_RESOLUTION * height / width;
                    width = SharedConstants.MAX_RESOLUTION;
                } else {
                    width = SharedConstants.MAX_RESOLUTION * width / height;
                    height = SharedConstants.MAX_RESOLUTION;
                }
            }
            if (width % 2 != 0) width += 1;
            if (height % 2 != 0) height += 1;

            Log.v(TAG, "Original: " + width + "x" + height + "px; scaled: " + width + "x" + height + "px");
            GPUMp4Composer composer = new GPUMp4Composer(input.getAbsolutePath(), output.getAbsolutePath());
            composer.videoBitrate((int) (.07 * 30 * width * height));
            composer.fillMode(FillMode.PRESERVE_ASPECT_FIT);
            composer.size(width, height);

            VideoFilter videoFilter = VideoFilter.valueOf(filter);
            Log.d(TAG, "doActualWork: filtername " + videoFilter.name());
            switch (videoFilter) {
                case BRIGHTNESS:
                    GlBrightnessFilter glf = new GlBrightnessFilter();
                    glf.setBrightness(0.2f);
                    composer.filter(glf);
                    break;
                case EXPOSURE:
                    composer.filter(new GlExposureFilter());
                    break;
                case GAMMA:
                    GlGammaFilter glf2 = new GlGammaFilter();
                    glf2.setGamma(2f);
                    composer.filter(glf2);
                    break;
                case GRAYSCALE:
                    composer.filter(new GlGrayScaleFilter());
                    break;
                case HAZE:
                    GlHazeFilter glf3 = new GlHazeFilter();
                    glf3.setSlope(-0.5f);
                    composer.filter(glf3);
                    break;
                case INVERT:
                    composer.filter(new GlInvertFilter());
                    break;
                case MONOCHROME:
                    composer.filter(new GlMonochromeFilter());
                    break;
                case PIXELATED:
                    composer.filter(new GlPixelationFilter());
                    break;
                case POSTERIZE:
                    composer.filter(new GlPosterizeFilter());
                    break;
                case SEPIA:
                    composer.filter(new GlSepiaFilter());
                    break;
                case SHARP:
                    GlSharpenFilter glf4 = new GlSharpenFilter();
                    glf4.setSharpness(1f);
                    composer.filter(glf4);
                    break;
                case SOLARIZE:
                    composer.filter(new GlSolarizeFilter());
                    break;
                case VIGNETTE:
                    composer.filter(new GlVignetteFilter());
                    break;
                default:
                    break;
            }

            final Object lock = new Object();
            final boolean[] success = {false};
            final boolean[] errorOrCanceled = {false};

            composer.listener(new GPUMp4Composer.Listener() {
                @Override
                public void onProgress(double progress) {
                }

                @Override
                public void onCompleted() {
                    Log.d(TAG, "MP4 composition has finished.");
                    synchronized (lock) {
                        success[0] = true;
                        lock.notify();
                    }
                }

                @Override
                public void onCanceled() {
                    Log.d(TAG, "MP4 composition was cancelled.");
                    errorOrCanceled[0] = true;
                    if (!output.delete())
                        Log.w(TAG, "Could not delete failed output file: " + output);
                    synchronized (lock) {
                        lock.notify();
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    Log.d(TAG, "MP4 composition failed with error.", e);
                    errorOrCanceled[0] = true;
                    if (!output.delete())
                        Log.w(TAG, "Could not delete failed output file: " + output);
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            });

            composer.start();

            // Wait for completion or failure
            synchronized (lock) {
                while (!success[0] && !errorOrCanceled[0]) {
                    lock.wait();
                }
            }
            return success[0];
        } catch (Exception e) {
            Log.e(TAG, "Exception processing video filter: " + e.getMessage(), e);
            return false;
        }
    }

    public static class FilterActivityViewModel extends ViewModel {
        public VideoFilter filter = VideoFilter.NONE;
    }
}
