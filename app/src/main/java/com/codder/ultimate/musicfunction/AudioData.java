package com.codder.ultimate.musicfunction;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class AudioData {

    public static String getAudioName(String filePath) {
        // Logic to fetch the audio name from filePath or database
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    public static String getAudioDuration(Context context,String filePath) {
        String[] projection = {MediaStore.Audio.Media.DURATION};
        Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        try (Cursor cursor = context.getContentResolver().query(
                contentUri,
                projection,
                MediaStore.Audio.Media.DATA + "=?",
                new String[]{filePath},
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                return formatDuration(duration);
            }
        }
        return "00:00";
    }

    private static String formatDuration(long durationMillis) {
        long minutes = (durationMillis / 1000) / 60;
        long seconds = (durationMillis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static String getAlbumArt(Context context, String filePath) {
        String albumId = getAlbumId(context, filePath);
        if (albumId != null) {
            String[] projection = {MediaStore.Audio.Albums.ALBUM_ART};
            Uri albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

            try (Cursor cursor = context.getContentResolver().query(
                    albumUri,
                    projection,
                    MediaStore.Audio.Albums._ID + "=?",
                    new String[]{albumId},
                    null
            )) {
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                }
            }
        }
        return null; // Return null if no album art is found
    }

    private static String getAlbumId(Context context, String filePath) {
        String[] projection = {MediaStore.Audio.Media.ALBUM_ID};
        Uri mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        try (Cursor cursor = context.getContentResolver().query(
                mediaUri,
                projection,
                MediaStore.Audio.Media.DATA + "=?",
                new String[]{filePath},
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            }
        }
        return null;
    }

}
