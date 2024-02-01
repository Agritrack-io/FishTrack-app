package io.agritrack.data.dto.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.agritrack.data.model.common.TemperatureData;

public class TemperatureDataDTO {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
    private static SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd");
    public String timestamp;
    public Double value;

    public TemperatureDataDTO(){}

    public TemperatureDataDTO(String ts, Double val){
        try {
            Date date = sdf.parse(ts);
            this.timestamp = dmyFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.value = val;
    }

    public static TemperatureDataDTO convert(TemperatureData value) {
        TemperatureDataDTO measurementsDTO = new TemperatureDataDTO();
        measurementsDTO.timestamp = value.timestamp;
        measurementsDTO.value = value.value;

        return measurementsDTO;
    }
}
