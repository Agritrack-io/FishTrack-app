package io.agritrack.data.model.wh;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "asset")
public class Asset {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "rfid")
    public String rfid;

    @ColumnInfo(name = "asset_type")
    public String assetType;

    @ColumnInfo(name = "am_code")
    public String amCode;

    @ColumnInfo(name = "barcode")
    public String barcode;

    @ColumnInfo(name = "code")
    public String code;

    @ColumnInfo(name = "rfid_barcode")
    public String rfidBarcode;

    @ColumnInfo(name = "site_id")
    public String siteId;

    @ColumnInfo(name = "site_code")
    public String siteCode;

    @ColumnInfo(name = "cage_rfid")
    public String cageRFId;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "shape")
    public String shape;

    @ColumnInfo(name = "supplier_id")
    public String supplierId;

    @ColumnInfo(name = "depth")
    public Double depth;

    @ColumnInfo(name = "width")
    public Double width;

    @ColumnInfo(name = "length")
    public Double length;

    @ColumnInfo(name = "volume")
    public Double volume;

    @ColumnInfo(name = "perimeter")
    public Double perimeter;

    @ColumnInfo(name = "net_eye_girth")
    public Double netEyeGirth;

    @ColumnInfo(name = "purchase_date")
    public Long purchaseDate;

    @ColumnInfo(name = "cost")
    public Double cost;

    @ColumnInfo(name = "enabled")
    public Boolean enabled;

    @ColumnInfo(name = "withdrawal")
    public Long withdrewAt;

    @ColumnInfo(name = "estimated_withdrawal_date")
    public Long estiWithdrewAt;

    @ColumnInfo(name = "comments")
    public String comments;

    @ColumnInfo(name = "max_days_in_cage")
    public Integer maxDaysInCage;

    @ColumnInfo(name = "max_days_in_water")
    public Integer maxDaysInWater;

    @ColumnInfo(name = "total_days_in_water")
    public Integer totalDaysInWater;

    @ColumnInfo(name = "location_at_storeroom")
    public String storeroomLoc;

    @ColumnInfo(name = "ERP_Code")
    public String erpCode;

    @ColumnInfo(name = "date_inserted_in_cage")
    public Long insertInCageAt;

    @ColumnInfo(name = "description")
    public String description;

    /*@Embedded
    public SpatialEntity location;*/
}
