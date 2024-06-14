package io.agritrack.philosofish.data.model.common;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "iot_logger")
public class IotLogger {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "rfid")
    public String rfid;

    @ColumnInfo(name = "model")
    public String model;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "vendor")
    public String vendor;

    @ColumnInfo(name = "barcode")
    public String barcode;

    @ColumnInfo(name = "asset_rfid")
    public String assetRFID;
}
