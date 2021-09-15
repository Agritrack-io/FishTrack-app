package io.agritrack.fishtrack.data.model.wh;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "consumable")
public class Consumable {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "barcode")
    public String barcode;

    @ColumnInfo(name = "erp_code")
    public String code;

    @ColumnInfo(name = "lot")
    public String lot;

    @ColumnInfo(name = "consumable_type")
    public String consumableType;

    @ColumnInfo(name = "quantity")
    public Integer qty;

    @ColumnInfo(name = "packaging_quantity")
    public Integer packagingQty;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "details_1")
    public String details1;

    @ColumnInfo(name = "details_2")
    public String details2;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_Consumable_Site"))
    public Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", foreignKey = @ForeignKey(name="FK_Consumable_Supplier"))
    public Supplier supplier;*/
}
