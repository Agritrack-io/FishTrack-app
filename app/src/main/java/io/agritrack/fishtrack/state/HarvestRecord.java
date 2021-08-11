package io.agritrack.fishtrack.state;

import java.util.List;

public class HarvestRecord {

    public int requesterPos = -1;
    public String requesterName;
    public String reqWeight;
    public int speciesPos = -1;
    public String speciesName;
    public String platformBC;
    public List<String> availBins;

    public HarvestRecord() {}
}
