package io.agritrack.kefalonia.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.time.LocalDate;
import java.util.UUID;

import io.agritrack.kefalonia.data.converter.LocalDateConverter;

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

    @TypeConverters(LocalDateConverter.class)
    @ColumnInfo(name = "last_fed")
    public LocalDate lastFed;

    @ColumnInfo(name = "cage_code")
    public String cageCode;
}