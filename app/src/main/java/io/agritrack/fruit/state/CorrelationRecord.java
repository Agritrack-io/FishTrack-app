package io.agritrack.fruit.state;

import io.agritrack.enums.AssetType;

public class CorrelationRecord {
    public String poleRFID;
    public String poleBarcode;
    public String loggerRFID;
    public String subSite;
    public AssetType assetType, loggerType;

    public Double longitude;
    public Double latitude;
}
