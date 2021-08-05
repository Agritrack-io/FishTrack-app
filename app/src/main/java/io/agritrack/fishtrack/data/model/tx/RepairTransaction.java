package io.agritrack.fishtrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "repair_transaction")
public class RepairTransaction {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name = "asset_rfid")
    private String assetRFID;

    @ColumnInfo(name = "asset_type")
    private String assetType;

    @ColumnInfo(name = "repair_type")
    private String repairType;

    @ColumnInfo(name = "on_field")
    private Boolean onField;

    @ColumnInfo(name = "internal_repair")
    private Boolean internalRepair;

    @ColumnInfo(name = "repair_FTEs")
    private String repairFTEs;

    @ColumnInfo(name = "repair_manager")
    private String repairManager;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "cost")
    private Double cost;

    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    @ColumnInfo(name = "next_repair")
    private Date nextRepair;

    @ColumnInfo(name = "estimated_withdrawal")
    private Date estimatedWithdrawal;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_RepairTx_Site"))
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="FK_RepairTx_User"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", foreignKey = @ForeignKey(name="FK_RepairTx_Supplier"))
    private Supplier supplier;

    @Embedded
    private SpatialEntity location;*/
}
