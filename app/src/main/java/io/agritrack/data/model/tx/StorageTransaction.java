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

    @ColumnInfo(name = "user_id")
    public String userId;

    @TypeConverters(StringListConverter.class)
    @ColumnInfo(name = "totes_for_storage")
    public List<String> totesForStorage;

    @TypeConverters(StringListConverter.class)
    @ColumnInfo(name = "ifco_for_storage")
    public List<String> ifcoForStorage;

    @ColumnInfo(name = "current_site")
    public String site;

    @ColumnInfo(name = "state")
    public String state;

    @ColumnInfo(name = "source")
    public String from;

    @ColumnInfo(name = "collection_lot")
    public String collectionLot;

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
