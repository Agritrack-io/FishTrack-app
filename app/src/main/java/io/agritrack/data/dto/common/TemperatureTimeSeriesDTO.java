package io.agritrack.data.dto.common;

import static io.agritrack.caen.api.ICAEN_API.DefaultInterval;

import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.data.model.common.TemperatureTimeSeries;

public class TemperatureTimeSeriesDTO {
    public String lot;
    public String logger_rfid;
    public String asset_rfid;
    public Long retrieved_at;
    public Short interval;
    public List<TemperatureDataDTO> values;

    public static TemperatureTimeSeriesDTO convert(TemperatureTimeSeries measurement) {
        TemperatureTimeSeriesDTO temperatureTimeSeriesDTO = new TemperatureTimeSeriesDTO();
        temperatureTimeSeriesDTO.retrieved_at = measurement.measurement.retrievedAt;
        temperatureTimeSeriesDTO.interval = DefaultInterval;
        temperatureTimeSeriesDTO.logger_rfid = measurement.measurement.loggerRFID;
        temperatureTimeSeriesDTO.asset_rfid = measurement.measurement.assetRFID;
        temperatureTimeSeriesDTO.values = measurement.data.stream().map(x-> new TemperatureDataDTO(x.timestamp, x.value)).collect(Collectors.toList());

        return temperatureTimeSeriesDTO;
    }
}
