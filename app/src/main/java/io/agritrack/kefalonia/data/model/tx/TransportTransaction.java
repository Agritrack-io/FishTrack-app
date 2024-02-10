package io.agritrack.kefalonia.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.kefalonia.data.converter.StringListConverter;

@Entity(tableName = "transport_transaction")
public class TransportTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "created_at")
    public Long createdAt;

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

    @ColumnInfo(name = "capacity")
    public Integer capacity;

    @ColumnInfo(name = "hash_code")
    public Integer hashCode;

    public void calcHash() {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String today = dateFormat.format(new Date());
            String bins = loadedBins.stream().sorted().collect(Collectors.joining("."));

            this.hashCode = String.format("%s:%s", today, bins).hashCode();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
