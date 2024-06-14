package io.agritrack.philosofish.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fishing_harvest_bin")
public class FishingHarvestBin {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "fishing_id")
    public String fishingId;

    @ColumnInfo(name = "harvestBin_rfid")
    public String harvestBinRFId;

    @ColumnInfo(name = "quantity")
    public Integer quantity;

    @ColumnInfo(name = "temperature")
    public String temperature;
}
