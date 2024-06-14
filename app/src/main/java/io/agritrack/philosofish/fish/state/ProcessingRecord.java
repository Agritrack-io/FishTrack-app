package io.agritrack.philosofish.fish.state;

import java.util.List;
import java.util.UUID;

import io.agritrack.philosofish.ui.adapter.BinWeightCageAdapter;

public class ProcessingRecord {
    public UUID txKey;
    public String dispatchNote;
    public String securityClip;

    public String packagingSite;
    public String fishCondition;
    public Short totalBinsUsed;
    public boolean cleanTruck = Boolean.TRUE;
    public boolean smellyTruck = Boolean.FALSE;
    public List<BinWeightCageAdapter.BinDetails> availBins;
    public List<BinWeightCageAdapter.BinDetails> expectedBins;
    public Double longitude;
    public Double latitude;

    public ProcessingRecord() {
    }
}
