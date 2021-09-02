package io.agritrack.fishtrack.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cage_details")
public class CageDetails {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "asset_rfid")
    public String rfid;

    @ColumnInfo(name = "ichthyopathologist")
    public String ichthyopathologist;

    @ColumnInfo(name = "hlot")
    public String hlot;

    @ColumnInfo(name = "fish_type")
    public String fishType;

    @ColumnInfo(name = "site_id")
    public Long site;

    @ColumnInfo(name = "last_fed")
    public String lastFed;
}