package io.agritrack.kefalonia.data.dto.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.agritrack.kefalonia.data.model.common.TemperatureData;

public class TemperatureDataDTO {

    public String timestamp;
    public Double value;

    public TemperatureDataDTO() {
    }

    public TemperatureDataDTO(String ts, Double val) {
        this.timestamp = ts;
        this.value = val;
    }

    public static TemperatureDataDTO convert(TemperatureData value) {
        TemperatureDataDTO measurementsDTO = new TemperatureDataDTO();
        measurementsDTO.timestamp = value.timestamp;
        measurementsDTO.value = value.value;

        return measurementsDTO;
    }
}
