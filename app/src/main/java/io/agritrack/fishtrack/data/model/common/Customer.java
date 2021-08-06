package io.agritrack.fishtrack.data.model.common;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "customer")
public class Customer {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "customer_code")
    public String code;

    @ColumnInfo(name = "customer_name")
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
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_Customer_Site"))
    public Site site;*/
}
