package io.agritrack.data.model.wh;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "rfid_inventory_item")
public class RFIDInventoryItem {

    @PrimaryKey
    public Long itmId;

    @ColumnInfo(name = "rfid")
    public String itemRFID;

    @ColumnInfo(name = "asset_type")
    public String assetType;

    @ColumnInfo(name = "code")
    public String code;

    public Long inventory;
}
