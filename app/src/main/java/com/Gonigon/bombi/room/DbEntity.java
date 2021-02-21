package com.Gonigon.bombi.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DbEntity {

    @PrimaryKey
    private int key;
    private String id;
    private String pw;

    public DbEntity(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }


    @Override
    public String toString() {
        return "DbEntity{" +
                "key=" + key +
                ", id='" + id + '\'' +
                ", pw='" + pw + '\'' +
                '}';
    }
}
