package io.agritrack.data.dto.common;
import io.agritrack.data.model.common.IotLogger;


public class IotLoggerDTO {

    public Long id;
    public String model;
    public String type;
    public String barcode;
    public String rfid;
    public String asset_rfid;
    public String code;

    public static IotLogger convert(IotLoggerDTO iotLoggerDTO) {
        IotLogger iotLogger = new IotLogger();
        iotLogger.id = iotLoggerDTO.id;
        iotLogger.model = iotLoggerDTO.model;
        iotLogger.type = iotLoggerDTO.type;
        iotLogger.barcode = iotLoggerDTO.barcode;
        iotLogger.rfid = iotLoggerDTO.rfid;
        iotLogger.assetRFID = iotLoggerDTO.asset_rfid;
        iotLogger.code = iotLoggerDTO.code;
        return iotLogger;
    }
}
