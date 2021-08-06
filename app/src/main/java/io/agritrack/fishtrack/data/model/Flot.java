package io.agritrack.fishtrack.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "flot")
public class Flot {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "site_id")
    public String siteId;

    @ColumnInfo(name = "cage_rfid")
    public String cageRFId;

    @ColumnInfo(name = "fish_type")
    public String fishType;

    @ColumnInfo(name = "last_fed_at")
    public Long lastFedAt;

}
