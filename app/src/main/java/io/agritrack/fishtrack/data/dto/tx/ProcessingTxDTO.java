package io.agritrack.fishtrack.data.dto.tx;

import io.agritrack.fishtrack.data.model.tx.ProcessingTransaction;

public class ProcessingTxDTO {

    public Long id;
    public String clean_truck;
    public String plot;
    public String fish_type;
    public String dispatch_note;
    public String fish_condition;
    public String security_clip_number;
    public String flot;
    public String site;
    public String harvest_load;
    public String user;

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
        processingTxDto.user = processing.user;

        return processingTxDto;
    }
}
