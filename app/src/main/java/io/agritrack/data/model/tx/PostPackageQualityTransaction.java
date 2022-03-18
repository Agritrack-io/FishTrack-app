package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "post_package_quality_transaction")
public class PostPackageQualityTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "timestamp")
    public Long timestamp;

    @ColumnInfo(name = "plot")
    public String plot;

    @ColumnInfo(name = "box_sn")
    public String boxSn;

    @ColumnInfo(name = "tempT1")
    public Double tempT1;

    @ColumnInfo(name = "tempT2")
    public Double tempT2;

    @ColumnInfo(name = "tempT3")
    public Double tempT3;

    @ColumnInfo(name = "site_id")
    public String site;

    @ColumnInfo(name = "user_id")
    public String user;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;
}
