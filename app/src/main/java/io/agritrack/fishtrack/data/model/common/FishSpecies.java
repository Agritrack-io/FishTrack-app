package io.agritrack.fishtrack.data.model.common;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "species")
public class FishSpecies {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name="country")
    public String country;

    @ColumnInfo(name="tax_name")
    public String scientificName;

    @ColumnInfo(name="local_name")
    public String localName;

    @ColumnInfo(name="name")
    public String name;
}
