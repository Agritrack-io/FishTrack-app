package io.agritrack.fishtrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "harvest_transaction")
public class HarvestTransaction {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name = "transaction_type")
    private String transactionType;

   /* @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_load_id", foreignKey = @ForeignKey(name="FK_HarvestTx_Harvest_Load"))
    private HarvestLoad harvestLoad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flot_id", foreignKey = @ForeignKey(name="FK_HarvestTx_FLOT"))
    private Flot flot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_HarvestTx_Site"))
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="FK_HarvestTx_User"))
    private User user;*/
}
