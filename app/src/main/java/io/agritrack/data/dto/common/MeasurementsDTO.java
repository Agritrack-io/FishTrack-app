package io.agritrack.data.dto.common;

import io.agritrack.data.model.common.Measurements;

public class MeasurementsDTO {
    public Long id;
    public Long logger_id;
    public String logger_rfid;
    public String values;

    public static MeasurementsDTO convert(Measurements measurements) {
        MeasurementsDTO measurementsDTO = new MeasurementsDTO();
        measurementsDTO.id = measurements.id;
        measurementsDTO.logger_id = measurements.loggerId;
        measurementsDTO.logger_rfid = measurements.loggerRFID;
        measurementsDTO.values = measurements.values;

        return measurementsDTO;
    }
}
