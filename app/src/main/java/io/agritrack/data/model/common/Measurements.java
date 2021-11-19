package io.agritrack.data.model.common;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "measurements")
public class Measurements {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "logger_id")
    public Long loggerId;

    @ColumnInfo(name = "logger_rfid")
    public String loggerRFID;

    @ColumnInfo(name = "retrieved_at")
    public Long retrievedAt;

    @ColumnInfo(name = "values")
    public String values;
}
