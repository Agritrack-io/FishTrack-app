package io.agritrack.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "harvest_request")
public class HarvestRequest {

    @PrimaryKey
    @NonNull
    public UUID id;

    @ColumnInfo(name = "request_id")
    public String requestId;

    @ColumnInfo(name = "request_quantity")
    public String reqQty;

    @ColumnInfo(name = "requester")
    public String requester;

    @ColumnInfo(name = "species")
    public String fishName;

    @ColumnInfo(name = "fish_size")
    public String fishSize;

    @ColumnInfo(name = "cage_rfid")
    public String cageRFID;

    @ColumnInfo(name = "cage_code")
    public String cageCode;

    @ColumnInfo(name = "notes")
    public String notes;

    @ColumnInfo(name = "user_name")
    public String user;

    @ColumnInfo(name = "site_name")
    public String site;

    @ColumnInfo(name = "packaging_plant")
    public String packagingPlant;

   /* @OneToMany(mappedBy = "harvestRequest", fetch = FetchType.LAZY)
    public List<HarvestLoad> harvestLoads;*/
}
