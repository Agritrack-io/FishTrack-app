package io.agritrack.fishtrack.data.model.wh;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import io.agritrack.fishtrack.data.converter.AssetTypeConverter;
import io.agritrack.fishtrack.data.converter.ConsumableTypeConverter;

@Entity(tableName = "co_inventory_item")
public class CoInventoryItem {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "barcode")
    public String barcode;

    @ColumnInfo(name = "quantity")
    public Integer quantity;

    @TypeConverters(ConsumableTypeConverter.class)
    @ColumnInfo(name = "consumable_type")
    public String consumableType;

    @ColumnInfo(name = "code")
    public String code;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    public Long CoInventory;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_Id", foreignKey = @ForeignKey(name="FK_CoInventoryItem_Inventory"))
    public Inventory inventory;*/
}
