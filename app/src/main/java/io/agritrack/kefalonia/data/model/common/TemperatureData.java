package io.agritrack.kefalonia.data.model.common;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.android.gms.common.util.Strings;

import java.text.SimpleDateFormat;
import java.util.UUID;

import io.agritrack.kefalonia.data.model.TempSample;

@Entity(tableName = "temperature_data")
public class TemperatureData {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "measurement_id")
    public UUID measurementId;

    @ColumnInfo
    public String timestamp;

    @ColumnInfo
    public Double value;

    public TemperatureData() {
    }

    public TemperatureData(UUID mId, String ts, Double val) {
        this.measurementId = mId;
        this.timestamp = ts;
        this.value = val;
    }

    public TemperatureData(UUID mId, String ts, String val) {
        this.measurementId = mId;
        this.timestamp = ts;
        this.value = !Strings.isEmptyOrWhitespace(val) && !"N/A".equalsIgnoreCase(val) ? Double.valueOf(val.replace(',', '.')) : Double.NaN;
    }

    public TempSample rawData(){
        String _val = this.value!=null ? this.value.toString() : "N/A";
        return new TempSample(this.timestamp, _val);
    }
}
