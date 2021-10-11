package io.agritrack.fish.state;

import io.agritrack.enums.AssetType;

public class WHCorrelationRecord {
    public AssetType assetType;
    public String barcode = null;
    public String rfid = null;

    public Double longitude;
    public Double latitude;
}
