package io.agritrack.data.dto.common;
import java.util.ArrayList;
import java.util.List;

import io.agritrack.data.model.common.IotLogger;


public class IotLoggerDTO {

    public String rfid;
    public String model;
    public String type;
    public String barcode;
    public String asset_rfid;
    public String vendor;

    public static IotLogger convert(IotLoggerDTO iotLoggerDTO) {
        IotLogger iotLogger = new IotLogger();
        iotLogger.rfid = iotLoggerDTO.rfid;
        iotLogger.model = iotLoggerDTO.model;
        iotLogger.type = iotLoggerDTO.type;
        iotLogger.vendor = iotLoggerDTO.vendor;
        iotLogger.barcode = iotLoggerDTO.barcode;
        iotLogger.assetRFID = iotLoggerDTO.asset_rfid;
        return iotLogger;
    }

    public static IotLoggerDTO convertDTO(IotLogger iotLogger) {
        IotLoggerDTO iotLoggerDTO = new IotLoggerDTO();
        iotLoggerDTO.rfid = iotLogger.rfid;
        iotLoggerDTO.model = iotLogger.model;
        iotLoggerDTO.type = iotLogger.type;
        iotLoggerDTO.vendor = iotLogger.vendor;
        iotLoggerDTO.barcode = iotLogger.barcode;
        iotLoggerDTO.asset_rfid = iotLogger.assetRFID;
        return iotLoggerDTO;
    }

    public static List<IotLoggerDTO> convertListDTO(List<IotLogger> iotLoggers) {
        List<IotLoggerDTO> result = new ArrayList<>();
        for (IotLogger iotLogger : iotLoggers) {
            IotLoggerDTO itemDto = convertDTO(iotLogger);
            result.add(itemDto);
        }
        return result;


    }
}
