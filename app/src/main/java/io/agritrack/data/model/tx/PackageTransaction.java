package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

import io.agritrack.data.converter.StringListConverter;
import io.agritrack.data.converter.TxStatusEnumConverter;
import io.agritrack.enums.TxStatus;

@Entity(tableName = "package_transaction")
public class PackageTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "site")
    public String site;

    @ColumnInfo(name = "user")
    public String user;

    @ColumnInfo(name = "packaging_lot")
    public String packagingLot;

    @ColumnInfo(name = "totes_cnt")
    public Integer totesCnt;

    @ColumnInfo(name = "palette_cnt")
    public Integer paletteCnt;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;

    @ColumnInfo(name = "created_at")
    public Long createdAt;

    @ColumnInfo(name = "packaging_site")
    public String packagingSite;
}
