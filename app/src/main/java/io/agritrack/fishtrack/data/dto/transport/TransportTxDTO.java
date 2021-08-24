package io.agritrack.fishtrack.data.dto.transport;

import io.agritrack.fishtrack.data.model.tx.TransportTransaction;

public class TransportTxDTO {

    public Long id;
    public String transport_head;
    public String packaging_site;
    public Boolean truck_refrigerated;
    public Boolean parallel_transport;
    public String truck_license_plate;
    public String security_clip_number;
    public String driver_name;
    public String driver_signature;

    public static TransportTxDTO convert(TransportTransaction transport) {
        TransportTxDTO transportTxDto = new TransportTxDTO();

        transportTxDto.transport_head = transport.transportHead;
        transportTxDto.packaging_site = transport.packagingSiteId;
        transportTxDto.truck_refrigerated = transport.isTruckRefrigerated;
        transportTxDto.parallel_transport = transport.isParallelTransport;
        transportTxDto.truck_license_plate = transport.truckLicensePlate;
        transportTxDto.security_clip_number = transport.securityClipNo;
        transportTxDto.driver_name = transport.driverName;
        transportTxDto.driver_signature = transport.driverSignature;

        return transportTxDto;
    }
}
