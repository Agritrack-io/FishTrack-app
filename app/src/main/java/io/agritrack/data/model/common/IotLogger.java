package io.agritrack.data.model.common;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "iot_logger")
public class IotLogger {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "model")
    public String model;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "vendor")
    public String vendor;

    @ColumnInfo(name = "barcode")
    public String barcode;

    @ColumnInfo(name = "rfid")
    public String rfid;

    @ColumnInfo(name = "asset_rfid")
    public String assetRFID;
}
