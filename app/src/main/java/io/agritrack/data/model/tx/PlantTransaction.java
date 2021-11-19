package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "plant_transaction")
public class PlantTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "site")
    public String site;

    @ColumnInfo(name = "user_id")
    public Long userId;

    @ColumnInfo(name = "plant_lot")
    public String plantLot;

    @ColumnInfo(name = "asset_rfid")
    public String assetRFID;

    @ColumnInfo(name = "logger_rfid")
    public String loggerRFID;

    @ColumnInfo(name = "species")
    public String species;
}
