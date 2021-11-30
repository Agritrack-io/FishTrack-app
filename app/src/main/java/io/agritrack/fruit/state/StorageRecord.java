package io.agritrack.fruit.state;

import java.util.List;

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

    public StorageRecord(){
    }
}
