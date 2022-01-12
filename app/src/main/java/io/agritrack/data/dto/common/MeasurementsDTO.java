package io.agritrack.data.dto.common;

import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.data.model.common.TemperatureTimeSeries;

public class MeasurementsDTO {
    public Long id;
    public String logger_rfid;
    public Long retrieved_at;
    public List<TemperatureDataDTO> values;

    public static MeasurementsDTO convert(TemperatureTimeSeries measurement) {
        MeasurementsDTO measurementsDTO = new MeasurementsDTO();
        measurementsDTO.id = measurement.measurement.id;
        measurementsDTO.retrieved_at = measurement.measurement.retrievedAt;
        measurementsDTO.logger_rfid = measurement.measurement.loggerRFID;
        measurementsDTO.values = measurement.data.stream().map(x-> new TemperatureDataDTO(x.timestamp, x.value)).collect(Collectors.toList());

        return measurementsDTO;
    }
}
