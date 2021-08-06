package io.agritrack.fishtrack.data.model.transport;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transport")
public class Transport {

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

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_load_id", foreignKey = @ForeignKey(name="FK_Transport_Harvest_Load"))
    public HarvestLoad harvestLoad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flot_id", foreignKey = @ForeignKey(name="FK_Transport_FLOT"))
    public Flot flot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_Transport_Site"))
    public Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="FK_Transport_User"))
    public User user;*/
}
