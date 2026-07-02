package com.codder.ultimate.live.activity;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.adapter.EffectAdapter;
import com.codder.ultimate.databinding.ActivityGotoLiveBinding;
import com.codder.ultimate.dialog.CustomDialogClass;
import com.codder.ultimate.leaderboard.LeaderBoardActivity;
import com.codder.ultimate.live.adapter.FilterAdapter;
import com.codder.ultimate.live.adapter.LivePagerAdapter;
import com.codder.ultimate.live.filters.ExposureFilter;
import com.codder.ultimate.live.filters.HazeFilter;
import com.codder.ultimate.live.filters.MonochromeFilter;
import com.codder.ultimate.live.filters.PixelatedFilter;
import com.codder.ultimate.live.filters.SolarizeFilter;
import com.codder.ultimate.live.model.LiveStreamRoot;
import com.codder.ultimate.live.model.RecorderActivityViewModel;
import com.codder.ultimate.live.model.SongRoot;
import com.codder.ultimate.live.model.StickerRoot;
import com.codder.ultimate.live.viewModel.PKLiveViewModel;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.utils.ARSurfaceProvider;
import com.codder.ultimate.utils.AnimationUtil;
import com.codder.ultimate.utils.BitmapUtil;
import com.codder.ultimate.utils.IntentUtil;
import com.codder.ultimate.utils.NoAnimationViewPagerTransformer;
import com.codder.ultimate.utils.SharedConstants;
import com.codder.ultimate.utils.TempUtil;
import com.codder.ultimate.utils.TextFormatUtil;
import com.codder.ultimate.utils.VideoFilter;
import com.codder.ultimate.utils.VideoUtil;
import com.codder.ultimate.viewModel.ViewModelFactory;
import com.codder.ultimate.worker.FileDownloadWorker;
import com.codder.ultimate.worker.VideoSpeedWorker2;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.filter.Filters;
import com.otaliastudios.cameraview.filters.BrightnessFilter;
import com.otaliastudios.cameraview.filters.GammaFilter;
import com.otaliastudios.cameraview.filters.SharpnessFilter;
import com.otaliastudios.cameraview.gesture.Gesture;
import com.otaliastudios.cameraview.gesture.GestureAction;
import com.otaliastudios.transcoder.Transcoder;
import com.otaliastudios.transcoder.TranscoderListener;
import com.otaliastudios.transcoder.TranscoderOptions;
import com.otaliastudios.transcoder.common.TrackType;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ai.deepar.ar.ARErrorType;
import ai.deepar.ar.AREventListener;
import ai.deepar.ar.ARTouchInfo;
import ai.deepar.ar.ARTouchType;
import ai.deepar.ar.CameraResolutionPreset;
import ai.deepar.ar.DeepAR;
import ai.deepar.ar.DeepARImageFormat;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GotoLiveActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks, SurfaceHolder.Callback, AREventListener {

    private static final String TAG = "GotoLiveActivity";
    public static final String EXTRA_SONG = "song";
    public static final String EXTRA_AUDIO = "audio";

    private @Nullable ProcessCameraProvider cameraProviderRef;
    ActivityGotoLiveBinding binding;
    private CameraView mCamera;
    private MediaPlayer player2;
    private MediaPlayer mMediaPlayer;
    private PKLiveViewModel viewModel;
    private Uri selectedImage;
    private String picturePath, picturePathAudio;
    private boolean isPrivateA = false;
    private int pendingAction = -1;
    private boolean isCameraFacingBack = false;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private static final int PERMISSION_REQUEST_CODE = 100;
    private boolean isActivityActive = false;
    private boolean arRecording = false;

    LivePagerAdapter livePagerAdapter;
    private String[] categories;
    private RecorderActivityViewModel mModel;
    private YoYo.YoYoString mPulse;

    private ActivityResultLauncher<String> pickImageLauncherForRoom;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickVideoLauncher;

    private final Runnable mStopper = this::stopRecording;
    int timeInSeconds = 0;
    private boolean isCameraReady = false;

    private Preview previewUseCase;          // for useExternalCameraTexture == true
    private ImageAnalysis imageAnalysisUseCase; // for analyzer path

    private final Runnable pictureTimeoutRunnable = () -> {
        if (customDialogClass != null && customDialogClass.isShowing() && isActivityValid()) {
            customDialogClass.dismiss();
            Toast.makeText(this, getString(R.string.picture_capture_timeout_please_try_again), Toast.LENGTH_SHORT).show();
        }
    };
    private boolean isFilterSnapshot = false;
    private DeepAR deepAR;
    ArrayList<String> effects;
    private volatile boolean isDeepArReady = false;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private final int defaultLensFacing = CameraSelector.LENS_FACING_FRONT;
    private int lensFacing = defaultLensFacing;
    private static final boolean useExternalCameraTexture = false;
    private ARSurfaceProvider surfaceProvider = null;
    private ByteBuffer[] buffers;
    private static final int NUMBER_OF_BUFFERS = 2;
    private int width = 0;
    private int height = 0;
    private boolean arAwaitingFirstFrame = false;
    private boolean arVisible = false;
    private boolean currentSwitchRecording = true;
    private boolean recording = false;
    private int currentEffect = 0;

    // Effects UI data (thumbnail sources + display names)
    private final List<Integer> effectImages = Arrays.asList(
            Const.imgNone, // None
            Const.imgBrightGlasses,
            Const.imgNeonDevilHorns,
            Const.imgMakeupKim,
            Const.imgBurningEffect,
            Const.imgSpringFairy,
            Const.imgBunnyEars,
            Const.imgButterflyHeadband,
            Const.imgCrackedPorcelainFace,
            Const.imgFaceSwap,
            Const.imgSequinButterfly,
            Const.imgSmallFlowers,
            Const.imgSpringDeer
    );


    private final List<String> effectNames = Arrays.asList(
            "None",
            "Bright Glasses",
            "Neon Devil Horns",
            "Makeup Kim",
            "Burning Effect",
            "Spring Fairy",
            "Bunny Ears",
            "Butterfly band",
            "Cracked Face",
            "Face Swap",
            "Sequin Butterfly",
            "Small Flowers",
            "Spring Deer"
    );
    private boolean arRecordingShouldNavigate = false;
    private File arOutputFile;

    private boolean arMode = false;


    /** Enable/disable AR mode with smooth crossfade + resource management */
    private void setArMode(boolean enable) {
        if (enable == arMode) return;

        if (enable) {
            arMode = true;

            // Prepare AR container but keep it transparent until first frame
            binding.layoutDeepAr.setAlpha(0f);
            binding.layoutDeepAr.setVisibility(View.VISIBLE);
            binding.lytEffects.setVisibility(View.VISIBLE);

            isDeepArReady = true;
            arAwaitingFirstFrame = true;
            arVisible = false;

            setupCamera();

            // ensure DeepAR has a render surface immediately
            SurfaceView arView = binding.surface;
            SurfaceHolder sh = arView.getHolder();
            if (sh.getSurface() != null && sh.getSurface().isValid()) {
                Rect frame = sh.getSurfaceFrame();
                deepAR.setRenderSurface(sh.getSurface(), frame.width(), frame.height());
            }
        } else {
            // Route through the smooth crossfade/teardown
            disableArModeSmoothly();
        }
    }

    /** Crossfade back to camera and tear down AR safely */
    private void disableArModeSmoothly() {
        if (!arMode) return;
        arMode = false;

        // 1) Start/ensure CameraView preview is visible BEFORE tearing AR down
        if (mCamera != null && !mCamera.isOpened()) {
            mCamera.setMode(Mode.PICTURE);
            mCamera.open();
        }

        View cam = binding.camera;        // your CameraView
        View ar = binding.layoutDeepAr;             // DeepAR container

        // 2) Crossfade camera in, AR out
        cam.setAlpha(0f);
        cam.setVisibility(View.VISIBLE);
        cam.animate().alpha(1f).setDuration(150).start();

        ar.animate().alpha(0f).setDuration(150).withEndAction(() -> {
            ar.setVisibility(View.GONE);
            ar.setAlpha(1f);
            binding.lytEffects.setVisibility(View.INVISIBLE);

            // NEW: reset first-frame gate
            arAwaitingFirstFrame = false;
            arVisible = false;

            // 3) Now it’s safe to release AR resources
            tearDownDeepAr();
        }).start();
    }

    /** Unbind AR use cases and release DeepAR render surface */
    private void tearDownDeepAr() {
        isDeepArReady = false;

        try {
            if (cameraProviderFuture != null) {
                cameraProviderRef = cameraProviderFuture.get();
                if (cameraProviderRef != null) {
                    if (useExternalCameraTexture) {
                        if (previewUseCase != null) {
                            cameraProviderRef.unbind(previewUseCase);
                            previewUseCase = null;
                        }
                    } else {
                        if (imageAnalysisUseCase != null) {
                            cameraProviderRef.unbind(imageAnalysisUseCase);
                            imageAnalysisUseCase = null;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        if (surfaceProvider != null) {
            surfaceProvider.stop();
            surfaceProvider = null;
        }
        if (deepAR != null) {
            deepAR.setRenderSurface(null, 0, 0);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_goto_live);
        getWindow().setFlags(512, 512);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Edge-to-edge is fine on R+; keep it
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.setSystemBarsAppearance(0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            }
        } else {
            // ⬇️ Clear fullscreen on pre-R so adjustResize works
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        // Apply bottom padding equal to keyboard/nav bar height so content scrolls above IME
        final NestedScrollView scroll = findViewById(R.id.scrollViewTop);

        // Auto-scroll the focused view when the IME shows
        ViewCompat.setOnApplyWindowInsetsListener(scroll, (v, insets) -> {
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int bottom = Math.max(bars.bottom, ime.bottom);
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottom);

            // If keyboard is visible, ensure the focused field is shown
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                scroll.post(() -> ensureFocusedVisible(scroll)); // run after layout
            }
            return insets;
        });

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        viewModel = new ViewModelProvider(this, new ViewModelFactory(new PKLiveViewModel())).get(PKLiveViewModel.class);
        initPickers();
        initView();
        if (hasAllPermissions()) {
            initCamera();
            initCameraListener();
        } else {
            requestAllPermissions();
        }
        initListener();
        initReels();
        viewModelListener();

        initDeepAR();
    }

    /** Init DeepAR engine, license, and event listener*/
    private void initDeepAR() {
        initializeDeepAR();
        initializeFilters();
        initializeViews();
    }

    private void initializeDeepAR() {
        deepAR = new DeepAR(this);
        deepAR.setLicenseKey(Const.DEEP_AR_LICENCE_KEY);
        deepAR.initialize(this, this);

    }

    private void setupCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProviderRef = cameraProviderFuture.get();
                bindImageAnalysis(cameraProviderRef);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        CameraResolutionPreset cameraResolutionPreset = CameraResolutionPreset.P1920x1080;
        int width, height;
        int orientation = getScreenOrientation();
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                || orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            width = cameraResolutionPreset.getWidth();
            height = cameraResolutionPreset.getHeight();
        } else {
            width = cameraResolutionPreset.getHeight();
            height = cameraResolutionPreset.getWidth();
        }

        Size cameraResolution = new Size(width, height);
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        if (useExternalCameraTexture) {
            previewUseCase = new Preview.Builder()
                    .setTargetResolution(cameraResolution)
                    .build();

            cameraProvider.unbindAll(); // we’re inside AR mode here; OK to reset AR graph
            cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, previewUseCase);

            if (surfaceProvider == null) {
                surfaceProvider = new ARSurfaceProvider(this, deepAR);
            }
            previewUseCase.setSurfaceProvider(surfaceProvider);
            surfaceProvider.setMirror(lensFacing == CameraSelector.LENS_FACING_FRONT);
            imageAnalysisUseCase = null; // not used in this mode

        } else {
            buffers = new ByteBuffer[NUMBER_OF_BUFFERS];
            for (int i = 0; i < NUMBER_OF_BUFFERS; i++) {
                buffers[i] = ByteBuffer.allocateDirect(width * height * 4);
                buffers[i].order(ByteOrder.nativeOrder());
                buffers[i].position(0);
            }

            imageAnalysisUseCase = new ImageAnalysis.Builder()
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .setTargetResolution(cameraResolution)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();

            imageAnalysisUseCase.setAnalyzer(ContextCompat.getMainExecutor(this), imageAnalyzer);

            cameraProvider.unbindAll(); // reset AR graph
            cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysisUseCase);
            previewUseCase = null; // not used in this path
        }
    }


    private int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

    /** Build AR effect list + hook adapter to switch effects */
    private void initializeFilters() {
        effects = new ArrayList<>();
        effects.add("none");
        effects.add("effect_bright-glasses.deepar");
        effects.add("effect_neon_devil_horns.deepar");
        effects.add("effect_makeup-kim.deepar");
        effects.add("effect_burning_effect.deepar");
        effects.add("effect_spring-fairy.deepar");
        effects.add("effect_bunny_ears.deepar");
        effects.add("effect_butterfly_headband.deepar");
        effects.add("effect_cracked_porcelain_face.deepar");
        effects.add("effect_face_swap.deepar");
        effects.add("effect_sequin_butterfly.deepar");
        effects.add("effect_small_flowers.deepar");
        effects.add("effect_spring_deer.deepar");

        binding.rvEffects.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        EffectAdapter adapter = new EffectAdapter(GotoLiveActivity.this, effectNames, effectImages, pos -> {
            currentEffect = pos;
            String effectFile = getEffectFileFromName(effectNames.get(pos));
            if (isDeepArReady && deepAR != null) {
                deepAR.switchEffect("effect", getFilterPath(effectFile));
            }
        });
        binding.rvEffects.setAdapter(adapter);
        binding.rvEffects.setItemAnimator(null);
        binding.rvEffects.setHasFixedSize(true);

    }


    @SuppressLint("ClickableViewAccessibility")
    private void initializeViews() {
        Log.d(TAG, "initializeViews:  ====> ");

        SurfaceView arView = binding.surface;

        arView.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    deepAR.touchOccurred(new ARTouchInfo(motionEvent.getX(), motionEvent.getY(), ARTouchType.Start));
                    return true;
                case MotionEvent.ACTION_MOVE:
                    deepAR.touchOccurred(new ARTouchInfo(motionEvent.getX(), motionEvent.getY(), ARTouchType.Move));
                    return true;
                case MotionEvent.ACTION_UP:
                    deepAR.touchOccurred(new ARTouchInfo(motionEvent.getX(), motionEvent.getY(), ARTouchType.End));
                    return true;
            }
            return false;
        });

        arView.getHolder().addCallback(this);

        binding.btnArEffect.setOnClickListener(v -> {
            if (arMode) {
                // Already in AR mode → just toggle the effects UI
                if (binding.lytEffects.getVisibility() == View.VISIBLE) {
                    fadeOutEffects();
                    disableArModeSmoothly();
                } else {
                    fadeInEffects();
                }
            } else {
                // Turn on AR mode (this shows layoutDeepAr, binds camera, etc.)
                setArMode(true);
                fadeInEffects();
            }
        });

        binding.ivEffectClose.setOnClickListener(v -> {
            fadeOutEffects();
            disableArModeSmoothly();
        });

        binding.ivFilterClose.setOnClickListener(v -> {
            binding.rvFilters.setVisibility(View.GONE);
            binding.topFiltersLayout.setVisibility(View.GONE);
        });
    }

    private String getFilterPath(String filterName) {
        Log.d(TAG, "getFilterPath: ====> " + filterName.toString());
        if (filterName.equals("none")) {
            return null;
        }
        return "file:///android_asset/" + filterName;
    }

    private void initPickers() {
        pickImageLauncherForRoom = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));

                        Glide.with(this).load(uri).apply(requestOptions).into(binding.imgProfile);

                        try {
                            // Save image to internal storage and get absolute path
                            picturePathAudio = copyUriToInternalStorage(uri, ".jpg");
                        } catch (IOException e) {
                            Log.e(TAG, "initPickers: ", e);
                            Toast.makeText(this, getString(R.string.error_loading_image), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedImage = uri;
                        startCropActivity(uri);
                    } else {
                        Toast.makeText(this, getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show();
                    }
                });

        pickVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        try {
                            String path = copyUriToInternalStorage(uri, ".mp4");
                            double sizeMB = getFileSizeMB(path);
                            if (sizeMB <= Const.UPLOADING_LIMIT) {
                                submitUploadReels(Uri.fromFile(new File(path)));
                            } else {
                                Toast.makeText(this,
                                        getString(R.string.you_cannot_upload_video_above_30_mb_text),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "initPickers: ", e);
                            Toast.makeText(this, getString(R.string.error_processing_video), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "initPickers: No video selected");
                    }
                });
    }

    private int currentBuffer = 0;
    private final ImageAnalysis.Analyzer imageAnalyzer = image -> {
        if (!isDeepArReady || deepAR == null) {
            image.close();
            return;
        }

        ByteBuffer src = image.getPlanes()[0].getBuffer();
        src.rewind();

        ByteBuffer dst = buffers[currentBuffer];
        dst.clear();              // IMPORTANT
        dst.put(src);
        dst.flip();               // position = 0, limit = size

        try {
            deepAR.receiveFrame(
                    dst,
                    image.getWidth(),
                    image.getHeight(),
                    image.getImageInfo().getRotationDegrees(),
                    lensFacing == CameraSelector.LENS_FACING_FRONT,
                    DeepARImageFormat.RGBA_8888,
                    image.getPlanes()[0].getPixelStride()
            );
        } catch (IllegalStateException e) {
            Log.w(TAG, "DeepAR not ready for frame yet", e);
        } finally {
            currentBuffer = (currentBuffer + 1) % NUMBER_OF_BUFFERS;
            image.close();
        }
    };


    private String copyUriToInternalStorage(Uri uri, String extension) throws IOException {
        File out = new File(
                getCacheDir(),
                "media_" + System.currentTimeMillis() + extension
        );
        try (
                InputStream in = getContentResolver().openInputStream(uri);
                OutputStream os = new FileOutputStream(out)
        ) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
        }
        return out.getAbsolutePath();
    }

    private double getFileSizeMB(String path) {
        return new File(path).length() / 1024.0 / 1024.0;
    }


    private void initReels() {
        mModel = new ViewModelProvider(this).get(RecorderActivityViewModel.class);

        SongRoot.SongItem songDummy = getIntent().getParcelableExtra(EXTRA_SONG);
        Uri audio = getIntent().getParcelableExtra(EXTRA_AUDIO);
        if (audio != null) {
            setupSong(songDummy, audio);
        }

        binding.done.setOnClickListener(view -> {
            if (mCamera == null || mModel == null) return;
            if (mCamera.isTakingVideo()) {
                Toast.makeText(this, R.string.recorder_error_in_progress, Toast.LENGTH_SHORT).show();
            } else if (mModel.segments.isEmpty()) {
                Toast.makeText(this, R.string.recorder_error_no_clips, Toast.LENGTH_SHORT).show();
            } else {
                commitRecordings(mModel.segments, mModel.audio);
            }
        });

        binding.flip.setOnClickListener(view -> {
            if (binding.layoutDeepAr.getVisibility() == View.VISIBLE) {
                lensFacing = lensFacing == CameraSelector.LENS_FACING_FRONT ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT;
                //unbind immediately to avoid mirrored frame.
                try {
                    cameraProviderRef = cameraProviderFuture.get();
                    cameraProviderRef.unbindAll();
                } catch (ExecutionException e) {
                    Log.e(TAG, "initReels: ", e);
                } catch (InterruptedException e) {
                    Log.e(TAG, "initReels: ", e);
                }
                setupCamera();
            } else {
                switchCamera();
            }
        });

        binding.flash.setOnClickListener(view -> {
            if (mCamera == null) return;
            if (mCamera.isTakingVideo()) {
                Toast.makeText(this, R.string.recorder_error_in_progress, Toast.LENGTH_SHORT).show();
            } else {
                mCamera.setFlash(mCamera.getFlash() == Flash.OFF ? Flash.TORCH : Flash.OFF);
            }
        });

        setupSpeedControl();

        binding.filter.setOnClickListener(view -> {
            if (binding.rvFilters.getVisibility() == View.VISIBLE) {
                binding.rvFilters.setVisibility(View.GONE);
                binding.topFiltersLayout.setVisibility(View.GONE);
                return;
            }
            binding.rvFilters.setVisibility(View.VISIBLE);
            binding.topFiltersLayout.setVisibility(View.VISIBLE);
            binding.lytEffects.setVisibility(View.GONE);
            if (binding.layoutDeepAr.getVisibility() == View.VISIBLE) {
                // AR mode: DeepAR screenshot
                mHandler.postDelayed(this::dismissDialogSafely, 5000);
                deepAR.takeScreenshot();
            } else {
                // Normal camera mode: snapshot
                if (mCamera == null || !mCamera.isOpened()) {
                    Toast.makeText(this, getString(R.string.camera_not_ready), Toast.LENGTH_SHORT).show();
                    return;
                }
                isFilterSnapshot = true;
                mHandler.postDelayed(this::dismissDialogSafely, 5000);
                mCamera.takePictureSnapshot();
            }
        });

        binding.sticker.setVisibility(getResources().getBoolean(R.bool.stickers_enabled) ? View.VISIBLE : View.GONE);

        setupTimerSheet();

        binding.sound.setOnClickListener(view -> {
            if (mModel == null) return;
            if (mModel.segments.isEmpty()) {
                startActivityForResult(new Intent(this, SongPickerActivity.class), SharedConstants.REQUEST_CODE_PICK_SONG);
            } else if (mModel.audio == null) {
                Toast.makeText(this, R.string.message_song_select, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.message_song_change, Toast.LENGTH_SHORT).show();
            }
        });

        binding.upload.setOnClickListener(view ->
                pickVideoLauncher.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                                .build()
                )
        );

        binding.segments.enableAutoProgressView(SharedConstants.MAX_DURATION);
        binding.segments.setDividerColor(Color.BLACK);
        binding.segments.setDividerEnabled(true);
        binding.segments.setDividerWidth(2f);
        binding.segments.setListener(length -> {/* Handle segment update if needed */});
        binding.segments.setShader(new int[]{
                ContextCompat.getColor(this, R.color.purple),
                ContextCompat.getColor(this, R.color.tintColor)
        });

        if (mCamera != null) {
            // CameraListener already added in initCameraListener()
        }

        binding.record.setOnClickListener(view -> {
            // If DeepAR UI is visible, record with DeepAR
            if (binding.layoutDeepAr.getVisibility() == View.VISIBLE) {
                if (deepAR == null || !isDeepArReady) {
                    Toast.makeText(this, getString(R.string.ar_is_starting_please_try_again), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (arRecording) {
                    arRecordingShouldNavigate = true;
                    deepAR.stopVideoRecording();
                    arRecording = false;
                    // optional UI: stop pulse, restore controls, etc.
                } else {
                    String ts = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(new Date());
                    arOutputFile = new File(getCacheDir(), "ar_video_" + ts + ".mp4");

                    // width/height are set after camera bind; fallback to a sane default if zero
                    int w = (width > 0 ? width : 720);
                    int h = (height > 0 ? height : 1280);

                    deepAR.startVideoRecording(arOutputFile.getAbsolutePath(), w / 2, h / 2);
                    arRecording = true;
                    Toast.makeText(this, getString(R.string.recording_started), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // Otherwise, fall back to your existing CameraView (reels) logic
            if (mCamera == null) return;
            if (mCamera.isTakingVideo()) {
                stopRecording();
                binding.tabTablayout.setVisibility(View.VISIBLE);
            } else {
                binding.rvFilters.setVisibility(View.GONE);
                binding.topFiltersLayout.setVisibility(View.GONE);
                binding.speeds.setVisibility(View.GONE);
                binding.tabTablayout.setVisibility(View.GONE);
                startRecording();
            }
        });

    }

    /**  Configure bottom sheet for countdown timer before recording */
    private void setupTimerSheet() {
        View sheet = findViewById(R.id.timer_sheet);
        BottomSheetBehavior<View> bsb = BottomSheetBehavior.from(sheet);
        ImageView close = sheet.findViewById(R.id.btnClose);
        TextView maximum = findViewById(R.id.maximum);
        Slider selection = findViewById(R.id.selection);

        close.setOnClickListener(v -> bsb.setState(BottomSheetBehavior.STATE_COLLAPSED));
        sheet.findViewById(R.id.btnDone).setOnClickListener(v -> {
            bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
            startTimer();
        });

        binding.timer.setOnClickListener(view -> {
            Log.d(TAG, "===> Timer icon clicked");
            if (mCamera != null && !mCamera.isTakingVideo()) {
                bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
                Log.d(TAG, "===> Bottom sheet state changed to EXPANDED");
            } else {
                Toast.makeText(this, getString(R.string.recorder_error_in_progress), Toast.LENGTH_SHORT).show();
            }
        });

        selection.setLabelFormatter(value -> TextFormatUtil.toMMSS((long) value));

        bsb.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED && mModel != null) {
                    long remaining = SharedConstants.MAX_DURATION - mModel.recorded();
                    long max = TimeUnit.MILLISECONDS.toSeconds(remaining);
                    max = TimeUnit.SECONDS.toMillis(max);
                    selection.setValue(0);
                    selection.setValueTo(max);
                    selection.setValue(max);
                    maximum.setText(TextFormatUtil.toMMSS(max));
                }
            }
        });
    }

    private void startTimer() {
        binding.count.setText(null);
        Slider selection = findViewById(R.id.selection);
        long duration = (long) selection.getValue();
        CountDownTimer timer = new CountDownTimer(3000, 1000) {

            @Override
            public void onTick(long remaining) {
                mHandler.post(() -> binding.count.setText(TimeUnit.MILLISECONDS.toSeconds(remaining) + 1 + ""));
            }

            @Override
            public void onFinish() {
                mHandler.post(() -> binding.countdown.setVisibility(View.GONE));
                startRecording();
                mHandler.postDelayed(mStopper, duration);
            }
        };
        binding.countdown.setOnClickListener(v -> {
            timer.cancel();
            binding.countdown.setVisibility(View.GONE);
        });
        binding.countdown.setVisibility(View.VISIBLE);
        timer.start();
    }

    private void setupSpeedControl() {

        binding.speed.setOnClickListener(view -> {
            if (mCamera != null && !mCamera.isTakingVideo()) {
                binding.speeds.setVisibility(binding.speeds.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            } else {
                Toast.makeText(this, getString(R.string.recorder_error_in_progress), Toast.LENGTH_SHORT).show();
            }
        });

        binding.speed.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? View.VISIBLE : View.GONE);

        if (mModel != null) {
            binding.speed05x.setChecked(mModel.speed == .5f);
            binding.speed075x.setChecked(mModel.speed == .75f);
            binding.speed1x.setChecked(mModel.speed == 1);
            binding.speed15x.setChecked(mModel.speed == 1.5f);
            binding.speed2x.setChecked(mModel.speed == 2);
        }

        binding.speeds.setOnCheckedChangeListener((group, checkedId) -> {
            float factor;
            if (checkedId == R.id.speed05x) {
                factor = 0.5f;
            } else if (checkedId == R.id.speed075x) {
                factor = 0.75f;
            } else if (checkedId == R.id.speed15x) {
                factor = 1.5f;
            } else if (checkedId == R.id.speed2x) {
                factor = 2f;
            } else {
                factor = 1f;
            }
            if (mModel != null) mModel.speed = factor;
        });
    }

    private void commitRecordings(@NonNull List<RecorderActivityViewModel.RecordSegment> segments, @Nullable Uri audio) {
        List<String> segmentPaths = new ArrayList<>();
        for (RecorderActivityViewModel.RecordSegment segment : segments) {
            segmentPaths.add(segment.file); // assuming 'file' is a path string
        }

        timeInSeconds = 0;
        startMergingFlow(audio, segmentPaths);
    }


    private boolean hasAllPermissions() {
        String[] perms = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        };
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(
                    this, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestAllPermissions() {
        String[] perms = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        };
        ActivityCompat.requestPermissions(
                this, perms, PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults
        );
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this
        );
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean ok = true;
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                initCamera();
                initCameraListener();
                executePendingAction();
            }
        }
    }

    private void initCamera() {
        mCamera = binding.camera;
        mCamera.setLifecycleOwner(this);
        mCamera.setMode(Mode.VIDEO);

        // Gestures
        mCamera.mapGesture(Gesture.TAP, GestureAction.AUTO_FOCUS);
        mCamera.mapGesture(Gesture.PINCH, GestureAction.ZOOM);
        mCamera.mapGesture(Gesture.SCROLL_VERTICAL, GestureAction.EXPOSURE_CORRECTION);

        // Video quality
        mCamera.setVideoBitRate(16_000_000); // 1080p30–60 sweet spot; adjust as needed
        mCamera.setPreviewFrameRate(30f);
    }

    /** Attach CameraListener: ready, snapshot, video start/stop, errors */
    private void initCameraListener() {
        if (mCamera != null) {
            mCamera.addCameraListener(new CameraListener() {

                @Override
                public void onCameraOpened(@NonNull CameraOptions options) {
                    isCameraReady = true;
                    if (pendingAction != -1) {
                        Log.d(TAG, "Executing pending action after camera ready: " + pendingAction);
                        executePendingAction(); // Will retry takePicture()
                    }
                }

                @Override
                public void onPictureTaken(@NonNull PictureResult result) {
                    Log.d(TAG, "onPictureTaken received at: " + System.currentTimeMillis());

                    if (isFilterSnapshot) {
                        isFilterSnapshot = false;
                        customDialogClass.dismiss();

                        result.toBitmap(bitmap -> {
                            if (bitmap != null) {
                                Bitmap square = BitmapUtil.getSquareThumbnail(bitmap, 250);
                                bitmap.recycle();
                                Bitmap rounded = BitmapUtil.addRoundCorners(square, 10);
                                square.recycle();
                                FilterAdapter adapter = new FilterAdapter(GotoLiveActivity.this, rounded);
                                adapter.setListener(GotoLiveActivity.this::applyPreviewFilter);
                                binding.rvFilters.setAdapter(adapter);
                                binding.rvFilters.setVisibility(View.VISIBLE);
                            }

                            customDialogClass.dismiss();
                        });
                        return;
                    }
                    handlePictureTaken(result);
                }

                @Override
                public void onCameraError(@NonNull CameraException exception) {
                    mHandler.removeCallbacks(pictureTimeoutRunnable); // Cancel timeout handler
                    dismissDialogSafely();
                    customDialogClass.dismiss();
                    Log.e(TAG, "Camera error: " + exception.getMessage());
                }


                @Override
                public void onVideoRecordingStart() {
                    Log.v(TAG, "Video recording started");
                    binding.segments.resume();
                    configureMediaPlayerSpeed();
                    if (mMediaPlayer != null) mMediaPlayer.start();
                    mPulse = YoYo.with(Techniques.Pulse).repeat(YoYo.INFINITE).playOn(binding.record);
                    binding.record.setSelected(true);
                    toggleVisibility(false);
                }

                @Override
                public void onVideoRecordingEnd() {
                    Log.v(TAG, "Video recording ended");
                    binding.segments.pause();
                    binding.segments.addDivider();
                    mHandler.removeCallbacks(mStopper);
                    mHandler.postDelayed(() -> processCurrentRecording(), 500);
                    if (mMediaPlayer != null) mMediaPlayer.pause();
                    if (mPulse != null) mPulse.stop();
                    binding.record.setSelected(false);
                    toggleVisibility(true);
                }
            });
        }
    }

    /** Apply preview-only GL filter on CameraView (small chooser) */
    private void applyPreviewFilter(VideoFilter filter) {
        switch (filter) {
            case BRIGHTNESS: {
                BrightnessFilter glf = (BrightnessFilter) Filters.BRIGHTNESS.newInstance();
                glf.setBrightness(1.2f);
                mCamera.setFilter(glf);
                break;
            }
            case EXPOSURE:
                mCamera.setFilter(new ExposureFilter());
                break;
            case GAMMA: {
                GammaFilter glf = (GammaFilter) Filters.GAMMA.newInstance();
                glf.setGamma(2);
                mCamera.setFilter(glf);
                break;
            }
            case GRAYSCALE:
                mCamera.setFilter(Filters.GRAYSCALE.newInstance());
                break;
            case HAZE: {
                HazeFilter glf = new HazeFilter();
                glf.setSlope(-0.5f);
                mCamera.setFilter(glf);
                break;
            }
            case INVERT:
                mCamera.setFilter(Filters.INVERT_COLORS.newInstance());
                break;
            case MONOCHROME:
                mCamera.setFilter(new MonochromeFilter());
                break;
            case PIXELATED: {
                PixelatedFilter glf = new PixelatedFilter();
                glf.setPixel(5);
                mCamera.setFilter(glf);
                break;
            }
            case POSTERIZE:
                mCamera.setFilter(Filters.POSTERIZE.newInstance());
                break;
            case SEPIA:
                mCamera.setFilter(Filters.SEPIA.newInstance());
                break;
            case SHARP: {
                SharpnessFilter glf = (SharpnessFilter) Filters.SHARPNESS.newInstance();
                glf.setSharpness(0.25f);
                mCamera.setFilter(glf);
                break;
            }
            case SOLARIZE:
                mCamera.setFilter(new SolarizeFilter());
                break;
            case VIGNETTE:
                mCamera.setFilter(Filters.VIGNETTE.newInstance());
                break;
            default:
                mCamera.setFilter(Filters.NONE.newInstance());
                break;
        }
    }

    /** Handle still picture: save file and open UploadPost */
    private void handlePictureTaken(@NonNull PictureResult result) {
        mHandler.removeCallbacks(pictureTimeoutRunnable); // Cancel timeout toast

        byte[] data = result.getData();
        if (data == null || data.length == 0) {
            mHandler.removeCallbacks(pictureTimeoutRunnable);

            dismissDialogSafely();
            Toast.makeText(this, getString(R.string.failed_to_capture_image), Toast.LENGTH_SHORT).show();
            return;
        }
        File imageFile = saveImageToFile(data);
        if (imageFile == null || !imageFile.exists()) {
            dismissDialogSafely();
            Toast.makeText(this, getString(R.string.failed_to_save_image), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, UploadPostActivity.class);
        Log.d(TAG, "handlePictureTaken:---------------- " + imageFile.getAbsolutePath());
        intent.putExtra(Const.CAPTURED_POST_IMAGE, imageFile.getAbsolutePath());
        intent.putExtra(Const.IS_CAPTURED, true);
        startActivity(intent);
        finish();
        mCamera.setMode(Mode.VIDEO); // Reset mode after picture
    }

    /** Play shutter sound safely from assets */
    public void makeCameraSound() {
        if (player2 != null) {
            player2.release();
            player2 = null;
        }
        try {
            player2 = new MediaPlayer();
            AssetFileDescriptor afd2 = getAssets().openFd("camera_shutter.mp3");
            player2.setDataSource(afd2.getFileDescriptor(), afd2.getStartOffset(), afd2.getLength());
            player2.prepare();
            player2.start();
        } catch (IOException | IllegalStateException e) {
            Log.d(TAG, "Camera sound error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private File saveImageToFile(byte[] data) {
        File imageFile = createImageFile();
        if (imageFile == null) return null;
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(data);
            fos.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error saving image: " + e.getMessage());
            return null;
        }
        return imageFile;
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            Log.e(TAG, "Error creating image file: " + e.getMessage());
            return null;
        }
    }

    private void stopRecording() {
        Log.d(TAG, "stopRecording: ");
        mCamera.stopVideo();
    }

    private void processCurrentRecording() {
        if (mModel == null) return;
        File currentVideo = mModel.video;
        mModel.video = null; // Avoid double processing
        if (currentVideo == null) return;

        long duration;
        try {
            duration = VideoUtil.getDuration(this, Uri.fromFile(currentVideo));
        } catch (IOException e) {
            Log.e(TAG, "Error getting video duration", e);
            return;
        }
        if (duration <= 0) {
            Log.e(TAG, "processCurrentRecording: Video duration is zero or negative, skipping segment.");
            return;
        }

        if (mModel.speed != 1) {
            applyVideoSpeed(currentVideo, mModel.speed, duration);
        } else {
            RecorderActivityViewModel.RecordSegment segment = new RecorderActivityViewModel.RecordSegment();
            segment.file = currentVideo.getAbsolutePath();
            segment.duration = duration;
            mModel.segments.add(segment);
        }
    }

    private void applyVideoSpeed(File file, float speed, long duration) {
        File output = TempUtil.createNewFile(this, ".mp4");
        customDialogClass.show();

        Data data = new Data.Builder()
                .putString(VideoSpeedWorker2.KEY_INPUT, file.getAbsolutePath())
                .putString(VideoSpeedWorker2.KEY_OUTPUT, output.getAbsolutePath())
                .putFloat(VideoSpeedWorker2.KEY_SPEED, speed)
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(VideoSpeedWorker2.class).setInputData(data).build();
        WorkManager wm = WorkManager.getInstance(this);
        wm.enqueue(request);
        wm.getWorkInfoByIdLiveData(request.getId()).observe(this, info -> {
            boolean ended = info.getState() == WorkInfo.State.CANCELLED || info.getState() == WorkInfo.State.FAILED || info.getState() == WorkInfo.State.SUCCEEDED;
            if (ended) {
                dismissDialogSafely();
            }
            if (info.getState() == WorkInfo.State.SUCCEEDED) {
                RecorderActivityViewModel.RecordSegment segment = new RecorderActivityViewModel.RecordSegment();
                segment.file = output.getAbsolutePath();
                segment.duration = duration;
                mModel.segments.add(segment);
            }
        });
    }

    private void configureMediaPlayerSpeed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mMediaPlayer != null) {
            float speed = 1f;
            if (mModel != null) {
                switch ((int) (mModel.speed * 100)) {
                    case 50:
                        speed = 2f;
                        break;
                    case 75:
                        speed = 1.5f;
                        break;
                    case 150:
                        speed = 0.75f;
                        break;
                    case 200:
                        speed = 0.5f;
                        break;
                }
            }
            PlaybackParams params = new PlaybackParams();
            params.setSpeed(speed);
            mMediaPlayer.setPlaybackParams(params);
        }
    }

    private void initView() {
        categories = new String[]{getString(R.string.audio_live), getString(R.string.relites), getString(R.string.post)};
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        viewModel.initLister();
        customDialogClass = new CustomDialogClass(this, R.style.customStyle);
        customDialogClass.setCancelable(false);
        customDialogClass.setCanceledOnTouchOutside(false);

        binding.etPasscode.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) bringPasscodeIntoView();
        });
        binding.etPasscode.setOnClickListener(v -> bringPasscodeIntoView());

        // When switching to the Audio tab, your code shows layTop:
        /// position == 1 branch — keep, it’s correct
        // but when toggling Private, make layPasscode visible then:
        binding.layPrivate.setOnClickListener(v -> {
            binding.layPasscode.setVisibility(View.VISIBLE);
            binding.etPasscode.requestFocus();
            bringPasscodeIntoView();
        });

        binding.etPasscode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.scrollViewTop.smoothScrollTo(0, binding.etPasscode.getBottom());
                }
            }
        });
    }

    private void bringPasscodeIntoView() {
        binding.etPasscode.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.showSoftInput(binding.etPasscode, InputMethodManager.SHOW_IMPLICIT);
            binding.scrollViewTop.post(() ->
                    binding.scrollViewTop.smoothScrollTo(0, binding.etPasscode.getBottom()));
        });
    }

    void showPasscode() {
        binding.layPasscode.setVisibility(View.VISIBLE);
        binding.etPasscode.requestFocus();
        binding.etPasscode.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.showSoftInput(binding.etPasscode, InputMethodManager.SHOW_IMPLICIT);
            binding.scrollViewTop.post(() -> binding.scrollViewTop.smoothScrollTo(0, binding.etPasscode.getBottom()));
        });
    }

    private void initListener() {
        if (binding == null) return;

        binding.ivBack.setOnClickListener(view -> onBackPressed());

        Glide.with(this).load(R.drawable.radio_selected).into(binding.rbpublic);
        Glide.with(this).load(R.drawable.radio_unselected).into(binding.rbPrivate);

        livePagerAdapter = new LivePagerAdapter(getSupportFragmentManager(), categories);
        binding.liveViewpager.setAdapter(livePagerAdapter);
        binding.liveViewpager.setCurrentItem(0, false);
        if (binding.liveViewpager.getParent() != null) {
            ((ViewGroup) binding.liveViewpager.getParent()).removeView(binding.liveViewpager);
            binding.framelayout.addView(binding.liveViewpager, 0);
        }

        binding.tabTablayout.setupWithViewPager(binding.liveViewpager);
        binding.liveViewpager.setPageTransformer(false, new NoAnimationViewPagerTransformer());

        binding.tabTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.liveViewpager.setCurrentItem(tab.getPosition(), false);
                View v = tab.getCustomView();
                if (v != null) {
                    TextView tv = v.findViewById(R.id.tvTab);
                    ImageView indicator = v.findViewById(R.id.indicator);
                    tv.setTextColor(getColorCompat(R.color.white));
                    Typeface typeface = ResourcesCompat.getFont(GotoLiveActivity.this, R.font.airbnbcereal_w_xbd);
                    tv.setTypeface(typeface);
                    indicator.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                if (v != null) {
                    TextView tv = v.findViewById(R.id.tvTab);
                    ImageView indicator = v.findViewById(R.id.indicator);
                    tv.setTextColor(getColorCompat(R.color.white_35));
                    Typeface typeface = ResourcesCompat.getFont(GotoLiveActivity.this, R.font.airbnbcereal_w_bk);
                    tv.setTypeface(typeface);
                    indicator.setVisibility(INVISIBLE);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        setTab(categories);

        binding.layPublic.setOnClickListener(view -> {
            isPrivateA = false;
            try {

                Glide.with(this).load(R.drawable.radio_selected).into(binding.rbpublic);
                Glide.with(this).load(R.drawable.radio_unselected).into(binding.rbPrivate);

                binding.scrollViewTop.post(() -> {
                    binding.layPasscode.animate().alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            binding.layPasscode.setVisibility(View.GONE);
                        }
                    });
                    ObjectAnimator animator = ObjectAnimator.ofInt(binding.scrollViewTop, "scrollY", 0);
                    animator.setDuration(500);
                    animator.start();
                });
            } catch (Exception ignored) {
            }
        });

        binding.layPrivate.setOnClickListener(view -> {
            isPrivateA = true;
            try {

                Glide.with(this).load(R.drawable.radio_unselected).into(binding.rbpublic);
                Glide.with(this).load(R.drawable.radio_selected).into(binding.rbPrivate);
                showPasscode();
                int randomNumber = 100000 + new Random().nextInt(900000);
                binding.etPasscode.setText(String.valueOf(randomNumber));
                binding.layPasscode.setAlpha(0f);
                binding.layPasscode.animate().alpha(1f).setDuration(300).setListener(null);
                binding.scrollViewTop.post(() -> {
                    int maxScrollY = binding.scrollViewTop.getChildAt(0).getHeight() - binding.scrollViewTop.getHeight();
                    ObjectAnimator animator = ObjectAnimator.ofInt(binding.scrollViewTop, "scrollY", maxScrollY);
                    animator.setDuration(500);
                    animator.start();
                });

                binding.ivCopy.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager) GotoLiveActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("", String.valueOf(randomNumber));
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(GotoLiveActivity.this, getString(R.string.copied_successfully), Toast.LENGTH_SHORT).show();
                });

                binding.ivShare.setOnClickListener(v -> {
                    String shareMessage = getString(R.string.here_s_the_private_passcode_for_the_room) + randomNumber;
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
                });
            } catch (Exception ignored) {
            }
        });


        binding.btnPk.setOnClickListener(v -> {
            try {
                int pos = binding.liveViewpager.getCurrentItem();
                if (pos == 0) goToAudioStreamActivity();
                else if (pos == 2) checkPermission(0);
                v.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
            } catch (Exception ignored) {
            }
        });

        binding.btnSwitchCamaraFunction.setOnClickListener(v -> switchCamera());

        binding.liveViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                vibrateDevice();
                Log.d(TAG, "onPageSelected: ==> " + position);
                if (position == 0) {
                    binding.lytLiveFilterFunctions.setVisibility(View.GONE);
                    binding.btnPk.setText(R.string.go_live);
                    binding.galleryBtn.setVisibility(View.GONE);
                    binding.btnSwitchCamara.setVisibility(View.GONE);
                    binding.lytReelFilterFunction.setVisibility(View.GONE);
                    binding.constraintLayout.setVisibility(View.GONE);
                    binding.topLayout.setVisibility(View.GONE);
                    binding.btnArEffect.setVisibility(View.GONE);
                    binding.segments.setVisibility(View.GONE);
                    binding.audioBg.setVisibility(View.VISIBLE);
                    binding.btnPk.setVisibility(View.VISIBLE);
                    ViewGroup.LayoutParams params = binding.btnPk.getLayoutParams();
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    binding.btnPk.setLayoutParams(params);

                    binding.layTop.setVisibility(View.VISIBLE);
                } else if (position == 1) {
                    binding.lytReelFilterFunction.setVisibility(View.VISIBLE);
                    binding.lytLiveFilterFunctions.setVisibility(View.GONE);
                    binding.btnPk.setVisibility(View.GONE);
                    binding.galleryBtn.setVisibility(View.GONE);
                    binding.audioBg.setVisibility(View.GONE);
                    binding.btnSwitchCamara.setVisibility(View.GONE);
                    binding.constraintLayout.setVisibility(View.VISIBLE);
                    binding.topLayout.setVisibility(View.VISIBLE);
                    binding.btnArEffect.setVisibility(View.VISIBLE);
                    binding.segments.setVisibility(View.VISIBLE);
                    binding.layTop.setVisibility(View.GONE);
                } else {
                    binding.lytLiveFilterFunctions.setVisibility(View.GONE);
                    binding.lytReelFilterFunction.setVisibility(View.GONE);
                    binding.btnPk.setVisibility(View.VISIBLE);
                    binding.btnPk.setText(R.string.take_picture);
                    ViewGroup.LayoutParams params = binding.btnPk.getLayoutParams();
                    params.width = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            170,
                            getResources().getDisplayMetrics()
                    );
                    binding.btnPk.setLayoutParams(params);
                    binding.galleryBtn.setVisibility(View.VISIBLE);
                    binding.btnSwitchCamara.setVisibility(View.VISIBLE);
                    binding.constraintLayout.setVisibility(View.GONE);
                    binding.topLayout.setVisibility(View.GONE);
                    binding.btnArEffect.setVisibility(View.GONE);
                    binding.audioBg.setVisibility(View.GONE);
                    binding.segments.setVisibility(View.GONE);
                    binding.layTop.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        binding.galleryBtn.setOnClickListener(v -> {
            int pos = binding.liveViewpager.getCurrentItem();
            if (pos == 1) { // Relites
                pickVideoLauncher.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                                .build()
                );
            } else if (pos == 2) { // Post (picture)
                pickImageLauncher.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build()
                );
            }
        });

        binding.btnSwitchCamara.setOnClickListener(v -> switchCamera());

        binding.btnMute.setOnClickListener(v -> {
            viewModel.isMuted = !viewModel.isMuted;
            if (viewModel.isMuted) {
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mute));
            } else {
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.unmute));
            }
        });

        binding.imgProfile.setOnClickListener(v -> {
                    pickImageLauncherForRoom.launch("image/*");
                }
        );


        viewModelObserver();

        binding.record.setOnClickListener(view -> {
            if (mCamera == null) return;
            if (mCamera.isTakingVideo()) {
                stopRecording();
                binding.tabTablayout.setVisibility(View.VISIBLE);
            } else {
                binding.rvFilters.setVisibility(View.GONE);
                binding.topFiltersLayout.setVisibility(View.GONE);
                binding.speeds.setVisibility(View.GONE);
                binding.tabTablayout.setVisibility(View.GONE);
                startRecording();
            }
        });
    }


    private void switchCamera() {
        if (mCamera.isTakingVideo()) {
            Toast.makeText(this, getString(R.string.recorder_error_in_progress), Toast.LENGTH_SHORT).show();
        } else {
            mCamera.toggleFacing();
            isCameraFacingBack = mCamera.getFacing() == Facing.BACK;
            Log.d(TAG, "switchCamera: Facing now " + (isCameraFacingBack ? "BACK" : "FRONT"));
        }
    }

    private void vibrateDevice() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(50);
            }
        }
    }

    private void viewModelListener() {
        viewModel.isShowFilterSheet.observe(this, aBoolean -> {
            if (aBoolean) {
                binding.rlGolive.setVisibility(View.GONE);
            } else {
                binding.rlGolive.setVisibility(View.VISIBLE);
            }
        });
    }

    private void viewModelObserver() {
        viewModel.isShowFilterSheet.observe(this, aBoolean -> {
            if (aBoolean) {
                binding.lytFilters.setVisibility(View.VISIBLE);
            } else {
                binding.lytFilters.setVisibility(View.GONE);
            }
        });
        viewModel.selectedFilter.observe(this, selectedFilter -> {
            if (selectedFilter.getTitle().equalsIgnoreCase("None")) {
                binding.imgFilter.setImageDrawable(null);
            } else {
               /* if (!isFinishing()) {
                    Glide.with(this).load(FilterUtils.getDraw(selectedFilter.getTitle())).into(binding.imgFilter);
                }*/
            }
        });
    }

    private void setTab(String[] categories) {
        binding.tabTablayout.setTabGravity(TabLayout.GRAVITY_FILL);
        binding.tabTablayout.removeAllTabs();
        for (int i = 0; i < categories.length; i++) {
            binding.tabTablayout.addTab(binding.tabTablayout.newTab().setCustomView(createCustomView(i, categories[i])));
        }

        ViewGroup tabStrip = (ViewGroup) binding.tabTablayout.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            View tabView = tabStrip.getChildAt(i);
            tabView.setPadding(0, 0, 35, 0);
        }

    }

    private View createCustomView(int i, String title) {
        View v = LayoutInflater.from(this).inflate(R.layout.custom_tabhorizontal, null);
        TextView tv = v.findViewById(R.id.tvTab);
        ImageView indicator = v.findViewById(R.id.indicator);
        tv.setText(title);
        if (i == 0) {
            tv.setTextColor(getColorCompat(R.color.white));
            Typeface typeface = ResourcesCompat.getFont(this, R.font.airbnbcereal_w_xbd);
            tv.setTypeface(typeface);
            indicator.setVisibility(VISIBLE);
        } else {
            tv.setTextColor(getColorCompat(R.color.white_35));
            Typeface typeface = ResourcesCompat.getFont(this, R.font.airbnbcereal_w_bk);
            tv.setTypeface(typeface);
            indicator.setVisibility(INVISIBLE);
        }
        tv.setTextSize(18);
        return v;
    }

    private int getColorCompat(int resId) {
        return ContextCompat.getColor(this, resId);
    }

    /** Validate inputs → multipart request → open Audio Live host UI */
    private void goToAudioStreamActivity() {

        try {
            String title = binding.etTitle.getText().toString();
            if (title.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_enter_room_title), Toast.LENGTH_SHORT).show();
                return;
            }

            customDialogClass.show();
            MediaType text = MediaType.parse("text/plain");

            RequestBody titleBody = RequestBody.create(text, title);
            RequestBody roomWelcomeBody = RequestBody.create(text, (binding.etWelcomeMessage.getText().toString().trim().isEmpty()) ? getString(R.string.welcome_to_the_party) : binding.etWelcomeMessage.getText().toString().trim());
            RequestBody userIdBody = RequestBody.create(text, sessionManager.getUser().getId());
            RequestBody channelBody = RequestBody.create(text, sessionManager.getUser().getId());
            RequestBody isPublicBody = RequestBody.create(text, String.valueOf(true));
            RequestBody audioBody = RequestBody.create(text, String.valueOf(true));
            RequestBody agoraUIDBody = RequestBody.create(text, String.valueOf(1));
            String defaultThemePath = sessionManager.getStringValue("isDefaultBackground");
            RequestBody themeBody = RequestBody.create(text, defaultThemePath != null ? defaultThemePath : "");

            MultipartBody.Part body = null;

            if (picturePathAudio != null) {
                File file = new File(picturePathAudio);
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                body = MultipartBody.Part.createFormData("roomImage", file.getName(), requestFile);
            }
            HashMap<String, RequestBody> map = new HashMap<>();
            map.put("roomName", titleBody);
            map.put("roomWelcome", roomWelcomeBody);
            map.put("userId", userIdBody);
            map.put("channel", channelBody);
            map.put("isPublic", isPublicBody);
            map.put("background", themeBody);

            RequestBody privateCodeBody;
            if (isPrivateA) {
                String raw = String.valueOf(binding.etPasscode.getText());
                // keep only digits; remove spaces/newlines/other chars
                String digitsOnly = raw.replaceAll("\\D+", "");
                if (digitsOnly.isEmpty()) {
                    dismissDialogSafely();
                    Toast.makeText(this, getString(R.string.please_enter_valid_passcode), Toast.LENGTH_SHORT).show();
                    return;
                }
                // Optional: enforce length range (e.g., 4–8)
                if (digitsOnly.length() < 4 || digitsOnly.length() > 8) {
                    dismissDialogSafely();
                    Toast.makeText(this, getString(R.string.passcode_length_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                // Force it to be a number string (single value)
                privateCodeBody = RequestBody.create(text, String.valueOf(Long.parseLong(digitsOnly)));
            } else {
                privateCodeBody = RequestBody.create(text, "0");
            }
            map.put("privateCode", privateCodeBody);
            map.put("audio", audioBody);
            map.put("agoraUID", agoraUIDBody);

            Log.d(TAG, "goToAudioStreamActivity: ====> " + themeBody);
            Call<LiveStreamRoot> call = RetrofitBuilder.create().makeLiveUser(map, body);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<LiveStreamRoot> call, Response<LiveStreamRoot> response) {
                    dismissDialogSafely();

                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        LiveStreamRoot.LiveUser liveUser = response.body().getLiveUser();
                        if (liveUser != null) {
                            Log.d(TAG, "onResponse: Background: " + liveUser.getBackground());
                            Intent intent = new Intent(GotoLiveActivity.this, HostLiveAudioActivity.class);
                            intent.putExtra(Const.DATA, new Gson().toJson(liveUser));
                            Log.d(TAG, "onResponse: LiveUser JSON: " + new Gson().toJson(liveUser));
                            intent.putExtra(Const.PRIVACY, isPrivateA ? "Private" : "Public");
                            startActivity(intent);
                            finish();
                        } else {
                            Log.w(TAG, "Live user data is null.");
                            Toast.makeText(GotoLiveActivity.this, getString(R.string.live_user_data_is_missing), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Unsuccessful response or invalid status");
                        Toast.makeText(GotoLiveActivity.this, getString(R.string.failed_to_start_live_stream), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LiveStreamRoot> call, Throwable throwable) {
                    Log.e(TAG, "onFailure: Error making live user request", throwable);
                    Toast.makeText(GotoLiveActivity.this, getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
                }

            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkPermission(int caseScenario) {
        pendingAction = caseScenario;
        if (hasAllPermissions()) {
            executePendingAction();
        } else {
            requestAllPermissions();
        }
    }

    private void executePendingAction() {
        switch (pendingAction) {
            case 0:
                takePicture();
                break;
            case 1:
                choosePhoto();
                break;
            case 2:
                chooseVideoForUpload();
                break;
        }
        pendingAction = -1;
    }

    public void takePicture() {
        if (mCamera == null || !isActivityValid()) return;

        if (!isCameraReady || !mCamera.isOpened()) {
            Log.d(TAG, "Camera not ready. Deferring picture capture.");
            pendingAction = 0;
            return;
        }

        try {
            if (!customDialogClass.isShowing()) {
                customDialogClass.show();
            }

            mCamera.setMode(Mode.PICTURE);
            makeCameraSound();
            mHandler.postDelayed(() -> {
                if (customDialogClass != null && customDialogClass.isShowing()) {
                    dismissDialogSafely(); // failsafe auto-dismiss
                    Log.e(TAG, "Camera response timed out. Dialog dismissed automatically.");
                }
            }, 5000); // auto-failsafe to avoid infinite loader

            Log.d(TAG, "Before takePicture()");
            mCamera.takePicture();
            Log.d(TAG, "After takePicture()");


        } catch (Exception e) {
            Log.e(TAG, "takePicture failed: ", e);
            dismissDialogSafely();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityActive = true;
    }

    @Override
    protected void onStop() {
        recording = false;
        currentSwitchRecording = false;
        isDeepArReady = false; // stop feeding frames

        try {
            if (cameraProviderRef != null) {
                cameraProviderRef.unbindAll();      // no blocking call
                cameraProviderRef = null;           // let it be GC'd
            }
        } catch (Exception e) {
            Log.w(TAG, "unbindAll failed", e);
        }

        if (surfaceProvider != null) {
            surfaceProvider.stop();
            surfaceProvider = null;
        }

        super.onStop();
        isActivityActive = false;

        mHandler.removeCallbacksAndMessages(null);

        if (customDialogClass != null && customDialogClass.isShowing()) {
            try {
                customDialogClass.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Failed to dismiss dialog onStop", e);
            }
        }

        if (arMode) {
            arMode = false;
            tearDownDeepAr();
        }
    }

    private void choosePhoto() {
        try {
            pickImageLauncher.launch(
                    new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build()
            );

        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_opening_gallery), Toast.LENGTH_SHORT).show();
        }
    }

    @AfterPermissionGranted(SharedConstants.REQUEST_CODE_PERMISSIONS_UPLOAD)
    private void chooseVideoForUpload() {
        IntentUtil.startChooser(this, SharedConstants.REQUEST_CODE_PICK_VIDEO, "video/mp4");
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, @Nullable Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            f.onActivityResult(requestCode, resultCode, data);
        }
        // UCrop crop
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            handleCropResult(data);
        } else if (requestCode == UCrop.RESULT_ERROR) {
            handleCropError(data);
        }
        // Song pick
        else if (
                requestCode == SharedConstants.REQUEST_CODE_PICK_SONG
                        && resultCode == RESULT_OK && data != null
        ) {
            SongRoot.SongItem song =
                    data.getParcelableExtra(EXTRA_SONG);
            Uri audioUri =
                    data.getParcelableExtra(EXTRA_AUDIO);
            setupSong(song, audioUri);
        }
        // Sticker pick
        else if (
                requestCode == SharedConstants.REQUEST_CODE_PICK_STICKER
                        && resultCode == RESULT_OK
                        && data != null
        ) {
            StickerRoot.StickerItem sticker =
                    data.getParcelableExtra(StickerPickerActivity.EXTRA_STICKER);
            downloadSticker(sticker);
        }
    }

    private void handleCropResult(@NonNull Intent result) {
        Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {
            selectedImage = resultUri;
            picturePath = getRealPathFromURI(selectedImage);
            Log.d(TAG, "handleCropResult:---------------- " + picturePath);
            startActivity(new Intent(this, UploadPostActivity.class)
                    .putExtra(Const.GALLERY_PHOTO_PATH, picturePath)
                    .putExtra(Const.IS_CAPTURED, false));
            finish();
        } else {
            Toast.makeText(this, getString(R.string.toast_cannot_retrieve_cropped_image), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCropError(@NonNull Intent result) {
        Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, getString(R.string.toast_unexpected_error), Toast.LENGTH_SHORT).show();
    }

    private void submitUploadReels(Uri data) {
        File copy = TempUtil.createCopy(this, data, ".mp4");
        proceedToFilter(copy);
    }

    private void setupSong(@Nullable SongRoot.SongItem songDummy, Uri file) {
        mMediaPlayer = MediaPlayer.create(this, file);
        mMediaPlayer.setOnCompletionListener(mp -> mMediaPlayer = null);
        if (songDummy != null) {
            binding.sound.setText(songDummy.getTitle());
            mModel.song = songDummy.getId();
        } else {
            binding.sound.setText(getString(R.string.audio_from_clip));
        }
        mModel.audio = file;
    }

    private void downloadSticker(StickerRoot.StickerItem stickerDummy) {
        File stickers = new File(getFilesDir(), "stickers");
        if (!stickers.exists() && !stickers.mkdirs()) {
            Log.w(TAG, "Could not create directory at " + stickers);
        }

        String extension = stickerDummy.getSticker().substring(stickerDummy.getSticker().lastIndexOf(".") + 1);
        File image = new File(stickers, stickerDummy.getId() + extension);
        if (image.exists()) {
            addSticker(image);
            return;
        }

        CustomDialogClass progress = new CustomDialogClass(this, R.style.customStyle);
        progress.setCancelable(false);
        progress.show();
        Data input = new Data.Builder()
                .putString(FileDownloadWorker.KEY_INPUT, stickerDummy.getSticker())
                .putString(FileDownloadWorker.KEY_OUTPUT, image.getAbsolutePath())
                .build();
        WorkRequest request = new OneTimeWorkRequest.Builder(FileDownloadWorker.class)
                .setInputData(input)
                .build();
        WorkManager wm = WorkManager.getInstance(this);
        wm.enqueue(request);
        wm.getWorkInfoByIdLiveData(request.getId()).observe(this, info -> {
            boolean ended = info.getState() == WorkInfo.State.CANCELLED
                    || info.getState() == WorkInfo.State.FAILED
                    || info.getState() == WorkInfo.State.SUCCEEDED;
            if (ended) {
                progress.dismiss();
            }
            if (info.getState() == WorkInfo.State.SUCCEEDED) {
                addSticker(image);
            }
        });
    }

    private void addSticker(File file) {
        binding.remove.setVisibility(View.VISIBLE);
    }

    private void startRecording() {
        long recorded = mModel.recorded();
        if (recorded >= SharedConstants.MAX_DURATION) {
            Toast.makeText(this, getString(R.string.recorder_error_maxed_out), Toast.LENGTH_SHORT).show();
        } else {
            mModel.video = TempUtil.createNewFile(this, ".mp4");
            mCamera.takeVideoSnapshot(mModel.video, (int) (SharedConstants.MAX_DURATION - recorded));
        }
    }

    private void toggleVisibility(boolean show) {
        if (!getResources().getBoolean(R.bool.clutter_free_recording_enabled)) return;

        AnimationUtil.toggleVisibilityToTop(binding.top, show);
        AnimationUtil.toggleVisibilityToLeft(binding.right, show);
        AnimationUtil.toggleVisibilityToBottom(binding.upload, show);
        AnimationUtil.toggleVisibilityToBottom(binding.done, show);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) mCamera.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!arMode && mCamera != null && !mCamera.isOpened()) {
            mCamera.setMode(Mode.PICTURE);
            mCamera.open();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            try {
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
            } catch (Exception ignored) {
            }
            mMediaPlayer = null;
        }
        if (mModel != null && mModel.segments != null) {
            for (RecorderActivityViewModel.RecordSegment segment : mModel.segments) {
                try {
                    File file = new File(segment.file);
                    if (file.exists()) file.delete();
                } catch (Exception ignored) {
                }
            }
        }


        if (surfaceProvider != null) {
            surfaceProvider.stop();
        }
        if (deepAR == null) {
            return;
        }
        deepAR.setAREventListener(null);
        deepAR.release();
        deepAR = null;

        if (!arRecordingShouldNavigate && arOutputFile != null && arOutputFile.exists()) {
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == SharedConstants.REQUEST_CODE_PERMISSIONS_UPLOAD) {
            chooseVideoForUpload();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, getString(R.string.you_need_storage_permission), Toast.LENGTH_SHORT).show();
    }

    private boolean mergeVideos(List<String> paths, String outputPath) {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};

        TranscoderOptions.Builder transcoder = Transcoder.into(outputPath);
        for (String path : paths) {
            transcoder.addDataSource(VideoUtil.createDataSource(this, path));
        }

        transcoder.setListener(new TranscoderListener() {
                    @Override
                    public void onTranscodeCompleted(int code) {
                        success[0] = true;
                        latch.countDown();
                    }

                    @Override
                    public void onTranscodeFailed(@NonNull Throwable e) {
                        e.printStackTrace();
                        success[0] = false;
                        latch.countDown();
                    }

                    @Override
                    public void onTranscodeProgress(double v) {
                    }

                    @Override
                    public void onTranscodeCanceled() {
                        success[0] = false;
                        latch.countDown();
                    }
                })
                .transcode();

        try {
            latch.await(); // blocks until finished
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return success[0];
    }

    public void startMergingFlow(@Nullable Uri audioUri, List<String> segmentPaths) {
        customDialogClass.show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                File mergedVideo = new File(getCacheDir(), "merged_video_" + System.currentTimeMillis() + ".mp4");
                boolean videoMerged = mergeVideos(segmentPaths, mergedVideo.getAbsolutePath());

                if (!videoMerged) {
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(this, getString(R.string.video_merge_failed), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                if (audioUri != null) {
                    File audioFile = new File(audioUri.getPath());
                    if (!audioFile.exists()) {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(this, getString(R.string.audio_file_missing), Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    File aacFile = new File(getCacheDir(), "audio_" + System.currentTimeMillis() + ".m4a");
                    boolean audioConverted = transcodeMp3ToAacBlocking(audioFile, aacFile);

                    if (!audioConverted) {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(this, getString(R.string.audio_conversion_failed), Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    File finalMerged = new File(getCacheDir(), "final_output_" + System.currentTimeMillis() + ".mp4");
                    boolean merged = mergeAudioAndVideo(mergedVideo, aacFile, finalMerged);

                    if (merged) {
                        proceedToFilterScreen(finalMerged);
                    } else {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(this, getString(R.string.final_merge_failed), Toast.LENGTH_SHORT).show();
                        });
                    }

                } else {
                    proceedToFilterScreen(mergedVideo);
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(this, getString(R.string.unexpected_error) + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private boolean transcodeMp3ToAacBlocking(File mp3Input, File aacOutput) {
        MediaExtractor extractor = new MediaExtractor();
        MediaCodec decoder = null;
        MediaCodec encoder = null;
        MediaMuxer muxer = null;

        try {
            extractor.setDataSource(mp3Input.getAbsolutePath());

            int audioTrackIndex = -1;
            MediaFormat inputFormat = null;

            // Find audio track
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime != null && mime.startsWith("audio/")) {
                    audioTrackIndex = i;
                    inputFormat = format;
                    break;
                }
            }

            if (audioTrackIndex == -1 || inputFormat == null) {
                Log.e("Transcode", "No audio track found in input");
                return false;
            }

            extractor.selectTrack(audioTrackIndex);

            // Setup decoder
            String inputMime = inputFormat.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(inputMime);
            decoder.configure(inputFormat, null, null, 0);
            decoder.start();

            // Setup encoder
            MediaFormat outputFormat = MediaFormat.createAudioFormat(
                    MediaFormat.MIMETYPE_AUDIO_AAC,
                    inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                    inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
            outputFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128000);
            outputFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);

            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            encoder.start();

            muxer = new MediaMuxer(aacOutput.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            MediaCodec.BufferInfo decodeInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo encodeInfo = new MediaCodec.BufferInfo();

            boolean decoderDone = false;
            boolean encoderDone = false;
            boolean muxerStarted = false;
            int muxerTrackIndex = -1;

            while (!encoderDone) {
                // Feed decoder
                if (!decoderDone) {
                    int inputBufferIndex = decoder.dequeueInputBuffer(10000);
                    if (inputBufferIndex >= 0) {
                        ByteBuffer inputBuffer = decoder.getInputBuffer(inputBufferIndex);
                        int sampleSize = extractor.readSampleData(inputBuffer, 0);

                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            decoderDone = true;
                        } else {
                            long presentationTimeUs = extractor.getSampleTime();
                            decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTimeUs, 0);
                            extractor.advance();
                        }
                    }
                }

                // Drain decoder, feed encoder
                int outputBufferIndex = decoder.dequeueOutputBuffer(decodeInfo, 10000);
                if (outputBufferIndex >= 0) {
                    ByteBuffer decodedBuffer = decoder.getOutputBuffer(outputBufferIndex);

                    if ((decodeInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        decoder.releaseOutputBuffer(outputBufferIndex, false);
                        continue;
                    }

                    if (decodeInfo.size != 0 && decodedBuffer != null) {
                        decodedBuffer.position(decodeInfo.offset);
                        decodedBuffer.limit(decodeInfo.offset + decodeInfo.size);

                        int inputBufferIndex = encoder.dequeueInputBuffer(10000);
                        if (inputBufferIndex >= 0) {
                            ByteBuffer encoderInput = encoder.getInputBuffer(inputBufferIndex);
                            encoderInput.clear();
                            encoderInput.put(decodedBuffer);
                            encoder.queueInputBuffer(inputBufferIndex, 0, decodeInfo.size,
                                    decodeInfo.presentationTimeUs, decodeInfo.flags);
                        }
                    }

                    decoder.releaseOutputBuffer(outputBufferIndex, false);

                    if ((decodeInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        boolean eosQueued = false;
                        while (!eosQueued) {
                            int eosInput = encoder.dequeueInputBuffer(10000);
                            if (eosInput >= 0) {
                                encoder.queueInputBuffer(eosInput, 0, 0, decodeInfo.presentationTimeUs,
                                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                eosQueued = true;
                            }
                        }
                    }
                }

                // Drain encoder
                while (true) {
                    int outputIndex = encoder.dequeueOutputBuffer(encodeInfo, 10000);
                    if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) break;
                    if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (muxerStarted) throw new RuntimeException("Format changed twice.");
                        MediaFormat newFormat = encoder.getOutputFormat();
                        muxerTrackIndex = muxer.addTrack(newFormat);
                        muxer.start();
                        muxerStarted = true;
                    } else if (outputIndex >= 0) {
                        ByteBuffer encodedData = encoder.getOutputBuffer(outputIndex);
                        if (encodeInfo.size > 0 && muxerStarted) {
                            encodedData.position(encodeInfo.offset);
                            encodedData.limit(encodeInfo.offset + encodeInfo.size);
                            muxer.writeSampleData(muxerTrackIndex, encodedData, encodeInfo);
                        }

                        encoder.releaseOutputBuffer(outputIndex, false);

                        if ((encodeInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            encoderDone = true;
                            break;
                        }
                    }
                }
            }

            muxer.stop();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                muxer.release();
            } catch (Exception ignored) {
            }
            try {
                decoder.stop();
                decoder.release();
            } catch (Exception ignored) {
            }
            try {
                encoder.stop();
                encoder.release();
            } catch (Exception ignored) {
            }
            try {
                extractor.release();
            } catch (Exception ignored) {
            }
        }
    }

    private boolean mergeAudioAndVideo(File video, File audio, File output) {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};

        Transcoder.into(output.getAbsolutePath())
                .addDataSource(TrackType.VIDEO, video.getAbsolutePath())
                .addDataSource(TrackType.AUDIO, audio.getAbsolutePath())
                .setListener(new TranscoderListener() {
                    @Override
                    public void onTranscodeCompleted(int code) {
                        success[0] = true;
                        latch.countDown();
                    }

                    @Override
                    public void onTranscodeFailed(@NonNull Throwable e) {
                        e.printStackTrace();
                        success[0] = false;
                        latch.countDown();
                    }

                    @Override
                    public void onTranscodeProgress(double v) {
                    }

                    @Override
                    public void onTranscodeCanceled() {
                        success[0] = false;
                        latch.countDown();
                    }
                })
                .transcode();

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return success[0];
    }

    private void proceedToFilterScreen(File finalFile) {
        runOnUiThread(() -> {
            customDialogClass.dismiss();
            Intent intent = new Intent(this, FilterActivity.class);
            intent.putExtra(FilterActivity.EXTRA_VIDEO, finalFile.getAbsolutePath());
            startActivity(intent);
            finish();
        });
    }

    private void proceedToFilter(File video) {
        Intent intent = new Intent(this, FilterActivity.class);
        intent.putExtra(FilterActivity.EXTRA_SONG, mModel.song);
        intent.putExtra(FilterActivity.EXTRA_VIDEO, video.getAbsolutePath());
        startActivity(intent);
        finish();
    }

    private boolean isActivityValid() {
        return isActivityActive && !isFinishing() && !isDestroyed()
                && getWindow() != null && getWindow().getDecorView().getWindowToken() != null;
    }

    private void dismissDialogSafely() {
        if (customDialogClass != null && customDialogClass.isShowing() && isActivityValid()) {
            try {
                customDialogClass.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Dialog dismiss failed: ", e);
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (deepAR != null) {
            deepAR.setRenderSurface(holder.getSurface(), holder.getSurfaceFrame().width(), holder.getSurfaceFrame().height());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // If we are using on screen rendering we have to set surface view where DeepAR will render
        if (deepAR != null) {
            deepAR.setRenderSurface(holder.getSurface(), width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (deepAR != null) {
            deepAR.setRenderSurface(null, 0, 0);
        }
    }

    @Override
    public void screenshotTaken(Bitmap bitmap) {
        runOnUiThread(() -> {
            try {
                if (bitmap != null) {
                    Bitmap square = BitmapUtil.getSquareThumbnail(bitmap, 250);
                    Bitmap rounded = BitmapUtil.addRoundCorners(square, 10);
                    square.recycle();
                    FilterAdapter adapter = new FilterAdapter(GotoLiveActivity.this, rounded);
                    adapter.setListener(GotoLiveActivity.this::applyPreviewFilter);
                    binding.rvFilters.setAdapter(adapter);
                    binding.rvFilters.setVisibility(View.VISIBLE);
                    binding.topFiltersLayout.setVisibility(View.VISIBLE);
                }
            } finally {
                dismissDialogSafely();
            }
        });
    }

    @Override
    public void videoRecordingStarted() {
        runOnUiThread(() -> {
            // match CameraView onVideoRecordingStart()
            binding.segments.resume();
            configureMediaPlayerSpeed();
            if (mMediaPlayer != null) mMediaPlayer.start();

            if (mPulse != null) mPulse.stop();
            mPulse = YoYo.with(Techniques.Pulse).repeat(YoYo.INFINITE).playOn(binding.record);

            binding.record.setSelected(true);
            toggleVisibility(false);
        });
    }

    @Override
    public void videoRecordingFinished() {
        runOnUiThread(() -> {
            // match CameraView onVideoRecordingEnd()
            binding.segments.pause();
            binding.segments.addDivider();

            if (mMediaPlayer != null) mMediaPlayer.pause();
            if (mPulse != null) mPulse.stop();

            binding.record.setSelected(false);
            toggleVisibility(true);

            // existing navigation
            if (arRecordingShouldNavigate && arOutputFile != null && arOutputFile.exists()) {
                arRecordingShouldNavigate = false;
                Intent intent = new Intent(this, FilterActivity.class);
                intent.putExtra(FilterActivity.EXTRA_VIDEO, arOutputFile.getAbsolutePath());
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void videoRecordingFailed() {
        runOnUiThread(() -> {
            // make sure UI resets on failure too
            binding.segments.pause();
            if (mPulse != null) mPulse.stop();
            binding.record.setSelected(false);
            toggleVisibility(true);
            Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void videoRecordingPrepared() {

    }

    @Override
    public void shutdownFinished() {

    }

    @Override
    public void initialized() {
        isDeepArReady = true;

        setupCamera();

        String initialFile = getEffectFileFromName(effectNames.get(currentEffect));
        deepAR.switchEffect("effect", getFilterPath(initialFile));
    }

    private String getEffectFileFromName(String name) {
        if (name == null) return "none";

        switch (name) {
            case "Bright Glasses":
                return "effect_bright-glasses.deepar";

            case "Neon Devil Horns":
                return "effect_neon_devil_horns.deepar";

            case "Makeup Kim":
                return "effect_makeup-kim.deepar";

            case "Burning Effect":
                return "effect_burning_effect.deepar";

            case "Spring Fairy":
                return "effect_spring-fairy.deepar";

            case "Bunny Ears":
                return "effect_bunny_ears.deepar";

            case "Butterfly band":
                return "effect_butterfly_headband.deepar";

            case "Cracked Face":
                return "effect_cracked_porcelain_face.deepar";

            case "Face Swap":
                return "effect_face_swap.deepar";

            case "Sequin Butterfly":
                return "effect_sequin_butterfly.deepar";

            case "Small Flowers":
                return "effect_small_flowers.deepar";

            case "Spring Deer":
                return "effect_spring_deer.deepar";

            case "None":
            default:
                return "none";
        }
    }

    @Override
    public void faceVisibilityChanged(boolean b) {

    }

    @Override
    public void imageVisibilityChanged(String s, boolean b) {

    }

    @Override
    public void frameAvailable(Image image) {
        // This is called from a background thread.
        if (!arMode) return;

        if (arAwaitingFirstFrame && !arVisible) {
            arAwaitingFirstFrame = false;
            runOnUiThread(() -> {
                // Crossfade AR in now that it has content
                View ar = binding.layoutDeepAr;
                if (ar.getVisibility() != View.VISIBLE) {
                    ar.setVisibility(View.VISIBLE);
                }
                ar.animate().alpha(1f).setDuration(150).withEndAction(() -> {
                    arVisible = true;
                    // Now that AR is visible and drawing, we can close CameraView to save resources
                    if (mCamera != null && mCamera.isOpened()) {
                        try {
                            mCamera.close();
                        } catch (Exception ignored) {
                        }
                    }
                }).start();
            });
        }
    }

    @Override
    public void error(ARErrorType arErrorType, String s) {

    }

    @Override
    public void effectSwitched(String s) {

    }

    private void fadeOutEffects() {
        final View effects = binding.lytEffects;
        final ViewGroup parent = (ViewGroup) effects.getParent();

        binding.filter.setVisibility(View.VISIBLE);
        binding.speed.setVisibility(View.VISIBLE);

        // Cancel any running anims
        effects.animate().cancel();

        // Avoid overdraw artifacts over SurfaceView
        if (Build.VERSION.SDK_INT >= 29 && parent != null) parent.suppressLayout(true);
        effects.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Slide down instead of fading alpha
        float offscreen = effects.getHeight() == 0 ? effects.getMeasuredHeight() : effects.getHeight();
        if (offscreen == 0) {
            // in case height isn't laid out yet, fallback to 200dp
            offscreen = getResources().getDisplayMetrics().density * 200f;
        }

        effects.animate()
                .translationY(offscreen)
                .setDuration(160)
                .withEndAction(() -> {
                    effects.setLayerType(View.LAYER_TYPE_NONE, null);
                    effects.setTranslationY(0f);
                    effects.setVisibility(View.INVISIBLE); // keep layout to avoid relayout jank
                    effects.setClickable(false);
                    effects.setFocusable(false);
                    if (Build.VERSION.SDK_INT >= 29 && parent != null) parent.suppressLayout(false);
                })
                .start();
    }

    private void fadeInEffects() {
        final View effects = binding.lytEffects;

        binding.filter.setVisibility(View.GONE);
        binding.speed.setVisibility(View.GONE);

        effects.animate().cancel();
        effects.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Prepare off-screen start position
        float startY = effects.getHeight() == 0 ? effects.getMeasuredHeight() : effects.getHeight();
        if (startY == 0) {
            startY = getResources().getDisplayMetrics().density * 200f;
        }

        effects.setVisibility(View.VISIBLE);
        effects.setClickable(true);
        effects.setFocusable(true);
        effects.setAlpha(1f);                  // keep fully opaque (no alpha blending over SurfaceView)
        effects.setTranslationY(startY);

        // Hide other filter lists while AR effects are open
        binding.rvFilters.setVisibility(View.GONE);
        binding.topFiltersLayout.setVisibility(View.GONE);

        effects.animate()
                .translationY(0f)
                .setDuration(180)
                .withEndAction(() -> effects.setLayerType(View.LAYER_TYPE_NONE, null))
                .start();
    }

    private void ensureFocusedVisible(@NonNull NestedScrollView scroll) {
        View focused = getCurrentFocus();
        if (focused == null) return;

        // Only if the focused view is inside this scroll container
        View content = scroll.getChildCount() > 0 ? scroll.getChildAt(0) : null;
        if (content == null) return;

        Rect r = new Rect();
        focused.getDrawingRect(r);
        scroll.offsetDescendantRectToMyCoords(focused, r);
        // This scrolls JUST ENOUGH to make r visible
        scroll.requestChildRectangleOnScreen(content, r, true);
    }

}
