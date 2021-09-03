package io.agritrack.fishtrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

import io.agritrack.fishtrack.data.converter.StringListConverter;

@Entity(tableName = "transport_transaction")
public class TransportTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "transport_head")
    public String transportHead;

    @ColumnInfo(name = "packaging_site_id")
    public String packagingSiteId;

    @ColumnInfo(name = "truck_refrigerated")
    public Boolean isTruckRefrigerated;

    @ColumnInfo(name = "parallel_transport")
    public Boolean isParallelTransport;

    @ColumnInfo(name = "truck_license_plate")
    public String truckLicensePlate;

    @ColumnInfo(name = "security_clip_number")
    public String securityClipNo;

    @ColumnInfo(name = "driver_name")
    public String driverName;

    @ColumnInfo(name = "driver_signature")
    public String driverSignature;

    @TypeConverters(StringListConverter.class)
    @ColumnInfo(name = "bins_loaded")
    public List<String> loadedBins;

    @ColumnInfo(name = "site_code")
    public String site;

    @ColumnInfo(name = "user_name")
    public String user;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_load_id", foreignKey = @ForeignKey(name="FK_Transport_Harvest_Load"))
    public HarvestLoad harvestLoad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flot_id", foreignKey = @ForeignKey(name="FK_Transport_FLOT"))
    public Flot flot;*/
}
