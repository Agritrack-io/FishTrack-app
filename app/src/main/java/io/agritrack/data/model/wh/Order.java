package io.agritrack.data.model.wh;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "order")
public class Order {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "attr1")
    public String attr1;

    @ColumnInfo(name = "attr2")
    public String attr2;

    @ColumnInfo(name = "order_no")
    public String orderNo;

    @ColumnInfo(name = "priority")
    public Short priority;

    @ColumnInfo(name = "order_status")
    public String orderStatus;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_site", foreignKey = @ForeignKey(name="FK_Order_Source_Site"))
    public Site sourceSite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_site", foreignKey = @ForeignKey(name="FK_Order_Target_Site"))
    public Site targetSite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="FK_Order_User"))
    public User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", foreignKey = @ForeignKey(name="FK_Order_Customer"))
    public Customer customer;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    public List<OrderItem> orderItems;*/
}
