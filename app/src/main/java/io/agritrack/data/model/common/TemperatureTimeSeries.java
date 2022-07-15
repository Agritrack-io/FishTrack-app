package io.agritrack.data.model.common;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Relation;

import java.util.List;

public class TemperatureTimeSeries {

    @Embedded
    public Measurement measurement;

    @Relation(
            parentColumn = "id",
            entityColumn = "measurement_id"
    )
    public List<TemperatureData> data;
}
