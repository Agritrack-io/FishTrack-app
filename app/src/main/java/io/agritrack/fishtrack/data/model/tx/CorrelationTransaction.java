package io.agritrack.fishtrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import io.agritrack.fishtrack.data.converter.AssetTypeConverter;

@Entity(tableName = "correlation_transaction")
public class CorrelationTransaction {
    @PrimaryKey
    public Long id;

    @TypeConverters(AssetTypeConverter.class)
    @ColumnInfo(name = "asset_type")
    public String assetType;

    @ColumnInfo(name = "barcode")
    public String barcode;

    @ColumnInfo(name = "rfid")
    public String rfid;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;
}
