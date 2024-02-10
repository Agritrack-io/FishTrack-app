package io.agritrack.kefalonia.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "asset_tx_item")
public class AssetTxItem {

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
