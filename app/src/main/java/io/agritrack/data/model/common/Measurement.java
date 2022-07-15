package io.agritrack.data.model.common;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "measurements")
public class Measurement {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "logger_rfid")
    public String loggerRFID;

    @ColumnInfo(name = "asset_rfid")
    public String assetRFID;

    @ColumnInfo(name = "retrieved_at")
    public Long retrievedAt;

    @ColumnInfo(name = "production_lane")
    public String productionLane;

    @ColumnInfo(name = "lot")
    public String lot;
}
