package com.kabadiwala.connect.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "materials")
public class Material {
    @PrimaryKey
    @NonNull
    public String materialId;
    public String name;
    public String category;
    
    public Material() {}
    
    @Ignore
    public Material(String materialId, String name, String category) {
        this.materialId = materialId;
        this.name = name;
        this.category = category;
    }
}
