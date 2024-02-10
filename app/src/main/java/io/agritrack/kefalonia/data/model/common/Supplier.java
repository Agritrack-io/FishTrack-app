package io.agritrack.kefalonia.data.model.common;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "supplier")
public class Supplier {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "supplier_code")
    public String code;

    @ColumnInfo(name = "supplier_name")
    public String name;

    @ColumnInfo(name = "main_address")
    public String mainAddress;

    @ColumnInfo(name = "shipping_address")
    public String shippingAddress;

    @ColumnInfo(name = "vat")
    public String vat;

    @ColumnInfo(name = "currency")
    public String currency;

    @ColumnInfo(name = "enabled")
    public Boolean enabled;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_Supplier_Site"))
    public Site site;*/
}
