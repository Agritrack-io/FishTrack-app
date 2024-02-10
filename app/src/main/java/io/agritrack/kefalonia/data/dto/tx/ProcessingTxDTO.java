package io.agritrack.kefalonia.data.dto.tx;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import io.agritrack.kefalonia.data.model.tx.ProcessingTransaction;

public class ProcessingTxDTO {

    public UUID id;
    public String clean_truck;
    public String species;
    public String dispatch_note;
    public String security_clip_number;
    public List<String> bins_received = new LinkedList<String>();
    public String flot;
    public String site;
    public String user;
    public Long occurred_at;
    public Double longitude;
    public Double latitude;

    public static ProcessingTxDTO convert(ProcessingTransaction processing) {
        ProcessingTxDTO processingTxDto = new ProcessingTxDTO();
        processingTxDto.id = processing.id;
        processingTxDto.clean_truck = processing.cleanTruck;
        processingTxDto.species = processing.species;
        processingTxDto.dispatch_note = processing.dispatchNote;
        processingTxDto.security_clip_number = processing.securityClipNumber;
        processingTxDto.flot = processing.flot;
        processingTxDto.site = processing.site;
        processingTxDto.bins_received = processing.receivedBins;
        processingTxDto.user = processing.user;
        processingTxDto.occurred_at = processing.createdAt;
        processingTxDto.longitude = processing.longitude;
        processingTxDto.latitude = processing.latitude;

        return processingTxDto;
    }
}
