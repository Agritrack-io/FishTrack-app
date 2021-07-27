package io.agritrack.fishtrack.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "site")
public class Site {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "code")
    public String code;

    @ColumnInfo(name = "site_lvl")
    public String siteLevel;

    @ColumnInfo(name = "active")
    public Boolean active;

    public Site() {
    }

    @NonNull
    public String toString() {
        return name + " [" + code + "]" ;
    }
}
