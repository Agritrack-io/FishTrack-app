package io.agritrack.fishtrack.data.model.common;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "species")
public class FishSpecies {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name="country")
    private String country;

    @ColumnInfo(name="tax_name")
    private String scientificName;

    @ColumnInfo(name="local_name")
    private String localName;

    @ColumnInfo(name="name")
    private String name;
}
