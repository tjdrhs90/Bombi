package com.Gonigon.bombi.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DbDao {

    @Query("SELECT * FROM DbEntity")
    List<DbEntity> getAll();

    @Insert
    void insert(DbEntity dbEntity);

    @Update
    void update(DbEntity dbEntity);

    @Delete
    void delete(DbEntity dbEntity);


//    @Query("SELECT * FROM DbEntity WHERE `key` = :key LIMIT 1")
//    DbEntity findValue(int key);
}
