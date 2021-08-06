package io.agritrack.fishtrack.data.model.wh;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expected_co_inventory")
public class ExpectedCoInventory {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "barcode")
    public String barcode;

    @ColumnInfo(name = "code")
    public String code;

    @ColumnInfo(name = "quantity")
    public Long quantity;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_Id", foreignKey = @ForeignKey(name="FK_ExpectedCoInventory_Inventory"))
    public Inventory inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site", foreignKey = @ForeignKey(name="FK_ExpectedCoInventory_Site"))
    public Site site;*/
}
