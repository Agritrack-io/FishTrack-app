package io.agritrack.philosofish.data.dto.common;

import static io.agritrack.philosofish.caen.api.ICAEN_API.DefaultInterval;

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
    public Double fish_temp1;
    public Double fish_temp2;
    public Double water_temp;
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
        temperatureTimeSeriesDTO.fish_temp1 = measurement.measurement.fishTemp;
        temperatureTimeSeriesDTO.fish_temp2 = measurement.measurement.fish2Temp;
        temperatureTimeSeriesDTO.water_temp = measurement.measurement.waterTemp;
        temperatureTimeSeriesDTO.measurements = measurement.data.stream().map(x -> new TemperatureDataDTO(x.timestamp, x.value)).collect(Collectors.toList());

        return temperatureTimeSeriesDTO;
    }
}
