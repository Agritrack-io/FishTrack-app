package io.agritrack.data.dto.tx;

import java.util.LinkedList;
import java.util.List;

import io.agritrack.data.model.tx.ProcessingTransaction;

public class ProcessingTxDTO {

    public Long id;
    public String clean_truck;
    public String plot;
    public String species;
    public String dispatch_note;
    public String product_condition;
    public String security_clip_number;
    public List<String> bins_received = new LinkedList<String>();
    public String flot;
    public String harvest_load;
    public String site;
    public String user;
    public Long timestamp;
    public Double longitude;
    public Double latitude;
    public List<TotesTxDTO> items;

    public static ProcessingTxDTO convert(ProcessingTransaction processing) {
        ProcessingTxDTO processingTxDto = new ProcessingTxDTO();

        processingTxDto.clean_truck = processing.cleanTruck;
        processingTxDto.plot = processing.plot;
        processingTxDto.species = processing.species;
        processingTxDto.dispatch_note = processing.dispatchNote;
        processingTxDto.product_condition = processing.productCondition;
        processingTxDto.security_clip_number = processing.securityClipNumber;
        processingTxDto.flot = processing.flot;
        processingTxDto.site = processing.site;
        processingTxDto.harvest_load = processing.harvestLoad;
        processingTxDto.bins_received = processing.receivedBins;
        processingTxDto.user = processing.user;
        processingTxDto.timestamp = processing.timestamp;
        processingTxDto.longitude = processing.longitude;
        processingTxDto.latitude = processing.latitude;

        return processingTxDto;
    }
}
