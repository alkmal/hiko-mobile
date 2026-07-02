package com.codder.ultimate.musicfunction;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import io.agora.rtc2.IAudioEffectManager;
import io.agora.rtc2.RtcEngine;

public class AudioMixingController {

    private static final String TAG = "AudioMixingController";
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    boolean isPlaying = false;
    private int lastPausedPosition = 0;

    AudioMixingListner audioMixingListner;
    private Context context;
    private IAudioEffectManager audioEffectManager;

    private boolean isSeekedToEnd = false;

    private RtcEngine rtcEngine;
    private String currentSongPath;
    private ArrayList<AudioDetails> selectedSongs = new ArrayList<>();
    private int currentIndex = 0;
    private Runnable progressRunnable;
    private boolean isTrackingProgress = false;
    private int lastPlayingIndex = -1;
    private int progress;


    public AudioMixingController() {

    }

    public static AudioMixingController getInstance() {
        return Holder.INSTANCE;
    }

    public long getTotalDuration() {
        if (rtcEngine != null) {
            return rtcEngine.getAudioMixingDuration(); // Returns duration in milliseconds
        }
        return 0;
    }

    public void startTrackingProgress() {
        if (isTrackingProgress) return;

        isTrackingProgress = true;
        Log.d(TAG, "startTrackingProgress: ");
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (rtcEngine != null && isPlaying) {
                    progress = rtcEngine.getAudioMixingCurrentPosition();

                    long totalDuration = getTotalDuration(); // Get total duration
                    Log.d(TAG, "run: totalDuration: " + totalDuration + " ms, progress: " + progress + " ms");
                    Log.d(TAG, "Progress: " + progress + " / " + totalDuration + " (index: " + currentIndex + ")");


                    // Notify the listener with progress and total duration
                    if (audioMixingListner != null) {
                        audioMixingListner.currentProgress(progress, totalDuration);
                    }
//                    long totalDuration = rtcEngine.getAudioMixingDuration();
//                    Log.d(TAG, "run: totalDuration: " + totalDuration + " ms, progress: " + progress + " ms");
//                    int progressSeconds = (int) (progress / 1000);
//                    int totalSeconds = (int) (totalDuration / 1000);
//                    int progressPercentage = (int) ((progress * 100) / totalDuration);
//
//                    // Notify the listener
//                    audioMixingListner.currentProgress(progress);
//                    Log.d(TAG, "run: progress: " + progressSeconds + "/" + totalSeconds + ", progressPercentage: " + progressPercentage + "%");
                    // Stop tracking when the audio ends
                    if (totalDuration > 0 && (progress >= totalDuration - 1000)) {
                        Log.d(TAG, "Song finished. currentIndex before increment: " + currentIndex);
                        stopTrackingProgress();

                        if (audioMixingListner != null) {
                            audioMixingListner.currentProgress(0, totalDuration);
                        }

                        if (currentIndex < selectedSongs.size() - 1) {
                            currentIndex++;
                            Log.d(TAG, "Autoplaying song at index: " + currentIndex);
                            delayedPlayNextTrack();
                        } else {
                            isPlaying = false;
                            isSeekedToEnd = false;
                            if (audioMixingListner != null) {
                                audioMixingListner.isPlaying(false);
                            }
                            Toast.makeText(context, "No more songs to play", Toast.LENGTH_SHORT).show(); // ✅ Show Toast
                            Log.d(TAG, "Playback finished. Last song played.");
                        }

                    }

                    else {
                        // Schedule the next update
                        progressHandler.postDelayed(this, 1000);
                    }
                }
            }
        };

        progressHandler.postDelayed(progressRunnable, 1000);
    }

    private void delayedPlayNextTrack() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (currentIndex < selectedSongs.size()) {
                play();
                if (audioMixingListner != null) {
                    audioMixingListner.isPlaying(isPlaying);
                }
            } else {
                Log.d(TAG, "delayedPlayNextTrack: Index out of bounds. Not playing.");
            }
        }, 300);
    }



    public void stopTrackingProgress() {
        isTrackingProgress = false;
        progressHandler.removeCallbacks(progressRunnable);
    }

    public AudioMixingListner getAudioMixingListner() {
        return audioMixingListner;
    }

    public void setAudioMixingListner(AudioMixingListner audioMixingListner) {
        this.audioMixingListner = audioMixingListner;
    }

    public void init(Context context, RtcEngine mRtcEngine) {

        if (mRtcEngine == null) {
            Log.e("AudioMixingController", "init: rtcEngine is null, skipping init");
            return; // ← safe exit
        }
        this.context = context;
        this.rtcEngine = mRtcEngine;
        audioEffectManager = mRtcEngine.getAudioEffectManager();
    }

    public void stop() {
        if (rtcEngine != null && isPlaying) {
            rtcEngine.stopAudioMixing();
            isPlaying = false;
            stopTrackingProgress();
            long totalDuration = getTotalDuration();
            if (audioMixingListner != null) {
                audioMixingListner.isPlaying(false);
                audioMixingListner.currentProgress(0, totalDuration);
            }
            Log.d(TAG, "stop: Audio playback stopped");
            lastPausedPosition = 0;
        }
    }

    public void play() {

        if (lastPlayingIndex == currentIndex && isPlaying && !isSeekedToEnd) {
            Log.d(TAG, "play: Already playing current index, ignoring play call");
            return;
        }


        if (currentIndex < 0 || currentIndex >= selectedSongs.size()) {
            Log.e(TAG, "Invalid currentIndex: " + currentIndex);
            return;
        }


        Log.d(TAG, "Selected songs size: " + selectedSongs.size());

        if (selectedSongs.isEmpty()) {
            Toast.makeText(context, "Playlist is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // If the same song is being requested again, skip to the next or loop to start
        // Do not forcefully change index here; let the caller control it
        if (lastPlayingIndex == currentIndex && isPlaying && !isSeekedToEnd) {
            Log.d(TAG, "play: Already playing current index, ignoring play call");
            return;
        }


        rtcEngine.enableAudio();
        rtcEngine.stopAudioMixing();
        stopTrackingProgress();
        isPlaying = false;
        progress = 0;

        currentSongPath = selectedSongs.get(currentIndex).getSongPath();
        String currentSongTitle = selectedSongs.get(currentIndex).getName();

        if (audioMixingListner != null) {
            audioMixingListner.onAudioTitleUpdated(currentSongTitle);
            audioMixingListner.currentProgress(0, getTotalDuration());
        }

        int success = rtcEngine.startAudioMixing(currentSongPath, false, -1, 0);
        Log.d(TAG, "Playing song at index " + currentIndex + ": " + currentSongTitle + " (startAudioMixing success: " + success + ")");

        if (success == 0) {
            isPlaying = true;
            lastPlayingIndex = currentIndex;
            isSeekedToEnd = false;
            startTrackingProgress();
        } else {
            Toast.makeText(context, "Failed to start audio mixing", Toast.LENGTH_SHORT).show();
            isPlaying = false;
        }

        lastPausedPosition = 0;
    }



    public void seekTo(int positionMs) {
        if (rtcEngine != null && isPlaying) {
            long totalDuration = getTotalDuration();

            // Consider it a seek-to-end if within 1 second of total
            isSeekedToEnd = (totalDuration - positionMs) <= 1000;

            rtcEngine.setAudioMixingPosition(positionMs);
            progress = positionMs;

            Log.d(TAG, "seekTo: Moved to position " + positionMs + " ms (SeekedToEnd: " + isSeekedToEnd + ")");
        } else {
            Log.d(TAG, "seekTo: Cannot seek. Audio is not playing or RtcEngine is null");
        }


        if (isSeekedToEnd) {
            stopTrackingProgress();

            if (currentIndex < selectedSongs.size() - 1) {
                currentIndex++;
                isSeekedToEnd = false;  // ✅ Reset before play()
                play();
                if (audioMixingListner != null) {
                    audioMixingListner.isPlaying(isPlaying);
                }
            } else {
                // Last song, stop and show toast
                stop();
                Toast.makeText(context, "No more songs to play", Toast.LENGTH_SHORT).show();  // ✅ Add here too
            }
        }


    }



    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean isLoaded() {
        return selectedSongs != null && !selectedSongs.isEmpty();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void togglePlayPause() {
        if (isPlaying) {
            lastPausedPosition = progress; // ✅ Store current progress
            rtcEngine.pauseAudioMixing();
            isPlaying = false;
            stopTrackingProgress();
        } else {
            // ✅ Resume from where it left off
            int success = rtcEngine.resumeAudioMixing();
            if (success == 0) {
                isPlaying = true;
                startTrackingProgress();
            } else {
                // If resume fails (e.g., stopped completely), try full play + seek
                play();  // fallback
                if (lastPausedPosition > 0) {
                    seekTo(lastPausedPosition);
                }
            }
        }

        if (audioMixingListner != null) {
            audioMixingListner.isPlaying(isPlaying);
        }
    }


    public void next() {
        if (currentIndex >= selectedSongs.size() - 1) {
            Toast.makeText(context, "No Next Song", Toast.LENGTH_SHORT).show();
        } else {
            currentIndex++;
            play();

            if (audioMixingListner != null) {
                audioMixingListner.isPlaying(isPlaying);
            }
        }
    }


    public void previous() {
        if (currentIndex > 0) {
            currentIndex--;
            play();

            if (audioMixingListner != null) {
                audioMixingListner.isPlaying(isPlaying);
            }
        } else {
            Toast.makeText(context, "No Previous Song", Toast.LENGTH_SHORT).show();
        }
    }


    public void load(ArrayList<AudioDetails> newSongs) {
        Log.d(TAG, "load() called, currentIndex: " + currentIndex);

        // Only reload if the list is different
        if (selectedSongs.equals(newSongs)) {
            Log.d(TAG, "load() skipped: Same playlist already loaded");
            return;
        }

        // Save index only if the same songs are passed
        int oldIndex = currentIndex;

        this.selectedSongs.clear();
        this.selectedSongs.addAll(newSongs);

        // Only reset index if old index is invalid
        if (oldIndex >= 0 && oldIndex < this.selectedSongs.size()) {
            currentIndex = oldIndex;
        } else {
            currentIndex = 0;
        }

        currentSongPath = this.selectedSongs.get(currentIndex).getSongPath();
        String currentSongTitle = this.selectedSongs.get(currentIndex).getName();

        if (audioMixingListner != null) {
            audioMixingListner.onAudioTitleUpdated(currentSongTitle);
            audioMixingListner.currentProgress(0, getTotalDuration());
        }
    }



    public void setVolume(int volume) {
        rtcEngine.adjustAudioMixingVolume(volume);
    }

    public int getVolume() {
        return rtcEngine.getAudioMixingPlayoutVolume();
    }

    public interface AudioMixingListner {
        void isPlaying(boolean isPlaying);

        void currentProgress(long progress, long totalDuration);

        void onAudioTitleUpdated(String title);
    }

    private static final class Holder {
        private static final AudioMixingController INSTANCE = new AudioMixingController();
    }


}
