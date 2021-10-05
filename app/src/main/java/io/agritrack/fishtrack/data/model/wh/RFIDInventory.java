package io.agritrack.fishtrack.data.model.wh;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rfid_inventory")
public class RFIDInventory {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "inventory_type")
    public String rfidInvType;

    @ColumnInfo(name = "site")
    public String site;

    @ColumnInfo(name = "performed_at")
    public Long performedAt;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", foreignKey = @ForeignKey(name="FK_Inventory_User"))
    public User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader", foreignKey = @ForeignKey(name="FK_Inventory_Reader"))
    public Reader reader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site", foreignKey = @ForeignKey(name="FK_Inventory_Site"))
    public Site site;

    @OneToMany(mappedBy = "", fetch = FetchType.LAZY)
    public List<RFIDInventoryItem> inventoryItems;*/
}
