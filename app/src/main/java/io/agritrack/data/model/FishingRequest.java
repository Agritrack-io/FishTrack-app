package io.agritrack.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fishing_request")
public class FishingRequest {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "request_id")
    public String requestId;

    @ColumnInfo(name = "lot")
    public String lot;

    @ColumnInfo(name = "harvest_date")
    public String harvestDate;

    @ColumnInfo(name = "farm_arrival")
    public String farmArrival;

    @ColumnInfo(name = "packaging_plant")
    public String packagingPlant;

    @ColumnInfo(name = "site")
    public String site;

    @ColumnInfo(name = "cage_rfid")
    public String cageRFID;

    @ColumnInfo(name = "cage_code")
    public String cageCode;

    @ColumnInfo(name = "species")
    public String species;

    @ColumnInfo(name = "average_weight")
    public Double averageWeight;

    @ColumnInfo(name = "request_quantity")
    public Double reqQty;

    @ColumnInfo(name = "quantity")
    public Double quantity;

    @ColumnInfo(name = "requester")
    public String requester;

    @ColumnInfo(name = "driver")
    public String driver;

    @ColumnInfo(name = "notes")
    public String notes;

    @ColumnInfo(name = "itin_no")
    public Short itinSNo;

    @ColumnInfo(name = "parent_itin_no")
    public Short parentItinSno;
}