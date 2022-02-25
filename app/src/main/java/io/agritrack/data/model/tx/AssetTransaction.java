package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;
import java.util.Map;

import io.agritrack.data.converter.StringMapConverter;

@Entity(tableName = "asset_transaction")
public class AssetTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "timestamp")
    public Long timestamp;

    @ColumnInfo(name = "user_id")
    public String userId;

    @TypeConverters(StringMapConverter.class)
    @ColumnInfo(name = "rfids")
    public Map<String, List<String>> itemRFIDs;

    @ColumnInfo(name = "asset_type")
    public String assetType;

    @ColumnInfo(name = "current_site")
    public String site;

    @ColumnInfo(name = "state")
    public String state;

    @ColumnInfo(name = "source")
    public String from;

    @ColumnInfo(name = "dest")
    public String to;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;
}
