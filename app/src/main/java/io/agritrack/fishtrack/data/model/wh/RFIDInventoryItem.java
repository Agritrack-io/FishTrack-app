package io.agritrack.fishtrack.data.model.wh;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rfid_inventory_item")
public class RFIDInventoryItem {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "rfid")
    public String rfid;

    @ColumnInfo(name = "code")
    public String code;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_Id", foreignKey = @ForeignKey(name="FK_RFIDInventoryItem_Inventory"))
    public Inventory inventory;*/
}
