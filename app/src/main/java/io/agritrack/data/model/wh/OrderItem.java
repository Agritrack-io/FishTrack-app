package io.agritrack.data.model.wh;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "order_item")
public class OrderItem {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "code")
    public String code;

    @ColumnInfo(name = "container_lvl1")
    public String containerLvl1;

    @ColumnInfo(name = "container_lvl2")
    public String containerLvl2;

    @ColumnInfo(name = "quantity")
    public Double quantity;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_Id", foreignKey = @ForeignKey(name="FK_OrderItem_Order"))
    public Order order;   */
}
