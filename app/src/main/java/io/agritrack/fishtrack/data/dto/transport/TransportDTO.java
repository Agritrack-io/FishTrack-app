package io.agritrack.fishtrack.data.dto.transport;

import io.agritrack.fishtrack.data.model.tx.TransportTransaction;

public class TransportDTO {

    public Long id;
    public String transportHead;
    public String packagingSiteId;
    public Boolean isTruckRefrigerated;
    public Boolean isParallelTransport;
    public String truckLicensePlate;
    public String securityClipNo;
    public String driverName;
    public String driverSignature;

    public static TransportTransaction convert(TransportDTO transportDTO) {
        TransportTransaction transport = new TransportTransaction();
        transport.id = transportDTO.id;
        transport.transportHead = transportDTO.transportHead;
        transport.packagingSiteId = transportDTO.packagingSiteId;
        transport.isTruckRefrigerated = transportDTO.isTruckRefrigerated;
        transport.isParallelTransport = transportDTO.isParallelTransport;
        transport.truckLicensePlate = transportDTO.truckLicensePlate;
        transport.securityClipNo = transportDTO.securityClipNo;
        transport.driverName = transportDTO.driverName;
        transport.driverSignature = transportDTO.driverSignature;
        return transport;
    }
}
