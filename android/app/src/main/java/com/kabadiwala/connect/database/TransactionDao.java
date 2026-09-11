package com.kabadiwala.connect.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.kabadiwala.connect.entities.Transaction;
import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    void insert(@NonNull Transaction transaction);
    
    @Query("SELECT * FROM transactions")
    @NonNull
    List<Transaction> getAll();
    
    @Query("SELECT * FROM transactions WHERE transactionId = :transactionId")
    Transaction getById(@NonNull String transactionId);
    
    @Query("SELECT * FROM transactions WHERE synced = 0")
    @NonNull
    List<Transaction> getUnsynced();
    
    @Update
    void update(@NonNull Transaction transaction);
}
