package io.agritrack.data.dto.tx;

import io.agritrack.data.model.tx.SeaTemperatureTransaction;

public class SeaTemperatureTxDTO {

    public Long timestamp;
    public String site_name;
    public Long site_id;
    public Double ref_temperature;
    public Double cage_temperature;
    public Double longitude;
    public Double latitude;

    public static SeaTemperatureTxDTO convert(SeaTemperatureTransaction seaTemperatureTransaction) {
        SeaTemperatureTxDTO seaTemperatureTxDTO = new SeaTemperatureTxDTO();

        seaTemperatureTxDTO.timestamp = seaTemperatureTransaction.timestamp;
        seaTemperatureTxDTO.site_name = seaTemperatureTransaction.siteName;
        seaTemperatureTxDTO.site_id = seaTemperatureTransaction.siteId;
        seaTemperatureTxDTO.ref_temperature = seaTemperatureTransaction.refTemp;
        seaTemperatureTxDTO.cage_temperature = seaTemperatureTransaction.cageTemp;
        seaTemperatureTxDTO.longitude = seaTemperatureTransaction.longitude;
        seaTemperatureTxDTO.latitude = seaTemperatureTransaction.latitude;

        return seaTemperatureTxDTO;
    }
}
