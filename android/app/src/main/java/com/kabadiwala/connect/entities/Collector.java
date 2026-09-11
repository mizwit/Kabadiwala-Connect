package com.kabadiwala.connect.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "collectors")
public class Collector {
    @PrimaryKey
    @NonNull
    public String collectorId;
    public String name;
    public String phone;
    public String preferredLanguage;
    public String operatingLocation;
    public String createdAt;
    
    public Collector() {}
    
    @Ignore
    public Collector(String collectorId, String name, String phone, String preferredLanguage, String operatingLocation) {
        this.collectorId = collectorId;
        this.name = name;
        this.phone = phone;
        this.preferredLanguage = preferredLanguage;
        this.operatingLocation = operatingLocation;
        this.createdAt = java.time.Instant.now().toString();
    }
}
