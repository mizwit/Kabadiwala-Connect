package com.kabadiwala.connect.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Ignore;

@Entity(tableName = "transactions",
        foreignKeys = {
            @ForeignKey(entity = MaterialLot.class, parentColumns = "lotId", childColumns = "lotId", onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Recycler.class, parentColumns = "recyclerId", childColumns = "recyclerId", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("lotId"), @Index("recyclerId")})
public class Transaction {
    @PrimaryKey
    @NonNull
    public String transactionId;
    public String lotId;
    public String recyclerId;
    public double quotedPrice;
    public double finalPrice;
    public String transactionDatetime;
    public String status;
    public boolean synced;
    
    public Transaction() {}
    
    @Ignore
    public Transaction(String transactionId, String lotId, String recyclerId, double quotedPrice, double finalPrice) {
        this.transactionId = transactionId;
        this.lotId = lotId;
        this.recyclerId = recyclerId;
        this.quotedPrice = quotedPrice;
        this.finalPrice = finalPrice;
        this.transactionDatetime = java.time.Instant.now().toString();
        this.status = "pending";
        this.synced = false;
    }
}
