package io.agritrack.data.model.common;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import io.agritrack.data.model.tx.SortingMeasurement;

public class SortingTimeSeries {

    @Embedded
    public SortingMeasurement measurement;

    @Relation(
            parentColumn = "id",
            entityColumn = "measurement_id"
    )
    public List<TemperatureData> data;
}
