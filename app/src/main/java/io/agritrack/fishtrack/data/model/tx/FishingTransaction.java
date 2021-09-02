package io.agritrack.fishtrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

import io.agritrack.fishtrack.data.converter.StringSetConverter;
import io.agritrack.fishtrack.data.converter.TxStatusEnumConverter;
import io.agritrack.fishtrack.enums.TxStatus;

@Entity(tableName = "fishing_transaction")
public class FishingTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "harvest_request")
    public String harvestRq;

    @ColumnInfo(name = "hlot")
    public String hlot;

    @ColumnInfo(name = "platform_rfid")
    public String platformRFID;

    @ColumnInfo(name = "cage_rfid")
    public String cageRFID;

    @ColumnInfo(name = "net_rfid")
    public String netRFID;

    @ColumnInfo(name = "ichthyopathologist")
    public String ichthyopathologist;

    @ColumnInfo(name = "fish_type")
    public String fishType;

    @ColumnInfo(name = "ice_adequacy")
    public String iceAdequacy;

    @ColumnInfo(name = "ice_supplier")
    public String iceSupplier;

    @ColumnInfo(name = "last_feed")
    public Long lastFeed;

    @ColumnInfo(name = "ordered_by")
    public String requester;

    @ColumnInfo(name = "ordered_quantity")
    public Double orderedQuantity;

    @ColumnInfo(name = "sea_temperature")
    public Double seaTemperature;

    @ColumnInfo(name = "total_quantity")
    public Double totalQty;

    @ColumnInfo(name = "number_harvest_bins")
    public Short harvestBinsCnt;

    @TypeConverters(StringSetConverter.class)
    @ColumnInfo(name = "harvest_bins")
    public List<String> harvestBins;

    @TypeConverters(TxStatusEnumConverter.class)
    @ColumnInfo(name = "status")
    public TxStatus txStatus = TxStatus.NONE;

   /* @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_load_id", foreignKey = @ForeignKey(name="FK_Fishing_Harvest_Load"))
    public HarvestLoad harvestLoad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flot_id", foreignKey = @ForeignKey(name="FK_Fishing_FLOT"))
    public Flot flot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_Fishing_Site"))
    public Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="FK_Fishing_User"))
    public User user;*/

}
