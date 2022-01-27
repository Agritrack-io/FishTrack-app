package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;
import io.agritrack.data.converter.StringListConverter;

@Entity(tableName = "storage_transaction")
public class StorageTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "user")
    public String user;

    @ColumnInfo(name = "number_totes")
    public Integer totesCnt;

    @ColumnInfo(name = "ifco_cnt")
    public Integer ifcoCnt;

    @ColumnInfo(name = "current_site")
    public String site;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "source")
    public String from;

    @ColumnInfo(name = "collection_lot")
    public String collectionLot;

    @ColumnInfo(name = "packaging_lot")
    public String packagingLot;

    @ColumnInfo(name = "total_weight")
    public String totalWeight;

    @ColumnInfo(name = "dest")
    public String to;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;

    @ColumnInfo(name = "created_at")
    public Long createdAt;
}
