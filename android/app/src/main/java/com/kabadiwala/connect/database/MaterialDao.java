package com.kabadiwala.connect.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.kabadiwala.connect.entities.Material;
import java.util.List;

@Dao
public interface MaterialDao {
    @Insert
    void insert(@NonNull Material material);
    
    @Query("SELECT * FROM materials")
    @NonNull
    List<Material> getAll();
    
    @Query("SELECT * FROM materials WHERE materialId = :materialId")
    Material getById(@NonNull String materialId);
}
