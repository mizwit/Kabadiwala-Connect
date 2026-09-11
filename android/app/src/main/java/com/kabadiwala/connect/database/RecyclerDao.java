package com.kabadiwala.connect.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.kabadiwala.connect.entities.Recycler;
import java.util.List;

@Dao
public interface RecyclerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(@NonNull Recycler recycler);
    
    @Query("SELECT * FROM recyclers")
    @NonNull
    List<Recycler> getAll();
    
    @Query("SELECT * FROM recyclers WHERE recyclerId = :recyclerId")
    Recycler getById(@NonNull String recyclerId);
}
