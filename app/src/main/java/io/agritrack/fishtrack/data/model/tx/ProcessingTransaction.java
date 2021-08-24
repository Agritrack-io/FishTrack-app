package io.agritrack.fishtrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "process_transaction")
public class ProcessingTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "clean_truck")
    public String cleanTruck;

    @ColumnInfo(name = "plot")
    public String plot;

    @ColumnInfo(name = "fish_type")
    public String fishType;

    @ColumnInfo(name = "dispatch_note")
    public String dispatchNote;

    @ColumnInfo(name = "fish_condition")
    public String fishCondition;

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
}
