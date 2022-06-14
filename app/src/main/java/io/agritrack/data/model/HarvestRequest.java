package io.agritrack.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "harvest_request")
public class HarvestRequest {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "request_id")
    public String requestId;

    @ColumnInfo(name = "harvest_date")
    public String harvestDate;

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

    @ColumnInfo(name = "requester")
    public String requester;

    @ColumnInfo(name = "notes")
    public String notes;
}