package io.agritrack.data.dto.tx;

import java.util.UUID;

import io.agritrack.data.model.tx.SeaTemperatureTransaction;

public class SeaTemperatureTxDTO {

    public Long id;
    public Long measured_at;
    public String site_id;
    public Double ref_temperature;
    public Double cage_temperature;
    public Double lon;
    public Double lat;

    public static SeaTemperatureTxDTO convert(SeaTemperatureTransaction seaTemperatureTransaction) {
        SeaTemperatureTxDTO seaTemperatureTxDTO = new SeaTemperatureTxDTO();

        //seaTemperatureTxDTO.id = seaTemperatureTransaction.id;
        seaTemperatureTxDTO.measured_at = seaTemperatureTransaction.timestamp;
        seaTemperatureTxDTO.site_id = seaTemperatureTransaction.siteId;
        seaTemperatureTxDTO.ref_temperature = seaTemperatureTransaction.refTemp;
        seaTemperatureTxDTO.cage_temperature = seaTemperatureTransaction.cageTemp;
        seaTemperatureTxDTO.lon = seaTemperatureTransaction.longitude;
        seaTemperatureTxDTO.lat = seaTemperatureTransaction.latitude;

        return seaTemperatureTxDTO;
    }

    public static SeaTemperatureTransaction convert(SeaTemperatureTxDTO seaTemperatureTxDTO) {
        SeaTemperatureTransaction seaTemperatureTransaction = new SeaTemperatureTransaction();

        seaTemperatureTransaction.id = seaTemperatureTxDTO.id;
        seaTemperatureTransaction.timestamp = seaTemperatureTxDTO.measured_at;
        seaTemperatureTransaction.siteId = seaTemperatureTxDTO.site_id;
        seaTemperatureTransaction.refTemp = seaTemperatureTxDTO.ref_temperature;
        seaTemperatureTransaction.cageTemp = seaTemperatureTxDTO.cage_temperature;

        return seaTemperatureTransaction;
    }
}
