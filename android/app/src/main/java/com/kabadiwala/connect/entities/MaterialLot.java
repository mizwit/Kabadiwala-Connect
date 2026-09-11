package com.kabadiwala.connect.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Ignore;

@Entity(tableName = "material_lots",
        foreignKeys = @ForeignKey(entity = Collector.class,
                                   parentColumns = "collectorId",
                                   childColumns = "collectorId",
                                   onDelete = ForeignKey.CASCADE,
                                   onUpdate = ForeignKey.CASCADE),
        indices = {@Index("collectorId")})
public class MaterialLot {
    @PrimaryKey
    @NonNull
    public String lotId;
    public String collectorId;
    public String sourceType;
    public double totalWeight;
    public double estimatedValue;
    public String collectionDatetime;
    public String status;
    public boolean synced; // For offline sync tracking
    
    public MaterialLot() {}
    
    @Ignore
    public MaterialLot(String lotId, String collectorId, String sourceType, double totalWeight, double estimatedValue) {
        this.lotId = lotId;
        this.collectorId = collectorId;
        this.sourceType = sourceType;
        this.totalWeight = totalWeight;
        this.estimatedValue = estimatedValue;
        this.collectionDatetime = java.time.Instant.now().toString();
        this.status = "pending";
        this.synced = false;
    }
}
