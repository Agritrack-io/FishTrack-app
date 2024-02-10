package io.agritrack.kefalonia.data.dto.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.agritrack.kefalonia.data.model.common.TemperatureData;

public class TemperatureDataDTO {

    private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public String timestamp;
    public Double value;

    public TemperatureDataDTO() {
    }

    public TemperatureDataDTO(String ts, Double val) {
        this.timestamp = parseDate(ts);
        this.value = val;
    }

    public static TemperatureDataDTO convert(TemperatureData value) {
        TemperatureDataDTO measurementsDTO = new TemperatureDataDTO();
        measurementsDTO.timestamp = value.timestamp;
        measurementsDTO.value = value.value;

        return measurementsDTO;
    }

    private String parseDate(String ts) {
        try {
            Date tts = sdf.parse(ts);
            return dmyFormat.format(tts);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ts;
    }
}
