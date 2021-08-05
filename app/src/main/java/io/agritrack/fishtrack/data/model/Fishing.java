package io.agritrack.fishtrack.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "fishing")
public class Fishing {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name = "hlot")
    private String hlot;

    @ColumnInfo(name = "platform_rfid")
    private String platformRFID;

    @ColumnInfo(name = "cage_rfid")
    private String cageRFID;

    @ColumnInfo(name = "net_rfid")
    private String netRFID;

    @ColumnInfo(name = "ichthyopathologist")
    private String ichthyopathologist;

    @ColumnInfo(name = "fish_type")
    private String fishType;

    @ColumnInfo(name = "ice_adequacy")
    private String iceAdequacy;

    @ColumnInfo(name = "ice_supplier")
    private String iceSupplier;

    @ColumnInfo(name = "last_feed")
    private Date lastFeed;

    @ColumnInfo(name = "ordered_quantity")
    private Double orderedQuantity;

    @ColumnInfo(name = "sea_temperature")
    private Double seaTemperature;

    @ColumnInfo(name = "total_quantity")
    private Double totalQty;

    @ColumnInfo(name = "number_harvest_bins")
    private Short harvestBinsCnt;

   /* @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_load_id", foreignKey = @ForeignKey(name="FK_Fishing_Harvest_Load"))
    private HarvestLoad harvestLoad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flot_id", foreignKey = @ForeignKey(name="FK_Fishing_FLOT"))
    private Flot flot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_Fishing_Site"))
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="FK_Fishing_User"))
    private User user;*/
}
