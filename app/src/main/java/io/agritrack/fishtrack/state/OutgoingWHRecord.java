package io.agritrack.fishtrack.state;

import java.util.List;

import io.agritrack.fishtrack.enums.AssetType;

public class OutgoingWHRecord {
    public AssetType assetType;
    public String outgoingFrom = null;
    public String outgoingTo = null;

    public int assetTypePos = -1;
    public List<String> outgoingItems;
}
