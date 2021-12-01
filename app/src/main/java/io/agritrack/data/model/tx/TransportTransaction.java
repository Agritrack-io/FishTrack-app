package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

import io.agritrack.data.converter.StringListConverter;

@Entity(tableName = "transport_transaction")
public class TransportTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "timestamp")
    public Long timestamp;

    @ColumnInfo(name = "transport_head")
    public String transportHead;

    @ColumnInfo(name = "destination")
    public String destination;

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

    @ColumnInfo(name = "driver_phone")
    public String driverPhone;

    @ColumnInfo(name = "driver_signature")
    public String driverSignature;

    @TypeConverters(StringListConverter.class)
    @ColumnInfo(name = "bins_loaded")
    public List<String> loadedBins;

    @ColumnInfo(name = "site_code")
    public String siteCode;

    @ColumnInfo(name = "user_name")
    public String user;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;
}
