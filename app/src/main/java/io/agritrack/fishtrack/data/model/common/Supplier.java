package io.agritrack.fishtrack.data.model.common;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "supplier")
public class Supplier {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name = "supplier_code")
    private String code;

    @ColumnInfo(name = "supplier_name")
    private String name;

    @ColumnInfo(name = "main_address")
    private String mainAddress;

    @ColumnInfo(name = "shipping_address")
    private String shippingAddress;

    @ColumnInfo(name = "vat")
    private String vat;

    @ColumnInfo(name = "currency")
    private String currency;

    @ColumnInfo(name = "enabled")
    private Boolean enabled;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_Supplier_Site"))
    private Site site;*/
}
