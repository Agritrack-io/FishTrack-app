package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

import io.agritrack.data.converter.StringListConverter;

@Entity(tableName = "process_transaction")
public class ProcessingTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "timestamp")
    public Long timestamp;

    @ColumnInfo(name = "clean_truck")
    public String cleanTruck;

    @ColumnInfo(name = "smells")
    public String smells;

    @ColumnInfo(name = "plot")
    public String plot;

    @ColumnInfo(name = "species")
    public String species;

    @ColumnInfo(name = "dispatch_note")
    public String dispatchNote;

    @ColumnInfo(name = "product_condition")
    public String productCondition;

    @TypeConverters(StringListConverter.class)
    @ColumnInfo(name = "bins_received")
    public List<String> receivedBins;

    @ColumnInfo(name = "security_clip_number")
    public String securityClipNumber;

    @ColumnInfo(name = "flot_id")
    public String flot;

    @ColumnInfo(name = "site_id")
    public String site;

    @ColumnInfo(name = "harvest_load_id")
    public String harvestLoad;

    @ColumnInfo(name = "user_id")
    public String user;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;
}
