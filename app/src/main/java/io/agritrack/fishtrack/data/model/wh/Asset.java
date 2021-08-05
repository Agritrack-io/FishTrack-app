package io.agritrack.fishtrack.data.model.wh;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "asset")
public class Asset {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name = "rfid")
    private String rfid;

    @ColumnInfo(name = "asset_type")
    private String assetType;

    @ColumnInfo(name = "am_code")
    private String amCode;

    @ColumnInfo(name = "barcode")
    private String barcode;

    @ColumnInfo(name = "code")
    private String code;

    @ColumnInfo(name = "rfid_barcode")
    private String rfidBarcode;

    @ColumnInfo(name = "site_id")
    private String siteId;

    @ColumnInfo(name = "cage_rfid")
    private String cageRFId;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "shape")
    private String shape;

    @ColumnInfo(name = "supplier_id")
    private String supplierId;

    @ColumnInfo(name = "depth")
    private Double depth;

    @ColumnInfo(name = "width")
    private Double width;

    @ColumnInfo(name = "length")
    private Double length;

    @ColumnInfo(name = "volume")
    private Double volume;

    @ColumnInfo(name = "perimeter")
    private Double perimeter;

    @ColumnInfo(name = "net_eye_girth")
    private Double netEyeGirth;

    @ColumnInfo(name = "purchase_date")
    private Long purchaseDate;

    @ColumnInfo(name = "cost")
    private Double cost;

    @ColumnInfo(name = "enabled")
    private Boolean enabled;

    @ColumnInfo(name = "withdrawal")
    private Long withdrewAt;

    @ColumnInfo(name = "estimated_withdrawal_date")
    private Long estiWithdrewAt;

    @ColumnInfo(name = "comments")
    private String comments;

    @ColumnInfo(name = "max_days_in_cage")
    private Integer maxDaysInCage;

    @ColumnInfo(name = "max_days_in_water")
    private Integer maxDaysInWater;

    @ColumnInfo(name = "total_days_in_water")
    private Integer totalDaysInWater;

    @ColumnInfo(name = "location_at_storeroom")
    private String storeroomLoc;

    @ColumnInfo(name = "ERP_Code")
    private String erpCode;

    @ColumnInfo(name = "date_inserted_in_cage")
    private Long insertInCageAt;

    @ColumnInfo(name = "description")
    private String description;

    /*@Embedded
    private SpatialEntity location;*/
}
