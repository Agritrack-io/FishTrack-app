package io.agritrack.data.model.common;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "measurements")
public class Measurement {

    public Measurement() {
        this.id = UUID.randomUUID();
    }

    @PrimaryKey
    @NonNull
    public UUID id;

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

    @ColumnInfo(name = "fish_temp")
    public Double fishTemp;

    @ColumnInfo(name = "fish2_temp")
    public Double fish2Temp;

    @ColumnInfo(name = "water_temp")
    public Double waterTemp;
}
