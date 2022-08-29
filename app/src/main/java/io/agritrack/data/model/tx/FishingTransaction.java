package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.data.converter.BinRecordConverter;
import io.agritrack.data.converter.LocalDateConverter;
import io.agritrack.data.converter.StringListConverter;
import io.agritrack.data.converter.TxStatusEnumConverter;
import io.agritrack.enums.TxStatus;
import io.agritrack.fish.ui.bo.BinWeightRecord;

@Entity(tableName = "fishing_transaction")
public class FishingTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "timestamp")
    public Long timestamp;

    @ColumnInfo(name = "fishing_request")
    public String fishingRq;

    @ColumnInfo(name = "platform_rfid")
    public String platformRFID;

    @ColumnInfo(name = "cage_rfid")
    public String cageRFID;

    @ColumnInfo(name = "cage_code")
    public String cageCode;

    @ColumnInfo(name = "hlot")
    public String hlot;

    @ColumnInfo(name = "ichthyopathologist")
    public String ichthyopathologist;

    @ColumnInfo(name = "fish_type")
    public String fishType;

    @ColumnInfo(name = "average_weight")
    public String averageWeight;

    @ColumnInfo(name = "ice_adequacy")
    public String iceAdequacy;

    @ColumnInfo(name = "ice_supplier")
    public String iceSupplier;

    @TypeConverters(LocalDateConverter.class)
    @ColumnInfo(name = "last_feed")
    public LocalDate lastFeed;

    @ColumnInfo(name = "ordered_by")
    public String requester;

    @ColumnInfo(name = "ordered_quantity")
    public Integer orderedQuantity;

    @ColumnInfo(name = "total_quantity")
    public Integer totalQty;

    @ColumnInfo(name = "number_harvest_bins")
    public Short harvestBinsCnt;

    @TypeConverters(BinRecordConverter.class)
    @ColumnInfo(name = "harvest_bins_data")
    public List<BinWeightRecord.BinRecord> harvestBinsData;

    @TypeConverters(StringListConverter.class)
    @ColumnInfo(name = "avail_bins")
    public List<String> availBins;

    @TypeConverters(StringListConverter.class)
    @ColumnInfo(name = "team_members")
    public List<String> team;

    @ColumnInfo(name = "notes")
    public String notes;

    @TypeConverters(TxStatusEnumConverter.class)
    @ColumnInfo(name = "status")
    public TxStatus txStatus = TxStatus.NONE;

    @ColumnInfo(name = "site_code")
    public String site;

    @ColumnInfo(name = "packaging_plant")
    public String packagingPlant;

    @ColumnInfo(name = "user_name")
    public String user;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;

    @ColumnInfo(name = "out_of_system")
    public boolean outOfSystemFishing;

    @ColumnInfo(name = "itin_no")
    public Short itinSno;

    @ColumnInfo(name = "hash_code")
    public Integer hashCode;


    public void calcHash() {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String today = dateFormat.format(new Date());
            String bins = harvestBinsData.stream().sorted((l, r) -> r.binEPC.compareTo(r.binEPC)).map(e -> e.binEPC).collect(Collectors.joining("."));

            this.hashCode = String.format("%s:%s", today, bins).hashCode();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
