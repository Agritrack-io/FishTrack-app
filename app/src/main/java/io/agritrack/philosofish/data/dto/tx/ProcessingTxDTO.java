package io.agritrack.philosofish.data.dto.tx;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import io.agritrack.philosofish.data.model.tx.ProcessingTransaction;

public class ProcessingTxDTO {

    private static final SimpleDateFormat simpleDateTime =  new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ss");

    public UUID id;
    public String clean_truck;
    public String species;
    public String dispatch_note;
    public String security_clip_number;
    public List<String> bins_received = new LinkedList<String>();
    public String flot;
    public String site;
    public String user;

    //@JsonFormat(pattern = "dd/mm/yyyy'T'HH:mm:ss")
    public String occurred_at;

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
        processingTxDto.occurred_at = simpleDateTime.format(new Date(processing.createdAt));
        processingTxDto.longitude = processing.longitude;
        processingTxDto.latitude = processing.latitude;

        return processingTxDto;
    }
}
