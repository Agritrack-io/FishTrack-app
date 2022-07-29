package io.agritrack.data.dto.tx;

import java.util.LinkedList;
import java.util.List;

import io.agritrack.data.model.tx.TransportTransaction;

public class TransportTxDTO {

    public Long id;
    public String transport_head;
    public String packaging_site;
    public Boolean truck_refrigerated;
    public Boolean parallel_transport;
    public String truck_license_plate;
    public String security_clip_number;
    public String driver_name;
    public String driver_phone;
    public String driver_signature;
    public List<String> bins_loaded = new LinkedList<String>();
    public String site;
    public String user;
    public Double longitude;
    public Double latitude;
    public List<TotesTxDTO> items;

    public static TransportTxDTO convert(TransportTransaction transport) {
        TransportTxDTO transportTxDto = new TransportTxDTO();

        transportTxDto.transport_head = transport.transportHead;
        transportTxDto.packaging_site = transport.destination;
        transportTxDto.truck_refrigerated = transport.isTruckRefrigerated;
        transportTxDto.parallel_transport = transport.isParallelTransport;
        transportTxDto.truck_license_plate = transport.truckLicensePlate;
        transportTxDto.security_clip_number = transport.securityClipNo;
        transportTxDto.driver_name = transport.driverName;
        transportTxDto.driver_phone = transport.driverPhone;
        transportTxDto.driver_signature = transport.driverSignature;
        transportTxDto.bins_loaded = transport.loadedBins;
        transportTxDto.user = transport.user;
        transportTxDto.site = transport.siteCode;
        transportTxDto.longitude = transport.longitude;
        transportTxDto.latitude = transport.latitude;

        return transportTxDto;
    }
}
