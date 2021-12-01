package io.agritrack.fruit.state;

import java.util.List;

import io.agritrack.data.dto.SiteDTO;
import io.agritrack.enums.WarehouseTxState;

public class StorageRecord {

    public List<String> receivedTotes;
    public String totalWeight;
    public String poleRFID;
    public String harvestLot;
    public Double longitude;
    public Double latitude;
    public Short totalTotesReceived;
    public String warehouse;
    public List<String> packagedIfco;
    public Short totalIfcoCnt;
    public WarehouseTxState state;

    public StorageRecord(){
    }
}
