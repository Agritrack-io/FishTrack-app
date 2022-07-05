package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import io.agritrack.data.model.common.Measurement;

@Entity(tableName = "sorting_measurement")
public class SortingMeasurement extends Measurement {

    @ColumnInfo(name = "production_line")
    public String productionLine;
}
