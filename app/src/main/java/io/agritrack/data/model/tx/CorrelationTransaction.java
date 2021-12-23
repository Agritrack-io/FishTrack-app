package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import io.agritrack.data.converter.AssetTypeConverter;

@Entity(tableName = "correlation_transaction")
public class CorrelationTransaction {
    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "user_name")
    public String user;

    @ColumnInfo(name = "site")
    public String site;

    @ColumnInfo(name = "timestamp")
    public Long timestamp;

    @TypeConverters(AssetTypeConverter.class)
    @ColumnInfo(name = "asset_type")
    public String assetType;

    @ColumnInfo(name = "barcode")
    public String barcode;

    @ColumnInfo(name = "asset_rfid")
    public String assetRFID;

    @TypeConverters(AssetTypeConverter.class)
    @ColumnInfo(name = "logger_type")
    public String loggerType;

    @ColumnInfo(name = "logger_rfid")
    public String loggerRFID;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;
}
