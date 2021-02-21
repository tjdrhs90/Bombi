package com.Gonigon.bombi.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DbEntity2.class}, version = 1)
public abstract class AppDatabase2 extends RoomDatabase {
    public abstract DbDao2 dbDao2();


}