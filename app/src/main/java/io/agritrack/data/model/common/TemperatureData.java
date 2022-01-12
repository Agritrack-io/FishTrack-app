package io.agritrack.data.model.common;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "temperature_data")
public class TemperatureData {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "measurement_id")
    public Long measurementId;

    @ColumnInfo
    public String timestamp;

    @ColumnInfo
    public Double value;

    public TemperatureData() {
    }

    public TemperatureData(Long mId, String ts, Double val) {
        this.measurementId = mId;
        this.timestamp = ts;
        this.value = val;
    }
}
