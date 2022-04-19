package io.agritrack.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.time.LocalDateTime;
import java.util.UUID;

import io.agritrack.data.converter.LocalDateTimeConverter;

@Entity(tableName = "cage_details")
public class CageDetails {

    @PrimaryKey
    @NonNull
    public UUID id;

    @ColumnInfo(name = "asset_rfid")
    public String rfid;

    @ColumnInfo(name = "ichthyopathologist")
    public String ichthyopathologist;

    @ColumnInfo(name = "hlot")
    public String hlot;

    @ColumnInfo(name = "species")
    public String species;

    @ColumnInfo(name = "site_id")
    public UUID site;

    @TypeConverters(LocalDateTimeConverter.class)
    @ColumnInfo(name = "last_fed")
    public LocalDateTime lastFed;

    @ColumnInfo(name = "cage_code")
    public String cageCode;
}