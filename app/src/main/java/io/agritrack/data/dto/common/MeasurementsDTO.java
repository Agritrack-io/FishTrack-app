package io.agritrack.data.dto.common;

import static io.agritrack.caen.api.ICAEN_API.DefaultInterval;

import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.data.model.common.TemperatureTimeSeries;

public class MeasurementsDTO {
    public String logger_rfid;
    public Long retrieved_at;
    public String enabled_at;
    public Short interval;
    public List<TemperatureDataDTO> values;

    public static MeasurementsDTO convert(TemperatureTimeSeries measurement) {
        MeasurementsDTO measurementsDTO = new MeasurementsDTO();
        measurementsDTO.retrieved_at = measurement.measurement.retrievedAt;
        measurementsDTO.enabled_at = measurement.measurement.enabledAt;
        measurementsDTO.interval = DefaultInterval;
        measurementsDTO.logger_rfid = measurement.measurement.loggerRFID;
        measurementsDTO.values = measurement.data.stream().map(x-> new TemperatureDataDTO(x.timestamp, x.value)).collect(Collectors.toList());

        return measurementsDTO;
    }
}
