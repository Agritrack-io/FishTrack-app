package io.agritrack.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bin_info")
public class BinInfo {

    @PrimaryKey
    @ColumnInfo(name = "bin_rfid")
    @NonNull
    public String rfid;

    @ColumnInfo(name = "cage")
    public String cage;

    @ColumnInfo(name = "species")
    public String species;

    @ColumnInfo(name = "total_weight")
    public Double totalWeight;

    @ColumnInfo(name = "farm")
    public String farm;

    @ColumnInfo(name = "last_update")
    public Long lastUpdate;

    @ColumnInfo(name = "inited_at")
    public Long initedAt;
}
