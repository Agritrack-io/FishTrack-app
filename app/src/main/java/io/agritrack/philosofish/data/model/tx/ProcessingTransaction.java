package io.agritrack.philosofish.data.model.tx;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.agritrack.philosofish.data.converter.StringListConverter;

@Entity(tableName = "process_transaction")
public class ProcessingTransaction {

    public ProcessingTransaction() {
        this.id = UUID.randomUUID();
    }

    @PrimaryKey
    @NonNull
    public UUID id;

    @ColumnInfo(name = "created_at")
    public Long createdAt;

    @ColumnInfo(name = "clean_truck")
    public String cleanTruck;

    @ColumnInfo(name = "smells")
    public String smells;

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

    @ColumnInfo(name = "hash_code")
    public Integer hashCode;

    @ColumnInfo(name = "is_init")
    public boolean isInit = false;

    public void calcHash() {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String today = dateFormat.format(new Date());
            String bins = receivedBins.stream().sorted().collect(Collectors.joining("."));

            this.hashCode = String.format("%s:%s", today, bins).hashCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
