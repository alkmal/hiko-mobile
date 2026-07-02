package com.codder.ultimate.live.model;

import android.net.Uri;

import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecorderActivityViewModel extends ViewModel {

    public Uri audio;
    public List<RecordSegment> segments = new ArrayList<>();
    public String song = "";
    public float speed = 1;
    public File video;

    public long recorded() {
        long recorded = 0;
        for (RecordSegment segment : segments) {
            recorded += segment.duration;
        }

        return recorded;
    }

    public static class RecordSegment {

        public String file;
        public long duration;
    }
}
