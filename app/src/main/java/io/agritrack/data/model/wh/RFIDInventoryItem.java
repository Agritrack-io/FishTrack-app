package io.agritrack.data.model.wh;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import io.agritrack.data.converter.AssetTypeConverter;

@Entity(tableName = "rfid_inventory_item")
public class RFIDInventoryItem {

    @PrimaryKey
    public Long itmId;

    @ColumnInfo(name = "rfid")
    public String itemRFID;

    @TypeConverters(AssetTypeConverter.class)
    @ColumnInfo(name = "asset_type")
    public String assetType;

    @ColumnInfo(name = "code")
    public String code;

    public Long inventory;
}
