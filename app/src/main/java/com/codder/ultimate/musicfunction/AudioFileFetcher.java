package com.codder.ultimate.musicfunction;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioFileFetcher {

    public Map<String, List<String>> getAudioFilesByFolder(ContentResolver contentResolver) {
        Map<String, List<String>> audioFoldersMap = new HashMap<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";

        try (Cursor cursor = contentResolver.query(uri, projection, selection, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                do {
                    String filePath = cursor.getString(dataColumn);
                    File file = new File(filePath);
                    String folderPath = file.getParent();

                    if (folderPath != null) {
                        if (!audioFoldersMap.containsKey(folderPath)) {
                            audioFoldersMap.put(folderPath, new ArrayList<>());
                        }
                        audioFoldersMap.get(folderPath).add(filePath);

                    }

                } while (cursor.moveToNext());
            }
        }
        Log.d("TAG", "getAudioFilesByFolder: .........." + audioFoldersMap);
        return audioFoldersMap;
    }

}
