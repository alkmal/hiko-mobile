package com.codder.ultimate.live.activity;

import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityVolumeBinding;
import com.codder.ultimate.utils.TempUtil;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.hmomeni.verticalslider.VerticalSlider;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VolumeActivity extends BaseActivity {

    private static final String TAG = "VolumeActivity";

    public static final String EXTRA_AUDIO = "audio";
    public static final String EXTRA_SONG = "song";
    public static final String EXTRA_VIDEO = "video";

    private ActivityVolumeBinding binding;
    private VolumeActivityViewModel mModel;

    private SimpleExoPlayer videoPlayer;
    private SimpleExoPlayer audioPlayer;

    private String videoPath, audioPath;
    private volatile boolean isProcessing = false;

    private final Handler volumeHandler = new Handler(Looper.getMainLooper());
    private final Runnable volumeUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (videoPlayer != null && audioPlayer != null) {
                long position = videoPlayer.getCurrentPosition();

                // Example: mute video and boost audio from 5s to 10s
                if (position >= 5000 && position <= 10000) {
                    videoPlayer.setVolume(0.0f); // Mute video completely
                    audioPlayer.setVolume(1.0f);  // Full music volume
                } else {
                    Float videoVol = mModel.video.getValue();
                    Float songVol = mModel.song.getValue();
                    videoPlayer.setVolume(videoVol != null ? videoVol : 1f);
                    audioPlayer.setVolume(songVol != null ? songVol : 1f);
                }

                // Repeat every 500ms

                Log.d(TAG, "CurrentPos: " + position + ", videoVol: " + videoPlayer.getVolume() + ", audioVol: " + audioPlayer.getVolume());

            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVolumeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mModel = new ViewModelProvider(this).get(VolumeActivityViewModel.class);

        Intent intent = getIntent();
        if (intent != null) {
            videoPath = intent.getStringExtra(EXTRA_VIDEO);
            audioPath = intent.getStringExtra(EXTRA_AUDIO);
            mModel.audio = (audioPath != null);
        }

        if (videoPath == null) {
            Log.e(TAG, "Video path missing");
            finish();
            return;
        }

        initializePlayers();
        setupUI();
        observeVolumeChanges();
    }

    private void initializePlayers() {
        DefaultDataSourceFactory factory = new DefaultDataSourceFactory(this, getString(R.string.app_name));

        videoPlayer = new SimpleExoPlayer.Builder(this).build();
        ProgressiveMediaSource videoSource = new ProgressiveMediaSource.Factory(factory)
                .createMediaSource(MediaItem.fromUri(android.net.Uri.fromFile(new File(videoPath))));
        videoPlayer.setMediaSource(videoSource);
        videoPlayer.prepare();
        videoPlayer.setPlayWhenReady(true);

        PlayerView playerView = binding.player;
        playerView.setPlayer(videoPlayer);
        videoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);

        videoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPositionDiscontinuity(int reason) {
                if (reason == 0) {
//                    Log.d(TAG, "Video restarted, stopping and restarting audio...");
                    if (audioPlayer != null) {
                        audioPlayer.setPlayWhenReady(false);
                        audioPlayer.seekTo(0);
                        audioPlayer.setPlayWhenReady(true);
                    }
                }
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.d(TAG, "onPlayerStateChanged - PlayWhenReady: " + playWhenReady + ", PlaybackState: " + playbackState);
            }
        });

        if (audioPath != null) {
            audioPlayer = new SimpleExoPlayer.Builder(this).build();
            ProgressiveMediaSource audioSource = new ProgressiveMediaSource.Factory(factory)
                    .createMediaSource(MediaItem.fromUri(android.net.Uri.fromFile(new File(audioPath))));
            audioPlayer.setMediaSource(audioSource);
            audioPlayer.prepare();
            audioPlayer.setPlayWhenReady(true);

            Float audioVol = mModel.song.getValue();
            audioPlayer.setVolume(audioVol != null ? audioVol : 1f);
        }

        Float videoVol = mModel.video.getValue();
        videoPlayer.setVolume(videoVol != null ? videoVol : 1f);
    }


    private void setupUI() {
        VerticalSlider videoSlider = binding.video;
        VerticalSlider audioSlider = binding.sound;

        videoSlider.setMax(100);
        audioSlider.setMax(100);

        videoSlider.setProgress(0);
        audioSlider.setProgress(100);

        mModel.video.setValue((float) 0);
        if (videoPlayer != null) {
            videoPlayer.setVolume(0);
        }

        videoSlider.setOnProgressChangeListener((progress, fromUser) -> {
            float vol = progress / 100f;
            mModel.video.setValue(vol);    // Update LiveData
            if (videoPlayer != null) {
                videoPlayer.setVolume(vol); // Update player volume immediately
            }
        });

        audioSlider.setOnProgressChangeListener((progress, fromUser) -> {
            float vol = progress / 100f;
            mModel.song.setValue(vol);
            if (audioPlayer != null) {
                audioPlayer.setVolume(vol);
            }
        });

        binding.imgDone.setOnClickListener(v -> {
            if (isProcessing) {
                showToast("Processing in progress, please wait...");
                return;
            }

            isProcessing = true;
            runOnUiThread(() -> binding.imgDone.setEnabled(false));
            showLoadingDialog();  // Show dialog immediately

            new Thread(() -> {
                try {
                    String mime = getAudioMimeType(audioPath);
                    Log.d(TAG, "Detected audio MIME type: " + mime);

                    if ("audio/mpeg".equals(mime)) {
                        Log.d(TAG, "Attempting to create AAC temp file");
                        File aacFile = TempUtil.createNewFile(VolumeActivity.this, ".m4a");
                        Log.d(TAG, "AAC temp file created: " + aacFile.getAbsolutePath());

                        transcodeMp3ToAacAsync(audioPath, aacFile.getAbsolutePath(),
                                () -> {
                                    Log.d(TAG, "Transcoding success callback triggered");
                                    runMerge(aacFile.getAbsolutePath());
                                    isProcessing = false;
                                    hideLoadingDialog();
                                },
                                () -> {
                                    Log.e(TAG, "Transcoding failed callback triggered");
                                    showToast("Audio conversion failed");
                                    runOnUiThread(() -> binding.imgDone.setEnabled(true));
                                    isProcessing = false;
                                    hideLoadingDialog();
                                });
                    } else {
                        Log.d(TAG, "Audio is not MP3, directly merging");
                        runMerge(audioPath);
                        isProcessing = false;
                        hideLoadingDialog();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception in audio processing thread", e);
                    showToast("Processing failed");
                    runOnUiThread(() -> binding.imgDone.setEnabled(true));
                    isProcessing = false;
                    hideLoadingDialog();
                }
            }).start();

        });

    }

    private void observeVolumeChanges() {
        mModel.video.observe(this, volume -> {
            if (videoPlayer != null && volume != null) {
                videoPlayer.setVolume(volume); // controls original video audio
            }
        });

        mModel.song.observe(this, volume -> {
            if (audioPlayer != null && volume != null) {
                audioPlayer.setVolume(volume); // controls added music
            }
        });
    }


    private String getAudioMimeType(String audioFilePath) throws IOException {
        if (audioFilePath == null) return null;
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(audioFilePath);
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith("audio/")) {
                extractor.release();
                return mime;
            }
        }
        extractor.release();
        return null;
    }

    @Override
    protected void onDestroy() {
        volumeHandler.removeCallbacks(volumeUpdateRunnable);
        if (videoPlayer != null) {
            videoPlayer.release();
            videoPlayer = null;
        }
        if (audioPlayer != null) {
            audioPlayer.release();
            audioPlayer = null;
        }
        super.onDestroy();
    }

    public static class VolumeActivityViewModel extends ViewModel {
        public boolean audio;
        public MutableLiveData<Float> song = new MutableLiveData<>(1f);
        public MutableLiveData<Float> video = new MutableLiveData<>(1f);
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show());
    }

    private void showLoadingDialog() {
        runOnUiThread(() -> {
            if (!customDialogClass.isShowing()) {
                customDialogClass.show();
            }
            // Mute both audio and video when loader is showing
            if (videoPlayer != null) {
                videoPlayer.setVolume(0f);
            }
            if (audioPlayer != null) {
                audioPlayer.setVolume(0f);
            }
        });
    }

    private void hideLoadingDialog() {
        runOnUiThread(() -> {
            if (customDialogClass != null && customDialogClass.isShowing()) {
                customDialogClass.dismiss();
            }

            // Restore the volume when loader is hidden
            Float videoVol = mModel.video.getValue();
            Float audioVol = mModel.song.getValue();
            if (videoPlayer != null) {
                videoPlayer.setVolume(videoVol != null ? videoVol : 1f);
            }
            if (audioPlayer != null) {
                audioPlayer.setVolume(audioVol != null ? audioVol : 1f);
            }
        });
    }

    private void transcodeMp3ToAacAsync(String mp3Path, String outputAacPath, Runnable onSuccess, Runnable onFailure) {
        new Thread(() -> {
            MediaExtractor extractor = new MediaExtractor();
            MediaCodec decoder = null;
            MediaCodec encoder = null;
            MediaMuxer muxer = null;

            try {
                extractor.setDataSource(mp3Path);

                // Select audio track
                int audioTrackIndex = -1;
                MediaFormat inputFormat = null;
                for (int i = 0; i < extractor.getTrackCount(); i++) {
                    MediaFormat format = extractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime != null && mime.startsWith("audio/")) {
                        audioTrackIndex = i;
                        inputFormat = format;
                        break;
                    }
                }
                if (audioTrackIndex == -1) throw new IOException("No audio track found");

                extractor.selectTrack(audioTrackIndex);

                // Configure decoder (MP3)
                String inputMime = inputFormat.getString(MediaFormat.KEY_MIME);
                decoder = MediaCodec.createDecoderByType(inputMime);
                decoder.configure(inputFormat, null, null, 0);
                decoder.start();

                // Configure encoder (AAC)
                MediaFormat outputFormat = MediaFormat.createAudioFormat(
                        MediaFormat.MIMETYPE_AUDIO_AAC,
                        inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                        inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                );
                outputFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
                outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128000);
                outputFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);

                encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
                encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                encoder.start();

                muxer = new MediaMuxer(outputAacPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

                MediaCodec.BufferInfo decodeBufferInfo = new MediaCodec.BufferInfo();
                MediaCodec.BufferInfo encodeBufferInfo = new MediaCodec.BufferInfo();

                boolean extractorDone = false;
                boolean decoderDone = false;
                boolean encoderDone = false;
                boolean muxerStarted = false;

                int muxerTrackIndex = -1;

                while (!encoderDone) {
                    // Feed decoder input
                    if (!extractorDone) {
                        int inputBufferIndex = decoder.dequeueInputBuffer(10000);
                        if (inputBufferIndex >= 0) {
                            ByteBuffer inputBuffer = decoder.getInputBuffer(inputBufferIndex);
                            int sampleSize = extractor.readSampleData(inputBuffer, 0);

                            if (sampleSize < 0) {
                                decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                extractorDone = true;
                            } else {
                                long presentationTimeUs = extractor.getSampleTime();
                                decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTimeUs, 0);
                                extractor.advance();
                            }
                        }
                    }

                    // Drain decoder and feed encoder
                    while (!decoderDone) {
                        int outputBufferIndex = decoder.dequeueOutputBuffer(decodeBufferInfo, 10000);
                        if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) break;
                        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) continue;

                        if (outputBufferIndex >= 0) {
                            ByteBuffer decodedData = decoder.getOutputBuffer(outputBufferIndex);

                            if ((decodeBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                decoder.releaseOutputBuffer(outputBufferIndex, false);
                                continue;
                            }

                            if (decodeBufferInfo.size != 0 && decodedData != null) {
                                decodedData.position(decodeBufferInfo.offset);
                                decodedData.limit(decodeBufferInfo.offset + decodeBufferInfo.size);

                                int inputBufferIndex = encoder.dequeueInputBuffer(10000);
                                if (inputBufferIndex >= 0) {
                                    ByteBuffer encoderInputBuffer = encoder.getInputBuffer(inputBufferIndex);
                                    encoderInputBuffer.clear();
                                    encoderInputBuffer.put(decodedData);
                                    encoder.queueInputBuffer(inputBufferIndex, 0, decodeBufferInfo.size,
                                            decodeBufferInfo.presentationTimeUs, decodeBufferInfo.flags);
                                    decoder.releaseOutputBuffer(outputBufferIndex, false);
                                } else {
                                    break; // Try again later
                                }
                            } else {
                                decoder.releaseOutputBuffer(outputBufferIndex, false);
                            }

                            // EOS from decoder → send to encoder
                            if ((decodeBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                decoderDone = true;
                                boolean eosQueued = false;
                                while (!eosQueued) {
                                    int inputBufferIndex = encoder.dequeueInputBuffer(10000);
                                    if (inputBufferIndex >= 0) {
                                        encoder.queueInputBuffer(inputBufferIndex, 0, 0,
                                                decodeBufferInfo.presentationTimeUs,
                                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                        eosQueued = true;
                                    }
                                }
                            }
                        }
                    }

                    // Drain encoder and write to muxer
                    while (true) {
                        int encoderStatus = encoder.dequeueOutputBuffer(encodeBufferInfo, 10000);
                        if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) break;
                        if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            if (muxerStarted) throw new RuntimeException("Format changed twice");
                            MediaFormat newFormat = encoder.getOutputFormat();
                            muxerTrackIndex = muxer.addTrack(newFormat);
                            muxer.start();
                            muxerStarted = true;
                        } else if (encoderStatus >= 0) {
                            ByteBuffer encodedData = encoder.getOutputBuffer(encoderStatus);
                            if (encodeBufferInfo.size != 0 && muxerStarted) {
                                encodedData.position(encodeBufferInfo.offset);
                                encodedData.limit(encodeBufferInfo.offset + encodeBufferInfo.size);
                                muxer.writeSampleData(muxerTrackIndex, encodedData, encodeBufferInfo);
                            }

                            encoder.releaseOutputBuffer(encoderStatus, false);

                            if ((encodeBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                encoderDone = true;
                                break;
                            }
                        }
                    }
                }

                // Clean up
                muxer.stop();
                muxer.release();
                decoder.stop();
                decoder.release();
                encoder.stop();
                encoder.release();
                extractor.release();

                runOnUiThread(onSuccess);

            } catch (Exception e) {
                if (muxer != null) try { muxer.release(); } catch (Exception ignored) {}
                if (decoder != null) try { decoder.stop(); decoder.release(); } catch (Exception ignored) {}
                if (encoder != null) try { encoder.stop(); encoder.release(); } catch (Exception ignored) {}
                extractor.release();

                runOnUiThread(onFailure);
            }
        }).start();
    }


    private void runMerge(String audioToUsePath) {
        try {
            File outputFile = TempUtil.createNewFile(this, ".mp4");
            Log.d(TAG, "Merging video and audio...");
            mergeVideoAndAudio(videoPath, audioToUsePath, outputFile.getAbsolutePath());
            Log.d(TAG, "Merge complete, output: " + outputFile.getAbsolutePath());


            runOnUiThread(() -> {
                showToast("Merge successful");
                binding.imgDone.setEnabled(true);
                if (getResources().getBoolean(R.bool.skip_pan_audio_screen)) {
                    if (outputFile.exists() && outputFile.length() > 0) {
                        Log.d(TAG, "Output file exists and is valid");
                        proceedToFilter(outputFile);
                    } else {
                        Log.e(TAG, "Output file missing or empty");
                        runOnUiThread(() -> {
                            showToast("Merge failed: Output file not created.");
                            binding.imgDone.setEnabled(true);
                        });
                    }

                } else {
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Merge failed", e);
            runOnUiThread(() -> {
                showToast("Merge failed: " + e.getMessage());
                binding.imgDone.setEnabled(true);
            });
        }
    }

    private void proceedToFilter(File file) {
        Intent intent = new Intent(this, FilterActivity.class);
        intent.putExtra(FilterActivity.EXTRA_SONG, getIntent().getStringExtra(EXTRA_SONG));
        intent.putExtra(FilterActivity.EXTRA_VIDEO, file.getAbsolutePath());
        startActivity(intent);
        finish();
    }


    private void mergeVideoAndAudio(String videoPath, String audioPath, String outputPath) throws IOException {
        MediaExtractor videoExtractor = new MediaExtractor();
        videoExtractor.setDataSource(videoPath);

        MediaExtractor audioExtractor = new MediaExtractor();
        audioExtractor.setDataSource(audioPath);

        MediaMuxer muxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        int videoTrackIndex = -1;
        int audioTrackIndex = -1;

        for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
            MediaFormat format = videoExtractor.getTrackFormat(i);
            if (format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                videoExtractor.selectTrack(i);
                videoTrackIndex = muxer.addTrack(format);
                break;
            }
        }

        for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
            MediaFormat format = audioExtractor.getTrackFormat(i);
            if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                audioExtractor.selectTrack(i);
                audioTrackIndex = muxer.addTrack(format);
                break;
            }
        }

        if (videoTrackIndex == -1) throw new IOException("No video track found");
        if (audioTrackIndex == -1) throw new IOException("No audio track found");

        muxer.start();

        copySamples(videoExtractor, muxer, videoTrackIndex);
        copySamples(audioExtractor, muxer, audioTrackIndex);

        muxer.stop();
        muxer.release();

        videoExtractor.release();
        audioExtractor.release();
    }

    private void copySamples(MediaExtractor extractor, MediaMuxer muxer, int trackIndex) {
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        while (true) {
            int sampleSize = extractor.readSampleData(buffer, 0);
            if (sampleSize < 0) break;

            info.offset = 0;
            info.size = sampleSize;
            info.presentationTimeUs = extractor.getSampleTime();
            info.flags = extractor.getSampleFlags();

            muxer.writeSampleData(trackIndex, buffer, info);
            extractor.advance();
        }
    }
}
