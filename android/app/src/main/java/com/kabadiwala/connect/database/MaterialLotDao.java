package com.kabadiwala.connect.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.kabadiwala.connect.entities.MaterialLot;
import java.util.List;

@Dao
public interface MaterialLotDao {
    @Insert
    void insert(@NonNull MaterialLot lot);
    
    @Query("SELECT * FROM material_lots")
    @NonNull
    List<MaterialLot> getAll();
    
    @Query("SELECT * FROM material_lots WHERE lotId = :lotId")
    MaterialLot getById(@NonNull String lotId);
    
    @Query("SELECT * FROM material_lots WHERE synced = 0")
    @NonNull
    List<MaterialLot> getUnsynced();
    
    @Update
    void update(@NonNull MaterialLot lot);
}
