package io.agritrack.fishtrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "repair_transaction")
public class RepairTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "timestamp")
    public Long timestamp;

    @ColumnInfo(name = "asset_rfid")
    public String assetRFID;

    @ColumnInfo(name = "asset_type")
    public String assetType;

    @ColumnInfo(name = "repair_type")
    public String repairType;

    @ColumnInfo(name = "repair_time")
    public String repairTime;

    @ColumnInfo(name = "on_field")
    public Boolean onField;

    @ColumnInfo(name = "internal_repair")
    public Boolean internalRepair;

    @ColumnInfo(name = "repair_FTEs")
    public String repairFTEs;

    @ColumnInfo(name = "repair_manager")
    public String repairManager;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "cost")
    public Double cost;

    @ColumnInfo(name = "next_repair")
    public Date nextRepair;

    @ColumnInfo(name = "estimated_withdrawal")
    public Date estimatedWithdrawal;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_RepairTx_Site"))
    public Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="FK_RepairTx_User"))
    public User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", foreignKey = @ForeignKey(name="FK_RepairTx_Supplier"))
    public Supplier supplier;

    @Embedded
    public SpatialEntity location;*/
}
