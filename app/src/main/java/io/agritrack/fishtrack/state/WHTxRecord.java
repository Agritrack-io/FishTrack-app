package io.agritrack.fishtrack.state;

import java.util.List;
import java.util.Map;

import io.agritrack.fishtrack.enums.AssetType;
import io.agritrack.fishtrack.enums.WarehouseTxState;

public class WHTxRecord {
    public AssetType assetType;
    public String incomingItemType;
    public String outgoingItemType;
    public String from = null;
    public String to = null;

    public int assetTypePos = -1;
    public Map<String, List<String>> items;
    public WarehouseTxState state;
}
