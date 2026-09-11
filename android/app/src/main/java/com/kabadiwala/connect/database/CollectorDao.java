package com.kabadiwala.connect.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.kabadiwala.connect.entities.Collector;
import java.util.List;

@Dao
public interface CollectorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(@NonNull Collector collector);
    
    @Query("SELECT * FROM collectors")
    @NonNull
    List<Collector> getAll();
    
    @Query("SELECT * FROM collectors WHERE collectorId = :collectorId")
    Collector getById(@NonNull String collectorId);
}
