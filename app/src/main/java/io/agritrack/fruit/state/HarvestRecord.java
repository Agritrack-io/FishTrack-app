package io.agritrack.fruit.state;

import java.util.List;

public class HarvestRecord {

    public String poleRFID;
    public String harvestLot;
    public List<String> totes;
    public Integer totalTotesUsed;

    public Double longitude;
    public Double latitude;
    public String greenhouse;
    public String speciesName;

    public HarvestRecord(){
    }
}
