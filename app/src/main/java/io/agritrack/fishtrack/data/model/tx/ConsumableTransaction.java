package io.agritrack.fishtrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "consumable_transaction")
public class ConsumableTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "timestamp")
    public Date timestamp;

    @ColumnInfo(name = "quantity")
    public Integer quantity;

    @ColumnInfo(name = "barcode")
    public String barcode;

    @ColumnInfo(name = "ftes")
    public String ftes;

    @ColumnInfo(name = "dispatch_note")
    public String dispatchNote;

    @ColumnInfo(name = "transaction_type")
    public String transactionType;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_ConsumableTx_Site"))
    public Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="FK_ConsumableTx_User"))
    public User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_site", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name="FK_ConsumableTx_Source_Site"))
    public Site sourceSite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_site", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name="FK_ConsumableTx_Target_Site"))
    public Site targetSite;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "consunable_id", foreignKey = @ForeignKey(name="FK_ConsumableTx_Consumable"))
    public Consumable consumable;*/
}
