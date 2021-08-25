package io.agritrack.fishtrack.state;

import java.util.List;

import io.agritrack.fishtrack.enums.AssetType;

public class IncomingWHRecord {
    public AssetType assetType;
    public String incomingFrom = null;
    public String incomingTo = null;

    public int assetTypePos = -1;
    public List<String> incomingItems;
}
