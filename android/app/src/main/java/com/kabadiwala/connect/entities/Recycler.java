package com.kabadiwala.connect.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "recyclers")
public class Recycler {
    @PrimaryKey
    @NonNull
    public String recyclerId;
    public String name;
    public String location;
    public String contactInfo;
    public boolean authorized;
    
    public Recycler() {}
    
    @Ignore
    public Recycler(String recyclerId, String name, String location, String contactInfo, boolean authorized) {
        this.recyclerId = recyclerId;
        this.name = name;
        this.location = location;
        this.contactInfo = contactInfo;
        this.authorized = authorized;
    }
}
