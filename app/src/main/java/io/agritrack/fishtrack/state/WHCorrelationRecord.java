package io.agritrack.fishtrack.state;

import io.agritrack.fishtrack.enums.AssetType;

public class WHCorrelationRecord {
    public AssetType assetType;
    public String barcode = null;
    public String rfid = null;

    public Double longitude;
    public Double latitude;
}
