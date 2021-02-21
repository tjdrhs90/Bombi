package com.Gonigon.bombi.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DbDao2 {

    @Query("SELECT * FROM DbEntity2")
    List<DbEntity2> getAll();

    @Insert
    void insert(DbEntity2 dbEntity);

    @Update
    void update(DbEntity2 dbEntity);

    @Delete
    void delete(DbEntity2 dbEntity);


//    @Query("SELECT * FROM DbEntity WHERE `key` = :key LIMIT 1")
//    DbEntity findValue(int key);
}
