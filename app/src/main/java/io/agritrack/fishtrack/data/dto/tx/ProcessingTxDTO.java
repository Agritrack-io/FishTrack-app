package io.agritrack.fishtrack.data.dto.tx;

import java.util.LinkedList;
import java.util.List;

import io.agritrack.fishtrack.data.model.tx.ProcessingTransaction;

public class ProcessingTxDTO {

    public Long id;
    public String clean_truck;
    public String plot;
    public String fish_type;
    public String dispatch_note;
    public String fish_condition;
    public String security_clip_number;
    public List<String> bins_received = new LinkedList<String>();
    public String flot;
    public String harvest_load;
    public String site;
    public String user;
    public Double longitude;
    public Double latitude;

    public static ProcessingTxDTO convert(ProcessingTransaction processing) {
        ProcessingTxDTO processingTxDto = new ProcessingTxDTO();

        processingTxDto.clean_truck = processing.cleanTruck;
        processingTxDto.plot = processing.plot;
        processingTxDto.fish_type = processing.fishType;
        processingTxDto.dispatch_note = processing.dispatchNote;
        processingTxDto.fish_condition = processing.fishCondition;
        processingTxDto.security_clip_number = processing.securityClipNumber;
        processingTxDto.flot = processing.flot;
        processingTxDto.site = processing.site;
        processingTxDto.harvest_load = processing.harvestLoad;
        processingTxDto.bins_received = processing.receivedBins;
        processingTxDto.user = processing.user;
        processingTxDto.longitude = processing.longitude;
        processingTxDto.latitude = processing.latitude;

        return processingTxDto;
    }
}
