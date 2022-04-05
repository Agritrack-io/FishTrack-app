package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "sea_temperature_transaction")
public class SeaTemperatureTransaction {
    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "timestamp")
    public Long timestamp;

    @ColumnInfo(name = "site_code")
    public UUID siteId;

    @ColumnInfo(name = "site_name")
    public String siteName;

    @ColumnInfo(name = "ref_temperature")
    public Double refTemp;

    @ColumnInfo(name = "cage_temperature")
    public Double cageTemp;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;
}
