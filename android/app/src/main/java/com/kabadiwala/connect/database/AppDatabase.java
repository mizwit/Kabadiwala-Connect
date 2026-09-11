package com.kabadiwala.connect.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.kabadiwala.connect.entities.Collector;
import com.kabadiwala.connect.entities.Material;
import com.kabadiwala.connect.entities.Recycler;
import com.kabadiwala.connect.entities.MaterialLot;
import com.kabadiwala.connect.entities.Transaction;

@Database(entities = {Collector.class, Material.class, Recycler.class, MaterialLot.class, Transaction.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    @NonNull
    public abstract CollectorDao collectorDao();
    @NonNull
    public abstract MaterialDao materialDao();
    @NonNull
    public abstract RecyclerDao recyclerDao();
    @NonNull
    public abstract MaterialLotDao materialLotDao();
    @NonNull
    public abstract TransactionDao transactionDao();
}
