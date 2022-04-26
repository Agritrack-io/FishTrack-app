package io.agritrack.data.dto.tx;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import io.agritrack.data.model.tx.FishingTransaction;
import io.agritrack.fish.ui.bo.BinWeightRecord;

public class FishingTxDTO {

    public String harvest_request;
    public String platform_rfid;
    public String cage_rfid;
    public String cage_code;
    public String net_rfid;
    public String ichthyopathologist;
    public String species;
    public String fish_size;
    public String ice_adequacy;
    public String ice_supplier;
    public LocalDate last_feed;
    public String hlot;
    public String requester;
    public Integer ordered_quantity;
    public Double sea_temperature;
    public Integer total_quantity;
    public Short number_harvest_bins;
    public List<BinWeightRecord.BinRecord> harvest_bins_data;
    public List<String> fishing_team = new LinkedList<String>();
    public String status;
    public String temp_data;
    public String user;
    public String site;
    public String packaging_plant;
    public Long timestamp;
    public Double longitude;
    public Double latitude;

    public static FishingTxDTO convert(FishingTransaction fishing) {
        FishingTxDTO fishingTxDTO = new FishingTxDTO();
        fishingTxDTO.harvest_request = fishing.harvestRq;
        fishingTxDTO.platform_rfid = fishing.platformRFID;
        fishingTxDTO.cage_rfid = fishing.cageRFID;
        fishingTxDTO.cage_code = fishing.cageCode;
        fishingTxDTO.net_rfid = fishing.netRFID;
        fishingTxDTO.ichthyopathologist = fishing.ichthyopathologist;
        fishingTxDTO.species = fishing.fishType;
        fishingTxDTO.fish_size = fishing.averageWeight;
        fishingTxDTO.ice_adequacy = fishing.iceAdequacy;
        fishingTxDTO.ice_supplier = fishing.iceSupplier;
        //fishingTxDTO.last_feed = fishing.lastFeed;//.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));//+" 00:00:00";
        fishingTxDTO.hlot = fishing.hlot;
        fishingTxDTO.requester = fishing.requester;
        fishingTxDTO.ordered_quantity = fishing.orderedQuantity;
        fishingTxDTO.sea_temperature = fishing.seaTemperature;
        fishingTxDTO.total_quantity = fishing.totalQty;
        fishingTxDTO.number_harvest_bins = fishing.harvestBinsCnt;
        fishingTxDTO.harvest_bins_data = fishing.harvestBinsData;
        fishingTxDTO.fishing_team = fishing.team;
        fishingTxDTO.status = fishing.txStatus.name();
        fishingTxDTO.user = fishing.user;
        fishingTxDTO.temp_data = fishing.tempData;
        fishingTxDTO.site = fishing.site;
        fishingTxDTO.packaging_plant = fishing.packagingPlant;
        fishingTxDTO.timestamp = fishing.timestamp;
        fishingTxDTO.longitude = fishing.longitude;
        fishingTxDTO.latitude = fishing.latitude;

        return fishingTxDTO;
    }
}
