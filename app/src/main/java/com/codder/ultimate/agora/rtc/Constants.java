package com.codder.ultimate.agora.rtc;

import io.agora.rtc2.video.BeautyOptions;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class Constants {
    public static final int DEFAULT_PROFILE_IDX = 2;
    public static final String PREF_RESOLUTION_IDX = "pref_profile_index";
    public static final String PREF_ENABLE_STATS = "pref_enable_stats";
    public static final String PREF_MIRROR_LOCAL = "pref_mirror_local";
    public static final String PREF_MIRROR_REMOTE = "pref_mirror_remote";
    public static final String PREF_MIRROR_ENCODE = "pref_mirror_encode";

    public static VideoEncoderConfiguration.VideoDimensions[] VIDEO_DIMENSIONS = new VideoEncoderConfiguration.VideoDimensions[]{
            VideoEncoderConfiguration.VD_320x240,
            VideoEncoderConfiguration.VD_480x360,
            VideoEncoderConfiguration.VD_640x360,
            VideoEncoderConfiguration.VD_640x480,
            new VideoEncoderConfiguration.VideoDimensions(960, 540),
            VideoEncoderConfiguration.VD_1280x720
    };
    public static int[] VIDEO_MIRROR_MODES = new int[]{
            io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_AUTO,
            io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_ENABLED,
            io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_DISABLED,
    };
}