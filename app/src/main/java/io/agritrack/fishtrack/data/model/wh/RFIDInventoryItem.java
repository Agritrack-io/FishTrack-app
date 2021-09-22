package io.agritrack.fishtrack.data.model.wh;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;
import java.util.Map;

import io.agritrack.fishtrack.data.converter.AssetTypeConverter;
import io.agritrack.fishtrack.data.converter.StringMapConverter;

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
