package io.agritrack.fishtrack.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "flot")
public class Flot {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name = "site_id")
    private String siteId;

    @ColumnInfo(name = "cage_rfid")
    private String cageRFId;

    @ColumnInfo(name = "fish_type")
    private String fishType;

    @ColumnInfo(name = "last_fed_at")
    private Long lastFedAt;

}
