package io.agritrack.fishtrack.data.model.transport;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transport")
public class Transport {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name = "transport_head")
    private String transportHead;

    @ColumnInfo(name = "packaging_site_id")
    private String packagingSiteId;

    @ColumnInfo(name = "truck_refrigerated")
    private Boolean isTruckRefrigerated;

    @ColumnInfo(name = "parallel_transport")
    private Boolean isParallelTransport;

    @ColumnInfo(name = "truck_license_plate")
    private String truckLicensePlate;

    @ColumnInfo(name = "security_clip_number")
    private String securityClipNo;

    @ColumnInfo(name = "driver_name")
    private String driverName;

    @ColumnInfo(name = "driver_signature")
    private String driverSignature;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_load_id", foreignKey = @ForeignKey(name="FK_Transport_Harvest_Load"))
    private HarvestLoad harvestLoad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flot_id", foreignKey = @ForeignKey(name="FK_Transport_FLOT"))
    private Flot flot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_Transport_Site"))
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="FK_Transport_User"))
    private User user;*/
}
