package io.agritrack.fishtrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "consumable_transaction")
public class ConsumableTransaction {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    @ColumnInfo(name = "quantity")
    private Double quantity;

    @ColumnInfo(name = "barcode")
    private String barcode;

    @ColumnInfo(name = "ftes")
    private String ftes;

    @ColumnInfo(name = "dispatch_note")
    private String dispatchNote;

    @ColumnInfo(name = "transaction_type")
    private String transactionType;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_ConsumableTx_Site"))
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="FK_ConsumableTx_User"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_site", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name="FK_ConsumableTx_Source_Site"))
    private Site sourceSite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_site", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name="FK_ConsumableTx_Target_Site"))
    private Site targetSite;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "consunable_id", foreignKey = @ForeignKey(name="FK_ConsumableTx_Consumable"))
    private Consumable consumable;*/
}
