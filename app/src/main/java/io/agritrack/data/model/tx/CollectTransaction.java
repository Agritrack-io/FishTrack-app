package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "collect_transaction")
public class CollectTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "site")
    public String site;

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "plant_lot")
    public String plantLot;

    @ColumnInfo(name = "asset_rfid")
    public String assetRFID;

    @ColumnInfo(name = "logger_rfid")
    public String loggerRFID;

    @ColumnInfo(name = "species")
    public String species;

    @ColumnInfo(name = "collection_lot")
    public String collectionLot;

    @ColumnInfo(name = "number_totes")
    public Integer totesCnt;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;

    @ColumnInfo(name = "created_at")
    public Long createdAt;
}
