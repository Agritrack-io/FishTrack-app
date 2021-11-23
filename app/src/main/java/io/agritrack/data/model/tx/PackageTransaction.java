package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

import io.agritrack.data.converter.StringListConverter;

@Entity(tableName = "package_transaction")
public class PackageTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "site")
    public String site;

    @ColumnInfo(name = "user_id")
    public Long userId;

    @ColumnInfo(name = "collection_lot")
    public String collectionLot;

    @TypeConverters(StringListConverter.class)
    @ColumnInfo(name = "totes_for_process")
    public List<String> totesForProcess;

    @TypeConverters(StringListConverter.class)
    @ColumnInfo(name = "packaged_ifco")
    public List<String> packagedIfco;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;
}
