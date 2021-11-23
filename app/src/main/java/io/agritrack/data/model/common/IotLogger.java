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

    @ColumnInfo(name = "barcode")
    public String barcode;

    @ColumnInfo(name = "rfid_barcode")
    public String rfidBarcode;

    @ColumnInfo(name = "asset_rfid")
    public String assetRFID;

    @ColumnInfo(name = "code")
    public String code;
}
