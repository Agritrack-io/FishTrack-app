package io.agritrack.fishtrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;
import java.util.Map;

import io.agritrack.fishtrack.data.converter.AssetTypeConverter;
import io.agritrack.fishtrack.data.converter.StringListConverter;
import io.agritrack.fishtrack.data.converter.StringMapConverter;

@Entity(tableName = "asset_transaction")
public class AssetTransaction {

    @PrimaryKey
    public Long id;

    @TypeConverters(StringMapConverter.class)
    @ColumnInfo(name = "rfids")
    public Map<String, List<String>> itemRFIDs;

    @TypeConverters(AssetTypeConverter.class)
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
}
