package io.agritrack.fishtrack.data.model.wh;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory")
public class Inventory {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "inventory_type")
    public String invType;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", foreignKey = @ForeignKey(name="FK_Inventory_User"))
    public User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader", foreignKey = @ForeignKey(name="FK_Inventory_Reader"))
    public Reader reader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site", foreignKey = @ForeignKey(name="FK_Inventory_Site"))
    public Site site;

    @OneToMany(mappedBy = "inventory", fetch = FetchType.LAZY)
    public List<RFIDInventoryItem> inventoryItems;*/
}
