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

    @ColumnInfo(name = "site_type")
    public String siteType;

    @ColumnInfo(name = "site_lvl")
    public Integer siteLevel;

    @ColumnInfo(name = "lvl1")
    public String lvl1;

    @ColumnInfo(name = "lvl2")
    public String lvl2;

    @ColumnInfo(name = "lvl3")
    public String lvl3;

    @ColumnInfo(name = "lvl4")
    public String lvl4;

    @ColumnInfo(name = "country")
    public String country;

    @ColumnInfo(name = "region")
    public String region;

    @ColumnInfo(name = "customer_site_id")
    public String customer_site_id;

    @ColumnInfo(name = "active")
    public Boolean active;

    @NonNull
    public String toString() {
        return name + " [" + code + "]" ;
    }
}
