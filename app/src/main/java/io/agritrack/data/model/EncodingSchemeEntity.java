package io.agritrack.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "encoding_scheme")
public class EncodingSchemeEntity {

    @PrimaryKey
    @NonNull
    public String code;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "encoding_index")
    public Integer encoding_index;
}