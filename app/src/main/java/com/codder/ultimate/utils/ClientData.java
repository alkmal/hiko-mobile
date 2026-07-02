package com.codder.ultimate.utils;

import androidx.room.Database;
import androidx.room.RoomDatabase;


@Database(entities = {Draft.class}, version = 3, exportSchema = false)
public abstract class ClientData extends RoomDatabase {

    public abstract DraftDao drafts();
}
