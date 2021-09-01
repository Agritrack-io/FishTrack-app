package io.agritrack.fishtrack.data.dto.tx;

import io.agritrack.fishtrack.data.model.tx.FishingTransaction;

public class FishingTxDTO {

    //public Long id;
    //public String hlot;
    public String platform_rfid;
    public String cage_rfid;
    public String net_rfid;
    public String ichthyopathologist;
    public String fish_type;
    public String ice_adequacy;
    public String ice_supplier;
    //public Date last_feed;
    public String requester;
    public Double ordered_quantity;
    public Double sea_temperature;
    public Double total_quantity;
    public Short number_harvest_bins;
    public String harvest_bins;
    public String status;

    public static FishingTxDTO convert(FishingTransaction fishing) {
        FishingTxDTO fishingTxDTO = new FishingTxDTO();
        //fishingTxDTO.id = fishing.id;
        //fishingTxDTO.hlot = fishing.hlot;
        fishingTxDTO.platform_rfid = fishing.platformRFID;
        fishingTxDTO.cage_rfid = fishing.cageRFID;
        fishingTxDTO.net_rfid = fishing.netRFID;
        fishingTxDTO.ichthyopathologist = fishing.ichthyopathologist;
        fishingTxDTO.fish_type = fishing.fishType;
        fishingTxDTO.ice_adequacy = fishing.iceAdequacy;
        fishingTxDTO.ice_supplier = fishing.iceSupplier;
        //fishingTxDTO.last_feed = fishing.lastFeed;
        fishingTxDTO.requester = fishing.requester;
        fishingTxDTO.ordered_quantity = fishing.orderedQuantity;
        fishingTxDTO.sea_temperature = fishing.seaTemperature;
        fishingTxDTO.total_quantity = fishing.totalQty;
        fishingTxDTO.number_harvest_bins = fishing.harvestBinsCnt;
        fishingTxDTO.harvest_bins = fishing.harvestBins;
        fishingTxDTO.status = fishing.txStatus.name();

        return fishingTxDTO;
    }
}
