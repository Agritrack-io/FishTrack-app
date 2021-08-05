package io.agritrack.fishtrack.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fishing_harvest_bin")
public class FishingHarvestBin {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name = "fishing_id")
    private String fishingId;

    @ColumnInfo(name = "harvestBin_rfid")
    private String harvestBinRFId;

    @ColumnInfo(name = "quantity")
    private Double quantity;

    @ColumnInfo(name = "temperature")
    private String temperature;
}
