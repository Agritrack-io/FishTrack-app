package io.agritrack.fruit.state;

import io.agritrack.enums.AssetType;

public class CorrelationRecord {
    public String poleRFID;
    public String loggerRFID;
    public String site;
    public AssetType assetType, parentType;

    public Double longitude;
    public Double latitude;
}
