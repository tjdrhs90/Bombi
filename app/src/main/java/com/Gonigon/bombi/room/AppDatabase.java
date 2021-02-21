package com.Gonigon.bombi.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DbEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DbDao dbDao();


}