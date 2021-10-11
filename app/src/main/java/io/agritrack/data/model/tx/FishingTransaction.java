package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

import io.agritrack.data.converter.StringListConverter;
import io.agritrack.data.converter.TxStatusEnumConverter;
import io.agritrack.enums.TxStatus;

@Entity(tableName = "fishing_transaction")
public class FishingTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "timestamp")
    public Long timestamp;

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
    public Integer orderedQuantity;

    @ColumnInfo(name = "sea_temperature")
    public Double seaTemperature;

    @ColumnInfo(name = "total_quantity")
    public Integer totalQty;

    @ColumnInfo(name = "number_harvest_bins")
    public Short harvestBinsCnt;

    @TypeConverters(StringListConverter.class)
    @ColumnInfo(name = "harvest_bins")
    public List<String> harvestBins;

    @TypeConverters(StringListConverter.class)
    @ColumnInfo(name = "team_members")
    public List<String> team;

    @TypeConverters(TxStatusEnumConverter.class)
    @ColumnInfo(name = "status")
    public TxStatus txStatus = TxStatus.NONE;

    @ColumnInfo(name = "site_code")
    public String site;

    @ColumnInfo(name = "user_name")
    public String user;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;
}
