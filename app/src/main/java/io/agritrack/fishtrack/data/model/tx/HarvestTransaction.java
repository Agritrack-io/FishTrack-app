package io.agritrack.fishtrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import io.agritrack.fishtrack.data.converter.TxStatusEnumConverter;
import io.agritrack.fishtrack.enums.TxStatus;

@Entity(tableName = "harvest_transaction")
public class HarvestTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "transaction_type")
    public String transactionType;

    @ColumnInfo(name = "user_name")
    public String user;

    @ColumnInfo(name = "site_name")
    public String site;

    @ColumnInfo(name = "fish_type")
    public String fishType;

    @ColumnInfo(name = "requester_name")
    public String reqName;

    @ColumnInfo(name = "requested_qty")
    public String reqQty;

    @ColumnInfo(name = "flot")
    public String flot;

    @TypeConverters(TxStatusEnumConverter.class)
    @ColumnInfo(name = "fishing_status")
    public TxStatus fishingStatus;

    @TypeConverters(TxStatusEnumConverter.class)
    @ColumnInfo(name = "transport_status")
    public TxStatus transportStatus;

    @TypeConverters(TxStatusEnumConverter.class)
    @ColumnInfo(name = "process_status")
    public TxStatus processingStatus;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;

   /* @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_load_id", foreignKey = @ForeignKey(name="FK_HarvestTx_Harvest_Load"))
    public HarvestLoad harvestLoad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flot_id", foreignKey = @ForeignKey(name="FK_HarvestTx_FLOT"))
    public Flot flot;*/
}
