package io.agritrack.kefalonia.data.dto.tx;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import io.agritrack.kefalonia.data.model.tx.FishingTransaction;
import io.agritrack.kefalonia.fish.ui.bo.BinWeightRecord;

public class FishingTxDTO {
    public UUID id;
    public String fishing_request;
    public String platform_rfid;
    public String cage_rfid;
    public String hlot;
    public String ichthyopathologist;
    public String species;
    public String requester;
    public Integer ordered_quantity;
    public Integer total_quantity;
    public Short number_harvest_bins;
    public List<BinWeightRecord.BinRecord> harvest_bins_data;
    public List<String> fishing_team = new LinkedList<String>();
    public String user;
    public String farm;
    public String packaging_plant;
    public Short parent_itinerary;
    public String reason_deviation;
    public Long occurred_at;
    public Double longitude;
    public Double latitude;

    public static FishingTxDTO convert(FishingTransaction fishingTx) {
        FishingTxDTO fishingTxDTO = new FishingTxDTO();
        fishingTxDTO.id = fishingTx.id;
        fishingTxDTO.fishing_request = fishingTx.fishingRq;
        fishingTxDTO.platform_rfid = fishingTx.platformRFID;
        fishingTxDTO.cage_rfid = fishingTx.cageRFID;
        fishingTxDTO.hlot = fishingTx.hlot;
        fishingTxDTO.ichthyopathologist = fishingTx.ichthyopathologist;
        fishingTxDTO.species = fishingTx.fishType;
        fishingTxDTO.requester = fishingTx.requester;
        fishingTxDTO.ordered_quantity = fishingTx.orderedQuantity;
        fishingTxDTO.total_quantity = fishingTx.totalQty;
        fishingTxDTO.number_harvest_bins = fishingTx.harvestBinsCnt;
        fishingTxDTO.harvest_bins_data = fishingTx.harvestBinsData;
        fishingTxDTO.fishing_team = fishingTx.team;
        fishingTxDTO.user = fishingTx.user;
        fishingTxDTO.farm = fishingTx.site;
        fishingTxDTO.packaging_plant = fishingTx.packagingPlant;
        fishingTxDTO.parent_itinerary = fishingTx.parentItinSno;
        fishingTxDTO.reason_deviation = fishingTx.reasonOfDeviation;
        fishingTxDTO.occurred_at = fishingTx.createdAt;
        fishingTxDTO.longitude = fishingTx.longitude;
        fishingTxDTO.latitude = fishingTx.latitude;

        return fishingTxDTO;
    }
}
