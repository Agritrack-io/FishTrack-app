package io.agritrack.kefalonia.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.UUID;

@Entity(tableName = "correlation_transaction")
public class CorrelationTransaction {
    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "user_name")
    public String user;

    @ColumnInfo(name = "site")
    public UUID site;

    @ColumnInfo(name = "sub_site")
    public String subSite;

    @ColumnInfo(name = "timestamp")
    public Long timestamp;

    @ColumnInfo(name = "asset_type")
    public String assetType;

    @ColumnInfo(name = "asset_code")
    public String assetCode;

    @ColumnInfo(name = "asset_rfid")
    public String assetRFID;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "code")
    public String code;

    @ColumnInfo(name = "rfid")
    public String rfid;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;
}
