package io.agritrack.fruit.state;

import java.util.LinkedList;
import java.util.List;

public class PackagingRecord {

    public String poleRFID;
    public List<String> totesForPackaging;
    public Integer totalTotesForPackaging;
    public String warehouse;
    public String collectionLot;

    public List<String> packagedIfco;
    public Integer totalPackagedIfco;

    public Double longitude;
    public Double latitude;
}
