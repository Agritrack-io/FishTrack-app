package io.agritrack.fishtrack.data.model.wh;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "co_inventory_item")
public class CoInventoryItem {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "barcode")
    public String barcode;

    @ColumnInfo(name = "quantity")
    public Long quantity;

    @ColumnInfo(name = "code")
    public String code;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_Id", foreignKey = @ForeignKey(name="FK_CoInventoryItem_Inventory"))
    public Inventory inventory;*/
}
