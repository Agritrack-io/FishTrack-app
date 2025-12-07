package io.agritrack.philosofish.data.dto.common;

import static io.agritrack.philosofish.caen.api.ICAEN_API.DefaultInterval;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.agritrack.philosofish.data.model.common.TemperatureTimeSeries;

public class TemperatureTimeSeriesDTO {
    public UUID id;
    public String lot;
    public String production_lane;
    public String logger_rfid;
    public String asset_rfid;
    public Long retrieved_at;
    public Short interval;

    @SerializedName("fish_temp1")
    public Double surface_temp;

    @SerializedName("fish_temp2")
    public Double bottom_temp;

    public String corrective_action;
    public List<TemperatureDataDTO> measurements;

    public static TemperatureTimeSeriesDTO convert(TemperatureTimeSeries measurement) {
        TemperatureTimeSeriesDTO temperatureTimeSeriesDTO = new TemperatureTimeSeriesDTO();
        temperatureTimeSeriesDTO.id = measurement.measurement.id;
        temperatureTimeSeriesDTO.retrieved_at = measurement.measurement.retrievedAt;
        temperatureTimeSeriesDTO.interval = DefaultInterval;
        temperatureTimeSeriesDTO.logger_rfid = measurement.measurement.loggerRFID;
        temperatureTimeSeriesDTO.asset_rfid = measurement.measurement.assetRFID;
        temperatureTimeSeriesDTO.production_lane = measurement.measurement.productionLane;
        temperatureTimeSeriesDTO.lot = measurement.measurement.lot;
        temperatureTimeSeriesDTO.surface_temp = measurement.measurement.surfaceTemp;
        temperatureTimeSeriesDTO.bottom_temp = measurement.measurement.bottomTemp;
        temperatureTimeSeriesDTO.corrective_action = measurement.measurement.correctiveAction;
        temperatureTimeSeriesDTO.measurements = measurement.data.stream().map(x -> new TemperatureDataDTO(x.timestamp, x.value)).collect(Collectors.toList());

        return temperatureTimeSeriesDTO;
    }
}
