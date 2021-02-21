package com.Gonigon.bombi.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DbEntity2 {

    @PrimaryKey
    private int key;
    private Long time;

    public DbEntity2(int key) {
        this.key = key;
    }


    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }


    @Override
    public String toString() {
        return "DbEntity2{" +
                "key=" + key +
                ", time=" + time +
                '}';
    }
}
